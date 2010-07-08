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
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SampleTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.SpaceIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.DataSetTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.authorization.PlateMaterialsSearchCriteriaPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * The <i>screening</i> server. Used internally.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningServer extends IServer
{
    /**
     * loads data about the plate for a specified sample id. Attaches information about images and
     * image analysis only if one dataset with such a data exist.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public PlateContent getPlateContent(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId plateId);

    /**
     * Returns plate content for a specified HCS_IMAGE dataset. Loads data about the plate for a
     * specified dataset, which is supposed to contain images in BDS-HCS format.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public PlateImages getPlateContentForDataset(String sessionToken,
            @AuthorizationGuard(guardClass = DataSetTechIdPredicate.class) TechId datasetId);

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<WellContent> getPlateLocations(
            String sessionToken,
            TechId geneMaterialId,
            @AuthorizationGuard(guardClass = SpaceIdentifierPredicate.class) ExperimentIdentifier experimentIdentifier);

    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<WellContent> listPlateLocations(
            String sessionToken,
            @AuthorizationGuard(guardClass = PlateMaterialsSearchCriteriaPredicate.class) PlateMaterialsSearchCriteria materialCriteria);

    /**
     * Loads all analysis results from all existing image-analysis datasets connected with the
     * specified experiment. It is assumed that all datasets are CSV files with the same headers.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public TableModel loadImageAnalysisForExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId);

    /**
     * Loads all analysis results from all existing image-analysis datasets connected with the
     * specified plate. It is assumed that all datasets are CSV files with the same headers.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public TableModel loadImageAnalysisForPlate(String sessionToken,
            @AuthorizationGuard(guardClass = SampleTechIdPredicate.class) TechId plateId);

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
     * Returns vocabulary with given code.
     */
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public Vocabulary getVocabulary(String sessionToken, String code) throws UserFailureException;
}
