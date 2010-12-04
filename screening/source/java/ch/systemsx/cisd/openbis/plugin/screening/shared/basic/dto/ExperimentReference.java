/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Points to an experiment.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentReference implements ISerializable, IEntityInformationHolderWithPermId
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private long experimentId;

    private String experimentPermId;

    private String experimentCode;

    private String experimentTypeCode;

    private String projectCode;

    private String spaceCode;

    // GWT only
    @SuppressWarnings("unused")
    private ExperimentReference()
    {
    }

    public ExperimentReference(long experimentId, String experimentPermId, String experimentCode,
            String experimentTypeCode, String projectCode, String spaceCode)
    {
        this.experimentId = experimentId;
        this.experimentPermId = experimentPermId;
        this.experimentCode = experimentCode;
        this.experimentTypeCode = experimentTypeCode;
        this.projectCode = projectCode;
        this.spaceCode = spaceCode;
    }

    public EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    public BasicEntityType getEntityType()
    {
        return new BasicEntityType(experimentTypeCode);
    }

    public Long getId()
    {
        return experimentId;
    }

    public String getCode()
    {
        return experimentCode;
    }

    public String getExperimentIdentifier()
    {
        String SEP = "/";
        return SEP + spaceCode + SEP + projectCode + SEP + experimentCode;
    }

    public String getPermId()
    {
        return experimentPermId;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public String getSpaceCode()
    {
        return spaceCode;
    }

    @Override
    public String toString()
    {
        return getExperimentIdentifier();
    }
}
