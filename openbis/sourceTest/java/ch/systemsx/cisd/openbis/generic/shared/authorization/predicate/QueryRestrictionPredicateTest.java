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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryRestriction;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * Test cases for corresponding {@link QueryRestrictionPredicate} class.
 * 
 * @author Christian Ribeaud
 */
public final class QueryRestrictionPredicateTest extends PredicateTestCase
{
    @Test
    public final void testWithoutInit()
    {
        final QueryRestrictionPredicate predicate = new QueryRestrictionPredicate();
        try
        {
            predicate.evaluate(createPerson(), createRoles(false), new QueryRestriction());
            fail("AssertionError expected");
        } catch (final AssertionError e)
        {
            assertEquals("Predicate has not been initialized", e.getMessage());
        }
        
        context.assertIsSatisfied();
    }

    @Test
    public void testHomeGroup()
    {
        QueryRestrictionPredicate predicate = new QueryRestrictionPredicate();
        QueryRestriction queryRestriction = new QueryRestriction();
        prepareProvider(createDatabaseInstance(), createGroups());
        predicate.init(provider);
        
        PersonPE person = createPerson();
        person.setHomeGroup(createGroup());
        Status status = predicate.evaluate(person, createRoles(false), queryRestriction);
        
        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testHomeGroupNotAllowed()
    {
        QueryRestrictionPredicate predicate = new QueryRestrictionPredicate();
        QueryRestriction queryRestriction = new QueryRestriction();
        prepareProvider(createAnotherDatabaseInstance(), createGroups());
        predicate.init(provider);
        
        PersonPE person = createPerson();
        person.setHomeGroup(createAnotherGroup());
        Status status = predicate.evaluate(person, createRoles(false), queryRestriction);
        
        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges "
                + "to access data in the group 'DB2:/G2'.", status.tryGetErrorMessage());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAllowedGroup()
    {
        QueryRestrictionPredicate predicate = new QueryRestrictionPredicate();
        QueryRestriction queryRestriction = new QueryRestriction();
        GroupIdentifier groupIdentifier = new GroupIdentifier(INSTANCE_CODE, GROUP_CODE);
        queryRestriction.setGroupIdentifier(groupIdentifier);
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);
        
        Status status = predicate.evaluate(createPerson(), createRoles(false), queryRestriction);
        
        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGroupNotAllowed()
    {
        QueryRestrictionPredicate predicate = new QueryRestrictionPredicate();
        QueryRestriction queryRestriction = new QueryRestriction();
        GroupIdentifier groupIdentifier = new GroupIdentifier(ANOTHER_INSTANCE_CODE, ANOTHER_GROUP_CODE);
        queryRestriction.setGroupIdentifier(groupIdentifier);
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createGroups());
        predicate.init(provider);
        
        Status status = predicate.evaluate(createPerson(), createRoles(false), queryRestriction);
        
        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges "
                + "to access data in the group 'DB2:/G2'.", status.tryGetErrorMessage());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAllowedProject()
    {
        QueryRestrictionPredicate predicate = new QueryRestrictionPredicate();
        QueryRestriction queryRestriction = new QueryRestriction();
        queryRestriction.setProjectIdentifier(new ProjectIdentifier(INSTANCE_CODE, GROUP_CODE, "p1"));
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);
        
        Status status = predicate.evaluate(createPerson(), createRoles(false), queryRestriction);
        
        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testProjectInWrongGroup()
    {
        QueryRestrictionPredicate predicate = new QueryRestrictionPredicate();
        QueryRestriction queryRestriction = new QueryRestriction();
        queryRestriction.setProjectIdentifier(new ProjectIdentifier(ANOTHER_INSTANCE_CODE,
                ANOTHER_GROUP_CODE, "p1"));
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createGroups());
        predicate.init(provider);
        
        Status status = predicate.evaluate(createPerson(), createRoles(false), queryRestriction);
        
        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges "
                + "to access data in the group 'DB2:/G2'.", status.tryGetErrorMessage());
        context.assertIsSatisfied();
    }
}
