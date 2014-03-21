/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.spi;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.Object;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.ObjectHeader;

public interface ObjectRegistry {
	/**
	 * Finds parser for given object type and class in the registry. Delegates parsing to found parser.
	 * @param objectType object type
	 * @param objectClass object class
	 * @param buffer object raw binary value to be parsed
	 * @return null if the parser for this object could not be found
	 * @throws PCEPDeserializerException if the parsing did not succeed
	 */
	Object parseObject(final int objectClass, final int objectType, final ObjectHeader header, final byte[] buffer) throws PCEPDeserializerException;

	/**
	 * Find serializer for given object. Delegates parsing to found serializer.
	 * @param object to be parsed
	 * @return null if the serializer for this object could not be found
	 */
	byte[] serializeObject(Object object);
}