/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Implementation of {@link IEntityOperationChecker} which does nothing because checking is done by
 * aspects.
 * 
 * @author Franz-Josef Elmer
 */
public class EntityOperationChecker implements IEntityOperationChecker
{

    @Override
    public void assertSpaceCreationAllowed(IAuthSession session, List<NewSpace> newSpaces)
    {
    }

    @Override
    public void assertMaterialCreationAllowed(IAuthSession session,
            Map<String, List<NewMaterial>> materials)
    {
    }

    @Override
    public void assertProjectCreationAllowed(Session session, List<NewProject> newProjects)
    {
    }

    @Override
    public void assertExperimentCreationAllowed(Session session, List<NewExperiment> newExperiments)
    {
    }

}
