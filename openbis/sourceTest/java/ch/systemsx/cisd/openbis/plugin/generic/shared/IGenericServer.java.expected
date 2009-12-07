/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.shared;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseCreateOrDeleteModification;
import ch.systemsx.cisd.openbis.generic.shared.DatabaseUpdateModification;
import ch.systemsx.cisd.openbis.generic.shared.IPluginCommonServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ExperimentUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.GroupIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewSamplesWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.DataSetTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ProjectTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * Definition of the client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericServer extends IPluginCommonServer
{

    /**
     * For given {@link ExperimentIdentifier} returns the corresponding {@link Experiment}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public Experiment getExperimentInfo(
            String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) ExperimentIdentifier identifier);

    /**
     * For given {@link TechId} returns the corresponding {@link Experiment}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public Experiment getExperimentInfo(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId);

    /**
     * For given {@link TechId} returns the corresponding {@link Material}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public Material getMaterialInfo(String sessionToken, TechId materialId);

    /**
     * For given {@link TechId} returns the corresponding {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public ExternalData getDataSetInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId datasetId);

    /**
     * Returns attachment described by given experiment identifier, filename and version.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public AttachmentWithContent getExperimentFileAttachment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId,
            String filename, int version) throws UserFailureException;

    /**
     * Registers samples of different types in batches.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public void registerSamples(
            final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class) final List<NewSamplesWithTypes> newSamplesWithType)
            throws UserFailureException;

    /**
     * Updates samples of different types in batches.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSamples(
            final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class) final List<NewSamplesWithTypes> newSamplesWithType)
            throws UserFailureException;

    /**
     * Registers experiment.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.EXPERIMENT)
    public void registerExperiment(
            String sessionToken,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class) final NewExperiment experiment,
            final Collection<NewAttachment> attachments);

    /**
     * Registers materials in batch.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public void registerMaterials(String sessionToken, String materialTypeCode,
            List<NewMaterial> newMaterials);

    /**
     * Returns attachment described by given sample identifier, filename and version.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public AttachmentWithContent getSampleFileAttachment(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId sampleId,
            String fileName, int version);

    /**
     * Returns attachment described by given project identifier, filename and version.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public AttachmentWithContent getProjectFileAttachment(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId,
            String fileName, int version);

    /**
     * Returns a list of unique codes.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public List<String> generateCodes(String sessionToken, String prefix, int number);

    /**
     * Saves changed experiment.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    @DatabaseUpdateModification(value =
        { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE })
    public ExperimentUpdateResult updateExperiment(
            String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentUpdatesPredicate.class) ExperimentUpdatesDTO updates);

    /**
     * Saves changed material.
     */
    @Transactional
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.MATERIAL)
    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, Date version);

    /**
     * Saves changed sample.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public Date updateSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class) SampleUpdatesDTO updates);

    /**
     * Saves changed data set.
     */
    @Transactional
    @RolesAllowed(RoleSet.POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public DataSetUpdateResult updateDataSet(
            String sessionToken,
            @AuthorizationGuard(guardClass = DataSetUpdatesPredicate.class) DataSetUpdatesDTO updates);
}
