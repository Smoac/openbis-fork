/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exception.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetCodePredicateTest extends AuthorizationTestCase
{
    @Test
    public void testSuccessfulEvaluation()
    {
        final DataSetAccessPE accessData =
                DataSetAccessPE.createDataSetAccessPEForTest("1", "d1", SPACE_CODE, "global_"
                        + INSTANCE_CODE, INSTANCE_CODE);

        context.checking(new Expectations()
            {
                {
                    one(provider).tryGetDatasetAccessData("d1");
                    will(returnValue(accessData));
                }
            });
        DataSetCodePredicate predicate = new DataSetCodePredicate();
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);

        Status evaluation = predicate.doEvaluation(createPerson(), createRoles(false), "d1");

        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public void testWithOneInvalidGroup()
    {
        final DataSetAccessPE accessData =
                DataSetAccessPE.createDataSetAccessPEForTest("1", "d1", ANOTHER_GROUP_CODE,
                        "global_" + ANOTHER_INSTANCE_CODE, ANOTHER_INSTANCE_CODE);
        context.checking(new Expectations()
            {
                {
                    one(provider).tryGetDatasetAccessData("d1");
                    will(returnValue(accessData));
                }
            });
        DataSetCodePredicate predicate = new DataSetCodePredicate();
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);

        Status evaluation = predicate.doEvaluation(createPerson(), createRoles(false), "d1");

        assertEquals(
                "User 'megapixel' does not have enough privileges.",
                evaluation.tryGetErrorMessage());
        context.assertIsSatisfied();
    }
}
