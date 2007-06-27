/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import static org.testng.AssertJUnit.*;

import java.util.Properties;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link ExtendedProperties} class.
 * 
 * @author Christian Ribeaud
 */
public final class ExtendedPropertiesTest
{
    private ExtendedProperties extendedProperties;
    
    @BeforeMethod
    public final void setUp() {
        Properties props = new Properties();
        props.setProperty("one", "eins");
        props.setProperty("un", "${one}");
        props.setProperty("two", "zwei");
        props.setProperty("three", "drei");
        extendedProperties = new ExtendedProperties(props);
    }
    
    @Test
    public final void testGetPropertyString()
    {
        assertEquals("eins", extendedProperties.getProperty("one"));
        assertEquals("eins", extendedProperties.getProperty("un"));
    }

    @Test
    public final void testGetSubsetString()
    {
        ExtendedProperties props = extendedProperties.getSubset("t");
        assert props.size() == 2;
        assert props.getProperty("two").equals("zwei");
        props = extendedProperties.getSubset("un");
        assert props.size() == 1;
        assert props.getProperty("un").equals("eins");
    }

}
