/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.framework;

public class SimpleMessage implements ProtocolMessage {

	private static final long serialVersionUID = 1L;

	private final String s;

	public SimpleMessage(final String s) {
		this.s = s;
	}

	public String getMessage() {
		return s;
	}
}