/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.UniqueId;

/**
 * An {@code EngineDescriptor} is a {@link org.junit.platform.engine.TestDescriptor} for a specific
 * {@link org.junit.platform.engine.TestEngine}.
 *
 * @since 1.0
 */
@API(Experimental)
public class EngineDescriptor extends AbstractTestDescriptor {

	/**
	 * Create a new {@code EngineDescriptor} with the supplied {@link UniqueId}
	 * and display name.
	 *
	 * @param uniqueId the {@code UniqueId} for the described {@code TestEngine};
	 * never {@code null}
	 * @param displayName the display name for the described {@code TestEngine};
	 * never {@code null} or blank
	 * @see org.junit.platform.engine.TestEngine#getId()
	 * @see org.junit.platform.engine.TestDescriptor#getDisplayName()
	 */
	public EngineDescriptor(UniqueId uniqueId, String displayName) {
		super(uniqueId, displayName);
	}

	/**
	 * Always returns {@link org.junit.platform.engine.TestDescriptor.Type#ENGINE}.
	 *
	 * @see org.junit.platform.engine.TestDescriptor#isContainer() Test2()
	 * @see org.junit.platform.engine.TestDescriptor#isTest()
	 */
	@Override
	public final Type getType() {
		return Type.ENGINE;
	}
}
