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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public abstract class CommonSampleValidatorSystemTest<O> extends CommonValidatorSystemTest<O>
{

    protected abstract SampleKind getSharedSampleKind();

    @Override
    public Object[] provideParams()
    {
        return provideSampleKinds(getSharedSampleKind());
    }

    @Override
    protected void assertWithMatchingSpaceAndMatchingProjectUser(PersonPE person, O object, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ_WRITE.equals(param))
        {
            assertNull(object);
        } else
        {
            super.assertWithMatchingSpaceAndMatchingProjectUser(person, object, t, param);
        }
    }

    @Override
    protected void assertWithMatchingSpaceAndNonMatchingProjectUser(PersonPE person, O object, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ_WRITE.equals(param))
        {
            assertNull(object);
        } else
        {
            super.assertWithMatchingSpaceAndNonMatchingProjectUser(person, object, t, param);
        }
    }

    @Override
    protected void assertWithMatchingSpaceAndNonMatchingSpaceUser(PersonPE person, O object, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ_WRITE.equals(param))
        {
            assertNull(object);
        } else
        {
            super.assertWithMatchingSpaceAndNonMatchingSpaceUser(person, object, t, param);
        }
    }

    @Override
    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person, O object, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNotNull(object);
        } else if (SampleKind.SHARED_READ_WRITE.equals(param) || SampleKind.SPACE.equals(param) || SampleKind.SPACE_CONTAINED.equals(param))
        {
            assertNull(object);
        } else
        {
            super.assertWithNonMatchingSpaceAndMatchingProjectUser(person, object, t, param);
        }
    }

    @Override
    protected void assertWithNonMatchingSpaceAndNonMatchingProjectUser(PersonPE person, O object, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNotNull(object);
        } else
        {
            super.assertWithNonMatchingSpaceAndNonMatchingProjectUser(person, object, t, param);
        }
    }

}
