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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.sample;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.common.SamplePermIdUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.CommonSamplePredicateScreeningSystemTest;

/**
 * @author pkupczyk
 */
public class WellIdentifierPredicateSystemTest extends CommonSamplePredicateScreeningSystemTest<WellIdentifier>
{

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ_WRITE;
    }

    @Override
    protected WellIdentifier createNonexistentObject(Object param)
    {
        return new WellIdentifier(null, null, "IDONTEXIST");
    }

    @Override
    protected WellIdentifier createObject(SpacePE spacePE, ProjectPE projectPE, Object param)
    {
        SamplePE samplePE = getSample(spacePE, projectPE, (SampleKind) param);
        return new WellIdentifier(null, null, samplePE.getPermId());
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider session, List<WellIdentifier> objects, Object param)
    {
        getBean(SamplePredicateScreeningTestService.class).testWellIdentifierPredicate(session, objects.get(0));
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "No well identifier specified.");
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        SamplePermIdUtil.assertWithNonexistentObjectForInstanceUser(person, t, param);
    }

    @Override
    protected void assertWithNonexistentObjectForProjectUser(PersonPE person, Throwable t, Object param)
    {
        SamplePermIdUtil.assertWithNonexistentObjectForProjectUser(person, t, param);
    }

    @Override
    protected void assertWithNonexistentObjectForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        SamplePermIdUtil.assertWithNonexistentObjectForSpaceUser(person, t, param);
    }

}
