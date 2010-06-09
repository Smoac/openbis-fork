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
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.etlserver.IDataSetHandlerRpc;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaBundleDataSetHandlerTest extends AbstractFileSystemTestCase
{
    private Mockery context;

    private IEncapsulatedOpenBISService openbisService;

    private CinaBundleDataSetHandler handler;

    private IDataSetHandlerRpc delegator;

    private ExternalData externalData;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        context = new Mockery();
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        setupOpenBisExpectations();

        delegator = context.mock(IDataSetHandlerRpc.class);

        initializeDataSetHandler();
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testHandling()
    {

        File dataSetFile =
                new File("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/");
        setupDataSetHandlerExpectations(dataSetFile);

        context.checking(new Expectations()
            {
                private long uniqueId = 1;

                private void delegatorHandleDataSetExpectation(final String path)
                {
                    final DataSetInformation dataSetInformation = new DataSetInformation();
                    dataSetInformation.setDataSetCode("Derived");
                    dataSetInformation.setSampleCode("" + uniqueId++);
                    dataSetInformation.setSpaceCode("Space");
                    dataSetInformation.setInstanceCode("Test");

                    allowing(delegator).linkAndHandleDataSet(with(new File(path)),
                            with(any(DataSetInformation.class)));
                    will(returnValue(Collections.singletonList(dataSetInformation)));
                }

                {
                    allowing(openbisService).drawANewUniqueID();
                    will(returnValue(uniqueId++));
                    allowing(openbisService).registerSample(with(any(NewSample.class)),
                            with("test"));
                    will(returnValue(new Long(1)));
                    delegatorHandleDataSetExpectation("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/Replica for MRC files/MRC for Thomas/test20090422_BacklashRef.mrc");
                    delegatorHandleDataSetExpectation("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/Replica for MRC files/MRC for Thomas/test20090424_TrackAtZeroRef.mrc");
                    delegatorHandleDataSetExpectation("sourceTest/java/ch/systemsx/cisd/cina/shared/metadata/Test.bundle/Annotations/Replica for STEM files/STEM/stem_134588_1.imag");
                }
            });

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

    private void setupOpenBisExpectations()
    {
        final SampleType sampleType = new SampleType();
        sampleType.setCode(CinaConstants.REPLICA_SAMPLE_TYPE_CODE);
        sampleType.setAutoGeneratedCode(true);
        sampleType.setGeneratedCodePrefix("Replica-");

        DataSetType dataSetType = new DataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
        final DataSetTypeWithVocabularyTerms dataSetTypeWithTerms =
                new DataSetTypeWithVocabularyTerms();
        dataSetTypeWithTerms.setDataSetType(dataSetType);

        externalData = new ExternalData();
        externalData.setCode("1");

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(openbisService).getSampleType(CinaConstants.REPLICA_SAMPLE_TYPE_CODE);
                    will(returnValue(sampleType));
                    one(openbisService).getDataSetType(CinaConstants.IMAGE_DATA_SET_TYPE_CODE);
                    will(returnValue(dataSetTypeWithTerms));
                    one(openbisService).tryGetDataSet("session-token", externalData.getCode());
                    will(returnValue(externalData));
                }
            });
    }

    private void setupDataSetHandlerExpectations(final File dataSet)
    {
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(externalData.getCode());
        dataSetInformation.setSampleCode("2");
        dataSetInformation.setSpaceCode("Space");
        dataSetInformation.setInstanceCode("Test");

        final SessionContextDTO sessionContext = new SessionContextDTO();
        sessionContext.setSessionToken("session-token");
        sessionContext.setUserEmail("test@test.bar");
        sessionContext.setUserName("test");

        // set up the expectations
        context.checking(new Expectations()
            {
                {
                    one(delegator).handleDataSet(dataSet);
                    will(returnValue(Collections.singletonList(dataSetInformation)));
                    allowing(delegator).getSessionContext();
                    will(returnValue(sessionContext));
                    one(delegator).getFileForExternalData(externalData);
                    will(returnValue(dataSet.getParentFile()));
                }
            });
    }
}
