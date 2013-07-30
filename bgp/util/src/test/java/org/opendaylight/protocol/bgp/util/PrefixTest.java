/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.Collections;

import org.junit.Test;
import org.opendaylight.protocol.bgp.concepts.ASPath;
import org.opendaylight.protocol.bgp.concepts.BGPOrigin;
import org.opendaylight.protocol.bgp.concepts.BaseBGPObjectState;
import org.opendaylight.protocol.bgp.concepts.Community;
import org.opendaylight.protocol.bgp.concepts.ExtendedCommunity;
import org.opendaylight.protocol.bgp.util.BGPIPv4PrefixImpl;
import org.opendaylight.protocol.bgp.util.BGPIPv6PrefixImpl;

import org.opendaylight.protocol.concepts.ASNumber;
import org.opendaylight.protocol.concepts.IPv4;
import org.opendaylight.protocol.concepts.IPv6;
import org.opendaylight.protocol.bgp.linkstate.NodeIdentifier;
import org.opendaylight.protocol.bgp.linkstate.OSPFRouterIdentifier;
import org.opendaylight.protocol.bgp.linkstate.RouteTag;
import org.opendaylight.protocol.bgp.linkstate.NetworkObjectState;
import org.opendaylight.protocol.bgp.linkstate.NetworkPrefixState;
import org.opendaylight.protocol.bgp.linkstate.IPv4PrefixIdentifier;
import org.opendaylight.protocol.bgp.linkstate.IPv6PrefixIdentifier;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PrefixTest {
	final BaseBGPObjectState base = new BaseBGPObjectState(BGPOrigin.EGP, null);
	final NetworkObjectState state = new NetworkObjectState(new ASPath(Lists.newArrayList(new ASNumber(10L))), Collections.<Community> emptySet(), Collections.<ExtendedCommunity> emptySet());
	final NetworkPrefixState prefixState = new NetworkPrefixState(this.state, Sets.<RouteTag> newTreeSet(), null);

	final BGPIPv4PrefixImpl r4 = new BGPIPv4PrefixImpl(this.base, new IPv4PrefixIdentifier(new NodeIdentifier(null, null, null, new OSPFRouterIdentifier(new byte[] {
			1, 2, 3, 4 })), IPv4.FAMILY.prefixForString("172.168.4.5/16")), this.prefixState);
	final BGPIPv6PrefixImpl r6 = new BGPIPv6PrefixImpl(this.base, new IPv6PrefixIdentifier(new NodeIdentifier(null, null, null, new OSPFRouterIdentifier(new byte[] {
			1, 2, 3, 4 })), IPv6.FAMILY.prefixForString("2001::4/32")), this.prefixState);

	@Test
	public void testIPv4Prefix() {
		final BGPIPv4PrefixImpl r2 = new BGPIPv4PrefixImpl(this.base, new IPv4PrefixIdentifier(new NodeIdentifier(null, null, null, new OSPFRouterIdentifier(new byte[] {
				1, 2, 3, 4 })), IPv4.FAMILY.prefixForString("172.168.4.5/16")), this.prefixState);

		assertEquals(this.r4, r2);
		assertEquals(this.r4.hashCode(), r2.hashCode());
		assertNotSame(this.r4, this.r6);
	}

	@Test
	public void testIPv6Route() {
		final BGPIPv6PrefixImpl r2 = new BGPIPv6PrefixImpl(this.base, new IPv6PrefixIdentifier(new NodeIdentifier(null, null, null, new OSPFRouterIdentifier(new byte[] {
				1, 2, 3, 4 })), IPv6.FAMILY.prefixForString("2001::4/32")), this.prefixState);

		assertEquals(this.r6.currentState(), r2.currentState());
		assertEquals(this.r6.toString(), r2.toString());
		assertEquals(this.r6.getPrefixIdentifier(), r2.getPrefixIdentifier());
	}
}