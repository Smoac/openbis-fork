/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.dataset;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.ProjectAuthorizationUser;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.DataSetTechIdUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestAssertions;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTestDataSetAssertions;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.dataset.DataSetPredicateTestService;

/**
 * @author pkupczyk
 */
public class DataSetTechIdPredicateSystemTest extends CommonPredicateSystemTest<TechId>
{

    @Override
    public Object[] getParams()
    {
        return getDataSetKinds();
    }

    @Override
    protected TechId createNonexistentObject(Object param)
    {
        return DataSetTechIdUtil.createNonexistentObject(param);
    }

    @Override
    protected TechId createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return DataSetTechIdUtil.createObject(this, spacePE, projectPE, param);
    }

    @Override
    protected void evaluateObjects(ProjectAuthorizationUser user, List<TechId> objects, Object param)
    {
        getBean(DataSetPredicateTestService.class).testDataSetTechIdPredicate(user.getSessionProvider(), objects.get(0));
    }

    @Override
    protected CommonPredicateSystemTestAssertions<TechId> getAssertions()
    {
        return new CommonPredicateSystemTestDataSetAssertions<TechId>(super.getAssertions())
            {
                @Override
                public void assertWithNullObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    assertException(t, UserFailureException.class, "No data set technical id specified.");
                }

                @Override
                public void assertWithNonexistentObject(ProjectAuthorizationUser user, Throwable t, Object param)
                {
                    assertNoException(t);
                }
            };
    }

}
