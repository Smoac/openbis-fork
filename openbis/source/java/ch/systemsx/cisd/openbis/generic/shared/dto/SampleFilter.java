/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierPattern;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * Bean specifying a {@link SamplePE} filter.
 * 
 * @author Franz-Josef Elmer
 */
public class SampleFilter extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private SampleIdentifierPattern[] patterns;

    private boolean notRegisteredToExperiment;

    private boolean hideInvalidated;

    /** see class getters documentation */
    public SampleFilter(final SampleIdentifierPattern[] patterns,
            final boolean notRegisteredToExperiment, final boolean hideInvalidated)
    {
        assert patterns != null : "Unspecified patterns";
        this.patterns = patterns;
        this.notRegisteredToExperiment = notRegisteredToExperiment;
        this.hideInvalidated = hideInvalidated;
    }

    /**
     * Returns whether only those samples should be passing the filters that are not registered to
     * an experiment.
     */
    public final boolean isNotRegisteredToExperiment()
    {
        return notRegisteredToExperiment;
    }

    /**
     * Filters samples visible for specified owner and with code matching the wildcards pattern (* =
     * any string, ? = any character). Note, that if there are no patterns, no sample will match.
     */
    public final SampleIdentifierPattern[] getPatterns()
    {
        return patterns;
    }

    /**
     * Returns either the patterns specified in the constructor or if not defined default patterns
     * based on the specified home group.
     */
    public SampleIdentifierPattern[] getPatternsOrDefaultPatterns(GroupPE homeGroupOrNull)
    {
        if (patterns.length > 0)
        {
            return patterns;
        }
        if (homeGroupOrNull == null || homeGroupOrNull.getCode() == null)
        {
            SampleOwnerIdentifier owner =
                    new SampleOwnerIdentifier(DatabaseInstanceIdentifier.createHome());
            return new SampleIdentifierPattern[]
                { SampleIdentifierPattern.createOwnedBy(owner, "*") };
        }
        GroupIdentifier groupIdentifier =
                new GroupIdentifier(DatabaseInstanceIdentifier.HOME, homeGroupOrNull.getCode());
        return SampleIdentifierPattern.createGroupVisible(groupIdentifier, "*");
    }

    /** If <code>true</code> only valid samples are returned. */
    public final boolean isHideInvalidated()
    {
        return hideInvalidated;
    }
}
