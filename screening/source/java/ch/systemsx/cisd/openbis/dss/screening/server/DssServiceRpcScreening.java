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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.HCSDatasetLoader;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.utils.CsvFileReaderHelper;

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
        try
        {
            List<String> result = new ArrayList<String>(); // keep the order
            for (IFeatureVectorDatasetIdentifier dataset : featureDatasets)
            {
                // add only new feature names
                String[] featureNames = extractFeatureNames(sessionToken, dataset);
                for (String featureName : featureNames)
                {
                    if (result.contains(featureName) == false)
                    {
                        result.add(featureName);
                    }
                }
            }
            return result;
        } catch (IOException ex)
        {
            wrapIOException(ex);
        }
        return null;
    }

    private String[] extractFeatureNames(String sessionToken,
            IFeatureVectorDatasetIdentifier dataset) throws IOException
    {
        return extractFeatureNames(getDatasetFile(sessionToken, dataset));
    }

    private File getDatasetFile(String sessionToken, IDatasetIdentifier dataset)
    {
        // FIXME return file in subdirectory
        return checkAccessAndGetRootDirectory(sessionToken, dataset.getDatasetCode());
    }

    //

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
        try
        {
            List<FeatureVectorDataset> result = new ArrayList<FeatureVectorDataset>();

            for (IFeatureVectorDatasetIdentifier dataset : featureDatasets)
            {
                result.add(createFeatureVectorDataset(sessionToken, dataset, featureNames));
            }
            return result;
        } catch (IOException ex)
        {
            wrapIOException(ex);
        }
        return null;
    }

    private FeatureVectorDataset createFeatureVectorDataset(String sessionToken,
            IFeatureVectorDatasetIdentifier dataset, List<String> featureNames) throws IOException
    {
        return createFeatureVectorDataset(getDatasetFile(sessionToken, dataset), dataset,
                featureNames);
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

    private static IllegalArgumentException wrapIOException(IOException exception)
    {
        return new IllegalArgumentException(exception.getMessage());
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

    //
    // helper methods and classes
    //

    // exposed for testing
    static String[] extractFeatureNames(File datasetFile) throws IOException
    {
        CsvReader reader = null;
        try
        {
            reader = CsvFileReaderHelper.getCsvReader(datasetFile);
            if (reader.readHeaders())
            {
                return reader.getHeaders();
            }
            return new String[0]; // empty file
        } finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    // exposed for testing
    static FeatureVectorDataset createFeatureVectorDataset(File datasetFile,
            IFeatureVectorDatasetIdentifier dataset, List<String> featureNames) throws IOException
    {
        DatasetFileLines fileLines = CsvFileReaderHelper.getDatasetFileLines(datasetFile);
        String[] headerTokens = fileLines.getHeaderTokens();

        List<String> existingFeatureNames = new ArrayList<String>();
        Set<String> columnNames = new HashSet<String>(Arrays.asList(headerTokens));
        for (String featureName : featureNames)
        {
            if (columnNames.contains(featureName))
            {
                existingFeatureNames.add(featureName);
            }
        }

        Map<String, Integer> indexByName = new HashMap<String, Integer>();
        for (int i = 0; i < headerTokens.length; i++)
        {
            indexByName.put(headerTokens[i].toLowerCase(), i);
        }

        FeatureVectorDatasetBuilder builder =
                new FeatureVectorDatasetBuilder(dataset, existingFeatureNames);
        int lineNumber = 1;
        for (String[] dataLine : fileLines.getDataLines())
        {
            FeatureVector vector =
                    tryExtractFeatureVector(dataLine, indexByName, existingFeatureNames);
            if (vector != null)
            {
                builder.addFeatureVector(vector);
            } else
            {
                operationLog.warn(String.format(
                        "wrong data format or well not found for data set %s (file: %s, line: %s)",
                        dataset.getDatasetCode(), datasetFile, lineNumber));
            }
            lineNumber++;
        }
        return builder.create();
    }

    private static FeatureVector tryExtractFeatureVector(String[] dataLine,
            Map<String, Integer> indexByName, List<String> featureNames)
    {
        WellPosition wellPositionOrNull = tryExtractWellPosition(dataLine, indexByName);

        if (wellPositionOrNull != null)
        {
            double[] values = new double[featureNames.size()];
            for (int i = 0; i < featureNames.size(); i++)
            {
                try
                {
                    int index = indexByName.get(featureNames.get(i));
                    values[i] = Double.parseDouble(dataLine[index]);
                } catch (NumberFormatException ex)
                {
                    // skip this feature
                    return null;
                }
            }
            return new FeatureVector(wellPositionOrNull, values);
        }
        return null;
    }

    private static WellPosition tryExtractWellPosition(String[] fileLine,
            Map<String, Integer> indexByName)
    {
        String coordinate;
        Integer wellIndexOrNull = indexByName.get("WellName".toLowerCase());
        if (wellIndexOrNull != null)
        {
            coordinate = fileLine[wellIndexOrNull];
        } else
        {
            Integer rowIndexOrNull = indexByName.get("row".toLowerCase());
            Integer colIndexOrNull = indexByName.get("col".toLowerCase());
            if (rowIndexOrNull == null || colIndexOrNull == null)
            {
                operationLog.warn("well position missing");
                return null;
            }
            coordinate = fileLine[rowIndexOrNull] + fileLine[colIndexOrNull];
        }

        Location location = Location.tryCreateLocationFromMatrixCoordinate(coordinate);
        return new WellPosition(location.getY(), location.getX());
    }

    private static class FeatureVectorDatasetBuilder
    {
        private final IFeatureVectorDatasetIdentifier dataset;

        private final List<String> featureNames;

        private final List<FeatureVector> featureVectors;

        public FeatureVectorDatasetBuilder(IFeatureVectorDatasetIdentifier dataset,
                List<String> featureNames)
        {
            this.dataset = dataset;
            this.featureNames = featureNames;
            this.featureVectors = new ArrayList<FeatureVector>();
        }

        public void addFeatureVector(FeatureVector vector)
        {
            featureVectors.add(vector);
        }

        public FeatureVectorDataset create()
        {
            return new FeatureVectorDataset(dataset, featureNames, featureVectors);
        }
    }
}
