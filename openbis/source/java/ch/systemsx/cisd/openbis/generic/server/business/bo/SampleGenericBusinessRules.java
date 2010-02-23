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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
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

    static private void assertValidParentRelation(final SamplePE parent, final SamplePE child,
            final String childRelationName) throws UserFailureException
    {
        if (parent == null || child == null)
            return;

        SampleIdentifier parentId = parent.getSampleIdentifier();
        // new identifier of a child is needed for comparison
        SampleIdentifier childId = IdentifierHelper.createSampleIdentifier(child);

        if (parentId.isGroupLevel())
        {
            if (childId.isDatabaseInstanceLevel())
            {
                throwUserFailureException("The database instance sample '%s' "
                        + "can not be %s the space sample '%s'.", child, parent, childRelationName);
            }
        }
    }

    static private void assertValidChildrenRelation(final List<SamplePE> children,
            final SamplePE parent, final String childRelationName) throws UserFailureException
    {
        if (children == null || children.size() == 0 || parent == null)
            return;

        // new identifier of a parent is needed for comparison
        SampleIdentifier parentId = IdentifierHelper.createSampleIdentifier(parent);

        if (parentId.isGroupLevel())
        {
            for (SamplePE child : children)
            {
                SampleIdentifier childId = child.getSampleIdentifier();
                if (childId.isDatabaseInstanceLevel())
                {
                    throwUserFailureException("Sample '%s' can not be a space sample because of "
                            + "a %s database instance sample '%s'.", parent, child,
                            childRelationName);
                }
            }
        }
    }

    static public void assertValidParents(SamplePE sample)
    {
        if (sample == null)
            return;
        assertValidParentRelation(sample.getContainer(), sample, "contained in");
        assertValidParentRelation(sample.getGeneratedFrom(), sample, "child of");
    }

    static public void assertValidChildren(SamplePE sample)
    {
        if (sample == null)
            return;
        assertValidChildrenRelation(sample.getContained(), sample, "contained");
        assertValidChildrenRelation(sample.getGenerated(), sample, "child");
    }

    static private void throwUserFailureException(String messageTemplate, SamplePE sample1,
            SamplePE sample2, String childRelationName)
    {
        throw UserFailureException.fromTemplate(messageTemplate, sample1.getSampleIdentifier(),
                childRelationName, sample2.getSampleIdentifier());
    }
}
