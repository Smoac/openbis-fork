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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataSetManagerTest extends AssertJUnit
{
    private static final long EXP1_ID = 11;
    private static final long SAMPLE1A_ID = 111;
    private static final long SAMPLE1B_ID = 112;
    private static final long EXP2_ID = 22;
    private static final long SAMPLE2A_ID = 221;
    private static final Sample SAMPLE1A = sample(EXP1_ID, SAMPLE1A_ID);
    private static final Sample SAMPLE1B = sample(EXP1_ID, SAMPLE1B_ID);
    private static final Sample SAMPLE2A = sample(EXP2_ID, SAMPLE2A_ID);
    
    private static Sample sample(long experimentID, long sampleID)
    {
        Sample sample = new Sample();
        sample.setId(sampleID);
        Experiment experiment = new Experiment();
        experiment.setId(experimentID);
        sample.setExperiment(experiment);
        return sample;
    }
    
    private Mockery context;
    private IDatasetLister dataSetLister;
    private DataSetManager manager;
    
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dataSetLister = context.mock(IDatasetLister.class);
        manager = new DataSetManager();
        manager.addSample(SAMPLE1A);
        manager.addSample(SAMPLE1B);
        manager.addSample(SAMPLE2A);
    }
    
    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testNoDataSets()
    {
        prepareListByExperimentIds(Collections.<ExternalData>emptyList());
        prepareListParentIds(Collections.<Integer, Integer>emptyMap());
        
        manager.gatherDataSets(dataSetLister);
        
        List<MsInjectionSample> samples = manager.getSamples();
        for (MsInjectionSample msInjectionSample : samples)
        {
            assertEquals(0, msInjectionSample.getLatestDataSets().size());
        }
        assertEquals(3, samples.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testHappyCase()
    {
        Map<Integer, Integer> parentsMap = new HashMap<Integer, Integer>();
        List<ExternalData> dataSets = new ArrayList<ExternalData>();
        dataSets.add(dataSet(1, "A", SAMPLE1A));
        dataSets.add(dataSet(2, "A", SAMPLE1A));
        dataSets.add(dataSet(3, "A", SAMPLE1B));
        dataSets.add(dataSet(4, "A", SAMPLE2A));
        dataSets.add(dataSet(5, "B", null));
        parentsMap.put(5, 1);
        dataSets.add(dataSet(6, "B", null));
        parentsMap.put(6, 3);
        dataSets.add(dataSet(7, "B", null));
        parentsMap.put(7, 3);
        dataSets.add(dataSet(8, "B", null));
        parentsMap.put(8, 4);
        dataSets.add(dataSet(9, "C", null));
        parentsMap.put(9, 6);
        dataSets.add(dataSet(10, "C", null));
        parentsMap.put(10, 8);
        prepareListByExperimentIds(dataSets);
        prepareListParentIds(parentsMap);
        
        manager.gatherDataSets(dataSetLister);
        
        List<MsInjectionSample> samples = manager.getSamples();
        Map<Long, MsInjectionSample> map = new HashMap<Long, MsInjectionSample>();
        for (MsInjectionSample msInjectionSample : samples)
        {
            map.put(msInjectionSample.getSample().getId(), msInjectionSample);
        }
        assertEquals(3, samples.size());
        assertLatestDataSets(map.get(SAMPLE1A_ID), "A:2", "B:5");
        assertLatestDataSets(map.get(SAMPLE1B_ID), "A:3", "B:7", "C:9");
        assertLatestDataSets(map.get(SAMPLE2A_ID), "A:4", "B:8", "C:10");
        context.assertIsSatisfied();
    }
    
    
    @Test
    public void testWrongParent()
    {
        Map<Integer, Integer> parentsMap = new HashMap<Integer, Integer>();
        List<ExternalData> dataSets = new ArrayList<ExternalData>();
        dataSets.add(dataSet(1, "A", SAMPLE1A));
        dataSets.add(dataSet(2, "B", null));
        parentsMap.put(2, 42);
        prepareListByExperimentIds(dataSets);
        prepareListParentIds(parentsMap);

        try
        {
            manager.gatherDataSets(dataSetLister);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Following data sets have wrong parents: ds-2", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    private void assertLatestDataSets(MsInjectionSample sample, String... typesAndIDs)
    {
        Map<String, ExternalData> latestDataSets = sample.getLatestDataSets();
        for (String typeAndID : typesAndIDs)
        {
            String[] array = typeAndID.split(":");
            assertEquals(array[1], latestDataSets.get(array[0]).getId().toString());
        }
        assertEquals(typesAndIDs.length, latestDataSets.size());
    }
    
    private void prepareListByExperimentIds(final List<ExternalData> dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetLister).listByExperimentTechIds(
                            new HashSet<TechId>(Arrays.asList(new TechId(EXP1_ID), new TechId(
                                    EXP2_ID))));
                    will(returnValue(dataSets));
                }
            });
    }
    
    private void prepareListParentIds(final Map<Integer, Integer> parentIds)
    {
        context.checking(new Expectations()
            {
                {
                    Set<Entry<Integer, Integer>> entrySet = parentIds.entrySet();
                    Map<Long, Set<Long>> result = new HashMap<Long, Set<Long>>();
                    Set<Long> keys = new HashSet<Long>();
                    for (Entry<Integer, Integer> entry : entrySet)
                    {
                        long key = entry.getKey().longValue();
                        keys.add(key);
                        result.put(key, Collections.singleton(entry.getValue().longValue()));
                    }
                    one(dataSetLister).listParentIds(keys);
                    will(returnValue(result));
                }
            });
    }

    private ExternalData dataSet(long id, String type, Sample sampleOrNull)
    {
        ExternalData dataSet = new ExternalData();
        dataSet.setId(id);
        dataSet.setCode("ds-" + id);
        dataSet.setRegistrationDate(new Date(100 * id));
        dataSet.setDataSetType(new DataSetType(type));
        dataSet.setSample(sampleOrNull);
        return dataSet;
    }
}
