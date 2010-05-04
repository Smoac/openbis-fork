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
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.RawDataServiceInternal.SPACE_CODE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.server.RawDataServiceInternal.RAW_DATA_SAMPLE_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;

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
    private ISampleLister sampleLister;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        boFactory = context.mock(ICommonBusinessObjectFactory.class);
        sampleLister = context.mock(ISampleLister.class);
        service = new RawDataServiceInternal(sessionManager, daoFactory, boFactory);
    }
    
    @Test
    public void testListRawDataSamples()
    {
        prepareGetSession();
        prepareListRawDataSamples(42L);
        
        List<Sample> samples = service.listRawDataSamples(SESSION_TOKEN);

        assertEquals(42L, samples.get(0).getId().longValue());
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
                    for (long id : ids)
                    {
                        one(sampleDAO).getByTechId(new TechId(id));
                        SamplePE sample = new SamplePE();
                        sample.setId(id);
                        sample.setCode("s" + id);
                        will(returnValue(sample));
                        
                        one(externalDataDAO).listExternalData(sample);
                        List<ExternalDataPE> dataSets = new ArrayList<ExternalDataPE>();
                        for (int i = 0; i < (int) (id % 3); i++)
                        {
                            ExternalDataPE dataSet = new ExternalDataPE();
                            dataSet.setCode("ds" + id + "." + i);
                            dataSets.add(dataSet);
                        }
                        will(returnValue(dataSets ));
                    }
                    
                    one(dataStoreDAO).listDataStores();
                    DataStorePE s1 = store("s1", service("a", PROCESSING), service(COPY_PROCESSING_KEY, QUERIES));
                    DataStorePE s2 = store("s2", service(COPY_PROCESSING_KEY, PROCESSING));
                    will(returnValue(Arrays.asList(s1, s2)));
                    
                    one(boFactory).createExternalDataTable(SESSION);
                    will(returnValue(externalDataTable));
                    
                    List<String> dataSetCodes = Arrays.asList("ds2.0", "ds2.1");
                    one(externalDataTable).processDatasets(COPY_PROCESSING_KEY, "s2", dataSetCodes, null);
                }
                
            });
        
        service.processRawData(SESSION_TOKEN, COPY_PROCESSING_KEY, ids);
        
        context.assertIsSatisfied();
    }

    private void prepareListRawDataSamples(final Long... sampleIDs)
    {
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
                    List<Sample> samples = new ArrayList<Sample>();
                    for (Long id : sampleIDs)
                    {
                        Sample sample = new Sample();
                        sample.setId(id);
                        Sample parent = new Sample();
                        sample.setGeneratedFrom(parent);
                        samples.add(sample);
                    }
                    will(returnValue(samples));
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
