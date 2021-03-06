/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.pcep.ietf;

import com.google.common.collect.Lists;
import javax.management.ObjectName;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.config.api.jmx.CommitStatus;
import org.opendaylight.controller.config.manager.impl.AbstractConfigTest;
import org.opendaylight.controller.config.manager.impl.factoriesresolver.HardcodedModuleFactoriesResolver;
import org.opendaylight.controller.config.util.ConfigTransactionJMXClient;
import org.opendaylight.controller.config.yang.pcep.spi.SimplePCEPExtensionProviderContextModuleFactory;
import org.opendaylight.controller.config.yang.pcep.spi.SimplePCEPExtensionProviderContextModuleTest;
import org.opendaylight.controller.config.yang.pcep.stateful07.cfg.SyncOptimizationsPCEPParserModuleFactory;

public class SyncOptimizationsPCEPParserModuleTest extends AbstractConfigTest {

    private static final String FACTORY_NAME = SyncOptimizationsPCEPParserModuleFactory.NAME;
    private static final String INSTANCE_NAME = "pcep-parser-sync-optimizations-instance";

    @Before
    public void setUp() {
        super.initConfigTransactionManagerImpl(new HardcodedModuleFactoriesResolver(this.mockedContext, new SyncOptimizationsPCEPParserModuleFactory(), new SimplePCEPExtensionProviderContextModuleFactory()));
    }

    @Test
    public void testCreateBean() throws Exception {
        final CommitStatus status = createSyncOptimizationsPCEPParserModuleInstance();
        assertBeanCount(1, FACTORY_NAME);
        assertStatus(status, 2, 0, 0);
    }

    @Test
    public void testReusingOldInstance() throws Exception {
        createSyncOptimizationsPCEPParserModuleInstance();
        final ConfigTransactionJMXClient transaction = this.configRegistryClient.createTransaction();
        assertBeanCount(1, FACTORY_NAME);
        final CommitStatus status = transaction.commit();
        assertBeanCount(1, FACTORY_NAME);
        assertStatus(status, 0, 0, 2);
    }

    private CommitStatus createSyncOptimizationsPCEPParserModuleInstance() throws Exception {
        final ConfigTransactionJMXClient transaction = this.configRegistryClient.createTransaction();
        final ObjectName baseParserON = transaction.createModule(FACTORY_NAME, INSTANCE_NAME);

        SimplePCEPExtensionProviderContextModuleTest.createPCEPExtensionsModuleInstance(transaction, Lists.newArrayList(baseParserON));
        return transaction.commit();
    }
}
