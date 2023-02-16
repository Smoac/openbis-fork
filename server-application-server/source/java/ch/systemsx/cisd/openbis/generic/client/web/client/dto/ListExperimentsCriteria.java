/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * Criteria for listing <i>experiments</i>.
 * 
 * @author Izabela Adamczyk
 */
public final class ListExperimentsCriteria extends
        DefaultResultSetConfig<String, TableModelRowWithObject<Experiment>> implements
        IsSerializable
{
    private ExperimentType experimentType;

    // one of the following two should be filled

    private Project projectOrNull;

    private Space spaceOrNull;

    private TechId metaprojectId;

    public ExperimentType getExperimentType()
    {
        return experimentType;
    }

    public void setExperimentType(final ExperimentType experimentType)
    {
        this.experimentType = experimentType;
    }

    // always specified
    public String getSpaceCode()
    {
        assert spaceOrNull != null || projectOrNull != null;
        return spaceOrNull != null ? spaceOrNull.getCode() : projectOrNull.getSpace().getCode();
    }

    public String tryGetProjectCode()
    {
        return projectOrNull != null ? projectOrNull.getCode() : null;
    }

    public Project tryGetProject()
    {
        return projectOrNull;
    }

    public void setProject(Project project)
    {
        this.projectOrNull = project;
    }

    public Space tryGetSpace()
    {
        return spaceOrNull;
    }

    public void setSpace(Space space)
    {
        this.spaceOrNull = space;
    }

    public TechId tryGetMetaprojectId()
    {
        return metaprojectId;
    }

    public void setMetaprojectId(TechId metaprojectId)
    {
        this.metaprojectId = metaprojectId;
    }

}
