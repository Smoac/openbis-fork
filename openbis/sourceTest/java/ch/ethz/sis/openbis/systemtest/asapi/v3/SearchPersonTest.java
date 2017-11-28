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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SearchPersonTest extends AbstractTest
{
    @Test
    public void testSearchPersonByUserId()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        searchCriteria.withUserId().thatStartsWith("observer");
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withRoleAssignments().withSpace();
        
        // Then
        List<Person> persons = v3api.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // When
        assertEquals(render(persons), "observer: John Observer observer@o.o, home space:CISD, "
                + "[SPACE_OBSERVER Space TESTGROUP]\n"
                + "observer_cisd: John ObserverCISD observer_cisd@o.o, home space:CISD, "
                + "[SPACE_ADMIN Space TESTGROUP, SPACE_OBSERVER Space CISD]\n");
    }
    
    private String render(List<Person> persons)
    {
        List<String> renderedPersons = new ArrayList<>();
        for (Person person : persons)
        {
            renderedPersons.add(render(person));
        }
        Collections.sort(renderedPersons);
        StringBuilder builder = new StringBuilder();
        for (String renderedPerson : renderedPersons)
        {
            builder.append(renderedPerson).append('\n');
        }
        return builder.toString();
    }
    
    private String render(Person person)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(person.getUserId()).append(":");
        appendTo(builder, person.getFirstName());
        appendTo(builder, person.getLastName());
        appendTo(builder, person.getEmail());
        Space space = person.getSpace();
        if (space != null)
        {
            builder.append(", home space:").append(space.getCode());
        }
        List<RoleAssignment> roleAssignments = person.getRoleAssignments();
        String string = renderAssignments(roleAssignments);
        builder.append(", ").append(string);
        return builder.toString();
    }

    private String renderAssignments(List<RoleAssignment> roleAssignments)
    {
        List<String> renderedAssignments = new ArrayList<>();
        for (RoleAssignment roleAssignment : roleAssignments)
        {
            renderedAssignments.add(roleAssignment.getRoleLevel() + "_" + roleAssignment.getRole() + " " + roleAssignment.getSpace());
        }
        Collections.sort(renderedAssignments);
        return renderedAssignments.toString();
    }
    
    private void appendTo(StringBuilder builder, String stringOrNull)
    {
        if (stringOrNull != null)
        {
            builder.append(" ").append(stringOrNull);
        }
    }
}
