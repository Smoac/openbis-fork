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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

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

    private static final String URL = "http://" + SESSION.getRemoteHost() + ":" + PORT;

    private ICommonBusinessObjectFactory boFactory;

    private IDataStoreServiceFactory dssfactory;

    private IDataStoreService dataStoreService;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        boFactory = context.mock(ICommonBusinessObjectFactory.class);
        dssfactory = context.mock(IDataStoreServiceFactory.class);
        dataStoreService = context.mock(IDataStoreService.class);
    }

    @Test
    public void testRegisterDataStoreServer()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
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
                    "Data Store Server version is " + (VERSION + 1) + " instead of " + VERSION,
                    e.getMessage());
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

        Sample sample = createService().tryGetSampleWithExperiment(SESSION_TOKEN, sampleIdentifier);

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
    public void testTryToGetSampleIdentifier()
    {
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).tryToFindByPermID("abc");
                    SamplePE sample = new SamplePE();
                    sample.setCode("s42");
                    will(returnValue(sample));
                }
            });

        SampleIdentifier identifier =
                createService().tryToGetSampleIdentifier(SESSION_TOKEN, "abc");

        assertEquals("s42", identifier.toString());
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
                    sampleTypePE.setSubcodeUnique(Boolean.FALSE);
                    will(returnValue(sampleTypePE));
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
                new SampleIdentifier(new DatabaseInstanceIdentifier("db"), "s1");
        SamplePE sample = createSample("s1");
        SamplePE top = createSample("s2");
        SamplePE parent = createSample("s3");
        parent.addParentRelationship(new SampleRelationshipPE(top, parent,
                createParentChildRelation()));
        sample.addParentRelationship(new SampleRelationshipPE(parent, sample,
                createParentChildRelation()));

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
        SamplePE sample = createSample("s1");
        SamplePE top = createSample("s2");
        SamplePE parent = createSample("s3");
        parent.addParentRelationship(new SampleRelationshipPE(top, parent,
                createParentChildRelation()));
        sample.addParentRelationship(new SampleRelationshipPE(parent, sample,
                createParentChildRelation()));
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
                    one(boFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(sample);
                    one(sampleBO).save();
                    one(evaluator).scheduleUpdate(
                            with(any(DynamicPropertyEvaluationOperation.class)));
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
                    one(boFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(sample);
                    one(sampleBO).save();
                    one(evaluator).scheduleUpdate(
                            with(any(DynamicPropertyEvaluationOperation.class)));
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
                    one(boFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(sample);
                    one(sampleBO).save();
                    one(evaluator).scheduleUpdate(
                            with(any(DynamicPropertyEvaluationOperation.class)));
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
                    one(boFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).define(sample);
                    one(sampleBO).save();
                    one(evaluator).scheduleUpdate(
                            with(any(DynamicPropertyEvaluationOperation.class)));
                    exactly(2).of(sampleBO).getSample();
                    will(returnValue(samplePE));

                    one(personDAO).tryFindPersonByUserId(CommonTestUtils.USER_ID);
                    will(returnValue(new PersonPE()));

                    one(boFactory).createExternalDataBO(SESSION);
                    will(returnValue(externalDataBO));

                    one(externalDataBO).define(externalData, samplePE, SourceType.MEASUREMENT);
                    one(externalDataBO).save();
                    one(externalDataBO).getExternalData();
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

    @Test()
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
                    one(boFactory).createSampleBO(SESSION);
                    will(returnValue(sampleBO));

                    one(sampleBO).update(sample);
                    one(sampleBO).save();
                    one(evaluator).scheduleUpdate(
                            with(any(DynamicPropertyEvaluationOperation.class)));
                    exactly(2).of(sampleBO).getSample();
                    will(returnValue(samplePE));

                    one(boFactory).createExternalDataBO(SESSION);
                    will(returnValue(externalDataBO));

                    one(externalDataBO).define(externalData, samplePE, SourceType.MEASUREMENT);
                    one(externalDataBO).save();
                    one(externalDataBO).getExternalData();
                    ExternalDataPE externalDataPE = new ExternalDataPE();
                    externalDataPE.setCode(externalData.getCode());
                    will(returnValue(externalDataPE));
                }
            });

        Sample result =
                createService().updateSampleAndRegisterDataSet(SESSION_TOKEN, sample, externalData);
        assertNotNull(result);
        assertEquals(sample.getSampleIdentifier().toString(), result.getIdentifier());
        assertEquals(experiment.getIdentifier(), result.getExperiment().getIdentifier());

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
                    sample.setSpace(createGroup("G1"));
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
        sampleType.setAutoGeneratedCode(false);
        sampleType.setSubcodeUnique(false);
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
        return new ETLService(authenticationService, sessionManager, daoFactory, boFactory,
                dssfactory);
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
