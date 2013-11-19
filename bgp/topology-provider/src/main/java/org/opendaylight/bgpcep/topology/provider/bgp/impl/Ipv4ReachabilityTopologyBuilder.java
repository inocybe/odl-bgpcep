/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.bgpcep.topology.provider.bgp.impl;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.tables.routes.ipv4.routes.Ipv4Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.route.Attributes;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class Ipv4ReachabilityTopologyBuilder extends AbstractReachabilityTopologyBuilder<Ipv4Route> {
	Ipv4ReachabilityTopologyBuilder(final InstanceIdentifier<Topology> topology) {
		super(topology, Ipv4Route.class);
	}

	@Override
	protected Attributes getAttributes(final Ipv4Route value) {
		return value.getAttributes();
	}

	@Override
	protected IpPrefix getPrefix(final Ipv4Route value) {
		return new IpPrefix(value.getKey().getPrefix());
	}
}