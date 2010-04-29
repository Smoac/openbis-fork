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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetCodePredicateTest extends AuthorizationTestCase
{
    @Test
    public void testSuccessfulEvaluation()
    {
        final ProjectPE project = new ProjectPE();
        project.setGroup(createGroup());
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
        final ProjectPE project = new ProjectPE();
        project.setGroup(createAnotherGroup());
        context.checking(new Expectations()
            {
                {
                    one(provider).tryToGetProject("d1");
                    will(returnValue(project));
                }
            });
        DataSetCodePredicate predicate = new DataSetCodePredicate();
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);

        Status evaluation = predicate.doEvaluation(createPerson(), createRoles(false), "d1");

        assertEquals(
                "User 'megapixel' does not have enough privileges to access data in the space 'DB2:/G2'.",
                evaluation.tryGetErrorMessage());
        context.assertIsSatisfied();
    }
}
