/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.protocol.pcep.segment.routing02;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.protocol.pcep.ietf.stateful07.Stateful07PCUpdateRequestMessageParser;
import org.opendaylight.protocol.pcep.spi.ObjectRegistry;
import org.opendaylight.protocol.pcep.spi.PCEPErrors;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.Lsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.pcupd.message.pcupd.message.Updates;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.pcupd.message.pcupd.message.UpdatesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.pcupd.message.pcupd.message.updates.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.srp.object.Srp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.lsp.setup.type._01.rev140507.Tlvs6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.lsp.setup.type._01.rev140507.Tlvs7;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.Message;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.Object;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.explicit.route.object.Ero;

public class SrPcUpdMessageParser extends Stateful07PCUpdateRequestMessageParser {

    public SrPcUpdMessageParser(ObjectRegistry registry) {
        super(registry);
    }

    @Override
    protected void serializeUpdate(Updates update, ByteBuf buffer) {
        if(isSegmentRoutingPath(update.getSrp())) {
            serializeObject(update.getSrp(), buffer);
            if(update.getLsp() != null) {
                serializeObject(update.getLsp(), buffer);
            }
            final Ero srEro = update.getPath().getEro();
            if(srEro != null) {
                serializeObject(srEro, buffer);
            }
        } else {
            super.serializeUpdate(update, buffer);
        }
    }

    @Override
    protected Updates getValidUpdates(List<Object> objects, List<Message> errors) {
        if(objects.get(0) instanceof Srp && isSegmentRoutingPath((Srp) objects.get(0))) {
            boolean isValid = true;
            final Srp srp = (Srp) objects.get(0);
            final UpdatesBuilder builder = new UpdatesBuilder();
            builder.setSrp(srp);
            objects.remove(0);
            if (objects.get(0) instanceof Lsp) {
                builder.setLsp((Lsp) objects.get(0));
                objects.remove(0);
            } else {
                errors.add(createErrorMsg(PCEPErrors.LSP_MISSING));
                isValid = false;
            }

            final Object obj = objects.get(0);
            if(obj instanceof Ero) {
                final Ero ero = (Ero) obj;
                final PCEPErrors error = SrEroUtil.validateSrEroSubobjects(ero);
                if(error != null) {
                    errors.add(createErrorMsg(error));
                    isValid = false;
                } else {
                    builder.setPath(new PathBuilder().setEro(ero).build());
                    objects.remove(0);
                }
            } else {
                errors.add(createErrorMsg(PCEPErrors.ERO_MISSING));
                isValid = false;
            }
            if(isValid) {
                return builder.build();
            }
            return null;
        }
        return super.getValidUpdates(objects, errors);
    }

    private boolean isSegmentRoutingPath(final Srp srp) {
        if(srp != null && srp.getTlvs() != null) {
            return SrEroUtil.isPst(srp.getTlvs().getAugmentation(Tlvs6.class)) || SrEroUtil.isPst(srp.getTlvs().getAugmentation(Tlvs7.class));
        }
        return false;
    }

}
