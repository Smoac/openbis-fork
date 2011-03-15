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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.collections.GroupByMap;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IContentRepository;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingTransformerDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageEnrichedDTO;

/**
 * Abstract superclass for calculating image transformations for images of each spot.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractSpotImagesTransformerProcessingPlugin extends AbstractDatastorePlugin
        implements IProcessingPluginTask
{
    protected abstract IImageTransformerFactoryProvider getTransformationProvider(
            List<ImgImageEnrichedDTO> spotImages, IContentRepository contentRepository);

    protected static final IImageTransformerFactoryProvider NO_TRANSFORMATION_PROVIDER =
            new IImageTransformerFactoryProvider()
                {
                    public IImageTransformerFactory tryGetTransformationFactory(
                            ImgImageEnrichedDTO image)
                    {
                        return null;
                    }
                };

    private static final long serialVersionUID = 1L;

    private static final String CHANNEL_CODE_PROPERTY = "channel";

    private final static IImagingReadonlyQueryDAO query = DssScreeningUtils.getQuery();

    private final String channelCode;

    public AbstractSpotImagesTransformerProcessingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        this.channelCode = PropertyUtils.getMandatoryProperty(properties, CHANNEL_CODE_PROPERTY);
    }

    public ProcessingStatus process(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        IImagingTransformerDAO transformerDAO = DssScreeningUtils.createImagingTransformerDAO();
        try
        {
            ProcessingStatus processingStatus = new ProcessingStatus();
            for (DatasetDescription dataset : datasets)
            {
                IContentRepository contentRepository = createContentRepository(context, dataset);
                GroupByMap<Long, ImgImageEnrichedDTO> imagesBySpot = fetchImages(dataset);
                for (Long spotId : imagesBySpot.getKeys())
                {
                    List<ImgImageEnrichedDTO> spotImages = imagesBySpot.tryGet(spotId);
                    calculateAndSetImageTransformation(spotImages, contentRepository,
                            transformerDAO);
                }
                transformerDAO.commit();
                processingStatus.addDatasetStatus(dataset, Status.OK);
            }
            return processingStatus;
        } finally
        {
            transformerDAO.close(true);
        }
    }

    interface IImageTransformerFactoryProvider
    {
        IImageTransformerFactory tryGetTransformationFactory(ImgImageEnrichedDTO image);
    }

    private void calculateAndSetImageTransformation(List<ImgImageEnrichedDTO> spotImages,
            IContentRepository contentRepository, IImagingTransformerDAO transformerDAO)
    {
        long start = System.currentTimeMillis();
        IImageTransformerFactoryProvider transformerFactoryProvider =
                getTransformationProvider(spotImages, contentRepository);
        for (ImgImageEnrichedDTO image : spotImages)
        {
            IImageTransformerFactory transformationFactory =
                    transformerFactoryProvider.tryGetTransformationFactory(image);
            transformerDAO.saveTransformerFactoryForImage(image.getAcquiredImageId(),
                    transformationFactory);
        }
        operationLog.info(String.format("Processed %d images of the spot in %d msec.",
                spotImages.size(), (System.currentTimeMillis() - start)));
    }

    private IContentRepository createContentRepository(DataSetProcessingContext context,
            DatasetDescription dataset)
    {
        File dataSetDirectory = context.getDirectoryProvider().getDataSetDirectory(dataset);
        IContentRepository contentRepository =
                HCSImageDatasetLoaderFactory.createContentRepository(dataSetDirectory);
        return contentRepository;
    }

    private GroupByMap<Long, ImgImageEnrichedDTO> fetchImages(DatasetDescription dataset)
    {
        List<ImgImageEnrichedDTO> allImages =
                query.listHCSImages(dataset.getDatasetCode(), channelCode);
        GroupByMap<Long, ImgImageEnrichedDTO> imagesBySpot =
                GroupByMap.create(allImages, new IKeyExtractor<Long, ImgImageEnrichedDTO>()
                    {
                        public Long getKey(ImgImageEnrichedDTO image)
                        {
                            return image.getSpotId();
                        }
                    });
        if (allImages.size() == 0)
        {
            operationLog
                    .warn(String
                            .format("Dataset %s has no images for channel '%s' to process! Have you specified the correct channel code?",
                                    dataset.getDatasetCode(), channelCode));
        } else
        {
            operationLog
                    .info(String
                            .format("Dataset %s has %d images (devided between %d spots) for channel '%s' to process.",
                                    dataset.getDatasetCode(), allImages.size(), imagesBySpot
                                            .getKeys().size(), channelCode));
        }
        return imagesBySpot;
    }
}
