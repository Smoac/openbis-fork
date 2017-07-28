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
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.CommonSamplePredicateScreeningSystemTest;

/**
 * @author pkupczyk
 */
public abstract class ScreeningPlateListReadOnlyPredicateSystemTest extends CommonSamplePredicateScreeningSystemTest<PlateIdentifier>
{

    @Override
    protected boolean isCollectionPredicate()
    {
        return true;
    }

    @Override
    protected SampleKind getSharedSampleKind()
    {
        return SampleKind.SHARED_READ;
    }

    @Override
    public Object[] provideParams()
    {
        return PlateIdentifierUtil.provideParams(getSharedSampleKind());
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider session, List<PlateIdentifier> objects, Object param)
    {
        getBean(SamplePredicateScreeningTestService.class).testScreeningPlateListReadOnlyPredicate(session, objects);
    }

    @Override
    protected void assertWithNullCollection(PersonPE person, Throwable t, Object param)
    {
        assertException(t, UserFailureException.class, "No plate specified.");
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t, Object param)
    {
        assertException(t, NullPointerException.class, null);
    }

}
