/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.rib.impl.spi;

import org.opendaylight.protocol.bgp.parser.BGPDocumentedException;

import org.opendaylight.protocol.framework.SessionPreferences;
import org.opendaylight.protocol.framework.SessionPreferencesChecker;

/**
 * Session characteristics initial proposal checker. BGP does not have any negotiation. If the characteristics are not
 * acceptable, the session ends.
 */
public abstract class BGPSessionProposalChecker implements SessionPreferencesChecker {

	@Override
	public abstract Boolean checkSessionCharacteristics(final SessionPreferences openObj) throws BGPDocumentedException;
}