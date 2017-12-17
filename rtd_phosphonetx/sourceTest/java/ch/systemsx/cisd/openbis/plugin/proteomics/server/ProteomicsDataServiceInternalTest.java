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

package ch.systemsx.cisd.openbis.plugin.proteomics.server;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind.PROCESSING;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind.QUERIES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.TestJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.server.authorization.TestAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.ManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.IBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.business.ISampleLoader;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.CommonConstants;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.MsInjectionSample;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ProteomicsDataServiceInternal.class)
public class ProteomicsDataServiceInternalTest extends AbstractServerTestCase
{
    private static final String GROUP_CODE = "g";

    private static final String COPY_PROCESSING_KEY = "copy-data-sets";

    private static final String EXPERIMENT_TYPE = "EXPE";

    private IProteomicsDataServiceInternal service;

    private ICommonBusinessObjectFactory commonBoFactory;

    private ExperimentTypePE experimentType;

    private IBusinessObjectFactory boFactory;

    private ISampleLoader sampleLoader;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        commonBoFactory = context.mock(ICommonBusinessObjectFactory.class);
        boFactory = context.mock(IBusinessObjectFactory.class);
        sampleLoader = context.mock(ISampleLoader.class);
        service =
                new ProteomicsDataServiceInternal(sessionManager, daoFactory,
                        propertiesBatchManager, commonBoFactory, boFactory,
                        new ManagedPropertyEvaluatorFactory(null, new TestJythonEvaluatorPool()));
        experimentType = new ExperimentTypePE();
        experimentType.setCode(EXPERIMENT_TYPE);
        PersonPE person = new PersonPE();
        RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setRole(RoleCode.ADMIN);
        SpacePE group = new SpacePE();
        group.setCode("Space-0");
        roleAssignment.setSpace(group);
        person.setRoleAssignments(Collections.singleton(roleAssignment));
        session.setPerson(person);
    }

    @Test
    public void testListRawDataSamples()
    {
        prepareGetSession();
        prepareListRawDataSamples(42L);

        List<MsInjectionSample> samples = service.listRawDataSamples(SESSION_TOKEN);

        assertEquals(42L, samples.get(0).getSample().getId().longValue());
        assertEquals(1, samples.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testProcessDataSets()
    {
        prepareGetSession();
        HashMap<String, String> parameterBindings = new HashMap<String, String>();
        prepareProcessDataSets(session, parameterBindings, "ds1", "ds2");

        service.processDataSets(SESSION_TOKEN, COPY_PROCESSING_KEY, Arrays.asList("ds1", "ds2"));

        context.assertIsSatisfied();
    }

    @Test
    public void testListExperiments()
    {
        prepareGetSession();
        final ExperimentPE e1 = experiment(1);
        final ExperimentPE e2 = experiment(2, "a");
        prepareListExperiments(e1, e2);

        List<Experiment> list = service.listExperiments(SESSION_TOKEN, EXPERIMENT_TYPE);

        assertEquals("/G/P/e1", list.get(0).getIdentifier());
        assertEquals(1, list.get(0).getRegistrationDate().getTime());
        assertEquals(0, list.get(0).getProperties().size());
        assertEquals("/G/P/e2", list.get(1).getIdentifier());
        assertEquals(4, list.get(1).getRegistrationDate().getTime());
        assertEquals("A", list.get(1).getProperties().get(0).getPropertyType().getCode());
        assertEquals("a-value", list.get(1).getProperties().get(0).getValue());
        assertEquals(1, list.get(1).getProperties().size());
        assertEquals(2, list.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testProcessSearchData()
    {
        final Session testSession = createSessionAndPrepareGetSession(GROUP_CODE);
        final ExperimentPE e1 = experiment(1);
        final ExperimentPE e2 = experiment(2, "a");
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).tryGetByTechId(new TechId(e1.getId()));
                    will(returnValue(e1));

                    one(dataSetDAO).listDataSets(e1);
                    ExternalDataPE ds1 = new ExternalDataPE();
                    ds1.setCode("ds1");
                    will(returnValue(Arrays.asList(ds1)));

                    one(experimentDAO).tryGetByTechId(new TechId(e2.getId()));
                    will(returnValue(e2));

                    one(dataSetDAO).listDataSets(e2);
                    ExternalDataPE ds2 = new ExternalDataPE();
                    ds2.setCode("ds2");
                    will(returnValue(Arrays.asList(ds2)));
                }
            });
        prepareProcessDataSets(testSession, new HashMap<String, String>(), "ds1", "ds2");

        service.processProteinResultDataSets(SESSION_TOKEN, COPY_PROCESSING_KEY, EXPERIMENT_TYPE,
                new long[] { e1.getId(), e2.getId() });

        context.assertIsSatisfied();
    }

    @Test
    public void testProcessSearchDataFilteredByValidator()
    {
        Session testSession = createSessionAndPrepareGetSession(GROUP_CODE + 2);
        final ExperimentPE e1 = experiment(1);
        final ExperimentPE e2 = experiment(2, "a");

        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getAuthorizationConfig();
                    will(returnValue(new TestAuthorizationConfig(false, false)));

                    one(experimentDAO).tryGetByTechId(new TechId(e1.getId()));
                    will(returnValue(e1));

                    one(experimentDAO).tryGetByTechId(new TechId(e2.getId()));
                    will(returnValue(e2));
                }
            });
        prepareProcessDataSets(testSession, new HashMap<String, String>());

        service.processProteinResultDataSets(SESSION_TOKEN, COPY_PROCESSING_KEY, EXPERIMENT_TYPE,
                new long[] { e1.getId(), e2.getId() });

        context.assertIsSatisfied();
    }

    @Test
    public void testProcessSearchDataFilteredByIds()
    {
        Session testSession = createSessionAndPrepareGetSession(GROUP_CODE);
        final ExperimentPE e1 = experiment(1);
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).tryGetByTechId(new TechId(e1.getId()));
                    will(returnValue(e1));

                    one(dataSetDAO).listDataSets(e1);
                    ExternalDataPE ds1 = new ExternalDataPE();
                    ds1.setCode("ds1");
                    will(returnValue(Arrays.asList(ds1)));
                }
            });
        prepareProcessDataSets(testSession, new HashMap<String, String>(), "ds1");

        service.processProteinResultDataSets(SESSION_TOKEN, COPY_PROCESSING_KEY, EXPERIMENT_TYPE,
                new long[] { e1.getId() });

        context.assertIsSatisfied();
    }

    private Session createSessionAndPrepareGetSession(String spaceCode)
    {
        final Session testSession =
                new Session(CommonTestUtils.USER_ID, SESSION_TOKEN, PRINCIPAL, "remote-host", 1);
        PersonPE person = new PersonPE();
        RoleAssignmentPE roleAssignmentPE = new RoleAssignmentPE();
        SpacePE group = new SpacePE();
        group.setCode(spaceCode);
        roleAssignmentPE.setRole(RoleCode.ADMIN);
        roleAssignmentPE.setSpace(group);
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(roleAssignmentPE)));
        testSession.setPerson(person);
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(testSession));
                }
            });
        return testSession;
    }

    private void prepareListExperiments(final ExperimentPE... experiments)
    {
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(EXPERIMENT_TYPE);
                    will(returnValue(experimentType));

                    one(experimentDAO).listExperimentsWithProperties(experimentType, null, null);
                    will(returnValue(Arrays.asList(experiments)));

                    one(metaprojectDAO).listMetaprojectAssignmentsForEntities(
                            session.tryGetPerson(), Arrays.asList(experiments),
                            EntityKind.EXPERIMENT);
                }
            });
    }

    private void prepareProcessDataSets(final Session testSession,
            final Map<String, String> parameterBindings, final String... dataSetCodes)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataStoreDAO).listDataStores();
                    DataStorePE s1 =
                            store("s1", service("a", PROCESSING),
                                    service(COPY_PROCESSING_KEY, QUERIES));
                    DataStorePE s2 = store("s2", service(COPY_PROCESSING_KEY, PROCESSING));
                    will(returnValue(Arrays.asList(s1, s2)));

                    one(commonBoFactory).createDataSetTable(testSession);
                    will(returnValue(dataSetTable));

                    one(dataSetTable).processDatasets(COPY_PROCESSING_KEY, "s2",
                            Arrays.asList(dataSetCodes), parameterBindings);
                }

            });
    }

    private void prepareListRawDataSamples(final Long... sampleIDs)
    {
        final List<Sample> samples = new ArrayList<Sample>();
        final LinkedHashSet<Long> experimentIds = new LinkedHashSet<Long>();
        final List<ExperimentPE> bioExperiments = new ArrayList<ExperimentPE>();
        for (Long id : sampleIDs)
        {
            Sample sample = new Sample();
            sample.setId(id);
            sample.setCode("s-" + id);
            sample.setIdentifier("S" + id);
            Experiment experiment = new Experiment();
            experiment.setId(id * 10);
            sample.setExperiment(experiment);
            Sample parent = new Sample();
            parent.setId(id * 100);
            Space space = new Space();
            space.setCode("Space-" + id % 2);
            parent.setSpace(space);
            Experiment parentExperiment = new Experiment();
            parentExperiment.setId(id * 20);
            parent.setExperiment(parentExperiment);
            sample.setGeneratedFrom(parent);
            samples.add(sample);
            if (id % 2 == 0)
            {
                ExperimentPE bioExperiment =
                        createExperiment("e-type", "exp-" + id, space.getCode());
                bioExperiment.setId(parentExperiment.getId());
                bioExperiments.add(bioExperiment);
                experimentIds.add(bioExperiment.getId());
            }
        }
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleLoader(session);
                    will(returnValue(sampleLoader));

                    one(sampleLoader).listSamplesWithParentsByTypeAndSpace(
                            CommonConstants.MS_INJECTION_SAMPLE_TYPE_CODE,
                            CommonConstants.MS_DATA_SPACE);
                    will(returnValue(samples));

                    one(experimentDAO).listExperimentsWithProperties(experimentIds);
                    will(returnValue(bioExperiments));

                    List<Sample> filteredSamples = new ArrayList<Sample>();
                    Map<Sample, List<PhysicalDataSet>> dataSetsBySamples =
                            new HashMap<Sample, List<PhysicalDataSet>>();
                    for (Sample sample : samples)
                    {
                        if ("Space-0".equals(sample.getGeneratedFrom().getSpace().getCode()))
                        {
                            Long id = sample.getId();
                            String dataSetType = "dt-" + id % 2;
                            long ds1Id = id * 1000;
                            PhysicalDataSet ds1 =
                                    new DataSetBuilder(ds1Id).code("ds-" + id)
                                            .registrationDate(new Date(ds1Id)).sample(sample)
                                            .type(dataSetType).getDataSet();

                            long ds2Id = (id + 1) * 1001;
                            PhysicalDataSet ds2 =
                                    new DataSetBuilder(ds2Id).code("ds-" + id + 1)
                                            .registrationDate(new Date(ds2Id)).sample(sample)
                                            .type(dataSetType).getDataSet();

                            long ds2ChildId = ds2.getId() + 1;
                            PhysicalDataSet ds2Child =
                                    new DataSetBuilder(ds2ChildId).code(ds2.getCode() + "-child")
                                            .registrationDate(new Date(ds2ChildId)).sample(sample)
                                            .type(dataSetType).getDataSet();

                            ds2.setChildren(Arrays.<AbstractExternalData> asList(ds2Child));
                            dataSetsBySamples.put(sample, Arrays.asList(ds1, ds2));

                            filteredSamples.add(sample);
                        }
                    }

                    one(commonBoFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));

                    one(datasetLister).listAllDataSetsFor(filteredSamples);
                    will(returnValue(dataSetsBySamples));

                }
            });
    }

    private ExperimentPE experiment(long id, String... properties)
    {
        ExperimentPE experiment = new ExperimentPE();
        experiment.setExperimentType(experimentType);
        experiment.setId(id);
        experiment.setCode("e" + id);
        ProjectPE project = new ProjectPE();
        project.setCode("p");
        SpacePE group =
                CommonTestUtils.createSpace(GROUP_CODE);
        project.setSpace(group);
        experiment.setProject(project);
        experiment.setRegistrationDate(new Date(id * id));
        if (properties.length > 0)
        {
            LinkedHashSet<EntityPropertyPE> props = new LinkedHashSet<EntityPropertyPE>();
            for (String property : properties)
            {
                props.add(CommonTestUtils.createExperimentPropertyPE(property, EXPERIMENT_TYPE,
                        DataTypeCode.VARCHAR, property + "-value"));
            }
            experiment.setProperties(props);
        }
        return experiment;
    }

    private DataStorePE store(String code, DataStoreServicePE... services)
    {
        DataStorePE store = new DataStorePE();
        store.setCode(code);
        store.setServices(new LinkedHashSet<DataStoreServicePE>(Arrays.asList(services)));
        return store;
    }

    private DataStoreServicePE service(String key, DataStoreServiceKind kind)
    {
        DataStoreServicePE dataStoreService = new DataStoreServicePE();
        dataStoreService.setKey(key);
        dataStoreService.setKind(kind);
        return dataStoreService;
    }
}
