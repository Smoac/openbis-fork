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

package ch.systemsx.cisd.openbis.dss.etl.bdsmigration;

import static ch.systemsx.cisd.openbis.dss.etl.bdsmigration.BDSMigrationMaintananceTask.DIR_SEP;
import static ch.systemsx.cisd.openbis.dss.etl.bdsmigration.BDSMigrationMaintananceTask.METADATA_DIR;
import static ch.systemsx.cisd.openbis.dss.etl.bdsmigration.BDSMigrationMaintananceTask.ORIGINAL_DIR;
import static ch.systemsx.cisd.openbis.dss.etl.bdsmigration.BDSMigrationMaintananceTask.asNum;
import static ch.systemsx.cisd.openbis.dss.etl.bdsmigration.BDSMigrationMaintananceTask.readLines;
import static ch.systemsx.cisd.openbis.dss.etl.bdsmigration.BDSMigrationMaintananceTask.tryGetOriginalDir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.plugins.IMigrator;
import ch.systemsx.cisd.openbis.dss.etl.AbstractHCSImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.etl.AcquiredPlateImage;
import ch.systemsx.cisd.openbis.dss.etl.HCSDatasetUploader;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor;
import ch.systemsx.cisd.openbis.dss.etl.RelativeImageReference;
import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ColorComponent;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingUploadDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;

/**
 * Uploads data to the imaging database.
 * 
 * @author Tomasz Pylak
 */
class BDSImagingDbUploader
{
    public static IMigrator createImagingDbUploaderMigrator(Properties properties)
    {
        final IImagingUploadDAO dao = createQuery(properties);
        final List<String> channelNames =
                PropertyUtils.getMandatoryList(properties, PlateStorageProcessor.CHANNEL_NAMES);
        final List<ColorComponent> channelColorComponentsOrNull =
                AbstractHCSImageFileExtractor.tryGetChannelComponents(properties);
        checkChannelsAndColorComponents(channelNames, channelColorComponentsOrNull);

        return new IMigrator()
            {
                public String getDescription()
                {
                    return "uploading data to the imaging database";
                }

                public boolean migrate(File dataset)
                {
                    return BDSImagingDbUploader.migrateDataset(dataset, dao, channelNames,
                            channelColorComponentsOrNull);
                }

                public void close()
                {
                    // do nothing
                }
            };
    }

    private static void checkChannelsAndColorComponents(List<String> channelNames,
            List<ColorComponent> channelColorComponentsOrNull)
    {
        if (channelColorComponentsOrNull != null
                && channelColorComponentsOrNull.size() != channelNames.size())
        {
            throw new ConfigurationFailureException(
                    "There should be exactly one color component for each channel name."
                            + " Correct the list of values for the components property.");
        }
    }

    private static IImagingUploadDAO createQuery(Properties properties)
    {
        DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        return QueryTool.getQuery(dataSource, IImagingUploadDAO.class);
    }

    private static boolean migrateDataset(File dataset, IImagingUploadDAO dao,
            List<String> channelNames, List<ColorComponent> channelColorComponentsOrNull)
    {
        String originalDatasetDirName = tryGetOriginalDatasetDirName(dataset);
        if (originalDatasetDirName == null)
        {
            return false;
        }
        return new BDSImagingDbUploader(dataset, dao, originalDatasetDirName, channelNames,
                channelColorComponentsOrNull).migrate();
    }

    private final File dataset;

    private final IImagingUploadDAO dao;

    private final String originalDatasetDirName;

    private final List<String> channelNames;

    private final List<ColorComponent> channelColorComponentsOrNull;

    BDSImagingDbUploader(File dataset, IImagingUploadDAO dao, String originalDatasetDirName,
            List<String> channelNames, List<ColorComponent> channelColorComponentsOrNull)
    {
        this.dataset = dataset;
        this.dao = dao;
        this.originalDatasetDirName = originalDatasetDirName;
        this.channelNames = channelNames;
        this.channelColorComponentsOrNull = channelColorComponentsOrNull;

    }

    boolean migrate()
    {
        List<AcquiredPlateImage> images = tryExtractMappings();
        if (images == null)
        {
            return false;
        }

        String relativeImagesDirectory = getRelativeImagesDirectory();
        for (AcquiredPlateImage acquiredPlateImage : images)
        {
            acquiredPlateImage.getImageReference().setRelativeImageFolder(relativeImagesDirectory);
        }

        ScreeningContainerDatasetInfo info = ScreeningDatasetInfoExtractor.tryCreateInfo(dataset);
        if (info == null)
        {
            return false;
        }

        Set<HCSImageFileExtractionResult.Channel> channels = extractChannels();

        return storeInDatabase(images, info, channels);
    }

    private Set<Channel> extractChannels()
    {
        Set<Channel> channels = new HashSet<Channel>();
        for (String channelName : channelNames)
        {
            channels.add(new Channel(channelName, null, null));
        }
        return channels;
    }

    private boolean storeInDatabase(List<AcquiredPlateImage> images,
            ScreeningContainerDatasetInfo info, Set<HCSImageFileExtractionResult.Channel> channels)
    {
        try
        {
            HCSDatasetUploader.upload(dao, info, images, channels);
        } catch (Exception ex)
        {
            ex.printStackTrace();
            logError("Uploading to the imaging db failed: " + ex.getMessage());
            dao.rollback();
            return false;
        }
        dao.commit();
        return true;
    }

    private String getRelativeImagesDirectory()
    {
        return ORIGINAL_DIR + DIR_SEP + originalDatasetDirName;
    }

    private static String tryGetOriginalDatasetDirName(File dataset)
    {
        File originalDir = tryGetOriginalDir(dataset);
        if (originalDir == null)
        {
            BDSMigrationMaintananceTask.logError(dataset, "Original directory does not exist.");
            return null;
        }
        File[] files = originalDir.listFiles();
        if (files.length != 1)
        {
            BDSMigrationMaintananceTask.logError(dataset, "Original directory '" + originalDir
                    + "' should contain exactly one file, but contains " + files.length + ": "
                    + files);
            return null;
        }
        return files[0].getName();
    }

    private List<AcquiredPlateImage> tryExtractMappings()
    {
        File mappingFile = new File(dataset, METADATA_DIR + DIR_SEP + "standard_original_mapping");
        if (mappingFile.isFile() == false)
        {
            logError("File '" + mappingFile + "' does not exist.");
            return null;
        }

        try
        {
            List<String> lines = readLines(mappingFile);
            return tryParseMappings(lines);
        } catch (IOException ex)
        {
            logError("Error when reading mapping file '" + mappingFile + "': " + ex.getMessage());
            return null;
        }
    }

    private List<AcquiredPlateImage> tryParseMappings(List<String> lines)
    {
        List<AcquiredPlateImage> images = new ArrayList<AcquiredPlateImage>();
        for (String line : lines)
        {
            List<AcquiredPlateImage> mapping = tryParseMapping(line);
            if (mapping != null)
            {
                images.addAll(mapping);
            } else
            {
                return null;
            }
        }
        return images;
    }

    private List<AcquiredPlateImage> tryParseMapping(String line)
    {
        String[] tokens = StringUtils.split(line);
        if (tokens.length != 3)
        {
            logError("Wrong number of tokens in the mapping line: " + line);
            return null;
        } else
        {
            try
            {
                return tryParseMappingLine(tokens[0], tokens[2]);
            } catch (NumberFormatException ex)
            {
                logError("Incorrect format of mapping line: " + line + ". Cannot parse a number: "
                        + ex.getMessage());
                return null;
            }
        }
    }

    // Example of standardPath: channel2/row1/column4/row2_column2.tiff
    private List<AcquiredPlateImage> tryParseMappingLine(String standardPath, String originalPath)
            throws NumberFormatException
    {
        String[] pathTokens = standardPath.split("/");
        if (pathTokens.length != 4)
        {
            logError("Wrong number of tokens in standard path: " + standardPath);
            return null;
        }
        int channelNum = asNum(pathTokens[0], "channel");
        int row = asNum(pathTokens[1], "row");
        int col = asNum(pathTokens[2], "column");
        Location wellLocation = new Location(col, row);

        String[] tileTokens = tryParseTileToken(pathTokens[3]);
        if (tileTokens == null)
        {
            return null;
        }
        int tileRow = asNum(tileTokens[0], "row");
        int tileCol = asNum(tileTokens[1], "column");
        Location tileLocation = new Location(tileCol, tileRow);

        String relativeImagePath = tryGetRelativeImagePath(originalPath);
        if (relativeImagePath == null)
        {
            return null;
        }
        String channelName = tryGetChannelName(channelNum, standardPath);
        if (channelName == null)
        {
            return null;
        }

        return createImages(wellLocation, tileLocation, relativeImagePath, channelName);
    }

    private List<AcquiredPlateImage> createImages(Location wellLocation, Location tileLocation,
            String relativeImagePath, String channelName)
    {
        List<AcquiredPlateImage> images = new ArrayList<AcquiredPlateImage>();
        if (channelColorComponentsOrNull != null)
        {
            for (int i = 0; i < channelColorComponentsOrNull.size(); i++)
            {
                ColorComponent colorComponent = channelColorComponentsOrNull.get(i);
                String channel = channelNames.get(i);
                images.add(createImage(wellLocation, tileLocation, relativeImagePath, channel,
                        colorComponent));
            }
        } else
        {
            images
                    .add(createImage(wellLocation, tileLocation, relativeImagePath, channelName,
                            null));
        }
        return images;
    }

    private static AcquiredPlateImage createImage(Location plateLocation, Location wellLocation,
            String imageRelativePath, String channelName, ColorComponent colorComponent)
    {
        return new AcquiredPlateImage(plateLocation, wellLocation, channelName, null, null,
                new RelativeImageReference(imageRelativePath, null, colorComponent));
    }

    // channelId - starts with 1
    private String tryGetChannelName(int channelId, String standardPath)
    {
        if (channelNames.size() < channelId)
        {
            logError("Name of the channel with the id " + channelId
                    + " has not been configured but is referenced in the path: " + standardPath
                    + ".");
            return null;
        }
        return channelNames.get(channelId - 1);
    }

    private String tryGetRelativeImagePath(String originalPath)
    {
        String prefixPath = originalDatasetDirName + "/";
        if (originalPath.startsWith(prefixPath) == false)
        {
            logError("Original path " + originalPath + " should start with " + prefixPath);
            return null;
        }
        return originalPath.substring(prefixPath.length());
    }

    // tileFile - e.g. row2_column2.tiff
    private String[] tryParseTileToken(String tileFile)
    {
        String tileDesc;
        int dotIndex = tileFile.indexOf(".");
        if (dotIndex != -1)
        {
            tileDesc = tileFile.substring(0, dotIndex);
        } else
        {
            tileDesc = tileFile;
        }
        String[] tileTokens = tileDesc.split("_");
        if (tileTokens.length != 2)
        {
            logError("Wrong number of tokens in tile file name: " + tileDesc);
            return null;
        }
        return tileTokens;
    }

    private void logError(String reason)
    {
        BDSMigrationMaintananceTask.logError(dataset, reason);
    }
}