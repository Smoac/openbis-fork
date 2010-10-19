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
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ExperimentUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewDataSetsWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewSamplesWithTypePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.DataSetTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ProjectTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;

/**
 * Definition of the client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericServer extends IServer
{
    /**
     * For given {@link TechId} returns the {@link Sample} and its derived (child) samples.
     * 
     * @return never <code>null</code>.
     * @throws UserFailureException if given <var>sessionToken</var> is invalid or whether sample
     *             uniquely identified by given <var>sampleId</var> does not exist.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public SampleParentWithDerived getSampleInfo(final String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) final TechId sampleId)
            throws UserFailureException;

    /**
     * Registers a new sample.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public void registerSample(final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class) final NewSample newSample,
            final Collection<NewAttachment> attachments);

    /**
     * For given {@link TechId} returns the corresponding {@link Material}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public Material getMaterialInfo(String sessionToken, TechId materialId);

    /**
     * For given {@link TechId} returns the corresponding {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public ExternalData getDataSetInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId datasetId);

    /**
     * Returns attachment described by given experiment identifier, filename and version.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public AttachmentWithContent getExperimentFileAttachment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId,
            String filename, int version) throws UserFailureException;

    /**
     * Registers samples of different types in batches.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public void registerSamples(
            final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class) final List<NewSamplesWithTypes> newSamplesWithType)
            throws UserFailureException;

    /**
     * Registers or updates samples of different types in batches.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public void registerOrUpdateSamples(
            final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class) final List<NewSamplesWithTypes> newSamplesWithType)
            throws UserFailureException;

    /**
     * Updates samples of different types in batches.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void updateSamples(
            final String sessionToken,
            @AuthorizationGuard(guardClass = NewSamplesWithTypePredicate.class) final List<NewSamplesWithTypes> newSamplesWithType)
            throws UserFailureException;

    /**
     * Registers experiment. At the same time samples may be registered or updated.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @DatabaseCreateOrDeleteModification(value =
        { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE })
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public void registerExperiment(
            String sessionToken,
            @AuthorizationGuard(guardClass = NewExperimentPredicate.class) final NewExperiment experiment,
            final Collection<NewAttachment> attachments) throws UserFailureException;

    /**
     * Registers materials in batch.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public void registerMaterials(String sessionToken, String materialTypeCode,
            List<NewMaterial> newMaterials) throws UserFailureException;

    /**
     * Updates materials in batch.
     * 
     * @return number of actually updated materials
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public int updateMaterials(String sessionToken, String materialTypeCode, List<NewMaterial> newMaterials,
            boolean ignoreUnregisteredMaterials) throws UserFailureException;
    
    /**
     * Registers new materials or if they exist updates in batch their properties (properties which
     * are not mentioned stay unchanged).
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.MATERIAL)
    public void registerOrUpdateMaterials(String sessionToken, String materialTypeCode,
            List<NewMaterial> newMaterials) throws UserFailureException;

    /**
     * Returns attachment described by given sample identifier, filename and version.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public AttachmentWithContent getSampleFileAttachment(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId sampleId,
            String fileName, int version);

    /**
     * Returns attachment described by given project identifier, filename and version.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public AttachmentWithContent getProjectFileAttachment(String sessionToken,
            @AuthorizationGuard(guardClass = ProjectTechIdPredicate.class) TechId projectId,
            String fileName, int version);

    /**
     * Returns a list of unique codes.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<String> generateCodes(String sessionToken, String prefix, int number);

    /**
     * Saves changed experiment.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @DatabaseUpdateModification(value =
        { ObjectKind.EXPERIMENT, ObjectKind.SAMPLE })
    public ExperimentUpdateResult updateExperiment(
            String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentUpdatesPredicate.class) ExperimentUpdatesDTO updates);

    /**
     * Saves changed material.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.MATERIAL)
    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, Date version);

    /**
     * Saves changed sample.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public SampleUpdateResult updateSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleUpdatesPredicate.class) SampleUpdatesDTO updates);

    /**
     * Saves changed data set.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public DataSetUpdateResult updateDataSet(
            String sessionToken,
            @AuthorizationGuard(guardClass = DataSetUpdatesPredicate.class) DataSetUpdatesDTO updates);

    /**
     * Updates data sets of different types in batches.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_POWER_USER)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public void updateDataSets(
            final String sessionToken,
            @AuthorizationGuard(guardClass = NewDataSetsWithTypePredicate.class) final NewDataSetsWithTypes newSamplesWithType)
            throws UserFailureException;

}
