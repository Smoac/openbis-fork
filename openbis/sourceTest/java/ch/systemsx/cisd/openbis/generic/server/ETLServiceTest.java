/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import static ch.systemsx.cisd.openbis.generic.shared.IDataStoreService.VERSION;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentContentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcessingInstructionDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ETLService.class)
public class ETLServiceTest extends AbstractServerTestCase
{
    private static final String DOWNLOAD_URL = "download-url";

    private static final String DSS_CODE = "my-dss";

    private static final String DSS_SESSION_TOKEN = "dss42";

    private static final int PORT = 443;

    private static final String URL = "https://" + SESSION.getRemoteHost() + ":" + PORT;

    private ICommonBusinessObjectFactory boFactory;

    private IDataStoreServiceFactory dssfactory;

    private IDataStoreService dataStoreService;

    private IDataStoreDAO dataStoreDAO;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        boFactory = context.mock(ICommonBusinessObjectFactory.class);
        dssfactory = context.mock(IDataStoreServiceFactory.class);
        dataStoreService = context.mock(IDataStoreService.class);
        dataStoreDAO = context.mock(IDataStoreDAO.class);
    }

    @Test
    public void testRegisterDataStoreServer()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getDataStoreDAO();
                    will(returnValue(dataStoreDAO));

                    one(dataStoreDAO).tryToFindDataStoreByCode(DSS_CODE);
                    will(returnValue(null));

                    one(dssfactory).create(URL);
                    will(returnValue(dataStoreService));

                    one(dataStoreService).getVersion(DSS_SESSION_TOKEN);
                    will(returnValue(IDataStoreService.VERSION));

                    one(dataStoreDAO).createOrUpdateDataStore(with(new BaseMatcher<DataStorePE>()
                        {
                            public void describeTo(Description description)
                            {
                            }

                            public boolean matches(Object item)
                            {
                                if (item instanceof DataStorePE)
                                {
                                    DataStorePE store = (DataStorePE) item;
                                    return DSS_CODE.equals(store.getCode())
                                            && URL.equals(store.getRemoteUrl())
                                            && DOWNLOAD_URL.equals(store.getDownloadUrl())
                                            && DSS_SESSION_TOKEN.equals(store.getSessionToken());
                                }
                                return false;
                            }

                        }));
                }
            });

        createService().registerDataStoreServer(SESSION_TOKEN, createDSSInfo());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterDataStoreServerAgain()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getDataStoreDAO();
                    will(returnValue(dataStoreDAO));

                    one(dataStoreDAO).tryToFindDataStoreByCode(DSS_CODE);
                    will(returnValue(new DataStorePE()));

                    one(dataStoreDAO).createOrUpdateDataStore(with(new BaseMatcher<DataStorePE>()
                        {
                            public void describeTo(Description description)
                            {
                            }

                            public boolean matches(Object item)
                            {
                                if (item instanceof DataStorePE)
                                {
                                    DataStorePE store = (DataStorePE) item;
                                    return DSS_CODE.equals(store.getCode())
                                            && URL.equals(store.getRemoteUrl())
                                            && DOWNLOAD_URL.equals(store.getDownloadUrl())
                                            && DSS_SESSION_TOKEN.equals(store.getSessionToken());
                                }
                                return false;
                            }

                        }));
                }
            });

        createService().registerDataStoreServer(SESSION_TOKEN, createDSSInfo());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterDataStoreServerWithWrongVersion()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getDataStoreDAO();
                    will(returnValue(dataStoreDAO));

                    one(dataStoreDAO).tryToFindDataStoreByCode(DSS_CODE);
                    will(returnValue(null));

                    one(dssfactory).create(URL);
                    will(returnValue(dataStoreService));

                    one(dataStoreService).getVersion(DSS_SESSION_TOKEN);
                    will(returnValue(VERSION + 1));
                }
            });

        try
        {
            createService().registerDataStoreServer(SESSION_TOKEN, createDSSInfo());
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException e)
        {
            assertEquals(
                    "Data Store Server version is " + (VERSION + 1) + " instead of " + VERSION, e
                            .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateDataSetCode()
    {
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).getSession(SESSION_TOKEN);

                    one(daoFactory).getPermIdDAO();
                    will(returnValue(permIdDAO));

                    one(permIdDAO).createPermId();
                    will(returnValue("abc"));
                }
            });

        String dataSetCode = createService().createDataSetCode(SESSION_TOKEN);

        assertEquals("abc", dataSetCode);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetBaseExperimentForAnUnknownSample()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        prepareTryToLoadSample(sampleIdentifier, null);

        ExperimentPE experiment =
                createService().tryToGetBaseExperiment(SESSION_TOKEN, sampleIdentifier);

        assertEquals(null, experiment);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetBaseExperimentForSampleWithNoValidProcedure()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        prepareTryToLoadSample(sampleIdentifier, new SamplePE());

        ExperimentPE experiment =
                createService().tryToGetBaseExperiment(SESSION_TOKEN, sampleIdentifier);

        assertEquals(null, experiment);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetBaseExperimentWithoutAttachment()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        final ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        SamplePE sample = createSampleWithExperiment(experiment);
        prepareTryToLoadSample(sampleIdentifier, sample);
        context.checking(new Expectations()
            {
                {
                    one(experimentAttachmentDAO).listAttachments(experiment);
                    will(returnValue(Collections.emptyList()));
                }
            });

        ExperimentPE actualExperiment =
                createService().tryToGetBaseExperiment(SESSION_TOKEN, sampleIdentifier);

        assertSame(experiment, actualExperiment);
        assertEquals(0, actualExperiment.getProcessingInstructions().length);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetBaseExperimentWithAProcessingInstruction()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        final ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        SamplePE sample = createSampleWithExperiment(experiment);
        prepareTryToLoadSample(sampleIdentifier, sample);
        context.checking(new Expectations()
            {
                {
                    one(experimentAttachmentDAO).listAttachments(experiment);
                    AttachmentPE attachment1 = new AttachmentPE();
                    attachment1.setFileName("blabla");
                    String code = "pCode";
                    AttachmentPE processingPath =
                            createProcessingInstruction(ETLService.PROCESSING_PATH_TEMPLATE, code,
                                    "myPath");
                    will(returnValue(Arrays.asList(attachment1, processingPath)));

                    one(experimentAttachmentDAO).tryFindAttachmentByOwnerAndFileName(experiment,
                            processingPath.getFileName());
                    will(returnValue(processingPath));

                    AttachmentPE processingDescription =
                            createProcessingInstruction(ETLService.PROCESSING_DESCRIPTION_TEMPLATE,
                                    code, "myDescription");
                    one(experimentAttachmentDAO).tryFindAttachmentByOwnerAndFileName(experiment,
                            processingDescription.getFileName());
                    will(returnValue(processingDescription));

                    AttachmentPE processingParameters =
                            createProcessingInstruction(ETLService.PROCESSING_PARAMETERS_TEMPLATE,
                                    code, "myParameters");
                    one(experimentAttachmentDAO).tryFindAttachmentByOwnerAndFileName(experiment,
                            processingParameters.getFileName());
                    will(returnValue(processingParameters));
                }
            });

        ExperimentPE actualExperiment =
                createService().tryToGetBaseExperiment(SESSION_TOKEN, sampleIdentifier);

        assertSame(experiment, actualExperiment);
        ProcessingInstructionDTO[] processingInstructions =
                actualExperiment.getProcessingInstructions();
        assertEquals(1, processingInstructions.length);
        assertEquals("myPath", processingInstructions[0].getPath());
        assertEquals("myDescription", processingInstructions[0].getDescription());
        assertEquals("myParameters", new String(processingInstructions[0].getParameters()));
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetPropertiesOfTopSampleForAnUnknownSample()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        prepareLoadSample(sampleIdentifier, null);

        SamplePropertyPE[] properties =
                createService().tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                        sampleIdentifier);

        assertEquals(null, properties);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetPropertiesOfTopSampleForAToplessSample()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        prepareLoadSample(sampleIdentifier, new SamplePE());

        SamplePropertyPE[] properties =
                createService().tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                        sampleIdentifier);

        assertEquals(0, properties.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetPropertiesOfTopSampleWhichHasNoProperties()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        SamplePE sample = new SamplePE();
        sample.setTop(new SamplePE());
        prepareLoadSample(sampleIdentifier, sample);

        SamplePropertyPE[] properties =
                createService().tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                        sampleIdentifier);

        assertEquals(0, properties.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetPropertiesOfTopSample()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        SamplePE sample = new SamplePE();
        SamplePE top = new SamplePE();
        SamplePropertyPE property = new SamplePropertyPE();
        top.setProperties(new LinkedHashSet<SamplePropertyPE>(Arrays.asList(property)));
        sample.setTop(top);
        prepareLoadSample(sampleIdentifier, sample);

        SamplePropertyPE[] properties =
                createService().tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                        sampleIdentifier);

        assertEquals(1, properties.length);
        assertSame(property, properties[0]);
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterDataSetForUnknownExperiment()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        prepareTryToLoadSample(sampleIdentifier, new SamplePE());

        try
        {
            createService().registerDataSet(SESSION_TOKEN, sampleIdentifier, null);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("No experiment found for sample DB:/s1", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterDataSetForInvalidExperiment()
    {
        SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        experiment.setInvalidation(new InvalidationPE());
        prepareTryToLoadSample(sampleIdentifier, createSampleWithExperiment(experiment));

        try
        {
            createService().registerDataSet(SESSION_TOKEN, sampleIdentifier, null);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals(
                    "Data set can not be registered because experiment 'DB:/G1/P/EXP1' is invalid.",
                    e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterDataSet()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        final ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        SamplePE sample = createSampleWithExperiment(experiment);
        prepareTryToLoadSample(sampleIdentifier, sample);
        final ExternalData externalData = new ExternalData();
        externalData.setCode("dc");
        externalData.setMeasured(true);
        prepareRegisterDataSet(sampleIdentifier, sample.getExperiment(), SourceType.MEASUREMENT,
                externalData);

        createService().registerDataSet(SESSION_TOKEN, sampleIdentifier, externalData);

        context.assertIsSatisfied();
    }

    private void prepareRegisterDataSet(final SampleIdentifier sampleIdentifier,
            final ExperimentPE experiment, final SourceType sourceType,
            final ExternalData externalData)
    {
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier);
                    one(sampleBO).getSample();
                    SamplePE sample = new SamplePE();
                    sample.setCode("s2");
                    sample.setGroup(createGroup("G1"));
                    sample.setExperiment(experiment);
                    will(returnValue(sample));

                    one(boFactory).createExternalDataBO(SESSION);
                    will(returnValue(externalDataBO));

                    one(externalDataBO).define(externalData, sample, sourceType);
                    one(externalDataBO).save();
                    one(externalDataBO).getExternalData();
                    ExternalDataPE externalDataPE = new ExternalDataPE();
                    externalDataPE.setCode(externalData.getCode());
                    will(returnValue(externalDataPE));
                }
            });
    }

    private SamplePE createSampleWithExperiment(ExperimentPE experiment)
    {
        SamplePE sample = new SamplePE();
        sample.setExperiment(experiment);
        return sample;
    }

    private void prepareTryToLoadSample(final SampleIdentifier identifier, final SamplePE sample)
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).tryToLoadBySampleIdentifier(identifier);
                    one(sampleBO).tryToGetSample();
                    will(returnValue(sample));
                }
            });
    }

    private void prepareLoadSample(final SampleIdentifier identifier, final SamplePE sample)
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(identifier);
                    one(sampleBO).getSample();
                    will(returnValue(sample));
                }
            });
    }

    private AttachmentPE createProcessingInstruction(String template, String code, String content)
    {
        AttachmentPE attachment = new AttachmentPE();
        attachment.setFileName(String.format(template, code));
        AttachmentContentPE attachmentContent = new AttachmentContentPE();
        attachmentContent.setValue(content.getBytes());
        attachment.setAttachmentContent(attachmentContent);
        return attachment;
    }

    private IETLLIMSService createService()
    {
        return new ETLService(sessionManager, daoFactory, boFactory, dssfactory);
    }

    private DataStoreServerInfo createDSSInfo()
    {
        DataStoreServerInfo info = new DataStoreServerInfo();
        info.setPort(PORT);
        info.setSessionToken(DSS_SESSION_TOKEN);
        info.setDataStoreCode(DSS_CODE);
        info.setDownloadUrl(DOWNLOAD_URL);
        return info;
    }
}
