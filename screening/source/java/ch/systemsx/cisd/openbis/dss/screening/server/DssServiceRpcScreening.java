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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import com.csvreader.CsvReader;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedFileInputStream;
import ch.systemsx.cisd.openbis.dss.etl.AbsoluteImageReference;
import ch.systemsx.cisd.openbis.dss.etl.HCSDatasetLoaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.IHCSDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.images.ImageChannelsUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
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

    private File getDatasetFile(String sessionToken, IDatasetIdentifier dataset) throws IOException
    {
        File originalDir =
                checkAccessAndGetFile(sessionToken, dataset.getDatasetCode(), "original");
        if (originalDir.isDirectory() == false)
        {
            throw new IllegalArgumentException(String.format(
                    "Dataset %s directory '%s' does not exist.", dataset.getDatasetCode(),
                    originalDir.getPath()));
        }
        File[] datasetFiles = originalDir.listFiles();
        if (datasetFiles.length == 1)
        {
            return datasetFiles[0];
        } else
        {
            throw new IllegalArgumentException(String.format(
                    "Exactly one item was expected in the '%s' directory,"
                            + " but %d have been found.", originalDir.getPath(),
                    datasetFiles.length));
        }
    }

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
            throw wrapIOException(ex);
        }
    }

    private String[] extractFeatureNames(String sessionToken,
            IFeatureVectorDatasetIdentifier dataset) throws IOException
    {
        return extractFeatureNames(getDatasetFile(sessionToken, dataset));
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
        IHCSDatasetLoader imageAccessor =
                HCSDatasetLoaderFactory.create(datasetRoot, dataset.getDatasetCode());
        File imageFile = getAnyImagePath(imageAccessor, dataset);
        Geometry wellGeometry = imageAccessor.getWellGeometry();
        int channelsNumber = imageAccessor.getChannelCount();
        int tilesNumber = wellGeometry.getColumns() * wellGeometry.getRows();
        BufferedImage image = ImageUtil.loadImage(imageFile);
        return new ImageDatasetMetadata(dataset, channelsNumber, tilesNumber, image.getWidth(),
                image.getHeight());
    }

    private static File getAnyImagePath(IHCSDatasetLoader imageAccessor,
            IImageDatasetIdentifier dataset)
    {
        Geometry plateGeometry = imageAccessor.getPlateGeometry();
        for (int row = 1; row <= plateGeometry.getRows(); row++)
        {
            for (int col = 1; col <= plateGeometry.getColumns(); col++)
            {
                AbsoluteImageReference image =
                        imageAccessor.tryGetImage(1, new Location(col, row), new Location(1, 1));
                if (image != null)
                {
                    return new File(image.getImageAbsolutePath());
                }
            }
        }
        throw new IllegalStateException("Cannot find any image in a dataset: " + dataset);
    }

    public List<FeatureVectorDataset> loadFeatures(String sessionToken,
            List<FeatureVectorDatasetReference> featureDatasets, List<String> featureNames)
    {
        try
        {
            List<FeatureVectorDataset> result = new ArrayList<FeatureVectorDataset>();

            for (FeatureVectorDatasetReference dataset : featureDatasets)
            {
                result.add(createFeatureVectorDataset(sessionToken, dataset, featureNames));
            }
            return result;
        } catch (IOException ex)
        {
            throw wrapIOException(ex);
        }
    }

    private FeatureVectorDataset createFeatureVectorDataset(String sessionToken,
            FeatureVectorDatasetReference dataset, List<String> featureNames) throws IOException
    {
        return createFeatureVectorDataset(getDatasetFile(sessionToken, dataset), dataset,
                featureNames);
    }

    public InputStream loadImages(String sessionToken, List<PlateImageReference> imageReferences)
    {
        Map<String, IHCSDatasetLoader> imageLoadersMap =
                getImageDatasetsMap(sessionToken, imageReferences);
        List<File> imageFiles = new ArrayList<File>();
        try
        {
            for (PlateImageReference imageReference : imageReferences)
            {
                IHCSDatasetLoader imageAccessor =
                        imageLoadersMap.get(imageReference.getDatasetCode());
                assert imageAccessor != null : "imageAccessor not found for: " + imageReference;
                File imageFile = tryGetImageFile(imageAccessor, imageReference);
                imageFiles.add(imageFile);
            }
        } finally
        {
            closeDatasetLoaders(imageLoadersMap.values());
        }
        return new ConcatenatedFileInputStream(true, imageFiles);
    }

    private static void closeDatasetLoaders(Collection<IHCSDatasetLoader> loaders)
    {
        for (IHCSDatasetLoader loader : loaders)
        {
            loader.close();
        }
    }

    // throws exception if some datasets cannot be found
    private Map<String/* image or feature vector dataset code */, IHCSDatasetLoader> getImageDatasetsMap(
            String sessionToken, List<PlateImageReference> imageReferences)
    {
        Map<String/* dataset code */, IHCSDatasetLoader> imageDatasetsMap =
                new HashMap<String, IHCSDatasetLoader>();
        for (PlateImageReference imageReference : imageReferences)
        {
            if (imageDatasetsMap.containsKey(imageReference.getDatasetCode()) == false)
            {
                ExternalData imageDataset =
                        tryFindImageDataset(sessionToken, imageReference.getDatasetCode());
                if (imageDataset != null)
                {
                    IHCSDatasetLoader imageAccessor = createImageLoader(imageDataset.getCode());
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

    private File tryGetImageFile(IHCSDatasetLoader imageAccessor, PlateImageReference imageRef)
    {
        Location wellLocation = asLocation(imageRef.getWellPosition());
        Location tileLocation =
                getTileLocation(imageRef.getTile(), imageAccessor.getWellGeometry());
        try
        {
            return ImageChannelsUtils.getImagePath(imageAccessor, wellLocation, tileLocation,
                    imageRef.getChannel() + 1);
        } catch (EnvironmentFailureException e)
        {
            return null; // no image found
        }
    }

    private static IllegalStateException wrapIOException(IOException exception)
    {
        return new IllegalStateException(exception.getMessage());
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

    private IHCSDatasetLoader createImageLoader(String datasetCode)
    {
        File datasetRoot = getRootDirectoryForDataSet(datasetCode);
        return HCSDatasetLoaderFactory.create(datasetRoot, datasetCode);
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

    private static final String WELL_POSITION_HEADER_NAME = "WellName";

    private static final String WELL_ROW_HEADER_NAME = "Row";

    private static final String WELL_COLUMN_HEADER_NAME = "Col";

    // exposed for testing
    static String[] extractFeatureNames(File datasetFile) throws IOException
    {
        CsvReader reader = null;
        try
        {
            reader = CsvFileReaderHelper.getCsvReader(datasetFile);
            if (reader.readHeaders())
            {
                String[] headers = reader.getHeaders();
                if (reader.readRecord())
                {
                    String[] values = reader.getValues();
                    return chooseNumericValues(headers, values);
                } else
                {
                    return headers;
                }
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

    // a heuristic to choose only those features which contain numbers. We check the first feature
    // vector to do that.
    private static String[] chooseNumericValues(String[] headers, String[] values)
    {
        List<String> featureNames = new ArrayList<String>();
        for (int i = 0; i < Math.min(headers.length, values.length); i++)
        {
            if (isWellColumnName(headers[i]) == false && isNumber(values[i]))
            {
                featureNames.add(headers[i]);
            }
        }
        return featureNames.toArray(new String[0]);
    }

    private static boolean isNumber(String value)
    {
        return tryGetDouble(value) != null;
    }

    // exposed for testing
    static FeatureVectorDataset createFeatureVectorDataset(File datasetFile,
            FeatureVectorDatasetReference dataset, List<String> featureNames) throws IOException
    {
        DatasetFileLines fileLines = CsvFileReaderHelper.getDatasetFileLines(datasetFile);
        String[] headerTokens = fileLines.getHeaderTokens();

        List<String> existingFeatureNames = new ArrayList<String>();
        int[] headerTokenFeatureIndexes = new int[headerTokens.length];
        Arrays.fill(headerTokenFeatureIndexes, -1);
        for (String featureName : featureNames)
        {
            if (isWellColumnName(featureName) == false) // well position columns have wrong format
            {
                int index = getColumnIndexForHeader(featureName, headerTokens);
                if (index > -1)
                {
                    existingFeatureNames.add(featureName);
                    headerTokenFeatureIndexes[index] = existingFeatureNames.size() - 1;
                }
            }
        }

        int[] wellIndexes = new int[3];
        Arrays.fill(wellIndexes, -1);
        wellIndexes[0] = getColumnIndexForHeader(WELL_POSITION_HEADER_NAME, headerTokens);
        wellIndexes[1] = getColumnIndexForHeader(WELL_ROW_HEADER_NAME, headerTokens);
        wellIndexes[2] = getColumnIndexForHeader(WELL_COLUMN_HEADER_NAME, headerTokens);

        FeatureVectorDatasetBuilder builder =
                new FeatureVectorDatasetBuilder(dataset, existingFeatureNames);
        int lineNumber = 1;
        for (String[] dataLine : fileLines.getDataLines())
        {
            FeatureVector vector =
                    tryExtractFeatureVector(dataLine, wellIndexes, headerTokenFeatureIndexes,
                            existingFeatureNames.size());
            if (vector != null)
            {
                builder.addFeatureVector(vector);
            } else
            {
                String logMsg =
                        "wrong data format or well position not found for data set %s (file: %s, line: %s)";
                operationLog.warn(String.format(logMsg, dataset.getDatasetCode(), datasetFile,
                        lineNumber));
            }
            lineNumber++;
        }
        return builder.create();
    }

    private static boolean isWellColumnName(String string)
    {
        return string.equalsIgnoreCase(WELL_POSITION_HEADER_NAME)
                || string.equalsIgnoreCase(WELL_ROW_HEADER_NAME)
                || string.equalsIgnoreCase(WELL_COLUMN_HEADER_NAME);
    }

    /** @return the column index for the column header or -1 if none was found */
    private static int getColumnIndexForHeader(String columnHeader, String[] headers)
    {
        for (int i = 0; i < headers.length; i++)
        {
            if (columnHeader.equalsIgnoreCase(headers[i]))
            {
                return i;
            }
        }
        return -1;
    }

    private static FeatureVector tryExtractFeatureVector(String[] dataLine, int[] wellIndexes,
            int[] headerTokenFeatureIndexes, int existingFeaturesSize)
    {
        WellPosition wellPositionOrNull = tryExtractWellPosition(dataLine, wellIndexes);

        if (wellPositionOrNull != null)
        {
            double[] values = new double[existingFeaturesSize];
            for (int i = 0; i < headerTokenFeatureIndexes.length; i++)
            {
                if (headerTokenFeatureIndexes[i] > -1)
                {
                    Double value = tryGetDouble(dataLine[i]);
                    if (value != null)
                    {
                        values[headerTokenFeatureIndexes[i]] = value;
                    } else
                    {
                        operationLog.warn("feature " + i
                                + " has wrong format - expected double, found: " + dataLine[i]);
                        return null;
                    }
                }
            }
            return new FeatureVector(wellPositionOrNull, values);
        }
        return null;
    }

    private static Double tryGetDouble(String text)
    {
        try
        {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex)
        {
            return null;
        }
    }

    private static WellPosition tryExtractWellPosition(String[] fileLine, int[] wellIndexes)
    {
        int wellIndex = wellIndexes[0];
        int rowIndex = wellIndexes[1];
        int colIndex = wellIndexes[2];

        String coordinate;
        if (wellIndex > -1)
        {
            coordinate = fileLine[wellIndex];
        } else
        {
            if (rowIndex == -1 || colIndex == -1)
            {
                operationLog.warn("well not found");
                return null;
            }
            coordinate = fileLine[rowIndex] + fileLine[colIndex];
        }

        Location location = Location.tryCreateLocationFromMatrixCoordinate(coordinate);
        return new WellPosition(location.getY(), location.getX());
    }

    private static class FeatureVectorDatasetBuilder
    {
        private final FeatureVectorDatasetReference dataset;

        private final List<String> featureNames;

        private final List<FeatureVector> featureVectors;

        public FeatureVectorDatasetBuilder(FeatureVectorDatasetReference dataset,
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

    public int getMajorVersion()
    {
        return 1;
    }

    public int getMinorVersion()
    {
        return 0;
    }
}
