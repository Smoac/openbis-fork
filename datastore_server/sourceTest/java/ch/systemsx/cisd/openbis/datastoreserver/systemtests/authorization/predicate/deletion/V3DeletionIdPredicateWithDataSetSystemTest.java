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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.deletion;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.DeletionUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonDataSetPredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.deletion.DeletionPredicateTestService;

/**
 * @author pkupczyk
 */
public class V3DeletionIdPredicateWithDataSetSystemTest extends CommonDataSetPredicateSystemTest<IDeletionId>
{

    @Override
    protected boolean isCollectionPredicate()
    {
        return true;
    }

    @Override
    protected IDeletionId createNonexistentObject(Object param)
    {
        return new DeletionTechId(-1L);
    }

    @Override
    protected IDeletionId createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        TechId techId = DeletionUtil.createObjectWithDataSet(this, spacePE, projectPE, param);
        return new DeletionTechId(techId.getId());
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<IDeletionId> objects, Object param)
    {
        try
        {
            getBean(DeletionPredicateTestService.class).testV3DeletionIdPredicate(sessionProvider, objects);
        } finally
        {
            if (objects != null)
            {
                for (IDeletionId object : objects)
                {
                    if (object != null)
                    {
                        getCommonService().untrash(((DeletionTechId) object).getTechId());
                    }
                }
            }
        }
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, NullPointerException.class, null);
    }

    @Override
    protected void assertWithNullCollection(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "No v3 deletion id object specified.");
    }

    @Override
    protected void assertWithNonexistentObject(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

}