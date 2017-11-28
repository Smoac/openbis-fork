/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GetPersonTest extends AbstractTest
{
    @Test
    public void testGetPersons()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        IPersonId id1 = new PersonPermId("observer");
        IPersonId id2 = new PersonPermId("test_role");
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withRegistrator();
        
        // When
        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, Arrays.asList(id1, id2), fetchOptions);
        
        // Then
        assertEquals(renderPerson(persons.get(id1)), "observer: John Observer observer@o.o, home space:CISD, "
                + "[SPACE_OBSERVER Space TESTGROUP]");
        assertEquals(renderPerson(persons.get(id2)), "test_role: John 3 Doe test role test_role@in.active, home space:CISD, "
                + "[SPACE_POWER_USER Space CISD], registrator: test");
    }
}
