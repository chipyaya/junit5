/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.commons.util.Preconditions.condition;
import static org.junit.platform.commons.util.Preconditions.notNull;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * {@code AssertLinesMatch} is a collection of utility methods that support asserting
 * lines of {@link String} equality or {@link java.util.regex.Pattern}-match in tests.
 *
 * @since 5.0
 */
class AssertLinesMatch {

	static void assertLinesMatch(List<String> expectedLines, List<String> actualLines) {
		notNull(expectedLines, "expectedLines must not be null");
		notNull(actualLines, "actualLines must not be null");

		// trivial case: same list instance
		if (expectedLines == actualLines) {
			return;
		}

		int expectedSize = expectedLines.size();
		int actualSize = actualLines.size();

		// trivial case: when expecting more then actual lines available, something is wrong
		if (expectedSize > actualSize) {
			// use standard assertEquals(Object, Object, message) to let IDEs present the textual difference
			String expected = String.join(System.lineSeparator(), expectedLines);
			String actual = String.join(System.lineSeparator(), actualLines);
			assertEquals(expected, actual, "expected " + expectedSize + " lines, but only got " + actualSize);
			fail("should not happen as expected != actual was asserted");
		}

		// simple case: both list are equally sized, compare them line-by-line
		if (expectedSize == actualSize) {
			boolean allOk = true;
			for (int i = 0; i < expectedSize; i++) {
				if (matches(expectedLines.get(i), actualLines.get(i))) {
					continue;
				}
				allOk = false;
				break;
			}
			if (allOk) {
				return;
			}
		}

		assertLinesMatchWithFastForward(expectedLines, actualLines);
	}

	private static void assertLinesMatchWithFastForward(List<String> expectedLines, List<String> actualLines) {
		Deque<String> expectedDeque = new LinkedList<>(expectedLines);
		Deque<String> actualDeque = new LinkedList<>(actualLines);

		while (!expectedDeque.isEmpty()) {
			String expectedLine = expectedDeque.pop();
			String actualLine = actualDeque.peek();

			// trivial case: take the fast path when they simply match
			if (matches(expectedLine, actualLine)) {
				actualDeque.pop();
				continue;
			}

			// fast-forward marker found in expected line: fast-forward actual line...
			if (isFastForwardLine(expectedLine)) {
				int fastForwardLimit = parseFastForwardLimit(expectedLine);

				// trivial case: fast-forward marker was in last expected line
				if (expectedDeque.isEmpty()) {
					int actualRemaining = actualDeque.size();
					// no limit given or perfect match? we're done.
					if (fastForwardLimit == Integer.MAX_VALUE || fastForwardLimit == actualRemaining) {
						return;
					}
					fail(format("terminal fast-forward(%d) error: fast-forward(%d) expected", fastForwardLimit,
						actualRemaining));
				}

				// peek next expected line
				expectedLine = expectedDeque.peek();

				// fast-forward limit was given: use it
				if (fastForwardLimit != Integer.MAX_VALUE) {
					// fast-forward now: actualDeque.pop(fastForwardLimit)
					for (int i = 0; i < fastForwardLimit; i++) {
						actualDeque.pop();
					}
					if (actualDeque.isEmpty()) {
						fail(format("%d more lines expected, actual lines is empty", expectedDeque.size()));
					}
					continue;
				}

				// fast-forward "unlimited": until next match
				while (true) {
					if (actualDeque.isEmpty()) {
						fail(format("no match for `%s` line fast-forwarding: %n%s", expectedLine, actualLines));
					}
					if (matches(expectedLine, actualDeque.pop())) {
						break;
					}
				}
			}
		}

		// after math
		if (!actualDeque.isEmpty()) {
			fail("more actual lines than expected: " + actualDeque.size());
		}
	}

	private static boolean isFastForwardLine(String line) {
		line = line.trim();
		return line.startsWith(">>") && line.endsWith(">>");
	}

	private static int parseFastForwardLimit(String fastForwardLine) {
		String text = fastForwardLine.trim().substring(2, fastForwardLine.length() - 2).trim();
		try {
			int limit = Integer.parseInt(text);
			condition(limit > 0, "fast-forward must greater than zero, it is: " + limit);
			return limit;
		}
		catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}

	private static boolean matches(String expectedLine, String actualLine) {
		if (expectedLine.equals(actualLine)) {
			return true;
		}
		try {
			Pattern pattern = Pattern.compile(expectedLine);
			Matcher matcher = pattern.matcher(actualLine);
			return matcher.matches();
		}
		catch (PatternSyntaxException ignore) {
			return false;
		}
	}
}
