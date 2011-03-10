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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType.DataSetTypeInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType.PropertyTypeInitializer;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetTypeTest extends AssertJUnit
{
    private static final String DATA_SET_TYPE_CODE = "dataSet-type";

    private DataSetType dataSetType;

    @BeforeMethod
    public void setUp()
    {
        DataSetTypeInitializer initializer = new DataSetTypeInitializer();
        initializer.setCode(DATA_SET_TYPE_CODE);

        PropertyTypeInitializer propTypeInitializer = new PropertyTypeInitializer();
        propTypeInitializer.setCode("PROP1");
        propTypeInitializer.setLabel("Property 1");
        initializer.addPropertyType(new PropertyType(propTypeInitializer));

        propTypeInitializer.setCode("PROP2");
        propTypeInitializer.setLabel("Property 2");
        propTypeInitializer.setDescription("Property 2 Description");

        initializer.addPropertyType(new PropertyType(propTypeInitializer));
        dataSetType = new DataSetType(initializer);
    }

    @Test(expectedExceptions =
        { IllegalArgumentException.class })
    public void testInitialization()
    {
        DataSetTypeInitializer initializer = new DataSetTypeInitializer();
        new DataSetType(initializer);
    }

    @Test
    public void testEquals()
    {
        DataSetTypeInitializer initializer = new DataSetTypeInitializer();
        initializer.setCode(DATA_SET_TYPE_CODE);
        PropertyTypeInitializer propTypeInitializer = new PropertyTypeInitializer();
        propTypeInitializer.setCode("PROP200");
        propTypeInitializer.setLabel("Property 200");
        initializer.addPropertyType(new PropertyType(propTypeInitializer));

        DataSetType myDataSetType = new DataSetType(initializer);
        assertTrue("Data sets with the same code should be equal.",
                dataSetType.equals(myDataSetType));
        assertEquals(dataSetType.hashCode(), myDataSetType.hashCode());

        initializer = new DataSetTypeInitializer();
        initializer.setCode(DATA_SET_TYPE_CODE);
        myDataSetType = new DataSetType(initializer);
        assertTrue("Data sets with the same code should be equal.",
                dataSetType.equals(myDataSetType));
        assertEquals(dataSetType.hashCode(), myDataSetType.hashCode());

        initializer = new DataSetTypeInitializer();
        initializer.setCode("code-2");
        propTypeInitializer = new PropertyTypeInitializer();
        propTypeInitializer.setCode("PROP1");
        propTypeInitializer.setLabel("Property 1");
        initializer.addPropertyType(new PropertyType(propTypeInitializer));

        propTypeInitializer.setCode("PROP2");
        propTypeInitializer.setLabel("Property 2");
        propTypeInitializer.setDescription("Property 2 Description");
        myDataSetType = new DataSetType(initializer);
        assertFalse("Data sets with the different ids should not be equal.",
                dataSetType.equals(myDataSetType));
    }

    @Test
    public void testToString()
    {
        String stringRepresentation = dataSetType.toString();
        assertEquals(
                "DataSetType[dataSet-type,[PropertyType[PROP1,Property 1,<null>], PropertyType[PROP2,Property 2,Property 2 Description]]]",
                stringRepresentation);
    }
}
