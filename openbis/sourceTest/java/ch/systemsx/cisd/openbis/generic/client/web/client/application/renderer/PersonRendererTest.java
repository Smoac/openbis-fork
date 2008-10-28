/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Date;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * Test cases for corresponding {@link PersonRenderer} class.
 * 
 * @author Christian Ribeaud
 */
public final class PersonRendererTest
{

    @Test
    public final void testToString()
    {
        final Person person = new Person();
        boolean fail = true;
        try
        {
            PersonRenderer.toString(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertEquals("", PersonRenderer.toString(person));
        person.setLastName("Doe");
        assertEquals("Doe", PersonRenderer.toString(person));
        person.setFirstName("John");
        assertEquals("Doe, John", PersonRenderer.toString(person));
        person.setUserId("jdoe");
        assertEquals("Doe, John [jdoe]", PersonRenderer.toString(person));
        person.setEmail("j@d.com");
        assertEquals("Doe, John [jdoe] <j@d.com>", PersonRenderer.toString(person));
        person.setRegistrator(person);
        person.setRegistrationDate(new Date());
        assertEquals("Doe, John [jdoe] <j@d.com>", PersonRenderer.toString(person));
    }

    @Test
    public final void testCreatePersonAnchor()
    {
        final Person person = new Person();
        boolean fail = true;
        try
        {
            PersonRenderer.createPersonAnchor(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        assertEquals("", PersonRenderer.createPersonAnchor(person));
        person.setFirstName("john");
        assertEquals("john", PersonRenderer.createPersonAnchor(person));
        person.setLastName("doe");
        assertEquals("doe, john", PersonRenderer.createPersonAnchor(person));
    }
}
