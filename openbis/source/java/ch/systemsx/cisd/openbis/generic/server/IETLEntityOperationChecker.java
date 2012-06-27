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
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetUpdatesCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewExternalDataPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewProjectPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleUpdatesCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;

/**
 * Checking methods to be invoked to check authorization in context of
 * {@link ETLService#performEntityOperations(String, ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails)}
 * .
 * 
 * @author Franz-Josef Elmer
 */
public interface IETLEntityOperationChecker
{
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    public void assertSpaceCreationAllowed(IAuthSession session, List<NewSpace> newSpaces);

    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    public void assertMaterialCreationAllowed(IAuthSession session,
            Map<String, List<NewMaterial>> materials);

    @RolesAllowed(
        { RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_PROJECTS_VIA_DSS")
    public void assertProjectCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewProjectPredicate.class)
            List<NewProject> newProjects);

    @RolesAllowed(
        { RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_EXPERIMENTS_VIA_DSS")
    public void assertExperimentCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class)
            List<NewExperiment> newExperiments);

    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    public void assertInstanceSampleCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            List<NewSample> instanceSamples);

    @RolesAllowed(
        { RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_SPACE_SAMPLES_VIA_DSS")
    public void assertSpaceSampleCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class)
            List<NewSample> spaceSamples);

    @RolesAllowed(RoleWithHierarchy.INSTANCE_ETL_SERVER)
    public void assertInstanceSampleUpdateAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleUpdatesCollectionPredicate.class)
            List<SampleUpdatesDTO> instanceSamples);

    @RolesAllowed(
        { RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_SPACE_SAMPLES_VIA_DSS")
    public void assertSpaceSampleUpdateAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleUpdatesCollectionPredicate.class)
            List<SampleUpdatesDTO> spaceSamples);

    @RolesAllowed(
        { RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_DATA_SETS_VIA_DSS")
    public void assertDataSetCreationAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = NewExternalDataPredicate.class)
            List<? extends NewExternalData> dataSets);

    @RolesAllowed(
        { RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("UPDATE_DATA_SETS_VIA_DSS")
    public void assertDataSetUpdateAllowed(IAuthSession session,
            @AuthorizationGuard(guardClass = DataSetUpdatesCollectionPredicate.class)
            List<DataSetBatchUpdatesDTO> dataSets);
}
