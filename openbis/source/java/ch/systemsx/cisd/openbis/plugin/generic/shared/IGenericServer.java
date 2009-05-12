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
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.ExperimentUpdatesPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.GroupIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewExperimentPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NewSamplePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.NullableGroupIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleOwnerIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Definition of the client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericServer extends IPluginCommonServer
{

    /**
     * For given {@link ExperimentIdentifier} returns the corresponding {@link ExperimentPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public ExperimentPE getExperimentInfo(
            String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) ExperimentIdentifier identifier);

    /**
     * For given {@link TechId} returns the corresponding {@link MaterialPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public MaterialPE getMaterialInfo(String sessionToken, TechId materialId);

    /**
     * For given <var>datasetCode</var> returns the corresponding {@link ExternalDataPE}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.OBSERVER)
    public ExternalDataPE getDataSetInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String datasetCode);

    /**
     * Returns attachment described by given experiment identifier, filename and version.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public AttachmentPE getExperimentFileAttachment(
            String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) ExperimentIdentifier experimentIdentifier,
            String filename, int version) throws UserFailureException;

    /**
     * Registers samples in batch.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    @DatabaseCreateOrDeleteModification(value = ObjectKind.SAMPLE)
    public void registerSamples(
            final String sessionToken,
            SampleType sampleType,
            @AuthorizationGuard(guardClass = NewSamplePredicate.class) final List<NewSample> newSamples)
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
            List<AttachmentPE> attachments);

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
    public AttachmentPE getSampleFileAttachment(
            String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class) SampleIdentifier sample,
            String fileName, int version);

    /**
     * Returns attachment described by given project identifier, filename and version.
     */
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public AttachmentPE getProjectFileAttachment(
            String sessionToken,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class) ProjectIdentifier project,
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
    public Date updateMaterial(String sessionToken, MaterialIdentifier identifier,
            List<MaterialProperty> properties, Date version);

    /**
     * Saves changed sample.
     */
    @Transactional
    @RolesAllowed(RoleSet.USER)
    @DatabaseUpdateModification(value = ObjectKind.SAMPLE)
    public Date updateSample(
            String sessionToken,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class) SampleIdentifier identifier,
            List<SampleProperty> properties,
            @AuthorizationGuard(guardClass = NullableGroupIdentifierPredicate.class) ExperimentIdentifier experimentIdentifierOrNull,
            List<AttachmentPE> attachments, Date version);

    /**
     * Saves changed data set.
     */
    @Transactional
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    @DatabaseUpdateModification(value = ObjectKind.DATA_SET)
    public Date updateDataSet(
            String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String code,
            @AuthorizationGuard(guardClass = SampleOwnerIdentifierPredicate.class) SampleIdentifier sampleIdentifier,
            List<DataSetProperty> properties, Date version);
}
