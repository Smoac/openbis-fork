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
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleFilter;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierPattern;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SampleFilterPredicateTest extends AuthorizationTestCase
{
    @Test
    public void testHomeDatabaseAllowed()
    {   
        SampleFilterPredicate predicate = new SampleFilterPredicate();
        prepareProvider(createDatabaseInstance(), createGroups());
        predicate.init(provider);

        SampleFilter sampleFilter = new SampleFilter(SampleIdentifierPattern.EMPTY_ARRAY, true, true);
        Status status = predicate.evaluate(createPerson(), createRoles(false), sampleFilter);

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testHomeDatabaseNotAllowed()
    {   
        SampleFilterPredicate predicate = new SampleFilterPredicate();
        prepareProvider(createAnotherDatabaseInstance(), createGroups());
        predicate.init(provider);
        
        SampleFilter sampleFilter = new SampleFilter(SampleIdentifierPattern.EMPTY_ARRAY, true, true);
        Status status = predicate.evaluate(createPerson(), createRoles(false), sampleFilter);
        
        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges "
                + "to read from database instance 'DB2'.", status.tryGetErrorMessage());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGroupFilterAllowed()
    {   
        SampleFilterPredicate predicate = new SampleFilterPredicate();
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);
        GroupIdentifier groupIdentifier = new GroupIdentifier(INSTANCE_CODE, GROUP_CODE);
        SampleIdentifierPattern[] patterns =
            SampleIdentifierPattern.createGroupVisible(groupIdentifier, "s*");
        
        SampleFilter sampleFilter = new SampleFilter(patterns, true, true);
        Status status = predicate.evaluate(createPerson(), createRoles(false), sampleFilter);
        
        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGroupFilterNotAllowed()
    {   
        SampleFilterPredicate predicate = new SampleFilterPredicate();
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createGroups());
        predicate.init(provider);
        GroupIdentifier groupIdentifier = new GroupIdentifier(ANOTHER_INSTANCE_CODE, ANOTHER_GROUP_CODE);
        SampleIdentifierPattern[] patterns =
            SampleIdentifierPattern.createGroupVisible(groupIdentifier, "s*");
        
        SampleFilter sampleFilter = new SampleFilter(patterns, true, true);
        Status status = predicate.evaluate(createPerson(), createRoles(false), sampleFilter);
        
        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges "
                + "to access data in the group 'DB2:/G2'.", status.tryGetErrorMessage());
        context.assertIsSatisfied();
    }
    
}
