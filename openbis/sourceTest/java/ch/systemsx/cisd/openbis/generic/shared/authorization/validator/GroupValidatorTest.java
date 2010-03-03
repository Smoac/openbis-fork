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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.GroupTranslator;

/**
 * Test cases for corresponding {@link SpaceValidator} class.
 * 
 * @author Christian Ribeaud
 */
public final class GroupValidatorTest extends AuthorizationTestCase
{
    @Test
    public final void testIsValidWithNull()
    {
        try
        {
            new SpaceValidator().isValid(null, null);
            fail("AssertionError expected");
        } catch (final AssertionError e)
        {
            assertEquals("Unspecified person", e.getMessage());
        }
        
        context.assertIsSatisfied();
    }

    @Test
    public final void testIsValidForAPersonWithoutAccessRights()
    {
        final SpaceValidator groupValidator = new SpaceValidator();
        final PersonPE personPE = createPerson();
        final GroupPE groupPE = createGroup();
        final Space group = GroupTranslator.translate(groupPE);
        
        assertFalse(groupValidator.isValid(personPE, group));

        context.assertIsSatisfied();
    }

    @Test
    public final void testIsValidWithMatchingRoleAssignmentOnGroupLevel()
    {
        final SpaceValidator groupValidator = new SpaceValidator();
        final PersonPE personPE = createPersonWithRoleAssignments();
        final GroupPE groupPE = createAnotherGroup();
        final Space group = GroupTranslator.translate(groupPE);
        assertTrue(groupValidator.isValid(personPE, group));
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testIsValidWithMatchingRoleAssignmentOnDatabaseinstanceLevel()
    {
        final SpaceValidator groupValidator = new SpaceValidator();
        final PersonPE personPE = createPersonWithRoleAssignments();
        final GroupPE groupPE = createGroup();
        final Space group = GroupTranslator.translate(groupPE);
        assertTrue(groupValidator.isValid(personPE, group));
        context.assertIsSatisfied();
    }
    
}
