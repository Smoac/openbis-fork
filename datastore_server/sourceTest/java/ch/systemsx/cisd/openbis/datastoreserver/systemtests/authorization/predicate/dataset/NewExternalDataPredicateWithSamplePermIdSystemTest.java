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

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SamplePermIdUtil;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonSamplePredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.dataset.DataSetPredicateTestService;

/**
 * @author pkupczyk
 */
public class NewExternalDataPredicateWithSamplePermIdSystemTest extends CommonSamplePredicateSystemTest<String>
{

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ_WRITE;
    }

    @Override
    protected String createNonexistentObject(Object param)
    {
        return SamplePermIdUtil.createNonexistentObject(param).toString();
    }

    @Override
    protected String createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        return SamplePermIdUtil.createObject(this, spacePE, projectPE, param).toString();
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<String> objects, Object param)
    {
        NewExternalData data = null;

        if (objects.get(0) != null)
        {
            data = new NewExternalData();
            data.setSamplePermIdOrNull(objects.get(0));
        }

        getBean(DataSetPredicateTestService.class).testNewExternalDataPredicate(sessionProvider, data);
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "No new data set specified.");
    }

    @Override
    protected void assertWithNullCollection(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Override
    protected void assertWithNonexistentObjectForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        assertException(t, AuthorizationFailureException.class, ".*There is no sample with perm id.*");
    }

    @Override
    protected void assertWithNonexistentObjectForProjectUser(PersonPE person, Throwable t, Object param)
    {
        assertException(t, AuthorizationFailureException.class, ".*There is no sample with perm id.*");
    }

}
