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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.common.collections.Modifiable;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentImageMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;

/**
 * @author pkupczyk
 */
public class ScreeningServerJson implements IScreeningApiServer
{

    private IScreeningApiServer server;

    public ScreeningServerJson(IScreeningApiServer server)
    {
        if (server == null)
        {
            throw new IllegalArgumentException("Server was null");
        }
        this.server = server;
    }

    public int getMajorVersion()
    {
        return server.getMajorVersion();
    }

    public int getMinorVersion()
    {
        return server.getMinorVersion();
    }

    public String tryLoginScreening(String userId, String userPassword)
            throws IllegalArgumentException
    {
        return server.tryLoginScreening(userId, userPassword);
    }

    public void logoutScreening(String sessionToken) throws IllegalArgumentException
    {
        server.logoutScreening(sessionToken);
    }

    public List<Plate> listPlates(String sessionToken) throws IllegalArgumentException
    {
        return new PlateList(server.listPlates(sessionToken));
    }

    public List<Plate> listPlates(String sessionToken, ExperimentIdentifier experiment)
            throws IllegalArgumentException
    {
        return new PlateList(server.listPlates(sessionToken, experiment));
    }

    public List<PlateMetadata> getPlateMetadataList(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        return new PlateMetadataList(server.getPlateMetadataList(sessionToken, plates));
    }

    public List<ExperimentIdentifier> listExperiments(String sessionToken)
    {
        return new ExperimentIdentifierList(server.listExperiments(sessionToken));
    }

    public List<ExperimentIdentifier> listExperiments(String sessionToken, String userId)
    {
        return new ExperimentIdentifierList(server.listExperiments(sessionToken, userId));
    }

    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        return new FeatureVectorDatasetReferenceList(server.listFeatureVectorDatasets(sessionToken,
                plates));
    }

    public List<ImageDatasetReference> listImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        return new ImageDatasetReferenceList(server.listImageDatasets(sessionToken, plates));
    }

    public List<ImageDatasetReference> listRawImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        return new ImageDatasetReferenceList(server.listRawImageDatasets(sessionToken, plates));
    }

    public List<ImageDatasetReference> listSegmentationImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates) throws IllegalArgumentException
    {
        return new ImageDatasetReferenceList(server.listSegmentationImageDatasets(sessionToken,
                plates));
    }

    public List<IDatasetIdentifier> getDatasetIdentifiers(String sessionToken,
            List<String> datasetCodes)
    {
        return new IDatasetIdentifierList(server.getDatasetIdentifiers(sessionToken, datasetCodes));
    }

    public List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            boolean findDatasets)
    {
        return new PlateWellReferenceWithDatasetsList(server.listPlateWells(sessionToken,
                experimentIdentifer, materialIdentifier, findDatasets));
    }

    public List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        return new PlateWellReferenceWithDatasetsList(server.listPlateWells(sessionToken,
                materialIdentifier, findDatasets));
    }

    public List<WellIdentifier> listPlateWells(String sessionToken, PlateIdentifier plateIdentifier)
    {
        return new WellIdentifierList(server.listPlateWells(sessionToken, plateIdentifier));
    }

    public Sample getWellSample(String sessionToken, WellIdentifier wellIdentifier)
    {
        return server.getWellSample(sessionToken, wellIdentifier);
    }

    public Sample getPlateSample(String sessionToken, PlateIdentifier plateIdentifier)
    {
        return server.getPlateSample(sessionToken, plateIdentifier);
    }

    public List<PlateWellMaterialMapping> listPlateMaterialMapping(String sessionToken,
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull)
    {
        return new PlateWellMaterialMappingList(server.listPlateMaterialMapping(sessionToken,
                plates, materialTypeIdentifierOrNull));
    }

    public ExperimentImageMetadata getExperimentImageMetadata(String sessionToken,
            ExperimentIdentifier experimentIdentifer)
    {
        return server.getExperimentImageMetadata(sessionToken, experimentIdentifer);
    }

    /*
     * The collections listed below have been created to help Jackson library embed/detect types of
     * the collection's items during JSON serialization/deserialization. (see
     * http://wiki.fasterxml.com/JacksonPolymorphicDeserialization#A5._Known_Issues)
     */

    private static class FeatureVectorDatasetReferenceList extends
            ArrayList<FeatureVectorDatasetReference> implements Modifiable
    {
        private static final long serialVersionUID = 1L;

        public FeatureVectorDatasetReferenceList(
                Collection<? extends FeatureVectorDatasetReference> c)
        {
            super(c);
        }
    }

    private static class PlateList extends ArrayList<Plate> implements Modifiable
    {
        private static final long serialVersionUID = 1L;

        public PlateList(Collection<? extends Plate> c)
        {
            super(c);
        }
    }

    private static class PlateMetadataList extends ArrayList<PlateMetadata> implements Modifiable
    {
        private static final long serialVersionUID = 1L;

        public PlateMetadataList(Collection<? extends PlateMetadata> c)
        {
            super(c);
        }
    }

    private static class ExperimentIdentifierList extends ArrayList<ExperimentIdentifier> implements
            Modifiable
    {
        private static final long serialVersionUID = 1L;

        public ExperimentIdentifierList(Collection<? extends ExperimentIdentifier> c)
        {
            super(c);
        }
    }

    private static class ImageDatasetReferenceList extends ArrayList<ImageDatasetReference>
            implements Modifiable
    {
        private static final long serialVersionUID = 1L;

        public ImageDatasetReferenceList(Collection<? extends ImageDatasetReference> c)
        {
            super(c);
        }
    }

    private static class IDatasetIdentifierList extends ArrayList<IDatasetIdentifier> implements
            Modifiable
    {
        private static final long serialVersionUID = 1L;

        public IDatasetIdentifierList(Collection<? extends IDatasetIdentifier> c)
        {
            super(c);
        }
    }

    private static class PlateWellReferenceWithDatasetsList extends
            ArrayList<PlateWellReferenceWithDatasets> implements Modifiable
    {
        private static final long serialVersionUID = 1L;

        public PlateWellReferenceWithDatasetsList(
                Collection<? extends PlateWellReferenceWithDatasets> c)
        {
            super(c);
        }
    }

    private static class WellIdentifierList extends ArrayList<WellIdentifier> implements Modifiable
    {
        private static final long serialVersionUID = 1L;

        public WellIdentifierList(Collection<? extends WellIdentifier> c)
        {
            super(c);
        }
    }

    private static class PlateWellMaterialMappingList extends ArrayList<PlateWellMaterialMapping>
            implements Modifiable
    {
        private static final long serialVersionUID = 1L;

        public PlateWellMaterialMappingList(Collection<? extends PlateWellMaterialMapping> c)
        {
            super(c);
        }
    }

}
