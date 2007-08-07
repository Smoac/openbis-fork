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

import static org.testng.AssertJUnit.*;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link HeaderFilePropertyMapper} class.
 * 
 * @author Christian Ribeaud
 */
public final class HeaderFilePropertyMapperTest
{

    @Test(expectedExceptions = AssertionError.class)
    public final void testNullHeaders()
    {
        new HeaderFilePropertyMapper(null);
    }

    @Test
    public final void testGetProperty()
    {
        IPropertyMapper propertyMapper = new HeaderFilePropertyMapper(new String[0]);
        assertNull(propertyMapper.getProperty("shouldBeNull"));
        String[] headers =
            { "firstName", "lastName", "address", null };
        propertyMapper = new HeaderFilePropertyMapper(headers);
        assertTrue(propertyMapper.getProperty("firstName").getColumn() == 0);
        assertTrue(propertyMapper.getProperty("address").getColumn() == 2);
    }

    @Test
    public final void testCasePropertyMapper()
    {
        HeaderFilePropertyMapper propertyMapper = new HeaderFilePropertyMapper(new String[]
            { "Code", "Description", "RegistrationTimestamp" });
        List<String> properties = new ArrayList<String>(propertyMapper.getAllPropertyNames());
        assertTrue(properties.indexOf("Code") < 0);
        assertTrue(properties.indexOf("code") > -1);
        assertTrue(properties.indexOf("Description") < 0);
        assertTrue(properties.indexOf("description") > -1);
        assertTrue(properties.indexOf("RegistrationTimestamp") < 0);
        assertTrue(properties.indexOf("registrationtimestamp") > -1);
    }
}
