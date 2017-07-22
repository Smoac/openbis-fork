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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate;

import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public abstract class CommonSamplePredicateSystemTest<O> extends CommonPredicateSystemTest<O>
{

    protected abstract SampleKind getSharedSampleKind();

    @Override
    public Object[] provideParams()
    {
        return provideSampleKinds(getSharedSampleKind());
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t, Object param)
    {
        assertNoException(t);
    }

    @Override
    protected void assertWithNonexistentObjectForSpaceUser(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNoException(t);
        } else
        {
            super.assertWithNonexistentObjectForSpaceUser(person, t, param);
        }
    }

    @Override
    protected void assertWithNonexistentObjectForProjectUser(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNoException(t);
        } else
        {
            super.assertWithNonexistentObjectForProjectUser(person, t, param);
        }
    }

    @Override
    protected void assertWithNonMatchingSpaceAndNonMatchingProjectUser(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNoException(t);
        } else
        {
            super.assertWithNonMatchingSpaceAndNonMatchingProjectUser(person, t, param);
        }
    }

    @Override
    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNoException(t);
        } else if (SampleKind.SHARED_READ_WRITE.equals(param) || SampleKind.SPACE.equals(param) || SampleKind.SPACE_CONTAINED.equals(param))
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        } else
        {
            super.assertWithNonMatchingSpaceAndMatchingProjectUser(person, t, param);
        }
    }

    @Override
    protected void assertWithMatchingSpaceAndMatchingProjectUser(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ_WRITE.equals(param))
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        } else
        {
            super.assertWithMatchingSpaceAndMatchingProjectUser(person, t, param);
        }
    }

    @Override
    protected void assertWithMatchingSpaceAndNonMatchingProjectUser(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ_WRITE.equals(param))
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        } else
        {
            super.assertWithMatchingSpaceAndNonMatchingProjectUser(person, t, param);
        }
    }

    @Override
    protected void assertWithMatchingSpaceAndNonMatchingSpaceUser(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ_WRITE.equals(param))
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        } else
        {
            super.assertWithMatchingSpaceAndNonMatchingSpaceUser(person, t, param);
        }
    }

    @Override
    protected void assertWithTwoObjectsWhereBothObjectsMatchAtProjectLevel(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNoException(t);
        } else if (SampleKind.SHARED_READ_WRITE.equals(param) || SampleKind.SPACE.equals(param) || SampleKind.SPACE_CONTAINED.equals(param))
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        } else
        {
            super.assertWithTwoObjectsWhereBothObjectsMatchAtProjectLevel(person, t, param);
        }
    }

    @Override
    protected void assertWithTwoObjectsWhereBothObjectsMatchAtSpaceLevel(PersonPE person, Throwable t, Object param)
    {
        if (SampleKind.SHARED_READ_WRITE.equals(param))
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        } else
        {
            super.assertWithTwoObjectsWhereBothObjectsMatchAtSpaceLevel(person, t, param);
        }
    }

    @Override
    protected void assertWithTwoObjectsWhereOneObjectMatchesAtSpaceLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(PersonPE person, Throwable t,
            Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNoException(t);
        } else
        {
            super.assertWithTwoObjectsWhereOneObjectMatchesAtSpaceLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(person, t, param);
        }
    }

    @Override
    protected void assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(PersonPE person, Throwable t,
            Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNoException(t);
        } else
        {
            super.assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(person, t, param);
        }
    }

    @Override
    protected void assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(PersonPE person, Throwable t,
            Object param)
    {
        if (SampleKind.SHARED_READ.equals(param))
        {
            assertNoException(t);
        } else if (SampleKind.SHARED_READ_WRITE.equals(param) || SampleKind.SPACE.equals(param) || SampleKind.SPACE_CONTAINED.equals(param))
        {
            assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
        } else
        {
            super.assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(person, t, param);
        }
    }

}
