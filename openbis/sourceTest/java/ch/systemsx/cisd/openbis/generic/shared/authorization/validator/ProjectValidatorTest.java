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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.ProjectTranslator;

/**
 * @author Franz-Josef Elmer
 */
public class ProjectValidatorTest extends AuthorizationTestCase
{
    @Test
    public void testIsValidWithProjectInTheRightGroup()
    {
        ProjectValidator validator = new ProjectValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, ProjectTranslator
                .translate(createProject(createAnotherGroup()))));
    }

    @Test
    public void testIsValidWithProjectInTheRightDatabaseInstance()
    {
        ProjectValidator validator = new ProjectValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, ProjectTranslator
                .translate(createProject(createGroup()))));
    }

    @Test
    public void testIsValidWithProjectInTheWrongGroup()
    {
        ProjectValidator validator = new ProjectValidator();
        PersonPE person = createPersonWithRoleAssignments();
        SpacePE group = createGroup("blabla", createAnotherDatabaseInstance());
        assertEquals(false, validator.isValid(person, ProjectTranslator
                .translate(createProject(group))));
    }
}
