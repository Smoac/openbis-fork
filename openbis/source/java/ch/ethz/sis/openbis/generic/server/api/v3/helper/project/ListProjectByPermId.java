/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.helper.project;

import java.util.LinkedList;
import java.util.List;

import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
public class ListProjectByPermId implements IListObjectById<ProjectPermId, ProjectPE>
{

    private IProjectDAO projectDAO;

    public ListProjectByPermId(IProjectDAO projectDAO)
    {
        this.projectDAO = projectDAO;
    }

    @Override
    public Class<ProjectPermId> getIdClass()
    {
        return ProjectPermId.class;
    }

    @Override
    public ProjectPermId createId(ProjectPE project)
    {
        return new ProjectPermId(project.getPermId());
    }

    @Override
    public List<ProjectPE> listByIds(List<ProjectPermId> ids)
    {
        List<String> permIds = new LinkedList<String>();

        for (ProjectPermId id : ids)
        {
            permIds.add(id.getPermId());
        }

        return projectDAO.listByPermID(permIds);
    }

}
