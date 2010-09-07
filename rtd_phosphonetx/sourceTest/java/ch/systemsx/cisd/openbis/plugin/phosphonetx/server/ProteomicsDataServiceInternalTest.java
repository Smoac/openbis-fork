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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind.PROCESSING;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind.QUERIES;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.ProteomicsDataServiceInternal.RAW_DATA_SAMPLE_TYPE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.ProteomicsDataServiceInternal.SEARCH_EXPERIMENT_TYPE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.ProteomicsDataServiceInternal.SPACE_CODE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = ProteomicsDataServiceInternal.class)
public class ProteomicsDataServiceInternalTest extends AbstractServerTestCase
{
    private static final String GROUP_CODE = "g";

    private static final String COPY_PROCESSING_KEY = "copy-data-sets";

    private IProteomicsDataServiceInternal service;

    private ICommonBusinessObjectFactory boFactory;

    private ExperimentTypePE experimentType;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        boFactory = context.mock(ICommonBusinessObjectFactory.class);
        service = new ProteomicsDataServiceInternal(sessionManager, daoFactory, boFactory);
        experimentType = new ExperimentTypePE();
        experimentType.setCode(SEARCH_EXPERIMENT_TYPE);
        experimentType.setDatabaseInstance(CommonTestUtils.createHomeDatabaseInstance());
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
    public void testCopyRawData()
    {
        prepareGetSession();
        prepareListRawDataSamples(1L, 2L, 3L, 42L);
        final long[] ids = new long[]
            { 2 };
        HashMap<String, String> parameterBindings = new HashMap<String, String>();
        parameterBindings.put("ds-2", "s-2");
        prepareProcessDataSets(SESSION, parameterBindings, "ds-2");

        service.processRawData(SESSION_TOKEN, COPY_PROCESSING_KEY, ids, "dt-0");

        context.assertIsSatisfied();
    }

    @Test
    public void testListSearchExperiments()
    {
        prepareGetSession();
        final ExperimentPE e1 = experiment(1);
        final ExperimentPE e2 = experiment(2, "a");
        prepareListSearchExperiments(e1, e2);
        
        List<Experiment> list = service.listSearchExperiments(SESSION_TOKEN);
        
        assertEquals("HOME_DATABASE:/G/P/e1", list.get(0).getIdentifier());
        assertEquals(1, list.get(0).getRegistrationDate().getTime());
        assertEquals(0, list.get(0).getProperties().size());
        assertEquals("HOME_DATABASE:/G/P/e2", list.get(1).getIdentifier());
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
        final Session session = createSessionAndPrepareGetSession(GROUP_CODE);
        final ExperimentPE e1 = experiment(1);
        final ExperimentPE e2 = experiment(2, "a");
        prepareListSearchExperiments(e1, e2);
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).tryGetByTechId(new TechId(e1.getId()));
                    will(returnValue(e1));

                    one(externalDataDAO).listExternalData(e1);
                    ExternalDataPE ds1 = new ExternalDataPE();
                    ds1.setCode("ds1");
                    will(returnValue(Arrays.asList(ds1)));

                    one(experimentDAO).tryGetByTechId(new TechId(e2.getId()));
                    will(returnValue(e2));

                    one(externalDataDAO).listExternalData(e2);
                    ExternalDataPE ds2 = new ExternalDataPE();
                    ds2.setCode("ds2");
                    will(returnValue(Arrays.asList(ds2)));
                }
            });
        prepareProcessDataSets(session, new HashMap<String, String>(), "ds1", "ds2");
        
        service.processSearchData(SESSION_TOKEN, COPY_PROCESSING_KEY, new long[] {e1.getId(), e2.getId()});
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testProcessSearchDataFilteredByValidator()
    {
        Session session = createSessionAndPrepareGetSession(GROUP_CODE + 2);
        final ExperimentPE e1 = experiment(1);
        final ExperimentPE e2 = experiment(2, "a");
        prepareListSearchExperiments(e1, e2);
        prepareProcessDataSets(session, new HashMap<String, String>());
        
        service.processSearchData(SESSION_TOKEN, COPY_PROCESSING_KEY, new long[] {e1.getId(), e2.getId()});
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testProcessSearchDataFilteredByIds()
    {
        Session session = createSessionAndPrepareGetSession(GROUP_CODE);
        final ExperimentPE e1 = experiment(1);
        final ExperimentPE e2 = experiment(2, "a");
        prepareListSearchExperiments(e1, e2);
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).tryGetByTechId(new TechId(e1.getId()));
                    will(returnValue(e1));

                    one(externalDataDAO).listExternalData(e1);
                    ExternalDataPE ds1 = new ExternalDataPE();
                    ds1.setCode("ds1");
                    will(returnValue(Arrays.asList(ds1)));
                }
            });
        prepareProcessDataSets(session, new HashMap<String, String>(), "ds1");

        service.processSearchData(SESSION_TOKEN, COPY_PROCESSING_KEY, new long[] {e1.getId()});
        
        context.assertIsSatisfied();
    }
    

    private Session createSessionAndPrepareGetSession(String spaceCode)
    {
        final Session session =
                new Session(CommonTestUtils.USER_ID, SESSION_TOKEN, PRINCIPAL, "remote-host", 1);
        PersonPE person = new PersonPE();
        RoleAssignmentPE roleAssignmentPE = new RoleAssignmentPE();
        GroupPE group = new GroupPE();
        group.setCode(spaceCode);
        group.setDatabaseInstance(CommonTestUtils.createHomeDatabaseInstance());
        roleAssignmentPE.setGroup(group);
        person.setRoleAssignments(new HashSet<RoleAssignmentPE>(Arrays.asList(roleAssignmentPE)));
        session.setPerson(person);
        context.checking(new Expectations()
            {
                {
                    allowing(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));
                }
            });
        return session;
    }
    
    private void prepareListSearchExperiments(final ExperimentPE... experiments)
    {
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(returnValue(entityTypeDAO));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(SEARCH_EXPERIMENT_TYPE);
                    will(returnValue(experimentType));

                    one(experimentDAO).listExperimentsWithProperties(experimentType, null);
                    will(returnValue(Arrays.asList(experiments)));
                }
            });
    }
    
    private void prepareProcessDataSets(final Session session,
            final Map<String, String> parameterBindings, final String... dataSetCodes)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataStoreDAO).listDataStores();
                    DataStorePE s1 =
                            store("s1", service("a", PROCESSING), service(COPY_PROCESSING_KEY,
                                    QUERIES));
                    DataStorePE s2 = store("s2", service(COPY_PROCESSING_KEY, PROCESSING));
                    will(returnValue(Arrays.asList(s1, s2)));

                    one(boFactory).createExternalDataTable(session);
                    will(returnValue(externalDataTable));

                    one(externalDataTable).processDatasets(COPY_PROCESSING_KEY, "s2",
                            Arrays.asList(dataSetCodes), parameterBindings);
                }

            });
    }
    
    private void prepareListRawDataSamples(final Long... sampleIDs)
    {
        final List<Sample> samples = new ArrayList<Sample>();
        final List<ExternalData> dataSets = new ArrayList<ExternalData>();
        final LinkedHashSet<TechId> experimentIds = new LinkedHashSet<TechId>();
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
            sample.setGeneratedFrom(parent);
            samples.add(sample);
            ExternalData dataSet = new ExternalData();
            dataSet.setId(id * 1000);
            dataSet.setCode("ds-" + id);
            dataSet.setDataSetType(new DataSetType("dt-" + id % 2));
            dataSet.setSample(sample);
            dataSets.add(dataSet);
            experimentIds.add(new TechId(id * 10));
        }
        context.checking(new Expectations()
            {
                {
                    one(boFactory).createSampleLister(SESSION);
                    will(returnValue(sampleLister));

                    one(sampleTypeDAO).tryFindSampleTypeByCode(RAW_DATA_SAMPLE_TYPE);
                    final SampleTypePE sampleType = new SampleTypePE();
                    sampleType.setCode(RAW_DATA_SAMPLE_TYPE);
                    sampleType.setId(20100104l);
                    sampleType.setListable(Boolean.TRUE);
                    sampleType.setAutoGeneratedCode(Boolean.FALSE);
                    sampleType.setSubcodeUnique(Boolean.FALSE);
                    sampleType.setGeneratedFromHierarchyDepth(0);
                    sampleType.setContainerHierarchyDepth(0);
                    sampleType.setSubcodeUnique(false);
                    will(returnValue(sampleType));

                    one(sampleLister).list(with(new BaseMatcher<ListOrSearchSampleCriteria>()
                        {
                            public boolean matches(Object item)
                            {
                                if (item instanceof ListOrSearchSampleCriteria)
                                {
                                    ListOrSearchSampleCriteria criteria =
                                            (ListOrSearchSampleCriteria) item;
                                    assertEquals(SPACE_CODE, criteria.getSpaceCode());
                                    assertEquals(true, criteria.isIncludeSpace());
                                    SampleType type = criteria.getSampleType();
                                    assertEquals(RAW_DATA_SAMPLE_TYPE, type.getCode());
                                    assertEquals(sampleType.getId(), type.getId());
                                    return true;
                                }
                                return false;
                            }

                            public void describeTo(Description description)
                            {
                                description.appendValue(sampleType);
                            }
                        }));
                    will(returnValue(samples));

                    one(boFactory).createDatasetLister(SESSION);
                    will(returnValue(datasetLister));

                    one(datasetLister).listByExperimentTechIds(experimentIds);
                    will(returnValue(dataSets));

                    one(datasetLister).listParentIds(Collections.<Long> emptySet());
                    will(returnValue(Collections.<Long, Set<Long>> emptyMap()));
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
        GroupPE group = CommonTestUtils.createGroup(GROUP_CODE, CommonTestUtils.createHomeDatabaseInstance());
        project.setGroup(group);
        experiment.setProject(project);
        experiment.setRegistrationDate(new Date(id * id));
        if (properties.length > 0)
        {
            LinkedHashSet<EntityPropertyPE> props = new LinkedHashSet<EntityPropertyPE>();
            for (String property : properties)
            {
                props.add(CommonTestUtils.createExperimentPropertyPE(property,
                        ProteomicsDataServiceInternal.SEARCH_EXPERIMENT_TYPE, DataTypeCode.VARCHAR,
                        property + "-value"));
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
