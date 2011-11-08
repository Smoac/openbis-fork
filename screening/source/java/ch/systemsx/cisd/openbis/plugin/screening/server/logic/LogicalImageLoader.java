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

import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetOverlayImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageChannelStack;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
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

        DataPE dataset = loadDatasetWithChildren(datasetCode);
        DatasetImagesReference datasetImagesReference =
                createDatasetImagesReference(translate(dataset), imageParameters);
        List<DatasetOverlayImagesReference> overlayDatasets = extractImageOverlays(dataset);
        return new ImageDatasetEnrichedReference(datasetImagesReference, overlayDatasets);
    }

    List<ImageDatasetEnrichedReference> loadImageDatasets(List<DataPE> datasets)
    {
        List<ImageDatasetEnrichedReference> refs = new ArrayList<ImageDatasetEnrichedReference>();
        List<DataPE> imageDatasets = ScreeningUtils.filterImageDatasets(datasets);
        for (DataPE imageDataset : imageDatasets)
        {
            DatasetImagesReference ref = loadImageDatasetReference(imageDataset);
            List<DatasetOverlayImagesReference> overlays = extractImageOverlays(imageDataset);
            ImageDatasetEnrichedReference enrichedRef =
                    new ImageDatasetEnrichedReference(ref, overlays);
            refs.add(enrichedRef);
        }
        return refs;
    }

    private List<DatasetOverlayImagesReference> extractImageOverlays(DataPE imageDataset)
    {
        List<ExternalData> overlayDatasets = fetchOverlayDatasets(imageDataset);

        List<DatasetOverlayImagesReference> overlays =
                new ArrayList<DatasetOverlayImagesReference>();
        for (ExternalData overlay : overlayDatasets)
        {
            overlays.add(loadOverlayDatasetReference(overlay));
        }
        return overlays;
    }

    private List<ExternalData> fetchOverlayDatasets(DataPE imageDataset)
    {
        List<DataPE> overlayPEs =
                ScreeningUtils.filterImageOverlayDatasets(imageDataset.getChildren());
        Collection<Long> datasetIds = extractIds(overlayPEs);
        return businessObjectFactory.createDatasetLister(session).listByDatasetIds(datasetIds);
    }

    private DatasetOverlayImagesReference loadOverlayDatasetReference(ExternalData overlay)
    {
        DatasetImagesReference imageDatasetReference = loadImageDatasetReference(overlay);
        String analysisProcedure = tryGetAnalysisProcedure(overlay);
        return DatasetOverlayImagesReference.create(imageDatasetReference.getDatasetReference(),
                imageDatasetReference.getImageParameters(), analysisProcedure);
    }

    private String tryGetAnalysisProcedure(ExternalData dataset)
    {
        return EntityHelper.tryFindPropertyValue(dataset, ScreeningConstants.ANALYSIS_PROCEDURE);
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

    DatasetImagesReference loadImageDatasetReference(DataPE imageDataset)
    {
        return loadImageDatasetReference(translate(imageDataset));
    }

    private DataPE loadDatasetWithChildren(String datasetPermId)
    {
        IDataBO dataBO = businessObjectFactory.createDataBO(session);
        dataBO.loadByCode(datasetPermId);
        dataBO.enrichWithChildren();
        DataPE dataSet = dataBO.getData();
        return dataSet;
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

    private ExternalData translate(DataPE dataSet)
    {
        return DataSetTranslator.translate(dataSet, session.getBaseIndexURL());
    }
}
