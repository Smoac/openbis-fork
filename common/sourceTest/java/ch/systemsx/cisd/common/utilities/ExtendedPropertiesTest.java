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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

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
    public final void setUp()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("one", "eins");
        props.setProperty("un", "${one}");
        props.setProperty("two", "zwei");
        props.setProperty("three", "drei");
        props.setProperty("four", "${one}${three}");
        extendedProperties = props;
    }

    @Test
    public final void testConstructor()
    {
        ExtendedProperties newProps = new ExtendedProperties(extendedProperties);
        assertEquals(5, extendedProperties.size());
        // We created an EMPTY list with defaults.
        assertEquals(0, newProps.size());
        assertEquals("{}", newProps.toString());
        // Returns the default value...
        assertEquals("eins", extendedProperties.getProperty("one"));
        // ...but the property itself is unknown.
        assertNull(newProps.get("one"));
    }

    @Test
    public final void testGetPropertyString()
    {
        assertEquals("eins", extendedProperties.getProperty("one"));
        assertEquals("eins", extendedProperties.getProperty("un"));
        assertEquals("einsdrei", extendedProperties.getProperty("four"));
    }

    @Test
    public final void testGetSubsetString()
    {
        ExtendedProperties props = extendedProperties.getSubset("t", false);
        assert props.size() == 2;
        assert props.getProperty("two").equals("zwei");
        assert props.getProperty("three").equals("drei");
        
        props = extendedProperties.getSubset("un", false);
        assert props.size() == 1;
        assert props.getProperty("un").equals("eins");
        
        props = extendedProperties.getSubset("t", true);
        assert props.size() == 2;
        assert props.getProperty("wo").equals("zwei");
        assert props.getProperty("hree").equals("drei");
        
        props = extendedProperties.getSubset("un", true);
        assert props.size() == 1;
        assert props.getProperty("").equals("eins");
    }

    @Test
    public final void testCyclicDependency()
    {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("a", "A${b}");
        props.setProperty("b", "B${c}");
        props.setProperty("c", "C${a}");
        assertEquals("ABC${a}", props.getProperty("a"));
    }

    @Test
    public final void testGetUnalteredProperty()
    {
        assertEquals("${one}", extendedProperties.getUnalteredProperty("un"));
        System.out.println(extendedProperties);
    }

}
