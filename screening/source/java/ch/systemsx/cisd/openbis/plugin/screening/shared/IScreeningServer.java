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

package ch.systemsx.cisd.openbis.plugin.screening.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.DataSetTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodePredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.authorization.DatasetReferencePredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.authorization.WellContentValidator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.authorization.WellSearchCriteriaPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummaryResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReplicaImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;

/**
 * The <i>screening</i> server. Used internally.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningServer extends IServer
{
    /**
     * Loads data about the plate for a specified sample id. Attaches information about images and
     * image analysis only if one dataset with such a data exist.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public PlateContent getPlateContent(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId plateId);

    /**
     * Loads feature vector of specified dataset with one feature specified by name.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public FeatureVectorDataset getFeatureVectorDataset(
            String sessionToken,
            @AuthorizationGuard(guardClass = DatasetReferencePredicate.class) DatasetReference dataset,
            CodeAndLabel featureName);

    /**
     * Loads all feature vector values for specified well.
     */
    // TODO can return null
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public FeatureVectorValues getWellFeatureVectorValues(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String datasetCode,
            String datastoreCode, WellLocation wellLocation);

    /**
     * Returns plate content for a specified HCS_IMAGE dataset. Loads data about the plate for a
     * specified dataset, which is supposed to contain images in BDS-HCS format.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public PlateImages getPlateContentForDataset(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId datasetId);

    /**
     * Finds wells matching the specified criteria. Loads wells content: metadata and (if available)
     * image dataset and feature vectors.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @ReturnValueFilter(validatorClass = WellContentValidator.class)
    public List<WellContent> listPlateWells(
            String sessionToken,
            @AuthorizationGuard(guardClass = WellSearchCriteriaPredicate.class) WellSearchCriteria materialCriteria);

    /**
     * Finds wells containing the specified material and belonging to the specified experiment.
     * Loads wells metadata and single image dataset for each well. If there are many image datasets
     * for the well, all but the first one are ignored. If there is no image dataset for the well,
     * the whole well is ignored.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<WellReplicaImage> listWellImages(String sessionToken, TechId materialId,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId);

    /**
     * @return materials with codes or properties matching to the query. If the experiment is
     *         specified, only materials inside well locations connected through the plate to this
     *         specified experiment(s) will be returned.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<Material> listMaterials(
            String sessionToken,
            @AuthorizationGuard(guardClass = WellSearchCriteriaPredicate.class) WellSearchCriteria materialCriteria);

    /**
     * Loads all materials of specified type connected with the specified experiment.
     * 
     * @param materialType
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<Material> listExperimentMaterials(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId,
            MaterialType materialType);

    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public LogicalImageInfo getImageDatasetInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String datasetCode,
            String datastoreCode, WellLocation wellLocationOrNull);

    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public ImageDatasetEnrichedReference getImageDatasetReference(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetCodePredicate.class) String datasetCode,
            String datastoreCode);

    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public ImageSampleContent getImageDatasetInfosForSample(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId sampleId,
            WellLocation wellLocationOrNull);

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
     * For given {@link TechId} returns the corresponding {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public ExternalData getDataSetInfo(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId datasetId);

    /**
     * For given {@link TechId} returns the corresponding {@link Material}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public Material getMaterialInfo(String sessionToken, TechId materialId);

    /**
     * Returns vocabulary with given code.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public Vocabulary getVocabulary(String sessionToken, String code) throws UserFailureException;

    /**
     * registers the contents of an uploaded library.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_ADMIN)
    public void registerLibrary(String sessionToken, String userEmail,
            List<NewMaterial> newGenesOrNull, List<NewMaterial> newOligosOrNull,
            List<NewSamplesWithTypes> newSamplesWithType);

    /**
     * Returns aggregated feature vectors with their rankings in the specified experiment for all
     * materials.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public ExperimentFeatureVectorSummary getExperimentFeatureVectorSummary(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId);

    /**
     * Returns a feature vector summary (with details for each replica) for the given experiment and
     * material.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public MaterialReplicaFeatureSummaryResult getMaterialFeatureVectorSummary(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId,
            TechId materialId);
}
