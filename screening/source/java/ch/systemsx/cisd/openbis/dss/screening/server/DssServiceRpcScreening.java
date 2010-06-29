/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.screening.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.common.io.IContent;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IHCSImageDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.FeatureTableBuilder;
import ch.systemsx.cisd.openbis.dss.generic.server.FeatureTableRow;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;

/**
 * Implementation of the screening API interface using RPC. The instance will be created in spring
 * and published as a HTTP invoker servlet configured in service.properties.
 * 
 * @author Tomasz Pylak
 */
public class DssServiceRpcScreening extends AbstractDssServiceRpc implements
        IDssServiceRpcScreening
{

    private IImagingQueryDAO dao;

    public DssServiceRpcScreening(String storeRootDir)
    {
        this(storeRootDir, null, ServiceProvider.getOpenBISService());
    }

    DssServiceRpcScreening(String storeRootDir, IImagingQueryDAO dao,
            IEncapsulatedOpenBISService service)
    {
        super(service);
        this.dao = dao;
        setStoreDirectory(new File(storeRootDir));

        // Register the service with the name server
        RpcServiceInterfaceVersionDTO ifaceVersion =
                new RpcServiceInterfaceVersionDTO("screening-dss",
                        "/rmi-datastore-server-screening-api-v1", getMajorVersion(),
                        getMinorVersion());
        HttpInvokerServiceExporter nameServiceExporter =
                ServiceProvider.getRpcNameServiceExporter();
        RpcServiceNameServer nameServer = (RpcServiceNameServer) nameServiceExporter.getService();
        nameServer.addSupportedInterfaceVersion(ifaceVersion);

        operationLog.info("[rpc] Started DSS RPC screening service V1.");
    }
    
    // ------------------ impl -----------------

    public List<String> listAvailableFeatureNames(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        assertDataSetsAreAccessible(sessionToken, featureDatasets);
        List<String> result = new ArrayList<String>(); // keep the order
        for (IFeatureVectorDatasetIdentifier identifier : featureDatasets)
        {
            // add only new feature names
            List<ImgFeatureDefDTO> featureDefinitions = getFeatureDefinitions(identifier);
            for (ImgFeatureDefDTO featureDefinition : featureDefinitions)
            {
                String featureName = featureDefinition.getName();
                if (result.contains(featureName) == false)
                {
                    result.add(featureName);
                }
            }
        }
        return result;
    }

    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        ArrayList<String> datasetCodes = new ArrayList<String>();
        for (IImageDatasetIdentifier dataset : imageDatasets)
        {
            datasetCodes.add(dataset.getDatasetCode());
        }
        Map<String, File> datasetRoots =
                checkAccessAndGetRootDirectories(sessionToken, datasetCodes);
        List<ImageDatasetMetadata> result = new ArrayList<ImageDatasetMetadata>();
        for (IImageDatasetIdentifier dataset : imageDatasets)
        {
            File rootDirectoryOrNull = datasetRoots.get(dataset.getDatasetCode());
            if (rootDirectoryOrNull != null)
            {
                result.add(extractImageMetadata(dataset, rootDirectoryOrNull));
            }
        }
        return result;
    }

    private static ImageDatasetMetadata extractImageMetadata(IImageDatasetIdentifier dataset,
            File datasetRoot)
    {
        IHCSImageDatasetLoader imageAccessor =
                HCSImageDatasetLoaderFactory.create(datasetRoot, dataset.getDatasetCode());
        IContent imageFile = getAnyImagePath(imageAccessor, dataset);
        Geometry wellGeometry = imageAccessor.getWellGeometry();
        int channelsNumber = imageAccessor.getChannelCount();
        int tilesNumber = wellGeometry.getColumns() * wellGeometry.getRows();
        BufferedImage image = ImageUtil.loadImage(imageFile.getInputStream());
        return new ImageDatasetMetadata(dataset, channelsNumber, tilesNumber, image.getWidth(),
                image.getHeight());
    }

    private static IContent getAnyImagePath(IHCSImageDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        Geometry plateGeometry = imageAccessor.getPlateGeometry();
        for (int row = 1; row <= plateGeometry.getRows(); row++)
        {
            for (int col = 1; col <= plateGeometry.getColumns(); col++)
            {
                AbsoluteImageReference image;
                for (String channelName : imageAccessor.getChannelsNames())
                {
                    image =
                            imageAccessor.tryGetImage(channelName, new Location(col, row),
                                    new Location(1, 1), null);
                    if (image != null)
                    {
                        return image.getContent();
                    }
                }
            }
        }
        throw new IllegalStateException("Cannot find any image in a dataset: " + dataset);
    }

    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureNames)
    {
        assertDataSetsAreAccessible(sessionToken, featureDatasets);
        List<FeatureVectorDataset> result = new ArrayList<FeatureVectorDataset>();
        for (FeatureVectorDatasetReference dataset : featureDatasets)
        {
            result.add(createFeatureVectorDataset(sessionToken, dataset, featureNames));
        }
        return result;
    }

    private FeatureVectorDataset createFeatureVectorDataset(String sessionToken,
            FeatureVectorDatasetReference dataset, List<String> featureNames)
    {
        FeatureTableBuilder builder = new FeatureTableBuilder(featureNames, getDAO(), getOpenBISService());
        builder.addFeatureVectorsOfDataSet(dataset.getDatasetCode());
        List<String> existingFeatureNames = builder.getFeatureNames();
        List<FeatureTableRow> featureTableRows = builder.getFeatureTableRows();
        List<FeatureVector> featureVectors = new ArrayList<FeatureVector>();
        for (FeatureTableRow featureTableRow : featureTableRows)
        {
            featureVectors.add(new FeatureVector(new WellPosition(
                    featureTableRow.getRowIndex() + 1, featureTableRow.getColumnIndex() + 1),
                    featureTableRow.getFeatureValues()));
        }
        return new FeatureVectorDataset(dataset, existingFeatureNames, featureVectors);
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences)
    {
        Map<String, IHCSImageDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        List<IContent> imageFiles = new ArrayList<IContent>();
        try
        {
            for (PlateImageReference imageReference : imageReferences)
            {
                IHCSImageDatasetLoader imageAccessor =
                        imageLoadersMap.get(imageReference.getDatasetCode());
                assert imageAccessor != null : "imageAccessor not found for: " + imageReference;
                AbsoluteImageReference image = tryGetImage(imageAccessor, imageReference);
                if (image == null
                        || (image.tryGetColorComponent() != null || image.tryGetPage() != null))
                {
                    // TODO 2010-06-01, Tomasz Pylak: support paging/merged channels images in API
                    imageFiles.add(null);
                } else
                {
                    imageFiles.add(image.getContent());
                }
            }
        } finally
        {
            closeDatasetLoaders(imageLoadersMap.values());
        }
        return new ConcatenatedContentInputStream(true, imageFiles);
    }

    private static void closeDatasetLoaders(Collection<IHCSImageDatasetLoader> loaders)
    {
        for (IHCSImageDatasetLoader loader : loaders)
        {
            loader.close();
        }
    }

    // throws exception if some datasets cannot be found
    private Map<String/* image or feature vector dataset code */, IHCSImageDatasetLoader> getImageDatasetsMap(
            String sessionToken, List<PlateImageReference> imageReferences)
    {
        Map<String/* dataset code */, IHCSImageDatasetLoader> imageDatasetsMap =
                new HashMap<String, IHCSImageDatasetLoader>();
        for (PlateImageReference imageReference : imageReferences)
        {
            if (imageDatasetsMap.containsKey(imageReference.getDatasetCode()) == false)
            {
                ExternalData imageDataset =
                        tryFindImageDataset(sessionToken, imageReference.getDatasetCode());
                if (imageDataset != null)
                {
                    IHCSImageDatasetLoader imageAccessor =
                            createImageLoader(imageDataset.getCode());
                    imageDatasetsMap.put(imageReference.getDatasetCode(), imageAccessor);
                } else
                {
                    throw UserFailureException.fromTemplate(
                            "Cannot find an image dataset for the reference: %s", imageReference);
                }
            }
        }
        return imageDatasetsMap;
    }

    private AbsoluteImageReference tryGetImage(IHCSImageDatasetLoader imageAccessor,
            PlateImageReference imageRef)
    {
        Location wellLocation = asLocation(imageRef.getWellPosition());
        Location tileLocation =
                getTileLocation(imageRef.getTile(), imageAccessor.getWellGeometry());
        try
        {
            return ImageChannelsUtils.getImage(imageAccessor, wellLocation, tileLocation, imageRef
                    .getChannel(), null);
        } catch (EnvironmentFailureException e)
        {
            return null; // no image found
        }
    }

    // tile - start from 0
    private static Location getTileLocation(int tile, Geometry wellGeometry)
    {
        int row = (tile / wellGeometry.getColumns()) + 1;
        int col = (tile % wellGeometry.getColumns()) + 1;
        return new Location(row, col);
    }

    private static Location asLocation(WellPosition wellPosition)
    {
        return new Location(wellPosition.getWellColumn(), wellPosition.getWellRow());
    }

    private IHCSImageDatasetLoader createImageLoader(String datasetCode)
    {
        File datasetRoot = getRootDirectoryForDataSet(datasetCode);
        return HCSImageDatasetLoaderFactory.create(datasetRoot, datasetCode);
    }

    private ExternalData tryFindImageDataset(String sessionToken, String datasetCode)
    {
        ExternalData dataset = tryGetDataSet(sessionToken, datasetCode);
        if (dataset == null)
        {
            throw new IllegalArgumentException("Dataset " + datasetCode + " cannot be found.");
        }
        if (isImageDataset(dataset))
        {
            return dataset;
        }
        // it may be the feature dataset
        Collection<ExternalData> parents = dataset.getParents();
        if (parents.size() > 1)
        {
            throw new IllegalArgumentException("Dataset " + datasetCode
                    + " should have at most 1 parent, but has: " + parents.size());
        }
        if (parents.size() == 1)
        {
            ExternalData parent = parents.iterator().next();
            if (isImageDataset(parent))
            {
                return parent;
            } else
            {
                return null;
            }
        } else
        {
            return null;
        }
    }

    private boolean isImageDataset(ExternalData dataset)
    {
        return dataset.getDataSetType().getCode().equals(ScreeningConstants.IMAGE_DATASET_TYPE);
    }

    public int getMajorVersion()
    {
        return 1;
    }

    public int getMinorVersion()
    {
        return 0;
    }
    
    private List<ImgFeatureDefDTO> getFeatureDefinitions(IDatasetIdentifier identifier)
    {
        ImgDatasetDTO dataSet = getDAO().tryGetDatasetByPermId(identifier.getDatasetCode());
        if (dataSet == null)
        {
            throw new UserFailureException("Unknown data set: " + identifier.getDatasetCode());
        }
        return getDAO().listFeatureDefsByDataSetId(dataSet.getId());
    }

    private void assertDataSetsAreAccessible(String sessionToken,
            List<? extends IDatasetIdentifier> identifiers)
    {
        List<String> dataSetCodes = new ArrayList<String>();
        for (IDatasetIdentifier identifier : identifiers)
        {
            dataSetCodes.add(identifier.getDatasetCode());
        }
        assertDatasetsAreAccessible(sessionToken, dataSetCodes);
    }

    private IImagingQueryDAO getDAO()
    {
        if (dao == null)
        {
            dao = DssScreeningUtils.createQuery();
        }
        return dao;
    }

}
