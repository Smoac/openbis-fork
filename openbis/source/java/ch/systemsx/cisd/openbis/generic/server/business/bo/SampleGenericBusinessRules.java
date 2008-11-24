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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Sample related business rules.
 * <ul>
 * <li>A database instance sample can be derived from a database instance sample.
 * <li>A group sample can be derived from a group sample of the same group only.
 * <li>A group sample can be derived from a database instance sample.
 * </ul>
 * 
 * @author Izabela Adamczyk
 */
public class SampleGenericBusinessRules
{

    static private void assertValidParentRelation(final SamplePE parent, final SamplePE child)
            throws UserFailureException
    {
        if (parent == null || child == null)
            return;

        SampleIdentifier parentId = parent.getSampleIdentifier();
        SampleIdentifier childId = child.getSampleIdentifier();

        if (parentId.isGroupLevel())
        {
            if (childId.isDatabaseInstanceLevel())
            {
                throwUserFailureException("The database instance sample '%s' "
                        + "can not be derived from the group sample '%s'.", child, parent);
            }
            if (parentId.getGroupLevel().equals(childId.getGroupLevel()) == false)
            {
                throwUserFailureException("The sample '%s' has to be in the same group as "
                        + "the sample '%s' from which it is derived.", child, parent);
            }
        }
    }

    static public void assertValidParents(SamplePE sample)
    {
        if (sample == null)
            return;
        assertValidParentRelation(sample.getContainer(), sample);
        assertValidParentRelation(sample.getGeneratedFrom(), sample);
    }

    static private void throwUserFailureException(String messageTemplate, SamplePE sample1,
            SamplePE sample2)
    {
        throw UserFailureException.fromTemplate(messageTemplate, sample1.getSampleIdentifier(),
                sample2.getSampleIdentifier());
    }
}
