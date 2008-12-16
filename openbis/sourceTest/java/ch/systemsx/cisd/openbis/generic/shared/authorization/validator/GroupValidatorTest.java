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

/**
 * Test cases for corresponding {@link GroupValidator} class.
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
            new GroupValidator().isValid(null, null);
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
        final GroupValidator groupValidator = new GroupValidator();
        
        assertFalse(groupValidator.isValid(createPerson(), createGroup()));

        context.assertIsSatisfied();
    }

    @Test
    public final void testIsValidWithMatchingRoleAssignmentOnGroupLevel()
    {
        final GroupValidator groupValidator = new GroupValidator();
        assertTrue(groupValidator.isValid(createPersonWithRoleAssignments(), createAnotherGroup()));
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testIsValidWithMatchingRoleAssignmentOnDatabaseinstanceLevel()
    {
        final GroupValidator groupValidator = new GroupValidator();
        assertTrue(groupValidator.isValid(createPersonWithRoleAssignments(), createGroup()));
        context.assertIsSatisfied();
    }
    
}
