/*
 * Copyright 2009 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.LocalExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * A DTO which describes the samples search criteria: the value of a chosen property. We look for
 * samples in a specified group. If the experiment identifier is specified, the search is restricted
 * to samples in that experiment.
 * 
 * @author Tomasz Pylak
 */
public class ListSamplesByPropertyCriteria implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    private String propertyCode;

    private String propertyValue;

    private String groupCode;

    private LocalExperimentIdentifier experimentIdentifierOrNull;

    public ListSamplesByPropertyCriteria(String propertyCode, String propertyValue,
            String groupCode, LocalExperimentIdentifier experimentIdentifierOrNull)
    {
        this.propertyCode = propertyCode;
        this.propertyValue = propertyValue;
        this.groupCode = groupCode;
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

    public String getPropertyCode()
    {
        return propertyCode;
    }

    public String getPropertyValue()
    {
        return propertyValue;
    }

    public String getGroupCode()
    {
        return groupCode;
    }

    /** can be null */
    public ExperimentIdentifier getExperimentIdentifier()
    {
        if (experimentIdentifierOrNull == null)
        {
            return null;
        } else
        {
            ProjectIdentifier projectIdentifier =
                    new ProjectIdentifier(getGroupIdentifier(), experimentIdentifierOrNull
                            .getProjectCode());
            return new ExperimentIdentifier(projectIdentifier, experimentIdentifierOrNull
                    .getExperimentCode());
        }
    }

    public GroupIdentifier getGroupIdentifier()
    {
        return new GroupIdentifier((String) null, groupCode);
    }

    @Override
    public String toString()
    {
        String expDesc =
                (experimentIdentifierOrNull == null ? "any" : experimentIdentifierOrNull.toString());
        return String.format(
                "Samples from the group '%s' with property '%s' set to '%s' in %s experiment",
                groupCode, propertyCode, propertyValue, expDesc);
    }
}
