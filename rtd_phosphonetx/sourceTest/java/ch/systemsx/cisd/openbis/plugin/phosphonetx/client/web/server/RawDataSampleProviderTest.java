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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server;

import static ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server.RawDataSampleProvider.CODE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server.RawDataSampleProvider.PARENT;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.server.RawDataSampleProvider.REGISTRATION_DATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=RawDataSampleProvider.class)
public class RawDataSampleProviderTest extends AbstractServerTestCase
{
    private IRawDataServiceInternal service;
    private RawDataSampleProvider provider;
    
    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        service = context.mock(IRawDataServiceInternal.class);
        provider = new RawDataSampleProvider(service, SESSION_TOKEN);
    }
    
    @Test
    public void testGetHeadersForNoData()
    {
        prepareListRawDataSamples();
        
        List<GenericTableColumnHeader> headers = provider.getHeaders();
        
        assertColumns(headers);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetHeaders()
    {
        Sample ms1 = sample("MS1", sample("ABC", "alpha", "beta"), "one");
        Sample ms2 = sample("MS2", sample("DE", "gamma", "beta"), "one");
        Sample ms3 = sample("MS3", sample("DE", "gamma", "alpha"), "two");
        prepareListRawDataSamples(ms1, ms2, ms3);
        
        List<GenericTableColumnHeader> headers = provider.getHeaders();
        
        assertColumns(headers, "one", "two", "alpha", "beta", "gamma");
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetOriginalDataForNoData()
    {
        prepareListRawDataSamples();
        
        List<GenericTableRow> data = provider.getOriginalData();
        
        assertEquals(0, data.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetOriginalData()
    {
        Sample ms1 = sample("MS1", sample("ABC", "beta", "alpha"), "one");
        Sample ms2 = sample("MS2", sample("DE", "gamma", "beta"), "one");
        Sample ms3 = sample("MS3", sample("FG", "alpha", "gamma"), "2");
        prepareListRawDataSamples(ms1, ms2, ms3);
        
        List<GenericTableRow> data = provider.getOriginalData();
        
        assertEquals(3, data.size());
        assertRow("MS1, Mon Mar 30 17:18:20 CET 1970, /G/ABC, null, 3.0, 6, 4, null", data.get(0));
        assertRow("MS2, Mon Mar 30 17:20:00 CET 1970, /G/DE, null, 3.0, null, 5, 5", data.get(1));
        assertRow("MS3, Mon Mar 30 17:21:40 CET 1970, /G/FG, 1, null, 5, null, 6", data.get(2));
        context.assertIsSatisfied();
    }
    
    private void assertRow(String expectedRow, GenericTableRow row)
    {
        StringBuilder builder = new StringBuilder();
        int length = expectedRow.split(",").length;
        for (int i = 0; i < length; i++)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }
            builder.append(row.tryToGetValue(i));
        }
        assertEquals(expectedRow, builder.toString());
    }
    
    private void assertColumns(List<GenericTableColumnHeader> headers,
            String... expectedTitles)
    {
        assertFixedColumns(headers);
        for (int i = 0; i < expectedTitles.length; i++)
        {
            assertPropertyHeader(expectedTitles[i], i + 3, headers);
        }
        assertEquals(expectedTitles.length + 3, headers.size());
    }
    
    private void assertFixedColumns(List<GenericTableColumnHeader> headers)
    {
        assertUntitledHeader(CODE, 0, true, DataTypeCode.VARCHAR, headers.get(0));
        assertUntitledHeader(REGISTRATION_DATE, 1, false, DataTypeCode.VARCHAR, headers.get(1));
        assertUntitledHeader(PARENT, 2, false, DataTypeCode.VARCHAR, headers.get(2));
    }
    
    private void assertUntitledHeader(String expectedCode, int expectedIndex,
            boolean expectedlinkableFlag, DataTypeCode expectedType, GenericTableColumnHeader header)
    {
        assertHeader(expectedCode, expectedCode, expectedIndex, expectedlinkableFlag, expectedType,
                header);
    }

    private void assertPropertyHeader(String expectedLabel, int index, List<GenericTableColumnHeader> headers)
    {
        DataTypeCode type = DataTypeCode.values()[expectedLabel.length()];
        GenericTableColumnHeader header = headers.get(index);
        assertHeader(expectedLabel, expectedLabel.toUpperCase(), index, false, type, header);
    }
    
    private void assertHeader(String expectedTitle, String expectedCode, int expectedIndex,
            boolean expectedlinkableFlag, DataTypeCode expectedType, GenericTableColumnHeader header)
    {
        assertEquals(expectedTitle, header.getTitle());
        assertEquals(expectedCode, header.getCode());
        assertEquals(expectedIndex, header.getIndex());
        assertEquals(expectedlinkableFlag, header.isLinkable());
        assertEquals(expectedType, header.getType());
    }
    
    private Sample sample(String code, Sample parent, String...properties)
    {
        Sample sample = sample(code, properties);
        sample.setGeneratedFrom(parent);
        return sample;
    }
    
    private Sample sample(String code, String... properties)
    {
        Sample sample = new Sample();
        sample.setCode(code);
        sample.setIdentifier("/G/" + code);
        sample.setId((long) code.hashCode());
        sample.setRegistrationDate(new Date(code.hashCode() * 100000L));
        sample.setProperties(createProperties(properties));
        return sample;
    }

    private List<IEntityProperty> createProperties(String... labels)
    {
        ArrayList<IEntityProperty> properties = new ArrayList<IEntityProperty>();
        for (String label : labels)
        {
            GenericValueEntityProperty property = new GenericValueEntityProperty();
            PropertyType propertyType = new PropertyType();
            propertyType.setLabel(label);
            propertyType.setCode(propertyType.getLabel().toUpperCase());
            DataType dataType = new DataType();
            dataType.setCode(DataTypeCode.values()[label.length()]);
            propertyType.setDataType(dataType);
            property.setPropertyType(propertyType);
            property.setValue(Integer.toString(label.length() + properties.size()));
            properties.add(property);
        }
        return properties;
    }
    
    private void prepareListRawDataSamples(final Sample... samples)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).listRawDataSamples(SESSION_TOKEN);
                    will(returnValue(Arrays.asList(samples)));
                }
            });
    }
}
