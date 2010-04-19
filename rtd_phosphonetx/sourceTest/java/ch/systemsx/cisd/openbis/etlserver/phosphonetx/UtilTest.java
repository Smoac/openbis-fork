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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UtilTest extends AssertJUnit
{
    @Test
    public void testGetUpdatedPropertiesForNoEntityProperties()
    {
        Sample sample = new Sample();
        sample.setProperties(Arrays.<IEntityProperty>asList());
        Properties properties = new Properties();
        properties.setProperty("blabla", "blub");
        
        List<IEntityProperty> props = Util.getUpdatedProperties(sample, new SampleType(), properties);
        
        assertEquals(0, props.size());
    }

    @Test
    public void testGetUpdatedProperties()
    {
        Sample sample = new Sample();
        EntityProperty p1 = createProperty("name", "Albert");
        EntityProperty p2 = createProperty("age", "26");
        sample.setProperties(Arrays.<IEntityProperty>asList(p1, p2));
        SampleType entityType = new SampleType();
        SampleTypePropertyType etpt1 = createETPT("name", false);
        SampleTypePropertyType etpt2 = createETPT("age", true);
        entityType.setSampleTypePropertyTypes(Arrays.asList(etpt1, etpt2));
        Properties properties = new Properties();
        properties.setProperty("blabla", "blub");
        properties.setProperty("age", "76");

        List<IEntityProperty> props = Util.getUpdatedProperties(sample, entityType, properties);
        
        assertEquals(2, props.size());
        assertEquals("name", props.get(0).getPropertyType().getCode());
        assertEquals("Albert", props.get(0).getValue());
        assertEquals("age", props.get(1).getPropertyType().getCode());
        assertEquals("76", props.get(1).getValue());
    }
    
    @Test
    public void testGetAndCheckProperties()
    {
        SampleType entityType = new SampleType();
        SampleTypePropertyType etpt1 = createETPT("answer", false);
        SampleTypePropertyType etpt2 = createETPT("greetings", true);
        entityType.setSampleTypePropertyTypes(Arrays.asList(etpt1, etpt2));
        Properties properties = new Properties();
        properties.setProperty("greetings", "hello");
        properties.setProperty("blabla", "blub");
        
        IEntityProperty[] entityProperties = Util.getAndCheckProperties(properties, entityType);
        
        assertEquals(1, entityProperties.length);
        assertEquals("greetings", entityProperties[0].getPropertyType().getCode());
        assertEquals("hello", entityProperties[0].tryGetAsString());
    }
    
    @Test
    public void testGetAndCheckPropertiesForMissingMandatoryProperty()
    {
        SampleType entityType = new SampleType();
        SampleTypePropertyType etpt1 = createETPT("answer", true);
        SampleTypePropertyType etpt2 = createETPT("greetings", true);
        entityType.setSampleTypePropertyTypes(Arrays.asList(etpt1, etpt2));
        Properties properties = new Properties();
        properties.setProperty("greetings", "hello");
        
        try
        {
            Util.getAndCheckProperties(properties, entityType);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("The following mandatory properties are missed: [answer]", ex.getMessage());
        }
    }
    
    private EntityProperty createProperty(String name, String value)
    {
        EntityProperty entityProperty = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(name);
        entityProperty.setPropertyType(propertyType);
        entityProperty.setValue(value);
        return entityProperty;
    }
    
    private SampleTypePropertyType createETPT(String code, boolean mandatory)
    {
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(code);
        SampleTypePropertyType etpt = new SampleTypePropertyType();
        etpt.setPropertyType(propertyType);
        etpt.setMandatory(mandatory);
        return etpt;
    }
}

