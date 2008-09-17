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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import ch.systemsx.cisd.common.utilities.ModifiedShortPrefixToStringStyle;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * A query restriction that is applied to experiments.
 * 
 * @author Franz-Josef Elmer
 */
public class QueryRestriction implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private boolean suppressInvalidated;

    private ProjectIdentifier projectIdentifier;

    private GroupIdentifier groupIdentifier;

    private MaterialIdentifier materialIdentifier;

    private TimeInterval timeInterval;

    /**
     * The experiment type code.
     */
    private String experimentTypeCode;

    public final boolean isSuppressInvalidated()
    {
        return suppressInvalidated;
    }

    public final void setSuppressInvalidated(final boolean suppressInvalid)
    {
        this.suppressInvalidated = suppressInvalid;
    }

    public final MaterialIdentifier getMaterialIdentifier()
    {
        return materialIdentifier;
    }

    public final void setMaterialIdentifier(final MaterialIdentifier materialIdentifier)
    {
        this.materialIdentifier = materialIdentifier;
    }

    public final ProjectIdentifier tryGetProjectIdentifier()
    {
        return projectIdentifier;
    }

    public final void setProjectIdentifier(final ProjectIdentifier projectIdentifier)
    {
        this.projectIdentifier = projectIdentifier;
    }

    public final GroupIdentifier tryGetGroupIdentifier()
    {
        return groupIdentifier;
    }

    public final void setGroupIdentifier(final GroupIdentifier groupIdentifier)
    {
        this.groupIdentifier = groupIdentifier;
    }

    public final TimeInterval getTimeInterval()
    {
        return timeInterval;
    }

    public final void setTimeInterval(final TimeInterval timeInterval)
    {
        this.timeInterval = timeInterval;
    }

    public final String getExperimentTypeCode()
    {
        return experimentTypeCode;
    }

    public final void setExperimentTypeCode(final String experimentTypeCode)
    {
        this.experimentTypeCode = experimentTypeCode;
    }

    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public final int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public final String toString()
    {
        return ToStringBuilder.reflectionToString(this,
                ModifiedShortPrefixToStringStyle.MODIFIED_SHORT_PREFIX_STYLE);
    }
}
