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

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetTest extends AssertJUnit
{
    private static final String DATA_SET_CODE = "dataSet-code";

    private static final String DATA_SET_TYPE_CODE = "dataSet-type";

    private DataSet dataSet;

    @BeforeMethod
    public void setUp()
    {
        DataSetInitializer initializer = new DataSetInitializer();
        initializer.setCode(DATA_SET_CODE);
        initializer.setDataSetTypeCode(DATA_SET_TYPE_CODE);
        initializer.putProperty("PROP1", "value1");
        dataSet = new DataSet(initializer);
    }

    @Test(expectedExceptions =
        { IllegalArgumentException.class })
    public void testInitialization()
    {
        DataSetInitializer initializer = new DataSetInitializer();
        initializer.setCode(DATA_SET_CODE);
        new DataSet(initializer);
    }

    @Test
    public void testEquals()
    {
        DataSetInitializer initializer = new DataSetInitializer();
        initializer.setCode(DATA_SET_CODE);
        initializer.setDataSetTypeCode(DATA_SET_TYPE_CODE);
        initializer.putProperty("PROP1", "value1");
        DataSet myDataSet = new DataSet(initializer);
        assertTrue("Data sets with the same code should be equal.", dataSet.equals(myDataSet));
        assertEquals(dataSet.hashCode(), myDataSet.hashCode());

        initializer = new DataSetInitializer();
        initializer.setCode(DATA_SET_CODE);
        initializer.setDataSetTypeCode("new-code");
        myDataSet = new DataSet(initializer);
        assertTrue("Data sets with the same code should be equal.", dataSet.equals(myDataSet));
        assertEquals(dataSet.hashCode(), myDataSet.hashCode());

        initializer = new DataSetInitializer();
        initializer.setCode("code-2");
        initializer.setDataSetTypeCode(DATA_SET_TYPE_CODE);
        initializer.putProperty("PROP1", "value1");
        myDataSet = new DataSet(initializer);
        assertFalse("Data sets with the different ids should not be equal.", dataSet
                .equals(myDataSet));
    }

    @Test
    public void testToString()
    {
        String stringRepresentation = dataSet.toString();
        assertEquals("DataSet[dataSet-code,dataSet-type,{PROP1=value1}]", stringRepresentation);
    }
}
