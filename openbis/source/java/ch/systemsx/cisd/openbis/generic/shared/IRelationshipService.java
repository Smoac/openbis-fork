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

package ch.systemsx.cisd.openbis.generic.shared;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ExperimentPEPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ProjectPEPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SpacePEPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Definition of an internal service through which entity relationships can be changed and deleted.
 * All the methods of this service require authorization,
 * 
 * @author anttil
 */
public interface IRelationshipService
{
    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("ASSIGN_EXPERIMENT_TO_PROJECT")
    public void assignExperimentToProject(IAuthSession session,
            @AuthorizationGuard(guardClass = ExperimentPEPredicate.class)
            ExperimentPE experiment,
            @AuthorizationGuard(guardClass = ProjectPEPredicate.class)
            ProjectPE project);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("ASSIGN_PROJECT_TO_SPACE")
    public void assignProjectToSpace(IAuthSession session,
            @AuthorizationGuard(guardClass = ProjectPEPredicate.class)
            ProjectPE project,
            @AuthorizationGuard(guardClass = SpacePEPredicate.class)
            SpacePE space);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("ASSIGN_SAMPLE_TO_EXPERIMENT")
    public void assignSampleToExperiment(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier sampleId,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            ExperimentIdentifier experimentid);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("UNASSIGN_SAMPLE_FROM_EXPERIMENT")
    public void unassignSampleFromExperiment(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier sample);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.INSTANCE_ETL_SERVER, RoleWithHierarchy.INSTANCE_ADMIN })
    @Capability("UNSHARE_SAMPLE")
    public void unshareSample(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier sample,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            SpaceIdentifier space);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("ASSIGN_SAMPLE_TO_SPACE")
    public void assignSampleToSpace(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier sample,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            SpaceIdentifier space);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.INSTANCE_ETL_SERVER, RoleWithHierarchy.INSTANCE_ADMIN })
    @Capability("SHARE_SAMPLE")
    public void shareSample(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier sample);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("ASSIGN_DATASET_TO_EXPERIMENT")
    public void assignDataSetToExperiment(IAuthSession session,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            String dataSetCode,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class)
            ExperimentIdentifier experiment);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("ASSIGN_DATASET_TO_SAMPLE")
    public void assignDataSetToSample(IAuthSession session,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class)
            String dataSetCode,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier sample);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("ADD_PARENT_TO_SAMPLE")
    public void addParentToSample(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier sample,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier parent);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("REMOVE_PARENT_FROM_SAMPLE")
    public void removeParentFromSample(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier sample,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier parent);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("ADD_CONTAINER_TO_SAMPLE")
    public void assignSampleToContainer(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier sampleId,
            SamplePE sample,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier containerId,
            SamplePE container);

    @Transactional(propagation = Propagation.MANDATORY)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_ETL_SERVER, RoleWithHierarchy.SPACE_POWER_USER })
    @Capability("REMOVE_CONTAINER_FROM_SAMPLE")
    public void removeSampleFromContainer(IAuthSession session,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class)
            SampleIdentifier sampleId,
            SamplePE sample);

}
