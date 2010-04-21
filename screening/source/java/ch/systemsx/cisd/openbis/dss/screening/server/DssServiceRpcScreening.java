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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.HCSDatasetLoader;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Implementation of the screening API interface using RPC. The instance will be created in spring
 * and published as a HTTP invoker servlet configured in service.properties.
 * 
 * @author Tomasz Pylak
 */
public class DssServiceRpcScreening extends AbstractDssServiceRpc implements
        IDssServiceRpcScreening
{

    public DssServiceRpcScreening(String storeRootDir)
    {
        super(ServiceProvider.getOpenBISService());
        setStoreDirectory(new File(storeRootDir));
        operationLog.info("Started RPC V1 screening service.");
    }

    public int getMinClientVersion()
    {
        return 1;
    }

    public int getVersion()
    {
        return 1;
    }

    // ------------------ impl -----------------

    public List<String> listAvailableFeatureNames(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        // TODO Auto-generated method stub

        return null;
    }

    public List<ImageDatasetMetadata> listImageMetadata(String sessionToken,
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets,
            List<String> featureNames)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream loadImage(String sessionToken, PlateImageReference imageReference)
    {
        ExternalData imageDataset =
                tryFindImageDataset(sessionToken, imageReference.getDatasetCode());
        if (imageDataset != null)
        {
            return getImageStream(imageDataset, imageReference);
        } else
        {
            return null;
        }
    }

    private InputStream getImageStream(ExternalData imageDataset, PlateImageReference imageRef)
    {
        HCSDatasetLoader imageAccessor = createImageLoader(imageRef.getDatasetCode());
        Location wellLocation = asLocation(imageRef.getWellPosition());
        Location tileLocation =
                getTileLocation(imageRef.getTile(), imageAccessor.getWellGeometry());
        try
        {
            File path =
                    ImageChannelsUtils.getImagePath(imageAccessor, wellLocation, tileLocation,
                            imageRef.getChannel());
            return new FileInputStream(path);
        } catch (EnvironmentFailureException e)
        {
            throw createNoImageException(imageRef);
        } catch (FileNotFoundException ex)
        {
            throw createNoImageException(imageRef);
        } finally
        {
            imageAccessor.close();
        }
    }

    private static IllegalArgumentException createNoImageException(PlateImageReference imageRef)
    {
        return new IllegalArgumentException("No image found: " + imageRef);
    }

    private static Location getTileLocation(int tile, Geometry wellGeometry)
    {
        int row = ((tile - 1) / wellGeometry.getColumns()) + 1;
        int col = ((tile - 1) % wellGeometry.getColumns()) + 1;
        return new Location(row, col);
    }

    private static Location asLocation(WellPosition wellPosition)
    {
        return new Location(wellPosition.getWellColumn(), wellPosition.getWellRow());
    }

    private HCSDatasetLoader createImageLoader(String datasetCode)
    {
        HCSDatasetLoader loader;
        File datasetRoot = getRootDirectoryForDataSet(datasetCode);
        loader = new HCSDatasetLoader(datasetRoot);
        return loader;
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

}
