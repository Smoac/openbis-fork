/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.cina.dss.bundle;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.Test;

import ch.systemsx.cisd.cina.dss.bundle.registrators.CinaBundleRegistrationTest;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaBundleDataSetHandlerTest extends CinaBundleRegistrationTest
{
    private CinaBundleDataSetHandler handler;

    /**
     * First set up expectations for the case that the entities do not exist, then set up the
     * expectations for existing entities to simulate registering new entities.
     */
    @Test
    public void testHandlingWithCreationOfNewEntities()
    {
        setupOpenBisExpectations();
        setupSessionContextExpectations();
        setupCallerDataSetInfoExpectations();

        File dataSetFile =
                new File("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/");

        setupNewEntititesExpectations();

        setupExistingGridPrepExpectations();
        setupHandleRawDataSetExpectations("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/RawData/ReplicTest");
        setupHandleReplicaMetadataDataSetExpectations("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/ReplicTest");
        setupHandleBundleMetadataDataSetExpectations("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/BundleMetadata.xml");

        initializeDataSetHandler();
        handler.handleDataSet(dataSetFile);

        context.assertIsSatisfied();
    }

    /**
     * Set up the expectations for existing entities to simulate registering updating entities.
     */
    @Test
    public void testRegistratorForExistingEntities()
    {
        setupOpenBisExpectations();
        setupSessionContextExpectations();
        setupCallerDataSetInfoExpectations();

        File dataSetFile =
                new File("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/");

        setupExistingGridPrepExpectations();
        setupExistingReplicaExpectations();
        setupHandleReplicaMetadataDataSetExpectations("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/ReplicTest");
        setupHandleBundleMetadataDataSetExpectations("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/BundleMetadata.xml");

        initializeDataSetHandler();
        handler.handleDataSet(dataSetFile);

        context.assertIsSatisfied();
    }

    private void initializeDataSetHandler()
    {
        final Properties props = new Properties();
        props.setProperty("data-store-server-code", "DSS1");
        // Don't use this
        // props.setProperty("data-folder", "targets/playground/data") ;
        props.setProperty("storeroot-dir", "store");
        props.setProperty("processor", "ch.systemsx.cisd.cina.dss.MockDefaultStorageProcessor");
        handler = new CinaBundleDataSetHandler(props, delegator, openbisService);
    }

}
