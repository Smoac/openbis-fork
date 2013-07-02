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

package ch.systemsx.cisd.openbis.clc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.clcbio.api.base.persistence.model.PersistenceContainer;
import com.clcbio.api.base.persistence.model.PersistenceModel;
import com.clcbio.api.base.persistence.model.PersistenceStructure;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;

/**
 * @author anttil
 */
public class SpaceProvider implements ContentProvider
{

    private IOpenbisServiceFacade openbis;

    public SpaceProvider(IOpenbisServiceFacade openbis)
    {
        this.openbis = openbis;
    }

    @Override
    public Collection<PersistenceStructure> getContent(PersistenceContainer parent, PersistenceModel model)
    {
        List<PersistenceStructure> spaces = new ArrayList<PersistenceStructure>();
        for (SpaceWithProjectsAndRoleAssignments a : openbis.getSpacesWithProjects())
        {
            List<String> projects = new ArrayList<String>();
            for (Project project : a.getProjects())
            {
                projects.add(project.getCode());
            }
            spaces.add(new Folder(a.getCode(), parent, model, new ProjectProvider(openbis, a.getCode(), projects)));
        }
        return spaces;
    }
}
