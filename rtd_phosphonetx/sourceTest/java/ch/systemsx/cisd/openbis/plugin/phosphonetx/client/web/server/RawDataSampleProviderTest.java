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

import static ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.RawDataSampleGridIDs.CODE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.RawDataSampleGridIDs.PARENT;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.RawDataSampleGridIDs.REGISTRATION_DATE;

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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=RawDataSampleProvider.class)
public class RawDataSampleProviderTest extends AbstractServerTestCase
{
    private IProteomicsDataServiceInternal service;
    private RawDataSampleProvider provider;
    
    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        service = context.mock(IProteomicsDataServiceInternal.class);
        provider = new RawDataSampleProvider(service, SESSION_TOKEN);
    }
    
    @Test
    public void testGetHeadersForNoData()
    {
        prepareListRawDataSamples();
        
        List<TableModelColumnHeader> headers = provider.getTableModel().getHeader();
        
        assertFixedColumns(headers);
        assertEquals(4, headers.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetHeaders()
    {
        Sample ms1 = sample("MS1", sample("ABC", "alpha", "beta"), "one");
        Sample ms2 = sample("MS2", sample("DE", "gamma", "beta"), "one");
        Sample ms3 = sample("MS3", sample("DE", "gamma", "alpha"), "two");
        prepareListRawDataSamples(ms1, ms2, ms3);
        
        List<TableModelColumnHeader> headers = provider.getTableModel().getHeader();
        
        assertFixedColumns(headers);
        assertPropertyHeader("one", "USER-ONE", 4, headers);
        assertPropertyHeader("two", "USER-TWO", 5, headers);
        assertPropertyHeader("alpha", "BIO_USER-ALPHA", 6, headers);
        assertPropertyHeader("beta", "BIO_USER-BETA", 7, headers);
        assertPropertyHeader("gamma", "BIO_USER-GAMMA", 8, headers);
        assertEquals(9, headers.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetOriginalDataForNoData()
    {
        prepareListRawDataSamples();
        
        List<TableModelRowWithObject<Sample>> data = provider.getTableModel().getRows();
        
        assertEquals(0, data.size());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetOriginalData()
    {
        Sample ms1 = sample("MS1", sample("ABC", "beta", "alpha"), "one");
        Sample ms2 = sample("MS2", sample("DE", "gamma", "beta"), "one");
        Sample parent = sample("FG", "alpha", "gamma");
        Experiment experiment = new Experiment();
        experiment.setIdentifier("/G/P/E1");
        parent.setExperiment(experiment);
        Sample ms3 = sample("MS3", parent, "2");
        prepareListRawDataSamples(ms1, ms2, ms3);
        
        TypedTableModel<Sample> tableModel = provider.getTableModel();
        List<TableModelRowWithObject<Sample>> data = tableModel.getRows();
        
        assertEquals(3, data.size());
        assertEquals("[null, null, null, null, 2, one, alpha, beta, gamma]", tableModel.getHeader().toString());
        assertRow("MS1, Mon Mar 30 17:18:20 CET 1970, /G/ABC, , , 3.0, 6, 4, ", data.get(0));
        assertRow("MS2, Mon Mar 30 17:20:00 CET 1970, /G/DE, , , 3.0, , 5, 5", data.get(1));
        assertRow("MS3, Mon Mar 30 17:21:40 CET 1970, /G/FG, /G/P/E1, 1, , 5, , 6", data.get(2));
        context.assertIsSatisfied();
    }
    
    private void assertRow(String expectedRow, TableModelRow row)
    {
        StringBuilder builder = new StringBuilder();
        int length = expectedRow.split(",").length;
        for (int i = 0; i < length; i++)
        {
            if (builder.length() > 0)
            {
                builder.append(", ");
            }
            builder.append(row.getValues().get(i));
        }
        assertEquals(expectedRow, builder.toString());
    }
    
    private void assertFixedColumns(List<TableModelColumnHeader> headers)
    {
        assertUntitledHeader(CODE, 0, DataTypeCode.VARCHAR, headers.get(0));
        assertUntitledHeader(REGISTRATION_DATE, 1, DataTypeCode.TIMESTAMP, headers.get(1));
        assertUntitledHeader(PARENT, 2, DataTypeCode.VARCHAR, headers.get(2));
    }
    
    private void assertUntitledHeader(String expectedCode, int expectedIndex,
            DataTypeCode expectedType, TableModelColumnHeader header)
    {
        assertHeader(null, expectedCode, expectedIndex, expectedType, header);
    }

    private void assertPropertyHeader(String expectedLabel, String expectedCode, int index, List<TableModelColumnHeader> headers)
    {
        DataTypeCode type = DataTypeCode.values()[expectedLabel.length()];
        TableModelColumnHeader header = headers.get(index);
        assertHeader(expectedLabel, expectedCode, index, type, header);
    }
    
    private void assertHeader(String expectedTitle, String expectedCode, int expectedIndex,
            DataTypeCode expectedType, TableModelColumnHeader header)
    {
        assertEquals(expectedTitle, header.getTitle());
        assertEquals(expectedCode, header.getId());
        assertEquals(expectedIndex, header.getIndex());
        assertEquals(expectedType, header.getDataType());
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
            GenericEntityProperty property = new GenericEntityProperty();
            PropertyType propertyType = new PropertyType();
            propertyType.setLabel(label);
            propertyType.setCode(propertyType.getLabel().toUpperCase());
            propertyType.setSimpleCode(propertyType.getCode());
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
                    List<MsInjectionSample> list = new ArrayList<MsInjectionSample>();
                    for (Sample sample : samples)
                    {
                        list.add(new MsInjectionSample(sample, Arrays.<ExternalData>asList()));
                    }
                    will(returnValue(list));
                }
            });
    }
}
