/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.protocol.pcep.pcc.mock;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.protocol.pcep.pcc.mock.api.PCCSession;
import org.opendaylight.protocol.pcep.pcc.mock.api.PCCTunnelManager;
import org.opendaylight.protocol.pcep.spi.PCEPErrors;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev131126.Srp1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev131126.Srp1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev131126.pcinitiate.message.pcinitiate.message.Requests;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.crabbe.initiated.rev131126.pcinitiate.message.pcinitiate.message.RequestsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.Pcrpt;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.PlspId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.SrpIdNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.SymbolicPathName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.LspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.lsp.object.lsp.TlvsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.pcupd.message.pcupd.message.Updates;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.pcupd.message.pcupd.message.UpdatesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.pcupd.message.pcupd.message.updates.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.srp.object.SrpBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.ietf.stateful.rev131222.symbolic.path.name.tlv.SymbolicPathNameBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.message.rev131007.Pcerr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.explicit.route.object.Ero;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.explicit.route.object.EroBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.explicit.route.object.ero.SubobjectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pcep.types.rev131005.pcep.error.object.ErrorObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.rsvp.rev150820.basic.explicit.route.subobjects.subobject.type.IpPrefixCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.rsvp.rev150820.basic.explicit.route.subobjects.subobject.type.ip.prefix._case.IpPrefixBuilder;

public class PCCTunnelManagerImplTest {

    private static final InetAddress ADDRESS = InetAddresses.forString("1.2.4.5");
    private static final Timer TIMER = new HashedWheelTimer();
    private static final byte[] SYMBOLIC_NAME = "tets".getBytes(Charsets.UTF_8);
    private static final Ero ERO = new EroBuilder()
        .setSubobject(Lists.newArrayList(new SubobjectBuilder().setSubobjectType(new IpPrefixCaseBuilder().setIpPrefix(
            new IpPrefixBuilder().setIpPrefix(new IpPrefix(new Ipv4Prefix("127.0.0.2/32"))).build()).build()).build())).build();
    private final List<PCEPErrors> errorsSession1 = new ArrayList<>();
    private final List<PCEPErrors> errorsSession2 = new ArrayList<>();
    @Mock
    private PCCSession session1;
    @Mock
    private PCCSession session2;
    @Mock
    private Optional<TimerHandler> timerHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doNothing().when(session1).sendReport(Mockito.any(Pcrpt.class));
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                errorsSession1.add(getError((Pcerr) invocation.getArguments()[0]));
                return null;
            }
        }).when(session1).sendError(Mockito.any(Pcerr.class));
        Mockito.doReturn(0).when(session1).getId();
        Mockito.doNothing().when(session2).sendReport(Mockito.any(Pcrpt.class));
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                errorsSession2.add(getError((Pcerr) invocation.getArguments()[0]));
                return null;
            }
        }).when(session2).sendError(Mockito.any(Pcerr.class));
        Mockito.doReturn(1).when(session2).getId();
    }

    @After
    public void tearDown() {
        this.errorsSession1.clear();
        this.errorsSession2.clear();
    }

    @Test
    public void testOnSessionUp() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 0, 0, TIMER, this.timerHandler);
        checkSessionUp(session1, tunnelManager);
        checkSessionUp(session2, tunnelManager);
    }

    @Test
    public void testOnSessionDownAndDelegateBack() throws InterruptedException {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 1, 10, TIMER, this.timerHandler);
        checkSessionUp(session1, tunnelManager);
        checkSessionUp(session2, tunnelManager);
        checkSessionDown(session1, tunnelManager);
        tunnelManager.onSessionUp(session1);
        Mockito.verify(session1, Mockito.times(4)).sendReport(Mockito.any(Pcrpt.class));
        Mockito.verify(session2, Mockito.times(2)).sendReport(Mockito.any(Pcrpt.class));
    }

    private static void checkSessionDown(final PCCSession session, final PCCTunnelManager tunnelManager) {
        tunnelManager.onSessionDown(session);
        Mockito.verify(session, Mockito.times(2)).sendReport(Mockito.any(Pcrpt.class));
    }

    private static void checkSessionUp(final PCCSession session, final PCCTunnelManager tunnelManager) {
        //1 reported LSP + 1 end-of-sync marker
        tunnelManager.onSessionUp(session);
        Mockito.verify(session, Mockito.times(2)).sendReport(Mockito.any(Pcrpt.class));
    }

    @Test
    public void testOnSessionDownAndDelegateToOther() throws InterruptedException {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 0, -1, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session2);
        checkSessionUp(session1, tunnelManager);
        checkSessionDown(session1, tunnelManager);
        //wait for re-delegation timeout expires
        Thread.sleep(500);
        Mockito.verify(session2, Mockito.times(3)).sendReport(Mockito.any(Pcrpt.class));
        tunnelManager.onSessionUp(session1);
        Mockito.verify(session1, Mockito.times(4)).sendReport(Mockito.any(Pcrpt.class));
    }

    @Test
    public void testReportToAll() throws InterruptedException {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onSessionUp(session2);
        tunnelManager.onMessagePcupd(createUpdateDelegate(1), session1);
        Mockito.verify(session1, Mockito.times(3)).sendReport(Mockito.any(Pcrpt.class));
        Mockito.verify(session2, Mockito.times(3)).sendReport(Mockito.any(Pcrpt.class));
    }

    @Test
    public void testReportToAllUnknownLsp() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onMessagePcupd(createUpdateDelegate(2), session1);
        Mockito.verify(session1, Mockito.times(1)).sendError(Mockito.any(Pcerr.class));
        assertEquals(PCEPErrors.UNKNOWN_PLSP_ID, errorsSession1.get(0));
    }

    @Test
    public void testReportToAllNonDelegatedLsp() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onSessionUp(session2);
        tunnelManager.onMessagePcupd(createUpdateDelegate(1), session2);
        Mockito.verify(session2, Mockito.times(1)).sendError(Mockito.any(Pcerr.class));
        assertEquals(PCEPErrors.UPDATE_REQ_FOR_NON_LSP, errorsSession2.get(0));
    }

    @Test
    public void testReturnDelegationPccLsp() throws InterruptedException {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 1, -1, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onSessionUp(session2);
        tunnelManager.onMessagePcupd(createUpdate(1), session1);
        Mockito.verify(session1, Mockito.times(3)).sendReport(Mockito.any(Pcrpt.class));
        Mockito.verify(session2, Mockito.times(2)).sendReport(Mockito.any(Pcrpt.class));
        //wait for re-delegation timer expires
        Thread.sleep(1200);
        Mockito.verify(session2, Mockito.times(3)).sendReport(Mockito.any(Pcrpt.class));
    }

    @Test
    public void testReturnDelegationUnknownLsp() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onMessagePcupd(createUpdate(2), session1);
        Mockito.verify(session1, Mockito.times(1)).sendError(Mockito.any(Pcerr.class));
        assertEquals(PCEPErrors.UNKNOWN_PLSP_ID, errorsSession1.get(0));
    }

    @Test
    public void testReturnDelegationNonDelegatedLsp() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onSessionUp(session2);
        tunnelManager.onMessagePcupd(createUpdate(1), session2);
        Mockito.verify(session2, Mockito.times(1)).sendError(Mockito.any(Pcerr.class));
        assertEquals(PCEPErrors.UPDATE_REQ_FOR_NON_LSP, errorsSession2.get(0));
    }

    @Test
    public void testAddTunnel() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(0, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onSessionUp(session2);
        tunnelManager.onMessagePcInitiate(createRequests(1), session1);
        Mockito.verify(session1, Mockito.times(1)).sendReport(Mockito.any(Pcrpt.class));
        Mockito.verify(session2, Mockito.times(1)).sendReport(Mockito.any(Pcrpt.class));
    }

    @Test
    public void testRemoveTunnel() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(0, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onSessionUp(session2);
        tunnelManager.onMessagePcInitiate(createRequests(1), session1);
        tunnelManager.onMessagePcInitiate(createRequestsRemove(1), session1);
        Mockito.verify(session1, Mockito.times(2)).sendReport(Mockito.any(Pcrpt.class));
        Mockito.verify(session2, Mockito.times(2)).sendReport(Mockito.any(Pcrpt.class));
    }

    @Test
    public void testRemoveTunnelUnknownLsp() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(0, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onMessagePcInitiate(createRequestsRemove(1), session1);
        Mockito.verify(session1, Mockito.times(1)).sendError(Mockito.any(Pcerr.class));
        assertEquals(PCEPErrors.UNKNOWN_PLSP_ID, errorsSession1.get(0));
    }

    @Test
    public void testRemoveTunnelNotPceInitiatedLsp() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onMessagePcInitiate(createRequestsRemove(1), session1);
        Mockito.verify(session1, Mockito.times(1)).sendError(Mockito.any(Pcerr.class));
        assertEquals(PCEPErrors.LSP_NOT_PCE_INITIATED, errorsSession1.get(0));
    }

    @Test
    public void testRemoveTunnelNotDelegated() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(0, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onSessionUp(session2);
        tunnelManager.onMessagePcInitiate(createRequests(1), session1);
        tunnelManager.onMessagePcInitiate(createRequestsRemove(1), session2);
        Mockito.verify(session2, Mockito.times(1)).sendError(Mockito.any(Pcerr.class));
        assertEquals(PCEPErrors.UPDATE_REQ_FOR_NON_LSP, errorsSession2.get(0));
    }

    @Test
    public void testTakeDelegation() throws InterruptedException {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(0, ADDRESS, 0, -1, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onSessionUp(session2);
        tunnelManager.onMessagePcInitiate(createRequests(1), session1); //AddTunel
        tunnelManager.onMessagePcupd(createUpdate(1), session1); //returnDelegation
        Mockito.verify(session1, Mockito.times(2)).sendReport(Mockito.any(Pcrpt.class));
        Mockito.verify(session2, Mockito.times(1)).sendReport(Mockito.any(Pcrpt.class));
        Thread.sleep(500);
        tunnelManager.onMessagePcInitiate(createRequestsDelegate(1), session2);//takeDelegation
        Mockito.verify(session1, Mockito.times(2)).sendReport(Mockito.any(Pcrpt.class));
        Mockito.verify(session2, Mockito.times(2)).sendReport(Mockito.any(Pcrpt.class));
    }

    @Test
    public void testTakeDelegationUnknownLsp() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(0, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onMessagePcInitiate(createRequestsDelegate(1), session1);
        Mockito.verify(session1, Mockito.times(1)).sendError(Mockito.any(Pcerr.class));
        assertEquals(PCEPErrors.UNKNOWN_PLSP_ID, errorsSession1.get(0));
    }

    @Test
    public void testTakeDelegationNotPceInitiatedLsp() {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(1, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onMessagePcInitiate(createRequestsDelegate(1), session1);
        Mockito.verify(session1, Mockito.times(1)).sendError(Mockito.any(Pcerr.class));
        assertEquals(PCEPErrors.LSP_NOT_PCE_INITIATED, errorsSession1.get(0));
    }

    @Test
    public void testReturnDelegationNoRetake() throws InterruptedException {
        final PCCTunnelManager tunnelManager = new PCCTunnelManagerImpl(0, ADDRESS, 0, 0, TIMER, this.timerHandler);
        tunnelManager.onSessionUp(session1);
        tunnelManager.onSessionUp(session2);
        tunnelManager.onMessagePcInitiate(createRequests(1), session1);
        tunnelManager.onMessagePcupd(createUpdate(1), session1);
        //wait for state timeout expires
        Thread.sleep(500);
        Mockito.verify(session1, Mockito.times(3)).sendReport(Mockito.any(Pcrpt.class));
        Mockito.verify(session2, Mockito.times(2)).sendReport(Mockito.any(Pcrpt.class));
    }
    private Updates createUpdateDelegate(final long plspId) {
        return createUpdate(plspId, Optional.of(true));
    }

    private Updates createUpdate(final long plspId) {
        return createUpdate(plspId, Optional.<Boolean>absent());
    }

    private static Updates createUpdate(final long plspId, final Optional<Boolean> delegate) {
        final UpdatesBuilder updsBuilder = new UpdatesBuilder();
        final LspBuilder lsp = new LspBuilder().setPlspId(new PlspId(plspId));
        if (delegate.isPresent()) {
            lsp.setDelegate(true);
        }
        updsBuilder.setLsp(lsp.build());
        final PathBuilder pathBuilder = new PathBuilder();
        pathBuilder.setEro(ERO);
        updsBuilder.setPath(pathBuilder.build());
        updsBuilder.setSrp(new SrpBuilder().setOperationId(new SrpIdNumber(0L)).build());
        return updsBuilder.build();
    }

    private static Requests createRequests(final long plspId, final Optional<Boolean> remove, final Optional<Boolean> delegate) {
        final RequestsBuilder reqBuilder = new RequestsBuilder();
        reqBuilder.setEro(ERO);
        final LspBuilder lsp = new LspBuilder().setTlvs(new TlvsBuilder().setSymbolicPathName(new SymbolicPathNameBuilder().setPathName(
            new SymbolicPathName(SYMBOLIC_NAME)).build()).build()).setPlspId(new PlspId(plspId));
        if (delegate.isPresent()) {
            lsp.setDelegate(true);
        }

        reqBuilder.setLsp(lsp.build());
        final SrpBuilder srpBuilder = new SrpBuilder();
        if (remove.isPresent()) {
            srpBuilder.addAugmentation(Srp1.class, new Srp1Builder().setRemove(true).build());
        }
        reqBuilder.setSrp(srpBuilder.setOperationId(new SrpIdNumber(0L)).build());
        return reqBuilder.build();
    }

    private static Requests createRequestsRemove(final long plspId) {
        return createRequests(plspId, Optional.of(true), Optional.<Boolean>absent());
    }

    private static Requests createRequestsDelegate(final long plspId) {
        return createRequests(plspId, Optional.<Boolean>absent(), Optional.of(true));
    }

    private static Requests createRequests(final long plspId) {
        return createRequests(plspId, Optional.<Boolean>absent(), Optional.<Boolean>absent());
    }

    private static PCEPErrors getError(final Pcerr errorMessage) {
        final ErrorObject errorObject = errorMessage.getPcerrMessage().getErrors().get(0).getErrorObject();
        return PCEPErrors.forValue(errorObject.getType(), errorObject.getValue());
    }

}