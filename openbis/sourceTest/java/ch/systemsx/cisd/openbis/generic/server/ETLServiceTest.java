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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationServerManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatabaseInstancePEBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ETLService.class)
public class ETLServiceTest extends AbstractServerTestCase
{
    private static final String UNKNOWN_DATA_SET_TYPE_CODE = "completely-unknown-code";

    private static final String DATA_SET_TYPE_CODE = "dataSetTypeCode1";

    private static final String DOWNLOAD_URL = "download-url";

    private static final int TIMEOUT_IN_MINUTES = 1;

    private static final long DSS_ID = 137L;

    private static final String DSS_CODE = "my-dss";

    private static final String DSS_SESSION_TOKEN = "dss42";

    private static final int PORT = 443;

    private static final String URL = "http://remote-host:" + PORT;

    private static final String USER_FOR_ENTITY_OPERATIONS = "eo-user";

    private ICommonBusinessObjectFactory boFactory;

    private IDataStoreServiceFactory dssfactory;

    private IDataStoreService dataStoreService;

    private IETLEntityOperationChecker entityOperationChecker;

    private IDataStoreServiceRegistrator dataStoreServiceRegistrator;

    private IServiceConversationClientManagerLocal conversationClient;

    private IServiceConversationServerManagerLocal conversationServer;

    private ISessionManager<Session> sessionManagerForEntityOperations;

    private PersonPE sessionPerson;

    @Override
    @BeforeMethod
    @SuppressWarnings("unchecked")
    public final void setUp()
    {
        super.setUp();
        boFactory = context.mock(ICommonBusinessObjectFactory.class);
        dssfactory = context.mock(IDataStoreServiceFactory.class);
        dataStoreService = context.mock(IDataStoreService.class);
        entityOperationChecker = context.mock(IETLEntityOperationChecker.class);
        dataStoreServiceRegistrator = context.mock(IDataStoreServiceRegistrator.class);
        conversationClient = context.mock(IServiceConversationClientManagerLocal.class);
        conversationServer = context.mock(IServiceConversationServerManagerLocal.class);
        sessionManagerForEntityOperations =
                context.mock(ISessionManager.class, "sessionManagerForEntityOperations");
        sessionPerson = new PersonPE();
        session.setPerson(sessionPerson);
    }

    @Test
    public void testListFileDataSets()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(dataStoreDAO).tryToFindDataStoreByCode(DSS_CODE);
                    DataStorePE store = new DataStorePE();
                    store.setId(DSS_ID);
                    store.setCode(DSS_CODE);
                    will(returnValue(store));

                    one(boFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));

                    one(datasetLister).listByDataStore(DSS_ID);
                    DataSetBuilder ds1 =
                            new DataSetBuilder()
                                    .type("my-type")
                                    .code("ds-1")
                                    .location("loc-a")
                                    .shareID("share-1")
                                    .size(4711L)
                                    .store(new DataStoreBuilder(DSS_CODE).getStore())
                                    .experiment(
                                            new ExperimentBuilder().identifier("DB:/G1/P/EXP1")
                                                    .getExperiment());
                    will(returnValue(Arrays.asList(ds1.getDataSet())));
                    allowing(daoFactory).getPersistencyResources();
                    will(returnValue(new PersistencyResources(null, null, null, null)));

                }
            });

        List<SimpleDataSetInformationDTO> dataSets =
                createService().listFileDataSets(SESSION_TOKEN, DSS_CODE);

        assertEquals(DSS_CODE, dataSets.get(0).getDataStoreCode());
        assertEquals("my-type", dataSets.get(0).getDataSetType());
        assertEquals("ds-1", dataSets.get(0).getDataSetCode());
        assertEquals("share-1", dataSets.get(0).getDataSetShareId());
        assertEquals("loc-a", dataSets.get(0).getDataSetLocation());
        assertEquals(4711L, dataSets.get(0).getDataSetSize().longValue());
        assertEquals("EXP1", dataSets.get(0).getExperimentCode());
        assertEquals("P", dataSets.get(0).getProjectCode());
        assertEquals("G1", dataSets.get(0).getGroupCode());
        assertEquals("DB", dataSets.get(0).getDatabaseInstanceCode());
        assertEquals(1, dataSets.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testListShareIds()
    {
        prepareGetSession();

        final List<DataSetShareId> list =
                Arrays.asList(createDataSetShareId(1), createDataSetShareId(2),
                        createDataSetShareId(3));
        context.checking(new Expectations()
            {
                {
                    one(dataStoreDAO).tryToFindDataStoreByCode(DSS_CODE);
                    DataStorePE store = new DataStorePE();
                    store.setId(DSS_ID);
                    store.setCode(DSS_CODE);
                    will(returnValue(store));

                    one(boFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));

                    one(datasetLister).listAllDataSetShareIdsByDataStore(DSS_ID);
                    will(returnValue(list));

                    allowing(daoFactory).getPersistencyResources();
                    will(returnValue(new PersistencyResources(null, null, null, null)));

                }
            });

        List<DataSetShareId> result = createService().listShareIds(SESSION_TOKEN, DSS_CODE);
        assertEquals(list, result);
        context.assertIsSatisfied();
    }

    private DataSetShareId createDataSetShareId(int nr)
    {
        DataSetShareId result = new DataSetShareId();
        result.setDataSetCode("ds-" + nr);
        result.setShareId("share-" + nr);
        return result;
    }

    @Test
    public void testRegisterDataStoreServer()
    {
        final DataStoreServerInfo info = createDSSInfo();
        prepareGetSession();
        final RecordingMatcher<DataStorePE> dataStoreRecordingMatcher =
                new RecordingMatcher<DataStorePE>();
        context.checking(new Expectations()
            {
                {
                    one(dataStoreServiceRegistrator).setServiceDescriptions(
                            with(dataStoreRecordingMatcher), with(info.getServicesDescriptions()));

                    one(conversationClient).setDataStoreInformation(URL, TIMEOUT_IN_MINUTES);
                    one(conversationServer).setDataStoreInformation(DSS_CODE, URL,
                            TIMEOUT_IN_MINUTES);

                    one(dataStoreDAO).tryToFindDataStoreByCode(DSS_CODE);
                    will(returnValue(null));

                    one(dssfactory).create(URL);
                    will(returnValue(dataStoreService));

                    one(dataStoreService).getVersion(DSS_SESSION_TOKEN);
                    will(returnValue(IDataStoreService.VERSION));

                    allowing(dataStoreDAO).createOrUpdateDataStore(with(dataStoreRecordingMatcher));
                }
            });

        createService().registerDataStoreServer(SESSION_TOKEN, info);

        List<DataStorePE> recordedObjects = dataStoreRecordingMatcher.getRecordedObjects();
        DataStorePE store1 = recordedObjects.get(0);
        assertEquals(DSS_CODE, store1.getCode());
        assertEquals(URL, store1.getRemoteUrl());
        assertEquals(DOWNLOAD_URL, store1.getDownloadUrl());
        assertEquals(DSS_SESSION_TOKEN, store1.getSessionToken());
        DataStorePE store2 = recordedObjects.get(1);
        assertSame(store1, store2);
        assertEquals(2, recordedObjects.size());
        context.assertIsSatisfied();
    }

    protected ArrayList<DataSetTypePE> getAllDataSetTypes()
    {
        // Prepare the collection of data set types
        DataSetTypePE dataSetType;
        ArrayList<DataSetTypePE> allDataSetTypes = new ArrayList<DataSetTypePE>();

        dataSetType = new DataSetTypePE();
        dataSetType.setCode(DATA_SET_TYPE_CODE);
        allDataSetTypes.add(dataSetType);

        dataSetType = new DataSetTypePE();
        dataSetType.setCode("dataSetTypeCode2");
        allDataSetTypes.add(dataSetType);

        dataSetType = new DataSetTypePE();
        dataSetType.setCode("differentPrefix");
        allDataSetTypes.add(dataSetType);
        return allDataSetTypes;
    }

    @Test
    public void testRegisterDataStoreServerAgain()
    {
        final String reportingPluginTypes = ".*";
        final String processingPluginTypes = "dataSet.*";
        final DataStoreServerInfo info =
                createDSSInfoWithWildcards(reportingPluginTypes, processingPluginTypes);
        final RecordingMatcher<DataStorePE> dataStoreRecordingMatcher =
                new RecordingMatcher<DataStorePE>();
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(dataStoreServiceRegistrator).setServiceDescriptions(
                            with(dataStoreRecordingMatcher), with(info.getServicesDescriptions()));

                    one(conversationClient).setDataStoreInformation(URL, TIMEOUT_IN_MINUTES);
                    one(conversationServer).setDataStoreInformation(DSS_CODE, URL,
                            TIMEOUT_IN_MINUTES);

                    one(dataStoreDAO).tryToFindDataStoreByCode(DSS_CODE);
                    will(returnValue(new DataStorePE()));

                    one(dssfactory).create(URL);
                    will(returnValue(dataStoreService));

                    one(dataStoreService).getVersion(DSS_SESSION_TOKEN);
                    will(returnValue(IDataStoreService.VERSION));

                    allowing(dataStoreDAO).createOrUpdateDataStore(with(dataStoreRecordingMatcher));
                }
            });

        createService().registerDataStoreServer(SESSION_TOKEN, info);

        List<DataStorePE> recordedObjects = dataStoreRecordingMatcher.getRecordedObjects();
        DataStorePE store1 = recordedObjects.get(0);
        assertEquals(DSS_CODE, store1.getCode());
        assertEquals(URL, store1.getRemoteUrl());
        assertEquals(DOWNLOAD_URL, store1.getDownloadUrl());
        assertEquals(DSS_SESSION_TOKEN, store1.getSessionToken());
        for (DataStoreServicePE service : store1.getServices())
        {
            // Check that the types found match those specified
            if (service.getKind() == DataStoreServiceKind.PROCESSING)
            {
                // expect 2 matches
                assertDataSetTypeMatch(service, processingPluginTypes, 2);
            }
            if (service.getKind() == DataStoreServiceKind.QUERIES)
            {
                // expect 3 matches
                assertDataSetTypeMatch(service, reportingPluginTypes, 3);
            }
        }
        DataStorePE store2 = recordedObjects.get(1);
        assertSame(store1, store2);
        assertEquals(2, recordedObjects.size());
        context.assertIsSatisfied();
    }

    private void assertDataSetTypeMatch(DataStoreServicePE service, String typeRegex,
            int numberExpected)
    {
        Set<DataSetTypePE> datasetTypes = service.getDatasetTypes();
        for (DataSetTypePE types : datasetTypes)
        {
            assertEquals("'" + types.getCode() + "' doesnot match '" + typeRegex + "'.", true,
                    types.getCode().matches(typeRegex));
        }
        assertEquals(numberExpected, datasetTypes.size());
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
                    "Data Store Server version is " + (VERSION + 1) + " instead of " + VERSION,
                    e.getMessage());
        }

        context.assertIsSatisfied();
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
    public void testCreatePermId()
    {
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).getSession(SESSION_TOKEN);

                    one(daoFactory).getPermIdDAO();
                    will(returnValue(permIdDAO));

                    one(permIdDAO).createPermId();
                    will(returnValue("permId"));

                    allowing(daoFactory).getPersistencyResources();
                    will(returnValue(new PersistencyResources(null, null, null, null)));

                }
            });

        String dataSetCode = createService().createPermId(SESSION_TOKEN);

        assertEquals("permId", dataSetCode);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryGetSampleWithExperimentForAnUnknownSample()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "S1");
        prepareTryToLoadSample(sampleIdentifier, null);

        Sample sample = createService().tryGetSampleWithExperiment(SESSION_TOKEN, sampleIdentifier);

        assertNull(sample);
        context.assertIsSatisfied();
    }

    @Test
    public void testTryGetSampleWithExperimentForSampleWithNoValidProcedure()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "S1");
        SamplePE samplePE = createSample();

        prepareTryToLoadSampleWithMetaProjects(sampleIdentifier, samplePE);

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
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "S1");
        final ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        SamplePE sample = createSampleWithExperiment(experiment);
        prepareTryToLoadSampleWithMetaProjects(sampleIdentifier, sample);

        Sample actualSample =
                createService().tryGetSampleWithExperiment(SESSION_TOKEN, sampleIdentifier);
        Experiment actualExperiment = actualSample.getExperiment();
        assertEquals(sample.getCode(), actualSample.getCode());
        assertEquals(sample.getSampleType().getCode(), actualSample.getSampleType().getCode());
        assertEquals(experiment.getCode(), actualExperiment.getCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetSampleIdentifier()
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryToFindByPermID("abc");
                    SamplePE sample = new SamplePE();
                    sample.setCode("S42");
                    will(returnValue(sample));
                    allowing(daoFactory).getPersistencyResources();
                    will(returnValue(new PersistencyResources(null, null, null, null)));

                }
            });

        SampleIdentifier identifier =
                createService().tryToGetSampleIdentifier(SESSION_TOKEN, "abc");

        assertEquals("S42", identifier.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetSampleType()
    {
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).getSession(SESSION_TOKEN);

                    one(sampleTypeDAO).tryFindSampleTypeByCode("MY_TYPE");
                    SampleTypePE sampleTypePE = new SampleTypePE();
                    sampleTypePE.setListable(Boolean.TRUE);
                    sampleTypePE.setGeneratedFromHierarchyDepth(new Integer(1));
                    sampleTypePE.setContainerHierarchyDepth(new Integer(1));
                    sampleTypePE.setAutoGeneratedCode(Boolean.FALSE);
                    sampleTypePE.setShowParentMetadata(Boolean.FALSE);
                    sampleTypePE.setSubcodeUnique(Boolean.FALSE);
                    will(returnValue(sampleTypePE));
                    allowing(daoFactory).getPersistencyResources();
                    will(returnValue(new PersistencyResources(null, null, null, null)));

                }
            });
        SampleType sampleType = createService().getSampleType(SESSION_TOKEN, "MY_TYPE");

        assertEquals(true, sampleType.isListable());
        assertEquals(1, sampleType.getGeneratedFromHierarchyDepth());
        assertEquals(1, sampleType.getContainerHierarchyDepth());
        assertEquals(false, sampleType.isAutoGeneratedCode());
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetPropertiesOfTopSampleForAnUnknownSample()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "S1");
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
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "S1");
        SamplePE toplessSample = new SamplePE();
        SamplePropertyPE property = setAnyProperty(toplessSample);
        prepareLoadSample(sampleIdentifier, toplessSample);

        final IEntityProperty[] properties =
                createService().tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                        sampleIdentifier);

        assertEquals(1, properties.length);
        assertEquals(property.getValue(), properties[0].getValue());
        context.assertIsSatisfied();
    }

    @Test
    public void testTryToGetPropertiesOfTopSampleWhichHasNoProperties()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "S1");
        SamplePE sample = createSample("S1");
        SamplePE top = createSample("S2");
        SamplePE parent = createSample("S3");
        parent.addParentRelationship(new SampleRelationshipPE(top, parent,
                createParentChildRelation(), null));
        sample.addParentRelationship(new SampleRelationshipPE(parent, sample,
                createParentChildRelation(), null));

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
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "S1");
        SamplePE sample = createSample("S1");
        SamplePE top = createSample("S2");
        SamplePE parent = createSample("S3");
        parent.addParentRelationship(new SampleRelationshipPE(top, parent,
                createParentChildRelation(), null));
        sample.addParentRelationship(new SampleRelationshipPE(parent, sample,
                createParentChildRelation(), null));
        SamplePropertyPE property = setAnyProperty(top);
        prepareLoadSample(sampleIdentifier, sample);

        IEntityProperty[] properties =
                createService().tryToGetPropertiesOfTopSampleRegisteredFor(SESSION_TOKEN,
                        sampleIdentifier);

        assertEquals(1, properties.length);
        assertEquals(property.getValue(), properties[0].getValue());
        context.assertIsSatisfied();
    }

    private SamplePE createSample(String code)
    {
        SamplePE sample = new SamplePE();
        sample.setCode(code);
        return sample;
    }

    private RelationshipTypePE createParentChildRelation()
    {
        RelationshipTypePE relationship2 = new RelationshipTypePE();
        relationship2.setCode(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
        return relationship2;
    }

    private SamplePropertyPE setAnyProperty(SamplePE top)
    {
        SamplePropertyPE property =
                createSamplePropertyPE("type code", DataTypeCode.VARCHAR, "The Valüe");

        top.setProperties(new LinkedHashSet<SamplePropertyPE>(Arrays.asList(property)));
        return property;
    }

    private final static SamplePropertyPE createSamplePropertyPE(final String code,
            final DataTypeCode dataType, final String value)
    {
        final SamplePropertyPE propertyPE = new SamplePropertyPE();
        final SampleTypePropertyTypePE entityTypePropertyTypePE = new SampleTypePropertyTypePE();
        final SampleTypePE sampleTypePE = new SampleTypePE();
        sampleTypePE.setCode(code + "ST");
        sampleTypePE.setListable(true);
        sampleTypePE.setAutoGeneratedCode(false);
        sampleTypePE.setSubcodeUnique(false);
        sampleTypePE.setShowParentMetadata(false);
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
        final long id = 123456789L;
        final NewSample sample = new NewSample();
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(sample);
                    one(sampleBO).save();
                    exactly(1).of(sampleBO).getSample();
                    SamplePE samplePE = new SamplePE();
                    samplePE.setId(id);
                    will(returnValue(samplePE));
                }
            });

        assertEquals(id, createService().registerSample(SESSION_TOKEN, sample, null));

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterSampleForAnExistingPerson()
    {
        prepareGetSession();
        final long id = 123456789L;
        final NewSample sample = new NewSample();
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(sample);
                    one(sampleBO).save();
                    exactly(2).of(sampleBO).getSample();
                    SamplePE samplePE = new SamplePE();
                    samplePE.setId(id);
                    will(returnValue(samplePE));

                    one(personDAO).tryFindPersonByUserId(CommonTestUtils.USER_ID);
                    will(returnValue(new PersonPE()));
                }
            });

        assertEquals(id,
                createService().registerSample(SESSION_TOKEN, sample, CommonTestUtils.USER_ID));

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterSampleForANonExistingPerson()
    {
        prepareGetSession();
        final long id = 123456789L;
        final NewSample sample = new NewSample();
        prepareRegisterPerson();
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(sample);
                    one(sampleBO).save();
                    exactly(2).of(sampleBO).getSample();
                    SamplePE samplePE = new SamplePE();
                    samplePE.setId(id);
                    will(returnValue(samplePE));

                    one(personDAO).tryFindPersonByUserId(CommonTestUtils.USER_ID);
                    will(returnValue(null));
                }
            });

        assertEquals(id,
                createService().registerSample(SESSION_TOKEN, sample, CommonTestUtils.USER_ID));

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterDataSetForUnknownExperiment()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "S1");
        prepareTryToLoadSample(sampleIdentifier, new SamplePE());

        try
        {
            createService().registerDataSet(SESSION_TOKEN, sampleIdentifier, null);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals("No experiment found for sample DB:/S1", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterDataSetForInvalidExperiment()
    {
        SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "S1");
        ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        experiment.setDeletion(new DeletionPE());
        prepareTryToLoadSample(sampleIdentifier, createSampleWithExperiment(experiment));

        try
        {
            createService().registerDataSet(SESSION_TOKEN, sampleIdentifier, null);
            fail("UserFailureException expected");
        } catch (UserFailureException e)
        {
            assertEquals(
                    "Data set can not be registered because experiment 'DB:/G1/P/EXP1' is in trash.",
                    e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterDataSet()
    {
        final SampleIdentifier sampleIdentifier =
                new SampleIdentifier(new DatabaseInstanceIdentifier("DB"), "S1");
        final ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        SamplePE sample = createSampleWithExperiment(experiment);
        prepareTryToLoadSample(sampleIdentifier, sample);
        final NewExternalData externalData = new NewExternalData();
        externalData.setCode("dc");
        externalData.setMeasured(true);
        prepareRegisterDataSet(session, sampleIdentifier, sample.getExperiment(),
                SourceType.MEASUREMENT, externalData);

        createService().registerDataSet(SESSION_TOKEN, sampleIdentifier, externalData);

        context.assertIsSatisfied();
    }

    @Test
    public void testListAdministrators()
    {
        prepareGetSession();

        context.checking(new Expectations()
            {
                {
                    final PersonPE personPE = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
                    assignRoles(personPE);
                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(personPE)));
                    allowing(daoFactory).getPersistencyResources();
                    will(returnValue(new PersistencyResources(null, null, null, null)));

                }
            });

        List<Person> admins = createService().listAdministrators(SESSION_TOKEN);
        assertEquals(1, admins.size());

        context.assertIsSatisfied();
    }

    @Test
    public void testTryPersonWithUserIdOrEmail()
    {
        prepareGetSession();

        context.checking(new Expectations()
            {
                {
                    final PersonPE personPE = CommonTestUtils.createPersonFromPrincipal(PRINCIPAL);
                    assignRoles(personPE);
                    // The first search is by userId
                    oneOf(personDAO).tryFindPersonByUserId(PRINCIPAL.getUserId());
                    will(returnValue(personPE));

                    // The second by email
                    one(personDAO).tryFindPersonByUserId(PRINCIPAL.getEmail());
                    will(returnValue(null));
                    one(personDAO).tryFindPersonByEmail(PRINCIPAL.getEmail());
                    will(returnValue(personPE));
                }
            });

        Person result;

        result = createService().tryPersonWithUserIdOrEmail(SESSION_TOKEN, PRINCIPAL.getUserId());
        assertEquals(PRINCIPAL.getEmail(), result.getEmail());

        result = createService().tryPersonWithUserIdOrEmail(SESSION_TOKEN, PRINCIPAL.getEmail());
        assertEquals(PRINCIPAL.getEmail(), result.getEmail());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterSampleAndDataSet()
    {
        prepareGetSession();

        final ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        final SamplePE samplePE = createSampleWithExperiment(experiment);
        final SampleIdentifier sampleIdentifier = samplePE.getSampleIdentifier();

        final NewSample sample = new NewSample();
        sample.setIdentifier(sampleIdentifier.toString());

        final NewExternalData externalData = new NewExternalData();
        externalData.setCode("dc");
        externalData.setMeasured(true);

        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(sample);
                    one(sampleBO).save();
                    exactly(2).of(sampleBO).getSample();
                    will(returnValue(samplePE));

                    one(personDAO).tryFindPersonByUserId(CommonTestUtils.USER_ID);
                    will(returnValue(new PersonPE()));

                    one(boFactory).createDataBO(session);
                    will(returnValue(dataBO));

                    one(dataBO).define(externalData, samplePE, SourceType.MEASUREMENT);
                    one(dataBO).save();
                    one(dataBO).getData();
                    ExternalDataPE externalDataPE = new ExternalDataPE();
                    externalDataPE.setCode(externalData.getCode());
                    will(returnValue(externalDataPE));
                }
            });

        Sample result =
                createService().registerSampleAndDataSet(SESSION_TOKEN, sample, externalData,
                        CommonTestUtils.USER_ID);
        assertNotNull(result);
        assertEquals(sample.getIdentifier(), result.getIdentifier());
        assertEquals(experiment.getIdentifier(), result.getExperiment().getIdentifier());

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateSampleAndRegisterDataSet()
    {
        prepareGetSession();

        final ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        final SamplePE samplePE = createSampleWithExperiment(experiment);
        final SampleIdentifier sampleIdentifier = samplePE.getSampleIdentifier();

        final Date version = new Date();
        final Collection<NewAttachment> attachments = Collections.<NewAttachment> emptyList();

        final SampleUpdatesDTO sample =
                new SampleUpdatesDTO(CommonTestUtils.TECH_ID, null, null, attachments, version,
                        sampleIdentifier, null, null);

        final NewExternalData externalData = new NewExternalData();
        externalData.setCode("dc");
        externalData.setMeasured(true);

        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).update(sample);
                    one(sampleBO).save();
                    one(sampleBO).getSample();
                    will(returnValue(samplePE));

                    one(boFactory).createDataBO(session);
                    will(returnValue(dataBO));

                    one(dataBO).define(externalData, samplePE, SourceType.MEASUREMENT);
                    one(dataBO).save();
                    one(dataBO).getData();
                    ExternalDataPE externalDataPE = new ExternalDataPE();
                    externalDataPE.setCode(externalData.getCode());
                    will(returnValue(externalDataPE));

                    allowing(daoFactory).getPersistencyResources();
                    will(returnValue(new PersistencyResources(null, null, null, null)));

                    one(metaprojectDAO).listMetaprojectsForEntity(sessionPerson, samplePE);
                }
            });

        Sample result =
                createService().updateSampleAndRegisterDataSet(SESSION_TOKEN, sample, externalData);
        assertNotNull(result);
        assertEquals(sample.getSampleIdentifier().toString(), result.getIdentifier());
        assertEquals(experiment.getIdentifier(), result.getExperiment().getIdentifier());

        context.assertIsSatisfied();
    }

    @Test
    public void testPerformOperationsWithoutRegistrationId()
    {
        prepareGetSession();

        final ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        final SamplePE samplePE = createSampleWithExperiment(experiment);
        final SampleIdentifier sampleIdentifier = samplePE.getSampleIdentifier();

        final Date version = new Date();
        final Collection<NewAttachment> attachments = Collections.<NewAttachment> emptyList();

        List<ExperimentUpdatesDTO> experimentUpdates = new ArrayList<ExperimentUpdatesDTO>();

        final SampleUpdatesDTO sampleUpdate =
                new SampleUpdatesDTO(CommonTestUtils.TECH_ID, null, null, attachments, version,
                        sampleIdentifier, null, null);

        final MaterialPE material = new MaterialPE();
        material.setCode("new-material");
        final MaterialTypePE materialType = new MaterialTypePE();
        materialType.setCode("new-material-type");
        materialType.setDatabaseInstance(new DatabaseInstancePEBuilder().code("DB")
                .getDatabaseInstance());
        final NewMaterial newMaterial = new NewMaterial(material.getCode());
        Map<String, List<NewMaterial>> materialRegistrations =
                new HashMap<String, List<NewMaterial>>();
        materialRegistrations.put(materialType.getCode(), Arrays.asList(newMaterial));

        List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();

        final SamplePE newSamplePE = createSampleWithExperiment(experiment);
        newSamplePE.setCode("SAMPLE_CODE_NEW");
        final SampleIdentifier newSampleIdentifier = newSamplePE.getSampleIdentifier();
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(newSampleIdentifier.toString());

        final NewExternalData externalData = new NewExternalData();
        externalData.setCode("dc");
        externalData.setMeasured(true);
        externalData.setSampleIdentifierOrNull(newSampleIdentifier);

        final String updatedDataSetCode = "updateDataSetCode";
        final DataSetBatchUpdatesDTO dataSetUpdate = new DataSetBatchUpdatesDTO();
        dataSetUpdate.setDatasetId(CommonTestUtils.TECH_ID);
        dataSetUpdate.setFileFormatTypeCode("new-file-format");
        dataSetUpdate.setModifiedContainedDatasetCodesOrNull(new String[]
            { "c1", "c2" });

        prepareEntityOperationsExpectations(samplePE, sampleUpdate, material, materialType,
                materialRegistrations, newSamplePE, newSampleIdentifier, newSample, externalData,
                updatedDataSetCode, dataSetUpdate);

        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetails(null, USER_FOR_ENTITY_OPERATIONS,
                        new ArrayList<NewSpace>(), new ArrayList<NewProject>(),
                        new ArrayList<NewExperiment>(), experimentUpdates,
                        Collections.singletonList(sampleUpdate),
                        Collections.singletonList(newSample), materialRegistrations,
                        materialUpdates, Collections.singletonList(externalData),
                        Collections.singletonList(dataSetUpdate));

        AtomicEntityOperationResult result =
                createService().performEntityOperations(SESSION_TOKEN, details);
        assertNotNull(result);
        assertEquals(1, result.getSamplesUpdatedCount());
        assertEquals(1, result.getSamplesCreatedCount());
        assertEquals(1, result.getDataSetsUpdatedCount());

        context.assertIsSatisfied();
    }

    private void prepareEntityOperationsExpectations(final SamplePE samplePE,
            final SampleUpdatesDTO sampleUpdate, final MaterialPE material,
            final MaterialTypePE materialType, final Map<String, List<NewMaterial>> newMaterials,
            final SamplePE newSamplePE, final SampleIdentifier newSampleIdentifier,
            final NewSample newSample, final NewExternalData externalData,
            final String updatedDataSetCode, final DataSetBatchUpdatesDTO dataSetUpdate)
    {
        final Session userSession = createSession(USER_FOR_ENTITY_OPERATIONS);
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManagerForEntityOperations).tryToOpenSession(
                            USER_FOR_ENTITY_OPERATIONS, "dummy password");
                    String sessionToken = "session-token-eo";
                    will(returnValue(sessionToken));

                    allowing(sessionManagerForEntityOperations).getSession(sessionToken);
                    will(returnValue(userSession));

                    one(sessionManagerForEntityOperations).closeSession(sessionToken);

                    allowing(personDAO).tryFindPersonByUserId(USER_FOR_ENTITY_OPERATIONS);
                    PersonPE user = createSystemUser();
                    will(returnValue(user));

                    allowing(daoFactory).getEntityTypeDAO(EntityKind.MATERIAL);
                    will(returnValue(entityTypeDAO));

                    allowing(entityTypeDAO).tryToFindEntityTypeByCode(materialType.getCode());
                    will(returnValue(materialType));

                    one(entityOperationChecker).assertMaterialCreationAllowed(userSession,
                            newMaterials);
                    List<NewMaterial> newMaterialsList = newMaterials.values().iterator().next();
                    one(propertiesBatchManager).manageProperties(materialType, newMaterialsList,
                            user);

                    one(boFactory).createMaterialTable(userSession);
                    will(returnValue(materialTable));

                    one(materialTable).add(newMaterialsList, materialType);
                    one(materialTable).save();
                    one(materialTable).getMaterials();
                    will(returnValue(Arrays.asList(material)));

                    one(entityOperationChecker).assertExperimentCreationAllowed(userSession,
                            Collections.<NewExperiment> emptyList());

                    List<NewSample> sampleList = Arrays.asList(newSample);
                    one(entityOperationChecker).assertSpaceSampleCreationAllowed(userSession,
                            sampleList);

                    one(boFactory).createSampleTable(userSession);
                    will(returnValue(sampleTable));

                    one(sampleTable).prepareForRegistration(sampleList, user);
                    one(sampleTable).save();

                    one(boFactory).createSampleTable(userSession);
                    will(returnValue(sampleTable));

                    List<SampleUpdatesDTO> sampleUpdateList = Arrays.asList(sampleUpdate);
                    one(entityOperationChecker).assertSpaceSampleUpdateAllowed(userSession,
                            sampleUpdateList);
                    one(sampleTable).checkBeforeUpdate(sampleUpdateList);
                    one(sampleTable).prepareForUpdateWithSampleUpdates(sampleUpdateList);
                    one(sampleTable).save();

                    one(entityOperationChecker).assertDataSetCreationAllowed(userSession,
                            Arrays.asList(externalData));

                    one(boFactory).createDataSetTable(userSession);
                    will(returnValue(dataSetTable));

                    one(entityOperationChecker).assertDataSetUpdateAllowed(userSession,
                            Arrays.asList(dataSetUpdate));
                    one(dataSetTable).checkBeforeUpdate(Arrays.asList(dataSetUpdate));
                    one(dataSetTable).update(Arrays.asList(dataSetUpdate));
                    one(dataSetTable).save();
                }
            });

        prepareTryToLoadSample(userSession, newSampleIdentifier, newSamplePE);
        prepareRegisterDataSet(userSession, newSampleIdentifier, newSamplePE.getExperiment(),
                SourceType.MEASUREMENT, externalData);

        context.checking(new Expectations()
            {
                {
                    one(dataBO).getData();
                    ExternalDataPE externalDataPE = new ExternalDataPE();
                    externalDataPE.setCode(externalData.getCode());

                    DataStorePE store = new DataStorePE();
                    store.setCode(DSS_CODE);
                    externalDataPE.setDataStore(store);
                    will(returnValue(externalDataPE));

                    // one(boFactory).createDataBO(SESSION);
                    // will(returnValue(dataBO));
                    //
                    // exactly(1).of(boFactory).createSampleBO(SESSION);
                    // will(returnValue(sampleBO));
                    //
                    // one(dataBO).define(externalData, samplePE, SourceType.MEASUREMENT);
                    // one(dataBO).save();
                    // one(dataBO).getExternalData();
                    // ExternalDataPE externalDataPE = new ExternalDataPE();
                    // externalDataPE.setCode(externalData.getCode());
                    // will(returnValue(externalDataPE));
                }
            });
    }

    @Test
    public void testPerformOperations()
    {
        prepareGetSession();

        final ExperimentPE experiment = createExperiment("TYPE", "EXP1", "G1");
        final SamplePE samplePE = createSampleWithExperiment(experiment);
        final SampleIdentifier sampleIdentifier = samplePE.getSampleIdentifier();

        final Date version = new Date();
        final Collection<NewAttachment> attachments = Collections.<NewAttachment> emptyList();

        final SampleUpdatesDTO sampleUpdate =
                new SampleUpdatesDTO(CommonTestUtils.TECH_ID, null, null, attachments, version,
                        sampleIdentifier, null, null);

        final MaterialPE material = new MaterialPE();
        material.setCode("new-material");
        final MaterialTypePE materialType = new MaterialTypePE();
        materialType.setCode("new-material-type");
        materialType.setDatabaseInstance(new DatabaseInstancePEBuilder().code("DB")
                .getDatabaseInstance());
        final NewMaterial newMaterial = new NewMaterial(material.getCode());
        Map<String, List<NewMaterial>> materialRegistrations =
                new HashMap<String, List<NewMaterial>>();
        materialRegistrations.put(materialType.getCode(), Arrays.asList(newMaterial));

        List<MaterialUpdateDTO> materialUpdates = new ArrayList<MaterialUpdateDTO>();

        final SamplePE newSamplePE = createSampleWithExperiment(experiment);
        newSamplePE.setCode("SAMPLE_CODE_NEW");
        final SampleIdentifier newSampleIdentifier = newSamplePE.getSampleIdentifier();
        final NewSample newSample = new NewSample();
        newSample.setIdentifier(newSampleIdentifier.toString());

        final NewExternalData externalData = new NewExternalData();
        externalData.setCode("dc");
        externalData.setMeasured(true);
        externalData.setSampleIdentifierOrNull(newSampleIdentifier);

        final String updatedDataSetCode = "updateDataSetCode";
        final DataSetBatchUpdatesDTO dataSetUpdate = new DataSetBatchUpdatesDTO();
        dataSetUpdate.setDatasetId(CommonTestUtils.TECH_ID);
        dataSetUpdate.setFileFormatTypeCode("new-file-format");
        dataSetUpdate.setModifiedContainedDatasetCodesOrNull(new String[]
            { "c1", "c2" });

        prepareEntityOperationsExpectations(samplePE, sampleUpdate, material, materialType,
                materialRegistrations, newSamplePE, newSampleIdentifier, newSample, externalData,
                updatedDataSetCode, dataSetUpdate);
        context.checking(new Expectations()
            {
                {
                    one(entityOperationsLogDAO).addLogEntry(new Long(1));

                }
            });

        AtomicEntityOperationDetails details =
                new AtomicEntityOperationDetails(new TechId(1), USER_FOR_ENTITY_OPERATIONS,
                        new ArrayList<NewSpace>(), new ArrayList<NewProject>(),
                        new ArrayList<NewExperiment>(), new ArrayList<ExperimentUpdatesDTO>(),
                        Collections.singletonList(sampleUpdate),
                        Collections.singletonList(newSample), materialRegistrations,
                        materialUpdates, Collections.singletonList(externalData),
                        Collections.singletonList(dataSetUpdate));

        AtomicEntityOperationResult result =
                createService().performEntityOperations(SESSION_TOKEN, details);
        assertNotNull(result);
        assertEquals(1, result.getSamplesUpdatedCount());
        assertEquals(1, result.getSamplesCreatedCount());
        assertEquals(1, result.getDataSetsUpdatedCount());

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchForSamples()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleLister(session);
                    will(returnValue(sampleLister));

                    one(hibernateSearchDAO).searchForEntityIds(with(session.getUserName()),
                            with(aNonNull(DetailedSearchCriteria.class)),
                            with(equal(EntityKind.SAMPLE)), with(aNonNull(List.class)));
                    will(returnValue(Arrays.asList(new Long(1), new Long(2))));

                    one(hibernateSearchDAO).getResultSetSizeLimit();
                    will(returnValue(100));

                    one(sampleLister).list(with(aNonNull(ListOrSearchSampleCriteria.class)));
                    SampleBuilder sample1 = new SampleBuilder().id(1);
                    SampleBuilder sample2 = new SampleBuilder().id(2);
                    will(returnValue(Arrays.asList(sample1, sample2)));

                }
            });

        List<Sample> sample =
                createService().searchForSamples(SESSION_TOKEN, createSearchCriteriaForSample());

        assertEquals(2, sample.size());
        context.assertIsSatisfied();
    }

    private SearchCriteria createSearchCriteriaForSample()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "a code"));
        sc.addMatchClause(MatchClause.createPropertyMatch("MY_PROPERTY2", "a property value"));
        return sc;
    }

    private void prepareRegisterDataSet(final Session userSession,
            final SampleIdentifier sampleIdentifier, final ExperimentPE experiment,
            final SourceType sourceType, final NewExternalData externalData)
    {
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(userSession);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(sampleIdentifier);
                    one(sampleBO).getSample();
                    SamplePE sample = new SamplePE();
                    sample.setCode("S2");
                    sample.setSpace(createGroup("G1"));
                    sample.setExperiment(experiment);
                    will(returnValue(sample));

                    one(boFactory).createDataBO(userSession);
                    will(returnValue(dataBO));

                    one(dataBO).define(externalData, sample, sourceType);
                    one(dataBO).save();
                    one(dataBO).getData();
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
        sample.setCode("SAMPLE_CODE");
        final SampleTypePE sampleType = new SampleTypePE();
        sampleType.setCode("SAMPLE_TYPE_CODE");
        sampleType.setContainerHierarchyDepth(1);
        sampleType.setGeneratedFromHierarchyDepth(1);
        sampleType.setListable(false);
        sampleType.setAutoGeneratedCode(false);
        sampleType.setShowParentMetadata(false);
        sampleType.setSubcodeUnique(false);
        sample.setSampleType(sampleType);
        return sample;
    }

    private void prepareTryToLoadSampleWithMetaProjects(final SampleIdentifier identifier,
            final SamplePE sample)
    {
        prepareTryToLoadSample(session, identifier, sample);
        context.checking(new Expectations()
            {
                {
                    one(metaprojectDAO).listMetaprojectsForEntity(sessionPerson, sample);
                }
            });
    }

    private void prepareTryToLoadSample(final SampleIdentifier identifier, final SamplePE sample)
    {
        prepareTryToLoadSample(session, identifier, sample);
    }

    private void prepareTryToLoadSample(final Session userSession,
            final SampleIdentifier identifier, final SamplePE sample)
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(userSession);
                    will(returnValue(sampleBO));

                    one(sampleBO).tryToLoadBySampleIdentifier(identifier);
                    one(sampleBO).tryToGetSample();
                    will(returnValue(sample));
                    allowing(daoFactory).getPersistencyResources();
                    will(returnValue(new PersistencyResources(null, null, null, null)));

                }
            });
    }

    private void prepareLoadSample(final SampleIdentifier identifier, final SamplePE sample)
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleBO(session);
                    will(returnValue(sampleBO));

                    one(sampleBO).loadBySampleIdentifier(identifier);
                    one(sampleBO).getSample();
                    will(returnValue(sample));
                }
            });
    }

    private IETLLIMSService createService()
    {
        ETLService etlService =
                new ETLService(authenticationService, sessionManager, daoFactory,
                        propertiesBatchManager, boFactory, dssfactory, null,
                        entityOperationChecker, dataStoreServiceRegistrator,
                        sessionManagerForEntityOperations);
        etlService.setConversationClient(conversationClient);
        etlService.setConversationServer(conversationServer);
        etlService.setDisplaySettingsProvider(new DisplaySettingsProvider());

        return etlService;
    }

    private DataStoreServerInfo createDSSInfo()
    {
        DataStoreServerInfo info = new DataStoreServerInfo();
        info.setPort(PORT);
        info.setSessionToken(DSS_SESSION_TOKEN);
        info.setDataStoreCode(DSS_CODE);
        info.setDownloadUrl(DOWNLOAD_URL);
        info.setTimeoutInMinutes(TIMEOUT_IN_MINUTES);
        List<DatastoreServiceDescription> reporting =
                Arrays.asList(createDataStoreService(DataStoreServiceKind.QUERIES, "reporting"));
        List<DatastoreServiceDescription> processing =
                Arrays.asList(createDataStoreService(DataStoreServiceKind.PROCESSING, "processing"));
        DatastoreServiceDescriptions services =
                new DatastoreServiceDescriptions(reporting, processing);
        info.setServicesDescriptions(services);
        return info;
    }

    private DataStoreServerInfo createDSSInfoWithWildcards(String reportingPluginTypes,
            String processingPluginTypes)
    {
        DataStoreServerInfo info = new DataStoreServerInfo();
        info.setPort(PORT);
        info.setSessionToken(DSS_SESSION_TOKEN);
        info.setDataStoreCode(DSS_CODE);
        info.setDownloadUrl(DOWNLOAD_URL);
        info.setTimeoutInMinutes(TIMEOUT_IN_MINUTES);
        List<DatastoreServiceDescription> reporting =
                Arrays.asList(createDataStoreServiceForWildcardTypes(DataStoreServiceKind.QUERIES,
                        "reporting", reportingPluginTypes));
        List<DatastoreServiceDescription> processing =
                Arrays.asList(createDataStoreServiceForWildcardTypes(
                        DataStoreServiceKind.PROCESSING, "processing", processingPluginTypes));
        DatastoreServiceDescriptions services =
                new DatastoreServiceDescriptions(reporting, processing);
        info.setServicesDescriptions(services);
        return info;
    }

    @SuppressWarnings("deprecation")
    private static DatastoreServiceDescription createDataStoreService(
            DataStoreServiceKind serviceKind, String key)
    {
        // unknown data set type codes should be silently discarded
        return new DatastoreServiceDescription(key, key, new String[]
            { DATA_SET_TYPE_CODE, UNKNOWN_DATA_SET_TYPE_CODE }, key, serviceKind);
    }

    @SuppressWarnings("deprecation")
    private static DatastoreServiceDescription createDataStoreServiceForWildcardTypes(
            DataStoreServiceKind serviceKind, String key, String regex)
    {
        // wildcards should be handled correctly
        return new DatastoreServiceDescription(key, key, new String[]
            { regex }, key, serviceKind);
    }

    private void assignRoles(PersonPE person)
    {
        final Set<RoleAssignmentPE> list = new HashSet<RoleAssignmentPE>();
        // Database assignment
        RoleAssignmentPE assignment = new RoleAssignmentPE();
        assignment.setRole(RoleCode.ADMIN);
        assignment.setDatabaseInstance(person.getDatabaseInstance());
        person.addRoleAssignment(assignment);
        list.add(assignment);
        person.setRoleAssignments(list);
    }
}
