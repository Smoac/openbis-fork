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
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.RawDataServiceInternal.RAW_DATA_SAMPLE_TYPE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.RawDataServiceInternal.SPACE_CODE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=RawDataServiceInternal.class)
public class RawDataServiceInternalTest extends AbstractServerTestCase
{
    private static final String COPY_PROCESSING_KEY = "copy-data-sets";
    private IRawDataServiceInternal service;
    private ICommonBusinessObjectFactory boFactory;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        boFactory = context.mock(ICommonBusinessObjectFactory.class);
        service = new RawDataServiceInternal(sessionManager, daoFactory, boFactory);
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
        final long[] ids = new long[] {2, 3};
        context.checking(new Expectations()
            {
                {
                    one(dataStoreDAO).listDataStores();
                    DataStorePE s1 =
                            store("s1", service("a", PROCESSING), service(COPY_PROCESSING_KEY,
                                    QUERIES));
                    DataStorePE s2 = store("s2", service(COPY_PROCESSING_KEY, PROCESSING));
                    will(returnValue(Arrays.asList(s1, s2)));

                    one(boFactory).createExternalDataTable(SESSION);
                    will(returnValue(externalDataTable));

                    List<String> dataSetCodes = Arrays.asList("ds-2", "ds-42");
                    HashMap<String, String> parameterBindings = new HashMap<String, String>();
                    parameterBindings.put("ds-2", "s-2");
                    parameterBindings.put("ds-42", "s-42");
                    one(externalDataTable).processDatasets(COPY_PROCESSING_KEY, "s2", dataSetCodes,
                            parameterBindings);
                }
                
            });
        
        service.processRawData(SESSION_TOKEN, COPY_PROCESSING_KEY, ids, "dt-0");
        
        context.assertIsSatisfied();
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
                    sampleType.setGeneratedFromHierarchyDepth(0);
                    sampleType.setContainerHierarchyDepth(0);
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
                    
                    one(boFactory).createDatasetLister(SESSION, "");
                    will(returnValue(datasetLister));
                    
                    one(datasetLister).listByExperimentTechIds(experimentIds);
                    will(returnValue(dataSets));
                    
                    one(datasetLister).listParentIds(Collections.<Long>emptySet());
                    will(returnValue(Collections.<Long, Set<Long>>emptyMap()));
                }
            });
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
