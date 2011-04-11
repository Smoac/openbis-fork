/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageDatasetLoader;

/**
 * @author Tomasz Pylak
 */
public class LogicalImageLoader
{
    /**
     * Loads information about the logical image in the chosen image dataset (restricted to one well
     * in HCS case).
     */
    public static LogicalImageInfo loadLogicalImageInfo(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, String datasetCode,
            String datastoreCode, WellLocation wellLocationOrNull)
    {
        return new LogicalImageLoader(session, businessObjectFactory).loadLogicalImageInfo(
                datasetCode, datastoreCode, wellLocationOrNull);
    }

    public static ImageDatasetEnrichedReference getImageDatasetReference(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, String datasetCode,
            String datastoreCode)
    {
        return new LogicalImageLoader(session, businessObjectFactory).getImageDatasetReference(
                datasetCode, datastoreCode);
    }

    private final Session session;

    private final IScreeningBusinessObjectFactory businessObjectFactory;

    public LogicalImageLoader(Session session, IScreeningBusinessObjectFactory businessObjectFactory)
    {
        this.session = session;
        this.businessObjectFactory = businessObjectFactory;
    }

    private ImageDatasetEnrichedReference getImageDatasetReference(String datasetCode,
            String datastoreCode)
    {
        IImageDatasetLoader datasetLoader =
                businessObjectFactory.createImageDatasetLoader(datasetCode, datastoreCode);
        return getImageDataset(datasetCode, datasetLoader);
    }

    LogicalImageInfo loadLogicalImageInfo(String datasetCode, String datastoreCode,
            WellLocation wellLocationOrNull)
    {
        IImageDatasetLoader datasetLoader =
                businessObjectFactory.createImageDatasetLoader(datasetCode, datastoreCode);
        List<ImageChannelStack> stacks = datasetLoader.listImageChannelStacks(wellLocationOrNull);
        ImageDatasetEnrichedReference imageDataset = getImageDataset(datasetCode, datasetLoader);
        return new LogicalImageInfo(imageDataset, stacks);
    }

    private ImageDatasetEnrichedReference getImageDataset(String datasetCode,
            IImageDatasetLoader datasetLoader)
    {
        ImageDatasetParameters imageParameters = datasetLoader.getImageParameters();

        ExternalDataPE dataset = loadDatasetWithChildren(datasetCode);
        DatasetImagesReference datasetImagesReference =
                createDatasetImagesReference(translate(dataset), imageParameters);
        List<DatasetImagesReference> overlayDatasets = extractImageOverlays(dataset);
        return new ImageDatasetEnrichedReference(datasetImagesReference, overlayDatasets);
    }

    List<ImageDatasetEnrichedReference> loadImageDatasets(List<ExternalDataPE> datasets)
    {
        List<ImageDatasetEnrichedReference> refs = new ArrayList<ImageDatasetEnrichedReference>();
        List<ExternalDataPE> imageDatasets = ScreeningUtils.filterImageDatasets(datasets);
        for (ExternalDataPE imageDataset : imageDatasets)
        {
            DatasetImagesReference ref = loadImageDatasetReference(imageDataset);
            List<DatasetImagesReference> overlays = extractImageOverlays(imageDataset);
            ImageDatasetEnrichedReference enrichedRef =
                    new ImageDatasetEnrichedReference(ref, overlays);
            refs.add(enrichedRef);
        }
        return refs;
    }

    private List<DatasetImagesReference> extractImageOverlays(ExternalDataPE imageDataset)
    {
        List<ExternalData> overlayDatasets = fetchOverlayDatasets(imageDataset);

        List<DatasetImagesReference> overlays = new ArrayList<DatasetImagesReference>();
        for (ExternalData overlay : overlayDatasets)
        {
            overlays.add(loadImageDatasetReference(overlay));
        }
        return overlays;
    }

    private List<ExternalData> fetchOverlayDatasets(ExternalDataPE imageDataset)
    {
        List<DataPE> overlayPEs =
                ScreeningUtils.filterImageOverlayDatasets(imageDataset.getChildren());
        Collection<Long> datasetIds = extractIds(overlayPEs);
        return businessObjectFactory.createDatasetLister(session).listByDatasetIds(datasetIds);
    }

    private DatasetImagesReference loadImageDatasetReference(ExternalData dataset)
    {
        ImageDatasetParameters imageParameters =
                ScreeningUtils.loadImageParameters(dataset, businessObjectFactory);
        return createDatasetImagesReference(dataset, imageParameters);
    }

    private DatasetImagesReference createDatasetImagesReference(ExternalData dataset,
            ImageDatasetParameters imageParameters)
    {
        return DatasetImagesReference.create(ScreeningUtils.createDatasetReference(dataset),
                imageParameters);
    }

    DatasetImagesReference loadImageDatasetReference(ExternalDataPE imageDataset)
    {
        return loadImageDatasetReference(translate(imageDataset));
    }

    private ExternalDataPE loadDatasetWithChildren(String datasetPermId)
    {
        IExternalDataBO externalDataBO = businessObjectFactory.createExternalDataBO(session);
        externalDataBO.loadByCode(datasetPermId);
        externalDataBO.enrichWithChildren();
        ExternalDataPE externalData = externalDataBO.getExternalData();
        return externalData;
    }

    private static Collection<Long> extractIds(List<DataPE> datasets)
    {
        List<Long> ids = new ArrayList<Long>();
        for (DataPE dataset : datasets)
        {
            ids.add(HibernateUtils.getId(dataset));
        }
        return ids;
    }

    private ExternalData translate(ExternalDataPE externalData)
    {
        return ExternalDataTranslator.translate(externalData, session.getBaseIndexURL());
    }
}
