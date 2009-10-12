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
import java.util.LinkedHashSet;
import java.util.List;

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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.InvalidationPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ETLService.class)
public class ETLServiceTest extends AbstractServerTestCase
{
    private static final String UNKNOWN_DATA_SET_TYPE_CODE = "completely-unknown-code";

    private static final String DATA_SET_TYPE_CODE = "dataSetTypeCode1";

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

                    prepareFindDatasetTypes(this);

                    allowing(dataStoreDAO).createOrUpdateDataStore(
                            with(new BaseMatcher<DataStorePE>()
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
                                                    && DSS_SESSION_TOKEN.equals(store
                                                            .getSessionToken());
                                        }
                                        return false;
                                    }

                                }));
                }
            });

        createService().registerDataStoreServer(SESSION_TOKEN, createDSSInfo());

        context.assertIsSatisfied();
    }

    private void prepareFindDatasetTypes(Expectations exp)
    {
        exp.allowing(dataSetTypeDAO).tryToFindDataSetTypeByCode(DATA_SET_TYPE_CODE);
        exp.will(Expectations.returnValue(new DataSetTypePE()));
        exp.allowing(dataSetTypeDAO).tryToFindDataSetTypeByCode(UNKNOWN_DATA_SET_TYPE_CODE);
        exp.will(Expectations.returnValue(null));
    }

    @Test
    public void testRegisterDataStoreServerAgain()
    {
        prepareGetSession();
        prepareGetVersion();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getDataStoreDAO();
                    will(returnValue(dataStoreDAO));

                    one(dataStoreDAO).tryToFindDataStoreByCode(DSS_CODE);
                    will(returnValue(new DataStorePE()));

                    prepareFindDatasetTypes(this);

                    allowing(dataStoreDAO).createOrUpdateDataStore(
                            with(new BaseMatcher<DataStorePE>()
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
                                                    && DSS_SESSION_TOKEN.equals(store
                                                            .getSessionToken());
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
        prepareGetVersion(VERSION + 1);

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

    private void prepareGetVersion()
    {
        prepareGetVersion(VERSION);
    }

    private void prepareGetVersion(final int version)
    {
        context.checking(new Expectations()
            {
                {
                    one(dssfactory).create(URL);
                    will(returnValue(dataStoreService));

                    one(dataStoreService).getVersion(DSS_SESSION_TOKEN);
                    will(returnValue(version));
                }
            });
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
    public void testTryGetSampleWithExperimentForAnUnknownSample()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        prepareTryToLoadSample(sampleIdentifier, null);

        Sample sample =
                createService().tryGetSampleWithExperiment(SESSION_TOKEN, sampleIdentifier);

        assertNull(sample);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryGetSampleWithExperimentForSampleWithNoValidProcedure()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        SamplePE samplePE = createSample();

        prepareTryToLoadSample(sampleIdentifier, samplePE);

        Sample actualSample =
                createService().tryGetSampleWithExperiment(SESSION_TOKEN, sampleIdentifier);

        assertEquals(samplePE.getCode(), actualSample.getCode());
        assertNull(actualSample.getExperiment());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetSampleWithExperimentWithoutAttachment()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        final ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        SamplePE sample = createSampleWithExperiment(experiment);
        prepareTryToLoadSample(sampleIdentifier, sample);

        Sample actualSample =
                createService().tryGetSampleWithExperiment(SESSION_TOKEN, sampleIdentifier);
        Experiment actualExperiment = actualSample.getExperiment();
        assertEquals(sample.getCode(), actualSample.getCode());
        assertEquals(sample.getSampleType().getCode(), actualSample.getSampleType().getCode());
        assertEquals(experiment.getCode(), actualExperiment.getCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetPropertiesOfTopSampleForAnUnknownSample()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        prepareLoadSample(sampleIdentifier, null);

        IEntityProperty[] properties =
                createService().tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                        sampleIdentifier);

        assertNull(properties);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetPropertiesOfTopSampleForAToplessSample()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        prepareLoadSample(sampleIdentifier, new SamplePE());

        final IEntityProperty[] properties =
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

        IEntityProperty[] properties =
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
        SamplePropertyPE property = createSamplePropertyPE("type code", EntityDataType.VARCHAR, "The Value");
        
        top.setProperties(new LinkedHashSet<SamplePropertyPE>(Arrays.asList(property)));
        sample.setTop(top);
        prepareLoadSample(sampleIdentifier, sample);

        IEntityProperty[] properties =
                createService().tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                        sampleIdentifier);

        assertEquals(1, properties.length);
        assertEquals(property.getValue(), properties[0].getValue());
        context.assertIsSatisfied();
    }

    private final static SamplePropertyPE createSamplePropertyPE(final String code,
            final EntityDataType dataType, final String value)
    {
        final SamplePropertyPE propertyPE = new SamplePropertyPE();
        final SampleTypePropertyTypePE entityTypePropertyTypePE = new SampleTypePropertyTypePE();
        final SampleTypePE sampleTypePE = new SampleTypePE();
        sampleTypePE.setCode(code + "ST");
        sampleTypePE.setListable(true);
        sampleTypePE.setGeneratedFromHierarchyDepth(0);
        sampleTypePE.setContainerHierarchyDepth(0);
        final PropertyTypePE propertyTypePE = new PropertyTypePE();
        propertyTypePE.setCode(code);
        propertyTypePE.setLabel(code);
        final DataTypePE type = new DataTypePE();
        type.setCode(dataType);
        propertyTypePE.setType(type);
        entityTypePropertyTypePE.setPropertyType(propertyTypePE);
        entityTypePropertyTypePE.setEntityType(sampleTypePE);
        propertyPE.setEntityTypePropertyType(entityTypePropertyTypePE);
        propertyPE.setValue(value);
        return propertyPE;
    }
    
    @Test
    public void testRegisterSample()
    {
        prepareGetSession();
        final NewSample sample = new NewSample();
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(sample);
                    one(sampleBO).save();

                }
            });

        createService().registerSample(SESSION_TOKEN, sample);
        
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
        final NewExternalData externalData = new NewExternalData();
        externalData.setCode("dc");
        externalData.setMeasured(true);
        prepareRegisterDataSet(sampleIdentifier, sample.getExperiment(), SourceType.MEASUREMENT,
                externalData);

        createService().registerDataSet(SESSION_TOKEN, sampleIdentifier, externalData);

        context.assertIsSatisfied();
    }

    private void prepareRegisterDataSet(final SampleIdentifier sampleIdentifier,
            final ExperimentPE experiment, final SourceType sourceType,
            final NewExternalData externalData)
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
        SamplePE sample = createSample();
        sample.setExperiment(experiment);
        return sample;
    }

    private SamplePE createSample()
    {
        final SamplePE sample = new SamplePE();
        sample.setCode("sample code");
        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode("sample type code");
        sampleType.setContainerHierarchyDepth(1);
        sampleType.setGeneratedFromHierarchyDepth(1);
        sampleType.setListable(false);
        sample.setSampleType(sampleType);
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
        List<DatastoreServiceDescription> reporting =
                Arrays.asList(createDataStoreService("reporting"));
        List<DatastoreServiceDescription> processing =
                Arrays.asList(createDataStoreService("processing"));
        DatastoreServiceDescriptions services =
                new DatastoreServiceDescriptions(reporting, processing);
        info.setServicesDescriptions(services);
        return info;
    }

    private static DatastoreServiceDescription createDataStoreService(String key)
    {
        // unknown data set type codes should be silently discarded
        return new DatastoreServiceDescription(key, key, new String[]
            { DATA_SET_TYPE_CODE, UNKNOWN_DATA_SET_TYPE_CODE }, key);
    }
}
