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

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewProjectPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Checking methods to be invoked to check authorization in context of
 * {@link ETLService#performEntityOperations(String, ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails)}
 * .
 * 
 * @author Franz-Josef Elmer
 */
public interface IEntityOperationChecker
{
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    public void assertSpaceCreationAllowed(IAuthSession session, List<NewSpace> newSpaces);

    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    public void assertMaterialCreationAllowed(IAuthSession session,
            Map<String, List<NewMaterial>> materials);

    @RolesAllowed(
        { RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_PROJECTS_VIA_DSS")
    public void assertProjectCreationAllowed(Session session,
            @AuthorizationGuard(guardClass = NewProjectPredicate.class)
            List<NewProject> newProjects);

    @RolesAllowed(
        { RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_PROJECTS_VIA_DSS")
    public void assertExperimentCreationAllowed(Session session,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class)
            List<NewExperiment> newExperiments);
}
