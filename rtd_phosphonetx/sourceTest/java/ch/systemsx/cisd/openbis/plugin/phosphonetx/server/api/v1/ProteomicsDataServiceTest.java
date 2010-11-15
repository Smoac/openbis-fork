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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.api.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.api.v1.ProteomicsDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IProteomicsDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.PropertyKey;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteomicsDataServiceTest extends AbstractServerTestCase
{
    private static final String RAW_DATA = "RAW_DATA";
    private static final String MZXML_DATA = "MZXML_DATA";
    
    private IProteomicsDataServiceInternal internalService;
    private IProteomicsDataService service;
    private SessionContextDTO session2;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        internalService = context.mock(IProteomicsDataServiceInternal.class);
        service = new ProteomicsDataService(sessionManager, daoFactory, internalService);
        session2 = new SessionContextDTO();
        session2.setSessionToken(SESSION_TOKEN + "2");
    }
    
    @Test
    public void testListDataStoreServerProcessingPluginInfos()
    {
        prepareGetSession();
        DataStoreServicePE s1 = new DataStoreServicePE();
        s1.setKind(DataStoreServiceKind.QUERIES);
        DataStoreServicePE s2 = new DataStoreServicePE();
        s2.setKind(DataStoreServiceKind.PROCESSING);
        s2.setKey("my-key");
        s2.setLabel("my label");
        DataSetTypePE dataSetType = new DataSetTypePE();
        dataSetType.setCode("my type");
        s2.setDatasetTypes(Collections.singleton(dataSetType));
        final DataStorePE store = new DataStorePE();
        store.setServices(new HashSet<DataStoreServicePE>(Arrays.asList(s1, s2)));
        context.checking(new Expectations()
            {
                {
                    one(dataStoreDAO).listDataStores();
                    will(returnValue(Arrays.asList(store)));
                }
            });

        List<DataStoreServerProcessingPluginInfo> infos =
                service.listDataStoreServerProcessingPluginInfos(SESSION_TOKEN);
        
        assertEquals(s2.getKey(), infos.get(0).getKey());
        assertEquals(s2.getLabel(), infos.get(0).getLabel());
        assertEquals(Arrays.asList(dataSetType.getCode()), infos.get(0).getDatasetTypeCodes());
        assertEquals(1, infos.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListRawDataSamplesForUnknownUser()
    {
        prepareGetSession();
        prepareLoginLogout(null);
        
        try
        {
            service.listRawDataSamples(SESSION_TOKEN, "abc");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown user ID: abc", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListRawDataSamples()
    {
        prepareGetSession();
        prepareLoginLogout(session2);
        final Sample sample = new Sample();
        sample.setId(4711L);
        sample.setCode("ms-inj-42");
        sample.setRegistrationDate(new Date(4711));
        IEntityProperty p1 = property("integer", DataTypeCode.INTEGER, "123456");
        IEntityProperty p2 = property("real", DataTypeCode.REAL, "1.25");
        IEntityProperty p3 = property("string", DataTypeCode.VARCHAR, "hello");
        sample.setProperties(Arrays.asList(p1, p2, p3));
        Sample parent = new Sample();
        parent.setId(4710L);
        parent.setIdentifier("parent");
        IEntityProperty p4 = property("boolean", DataTypeCode.BOOLEAN, "true");
        IEntityProperty p5 = property("link", DataTypeCode.HYPERLINK, "link");
        parent.setProperties(Arrays.asList(p4, p5));
        Experiment experiment = new Experiment();
        experiment.setId(4747L);
        experiment.setCode("exp1");
        Project project = new Project();
        project.setCode("project1");
        Space space = new Space();
        space.setCode("space1");
        project.setSpace(space);
        experiment.setProject(project);
        experiment.setRegistrationDate(new Date(1234567));
        IEntityProperty p6 = property("ex", DataTypeCode.VARCHAR, "exp");
        experiment.setProperties(Arrays.<IEntityProperty>asList(p6));
        parent.setExperiment(experiment);
        sample .setGeneratedFrom(parent);
        final ExternalData ds1 = createDataSet(RAW_DATA, 10);
        final ExternalData ds2 = createDataSet(MZXML_DATA, 20);
        ExternalData ds3 = createDataSet(MZXML_DATA, 15);
        ExternalData ds4 = createDataSet(RAW_DATA, 30);
        ds2.setChildren(Arrays.asList(ds3));
        ds3.setChildren(Arrays.asList(ds4));
        context.checking(new Expectations()
            {
                {
                    one(internalService).listRawDataSamples(session2.getSessionToken());
                    MsInjectionSample msInjectionSample = new MsInjectionSample(sample, Arrays.asList(ds1, ds2));
                    will(returnValue(Arrays.asList(msInjectionSample)));
                }
            });

        List<MsInjectionDataInfo> infos = service.listRawDataSamples(SESSION_TOKEN, "abc");

        MsInjectionDataInfo info = infos.get(0);
        assertEquals(sample.getId().longValue(), info.getMsInjectionSampleID());
        assertEquals(sample.getCode(), info.getMsInjectionSampleCode());
        assertEquals(sample.getRegistrationDate(), info.getMsInjectionSampleRegistrationDate());
        checkProperties(info.getMsInjectionSampleProperties(), p1, p2, p3);
        assertEquals(parent.getId().longValue(), info.getBiologicalSampleID());
        assertEquals(parent.getIdentifier(), info.getBiologicalSampleIdentifier());
        checkProperties(info.getBiologicalSampleProperties(), p4, p5);
        assertEquals("4747:space1/project1/exp1 date:1234567 {ex[EX]=exp}",
                renderExperiment(info.getBiologicalExperiment()));
        checkProperties(info.getBiologicalExperiment().getProperties(), p6);
        List<DataSet> dataSets = new ArrayList<DataSet>(info.getDataSets());
        Collections.sort(dataSets, new Comparator<DataSet>()
            {
                public int compare(DataSet d1, DataSet d2)
                {
                    return d1.getCode().compareTo(d2.getCode());
                }
            });
        assertEquals("20:MZXML_DATA-20 [MZXML_DATA date:20] {n[N]=2.5}, children:15",
                renderDataSet(dataSets.get(0)));
        assertEquals("10:RAW_DATA-10 [RAW_DATA date:10] {n[N]=2.5}", renderDataSet(dataSets.get(1)));
        assertEquals("15:MZXML_DATA-15 [MZXML_DATA date:15] {n[N]=2.5}, parent:20 , children:30",
                renderDataSet(dataSets.get(0).getChildren().iterator().next()));
        assertEquals("30:RAW_DATA-30 [RAW_DATA date:30] {n[N]=2.5}, parent:15",
                renderDataSet(dataSets.get(0).getChildren().iterator().next().getChildren()
                        .iterator().next()));
        assertEquals(2, dataSets.size());
        Map<String, Date> dates = info.getLatestDataSetRegistrationDates();
        assertEquals(30, dates.get(RAW_DATA).getTime());
        assertEquals(20, dates.get(MZXML_DATA).getTime());
        assertEquals(2, dates.size());
        assertEquals(1, infos.size());
        context.assertIsSatisfied();
    }
    
    private String renderExperiment(ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.Experiment experiment)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(experiment.getId()).append(":").append(experiment.getSpaceCode());
        builder.append('/').append(experiment.getProjectCode());
        builder.append('/').append(experiment.getCode());
        builder.append(" date:").append(experiment.getRegistrationDate().getTime());
        builder.append(' ').append(experiment.getProperties());
        return builder.toString();
    }
    
    private String renderDataSet(DataSet dataSet)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(dataSet.getId()).append(":").append(dataSet.getCode());
        builder.append(" [").append( dataSet.getType()).append(" date:");
        builder.append(dataSet.getRegistrationDate().getTime()).append("] ");
        builder.append(dataSet.getProperties());
        Set<DataSet> parents = dataSet.getParents();
        if (parents != null && parents.isEmpty() == false)
        {
            builder.append(", parent:");
            for (DataSet parent : parents)
            {
                builder.append(parent.getId()).append(' ');
            }
        }
        Set<DataSet> children = dataSet.getChildren();
        if (children != null && children.isEmpty() == false)
        {
            builder.append(", children:");
            for (DataSet child : children)
            {
                builder.append(child.getId()).append(' ');
            }
        }
        return builder.toString().trim();
    }

    @Test
    public void testProcessDataSetsForUnknownUser()
    {
        prepareGetSession();
        prepareLoginLogout(null);

        try
        {
            service.processDataSets(SESSION_TOKEN, "abc", null, Arrays.asList("ds1"));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown user ID: abc", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testProcessDataSets()
    {
        prepareGetSession();
        prepareLoginLogout(session2);
        context.checking(new Expectations()
            {
                {
                    one(internalService).processDataSets(session2.getSessionToken(), "dsp1", Arrays.asList("ds1"));
                }
            });

        service.processDataSets(SESSION_TOKEN, "abc", "dsp1", Arrays.asList("ds1"));

        context.assertIsSatisfied();
    }
    
    @Test
    public void testListSearchExperiments()
    {
        prepareGetSession();
        prepareLoginLogout(session2);
        final Experiment e = new Experiment();
        e.setId(42L);
        e.setCode("E42");
        Project project = new Project();
        project.setCode("p");
        Space space = new Space();
        space.setCode("s");
        project.setSpace(space);
        e.setProject(project);
        e.setRegistrationDate(new Date(4711));
        EntityProperty p1 = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("ANSWER");
        propertyType.setLabel("answer");
        propertyType.setDataType(new DataType(DataTypeCode.INTEGER));
        p1.setPropertyType(propertyType);
        p1.setValue("42");
        e.setProperties(Arrays.<IEntityProperty>asList(p1));
        context.checking(new Expectations()
            {
                {
                    one(internalService).listSearchExperiments(session2.getSessionToken());
                    will(returnValue(Arrays.asList(e)));
                }
            });
        
        List<ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.Experiment> experiments =
                service.listSearchExperiments(SESSION_TOKEN, "abc");
        
        assertEquals(e.getId().longValue(), experiments.get(0).getId());
        assertEquals(e.getCode(), experiments.get(0).getCode());
        assertEquals(e.getProject().getCode(), experiments.get(0).getProjectCode());
        assertEquals(e.getProject().getSpace().getCode(), experiments.get(0).getSpaceCode());
        assertEquals(e.getRegistrationDate(), experiments.get(0).getRegistrationDate());
        Map<PropertyKey, Serializable> properties = experiments.get(0).getProperties();
        Set<Entry<PropertyKey, Serializable>> entrySet = properties.entrySet();
        List<Entry<PropertyKey, Serializable>> list = new ArrayList<Entry<PropertyKey, Serializable>>(entrySet);
        assertEquals(e.getProperties().get(0).getPropertyType().getCode(), list.get(0).getKey().getId());
        assertEquals(e.getProperties().get(0).getPropertyType().getLabel(), list.get(0).getKey().getLabel());
        Serializable value = list.get(0).getValue();
        assertEquals(Long.class, value.getClass());
        assertEquals(e.getProperties().get(0).getValue(), value.toString());
        assertEquals(1, properties.size());
        assertEquals(1, experiments.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testProcessSearchData()
    {
        prepareGetSession();
        prepareLoginLogout(session2);
        final long[] ids = new long[] {42};
        context.checking(new Expectations()
            {
                {
                    one(internalService).processSearchData(session2.getSessionToken(), "dsp1", ids);
                }
            });

        service.processSearchData(SESSION_TOKEN, "abc", "dsp1", ids);

        context.assertIsSatisfied();
    }
    
    private void prepareLoginLogout(final SessionContextDTO session)
    {
        context.checking(new Expectations()
            {
                {
                    one(internalService).tryToAuthenticate("abc", "dummy-password");
                    will(returnValue(session));
                    
                    if (session != null)
                    {
                        one(internalService).logout(session.getSessionToken());
                    }
                }
            });
    }
    
    private void checkProperties(Map<PropertyKey, Serializable> properties, IEntityProperty... expectedProperties)
    {
        for (IEntityProperty expectedProperty : expectedProperties)
        {
            PropertyType propertyType = expectedProperty.getPropertyType();
            PropertyKey key = new PropertyKey(propertyType.getCode(), propertyType.getLabel());
            Serializable v = properties.get(key);
            assertNotNull("Missing property: " + key, v);
            assertEquals("Property " + propertyType, expectedProperty.tryGetAsString(), String.valueOf(v));
        }
        assertEquals(expectedProperties.length, properties.size());
    }

    private IEntityProperty property(String code, DataTypeCode dataTypeCode, String value)
    {
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(code.toUpperCase());
        propertyType.setLabel(code);
        propertyType.setDataType(new DataType(dataTypeCode));
        property.setPropertyType(propertyType);
        property.setValue(value);
        return property;
    }

    private ExternalData createDataSet(String type, long date)
    {
        ExternalData dataSet = new ExternalData();
        dataSet.setId(date);
        dataSet.setCode(type + "-" + date);
        dataSet.setDataSetType(new DataSetType(type));
        dataSet.setRegistrationDate(new Date(date));
        dataSet.setDataSetProperties(Arrays.asList(property("n", DataTypeCode.REAL, "2.5")));
        return dataSet;
    }
    
}
