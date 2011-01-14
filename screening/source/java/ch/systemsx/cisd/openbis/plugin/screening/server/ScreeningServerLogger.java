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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;

/**
 * The <i>screening</i> specific {@link AbstractServerLogger} extension.
 * 
 * @author Tomasz Pylak
 */
final class ScreeningServerLogger extends AbstractServerLogger implements IScreeningServer,
        IScreeningApiServer
{
    ScreeningServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        logAccess(sessionToken, "getSampleInfo", "ID(%s)", sampleId);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample,
            final Collection<NewAttachment> attachments)
    {
        logTracking(sessionToken, "registerSample", "SAMPLE_TYPE(%s) SAMPLE(%s) ATTACHMENTS(%s)",
                newSample.getSampleType(), newSample.getIdentifier(), attachments.size());
    }

    public PlateContent getPlateContent(String sessionToken, TechId plateId)
    {
        logAccess(sessionToken, "getPlateContent", "PLATE(%s)", plateId.getId());
        return null;
    }

    public PlateImages getPlateContentForDataset(String sessionToken, TechId datasetId)
    {
        logAccess(sessionToken, "getPlateContentForDataset", "DATASET(%s)", datasetId.getId());
        return null;
    }

    public List<WellContent> listPlateWells(String sessionToken, WellSearchCriteria materialCriteria)
    {
        logAccess(sessionToken, "getPlateLocations", "criteria(%s)", materialCriteria);
        return null;
    }

    public LogicalImageInfo getImageDatasetInfo(String sessionToken, String datasetCode,
            String datastoreCode, WellLocation wellLocationOrNull)
    {
        logAccess(sessionToken, "getImageDatasetInfo", "dataset(%s) well(%s)", datasetCode,
                wellLocationOrNull);
        return null;
    }

    public ImageDatasetEnrichedReference getImageDatasetReference(String sessionToken,
            String datasetCode, String datastoreCode)
    {
        logAccess(sessionToken, "getImageDatasetReference", "dataset(%s) datastore(%s)",
                datasetCode, datastoreCode);
        return null;
    }

    public ImageSampleContent getImageDatasetInfosForSample(String sessionToken, TechId sampleId,
            WellLocation wellLocationOrNull)
    {
        logAccess(sessionToken, "getImageDatasetInfosForSample", "sample(%s) well(%s)", sampleId,
                wellLocationOrNull);
        return null;
    }

    public ExternalData getDataSetInfo(String sessionToken, TechId datasetId)
    {
        logAccess(sessionToken, "getDataSetInfo", "datasetId(%s)", datasetId.getId());
        return null;
    }

    public TableModel loadImageAnalysisForExperiment(String sessionToken, TechId experimentId)
    {
        logAccess(sessionToken, "loadImageAnalysisForExperiment", "EXPERIMENT(%s)",
                experimentId.getId());
        return null;
    }

    public TableModel loadImageAnalysisForPlate(String sessionToken, TechId plateId)
    {
        logAccess(sessionToken, "loadImageAnalysisForPlate", "PLATE(%s)", plateId.getId());
        return null;
    }

    public Vocabulary getVocabulary(String sessionToken, String code) throws UserFailureException
    {
        logAccess(sessionToken, "getVocabulary", "CODE(%s)", code);
        return null;
    }

    // --- IScreeningApiServer

    public void logoutScreening(String sessionToken)
    {
        // No logging because already done by the session manager
    }

    public String tryLoginScreening(String userId, String userPassword)
    {
        // No logging because already done by the session manager
        return null;
    }

    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates)
    {
        logAccess(sessionToken, "listFeatureVectorDatasets", "#plates: %s", plates.size());
        return null;
    }

    public List<ImageDatasetReference> listImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates)
    {
        logAccess(sessionToken, "listImageDatasets", "#plates: %s", plates.size());
        return null;
    }

    public List<PlateWellReferenceWithDatasets> listPlateWells(
            String sessionToken,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier experimentIdentifer,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        logAccess(sessionToken, "listPlateWells", "experiment: %s, material: %s",
                experimentIdentifer, materialIdentifier);
        return null;
    }

    public List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        logAccess(sessionToken, "listPlateWells", "material: %s", materialIdentifier);
        return null;
    }

    public List<WellIdentifier> listPlateWells(String sessionToken, PlateIdentifier plateIdentifier)
    {
        logAccess(sessionToken, "listPlateWells", "plate: %s", plateIdentifier);
        return null;
    }

    public Sample getWellSample(String sessionToken, WellIdentifier wellIdentifier)
    {
        logAccess(sessionToken, "getWellSample", "%s", wellIdentifier);
        return null;
    }

    public List<Plate> listPlates(String sessionToken)
    {
        logAccess(sessionToken, "listPlates");
        return null;
    }

    public List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier> listExperiments(
            String sessionToken)
    {
        logAccess(sessionToken, "listExperiments");
        return null;
    }

    public List<IDatasetIdentifier> getDatasetIdentifiers(String sessionToken,
            List<String> datasetCodes)
    {
        logAccess(sessionToken, "getDatasetIdentifiers", "datasets(%s)", datasetCodes);
        return null;
    }

    public List<PlateWellMaterialMapping> listPlateMaterialMapping(String sessionToken,
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull)
    {
        if (materialTypeIdentifierOrNull != null)
        {
            logAccess(sessionToken, "listPlateMaterialMapping", "plates(%s), materialType(%s)",
                    plates, materialTypeIdentifierOrNull);
        } else
        {
            logAccess(sessionToken, "listPlateMaterialMapping", "plates(%s)", plates);
        }
        return null;
    }

    public List<Material> listExperimentMaterials(String sessionToken, TechId experimentId,
            MaterialType materialType)
    {
        return null;
    }

    public int getMajorVersion()
    {
        return MAJOR_VERSION;
    }

    public int getMinorVersion()
    {
        return ScreeningServer.MINOR_VERSION;
    }

    public void registerLibrary(String sessionToken, String userEmail,
            List<NewMaterial> newGenesOrNull, List<NewMaterial> newOligosOrNull,
            List<NewSamplesWithTypes> newSamplesWithType)
    {
        logAccess(sessionToken, "registerLibrary",
                "userEmail(%s), newGenesOrNull(%s), newOligosOrNull(%s), newSamplesWithType(%s)",
                userEmail, newGenesOrNull, newOligosOrNull, newSamplesWithType);
    }
}
