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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.api.v1.RawDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IRawDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.PropertyKey;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class RawDataServiceTest extends AbstractServerTestCase
{
    private static final String RAW_DATA = "RAW_DATA";
    private static final String MZXML_DATA = "MZXML_DATA";
    
    private IRawDataServiceInternal internalService;
    private IRawDataService service;
    private SessionContextDTO session2;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        internalService = context.mock(IRawDataServiceInternal.class);
        service = new RawDataService(sessionManager, daoFactory, internalService);
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
        sample .setGeneratedFrom(parent);
        context.checking(new Expectations()
            {
                {
                    one(internalService).listRawDataSamples(session2.getSessionToken());
                    MsInjectionSample msInjectionSample = new MsInjectionSample(sample);
                    msInjectionSample.addLatestDataSet(createDataSet(RAW_DATA, 10));
                    msInjectionSample.addLatestDataSet(createDataSet(MZXML_DATA, 20));
                    msInjectionSample.addLatestDataSet(createDataSet(MZXML_DATA, 15));
                    msInjectionSample.addLatestDataSet(createDataSet(RAW_DATA, 30));
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
        Map<String, Date> dates = info.getLatestDataSetRegistrationDates();
        assertEquals(30, dates.get(RAW_DATA).getTime());
        assertEquals(20, dates.get(MZXML_DATA).getTime());
        assertEquals(2, dates.size());
        assertEquals(1, infos.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRawDataForUnknownUser()
    {
        prepareGetSession();
        prepareLoginLogout(null);

        try
        {
            service.processingRawData(SESSION_TOKEN, "abc", null, new long[0], null);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown user ID: abc", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRawData()
    {
        prepareGetSession();
        prepareLoginLogout(session2);
        final long[] ids = new long[] {42};
        context.checking(new Expectations()
            {
                {
                    one(internalService).processRawData(session2.getSessionToken(), null, ids, "my-type");
                }
            });

        service.processingRawData(SESSION_TOKEN, "abc", null, ids, "my-type");

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
        dataSet.setDataSetType(new DataSetType(type));
        dataSet.setRegistrationDate(new Date(date));
        return dataSet;
    }
    
}
