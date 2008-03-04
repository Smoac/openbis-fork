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

package ch.systemsx.cisd.common.parser;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link DefaultAliasPropertyMapper} class.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultAliasPropertyMapperTest
{

    @Test(expectedExceptions = AssertionError.class)
    public final void testNullHeaders()
    {
        new DefaultAliasPropertyMapper(null);
    }

    @Test
    public final void testConstructorWithNullInHeaders()
    {
        final String[] headers =
            { "firstName", "lastName", "address", null };
        try
        {
            new DefaultAliasPropertyMapper(headers);
            fail("No blank value is allowed.");
        } catch (IllegalArgumentException ex)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testGetProperty()
    {
        IPropertyMapper propertyMapper = new DefaultAliasPropertyMapper(new String[0]);
        try
        {
            assertNull(propertyMapper.getPropertyModel("shouldBeNull"));
            fail("Given property name 'shouldBeNull' does not exist.");
        } catch (final IllegalArgumentException ex)
        {
        }
        final String[] headers =
            { "firstName", "lastName", "address" };
        propertyMapper = new DefaultAliasPropertyMapper(headers);
        assertTrue(propertyMapper.getPropertyModel("firstName").getColumn() == 0);
        assertTrue(propertyMapper.getPropertyModel("address").getColumn() == 2);
    }

    @Test
    public final void testCasePropertyMapper()
    {
        DefaultAliasPropertyMapper propertyMapper = new DefaultAliasPropertyMapper(new String[]
            { "Code", "Description", "RegistrationTimestamp" });
        List<String> properties = new ArrayList<String>(propertyMapper.getAllPropertyNames());
        assertTrue(properties.indexOf("Code") > -1);
        assertTrue(properties.indexOf("code") < 0);
        assertTrue(properties.indexOf("Description") > -1);
        assertTrue(properties.indexOf("description") < 0);
        assertTrue(properties.indexOf("RegistrationTimestamp") > -1);
        assertTrue(properties.indexOf("registrationtimestamp") < 0);
    }
}
