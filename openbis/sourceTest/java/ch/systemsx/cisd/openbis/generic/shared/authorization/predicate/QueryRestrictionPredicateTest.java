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

import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.QueryRestrictionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryRestriction;

/**
 * Test cases for corresponding {@link QueryRestrictionPredicate} class.
 * 
 * @author Christian Ribeaud
 */
public final class QueryRestrictionPredicateTest
{
    private final QueryRestriction createQueryRestriction()
    {
        return new QueryRestriction();
    }

    @Test
    public final void testWithoutInit()
    {
        final QueryRestrictionPredicate predicate = new QueryRestrictionPredicate();
        boolean fail = true;
        try
        {
            predicate.evaluate(DatabaseInstanceIdentifierPredicateTest.createPerson(),
                    DatabaseInstanceIdentifierPredicateTest.createAllowedRoles(false),
                    createQueryRestriction());
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
    }

}
