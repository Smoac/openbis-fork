/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.bds.hcs.Channel;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.etlserver.IHCSImageFileAccepter;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageCheckList.FullLocation;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimension;

/**
 * Storage processor which stores HCS plate images in a special-purpose imaging database.
 * <p>
 * See {@link AbstractImageStorageProcessor} documentation.
 * 
 * @author Tomasz Pylak
 */
public final class PlateStorageProcessor extends AbstractImageStorageProcessor
{
    // a class of the old-style image extractor
    private static final String DEPRECATED_FILE_EXTRACTOR_PROPERTY = "deprecated-file-extractor";

    // ---

    private final ch.systemsx.cisd.etlserver.IHCSImageFileExtractor deprecatedImageFileExtractor;

    public PlateStorageProcessor(Properties properties)
    {
        super(properties);
        if (imageFileExtractor == null)
        {
            String fileExtractorClass = getMandatoryProperty(DEPRECATED_FILE_EXTRACTOR_PROPERTY);
            this.deprecatedImageFileExtractor =
                    ClassUtils.create(ch.systemsx.cisd.etlserver.IHCSImageFileExtractor.class,
                            fileExtractorClass, properties);
        } else
        {
            this.deprecatedImageFileExtractor = null;
        }
    }

    private static final class HCSImageFileAccepter implements IHCSImageFileAccepter
    {
        private final List<AcquiredPlateImage> images = new ArrayList<AcquiredPlateImage>();

        private final File imageFileRootDirectory;

        private final List<String> channelCodes;

        public HCSImageFileAccepter(File imageFileRootDirectory, List<String> channelCodes)
        {
            this.imageFileRootDirectory = imageFileRootDirectory;
            this.channelCodes = channelCodes;
        }

        public final void accept(final int channel, final Location wellLocation,
                final Location tileLocation, final IFile imageFile)
        {
            final String imageRelativePath =
                    FileUtilities.getRelativeFile(imageFileRootDirectory,
                            new File(imageFile.getPath()));
            assert imageRelativePath != null : "Image relative path should not be null.";
            String channelCode = getChannelCodeOrLabel(channelCodes, channel);
            AcquiredPlateImage imageDesc =
                    new AcquiredPlateImage(wellLocation, tileLocation, channelCode, null, null,
                            new RelativeImageReference(imageRelativePath, null, null));
            images.add(imageDesc);
        }

        public List<AcquiredPlateImage> getImages()
        {
            return images;
        }
    }

    // adapts old-style image extractor to the new one which is stateless
    private static IImageFileExtractor adapt(
            final ch.systemsx.cisd.etlserver.IHCSImageFileExtractor extractor,
            final File imageFileRootDirectory, final List<ChannelDescription> descriptions)
    {
        return new IImageFileExtractor()
            {
                public HCSImageFileExtractionResult extract(File incomingDataSetDirectory,
                        DataSetInformation dataSetInformation)
                {
                    HCSImageFileAccepter accepter =
                            new HCSImageFileAccepter(imageFileRootDirectory,
                                    extractChannelCodes(descriptions));
                    ch.systemsx.cisd.etlserver.HCSImageFileExtractionResult originalResult =
                            extractor.process(
                                    NodeFactory.createDirectoryNode(incomingDataSetDirectory),
                                    dataSetInformation, accepter);
                    List<HCSImageFileExtractionResult.Channel> channels =
                            convert(originalResult.getChannels());
                    return new HCSImageFileExtractionResult(accepter.getImages(),
                            asRelativePaths(originalResult.getInvalidFiles()), channels);
                }

                private List<HCSImageFileExtractionResult.Channel> convert(Set<Channel> channels)
                {
                    List<HCSImageFileExtractionResult.Channel> result =
                            new ArrayList<HCSImageFileExtractionResult.Channel>();
                    for (Channel channel : channels)
                    {
                        result.add(new HCSImageFileExtractionResult.Channel(getChannelCodeOrLabel(
                                extractChannelCodes(descriptions), channel.getCounter()), null,
                                channel.getWavelength(), getChannelCodeOrLabel(
                                        extractChannelLabels(descriptions), channel.getCounter())));
                    }
                    return result;
                }

                private List<File> asRelativePaths(List<IFile> files)
                {
                    List<File> result = new ArrayList<File>();
                    for (IFile file : files)
                    {
                        result.add(new File(file.getPath()));
                    }
                    return result;
                }
            };
    }

    @Override
    protected void validateImages(DataSetInformation dataSetInformation, IMailClient mailClient,
            File incomingDataSetDirectory, HCSImageFileExtractionResult extractionResult)
    {
        HCSImageCheckList imageCheckList = createImageCheckList(dataSetInformation);
        checkImagesForDuplicates(extractionResult, imageCheckList);
        if (extractionResult.getInvalidFiles().size() > 0)
        {
            throw UserFailureException.fromTemplate("Following invalid files %s have been found.",
                    CollectionUtils.abbreviate(extractionResult.getInvalidFiles(), 10));
        }
        if (extractionResult.getImages().size() == 0)
        {
            throw UserFailureException.fromTemplate(
                    "No extractable files were found inside a dataset '%s'."
                            + " Have you changed your naming convention?",
                    incomingDataSetDirectory.getAbsolutePath());
        }
        checkCompleteness(imageCheckList, dataSetInformation, incomingDataSetDirectory.getName(),
                mailClient);
    }

    private static void checkImagesForDuplicates(HCSImageFileExtractionResult extractionResult,
            HCSImageCheckList imageCheckList)
    {
        List<AcquiredPlateImage> images = extractionResult.getImages();
        for (AcquiredPlateImage image : images)
        {
            imageCheckList.checkOff(image);
        }
    }

    private PlateDimension getPlateGeometry(final DataSetInformation dataSetInformation)
    {
        return HCSContainerDatasetInfo.getPlateGeometry(dataSetInformation);
    }

    private HCSImageCheckList createImageCheckList(DataSetInformation dataSetInformation)
    {
        PlateDimension plateGeometry = getPlateGeometry(dataSetInformation);
        List<String> channelCodes = new ArrayList<String>();
        for (ChannelDescription cd : channelDescriptions)
        {
            channelCodes.add(cd.getCode());
        }
        return new HCSImageCheckList(channelCodes, plateGeometry, spotGeometry);
    }

    private void checkCompleteness(HCSImageCheckList imageCheckList,
            final DataSetInformation dataSetInformation, final String dataSetFileName,
            final IMailClient mailClientOrNull)
    {
        final List<FullLocation> fullLocations = imageCheckList.getCheckedOnFullLocations();
        final boolean complete = fullLocations.size() == 0;
        dataSetInformation.setComplete(complete);
        if (complete == false)
        {
            final String message =
                    String.format("Incomplete data set '%s': %d image file(s) "
                            + "are missing (locations: %s)", dataSetFileName, fullLocations.size(),
                            CollectionUtils.abbreviate(fullLocations, 10));
            operationLog.warn(message);
            if (mailClientOrNull != null)
            {
                Experiment experiment = dataSetInformation.tryToGetExperiment();
                assert experiment != null : "dataset not connected to an experiment: "
                        + dataSetInformation;
                final String email = experiment.getRegistrator().getEmail();
                if (StringUtils.isBlank(email) == false)
                {
                    try
                    {
                        mailClientOrNull.sendMessage("Incomplete data set '" + dataSetFileName
                                + "'", message, null, null, email);
                    } catch (final EnvironmentFailureException e)
                    {
                        notificationLog.error("Couldn't send the following e-mail to '" + email
                                + "': " + message, e);
                    }
                } else
                {
                    notificationLog.error("Unspecified e-mail address of experiment registrator "
                            + experiment.getRegistrator());
                }
            }
        }
    }

    @Override
    protected IImageFileExtractor getImageFileExtractor(File incomingDataSetDirectory)
    {
        IImageFileExtractor extractor = imageFileExtractor;
        if (extractor == null)
        {
            extractor =
                    adapt(deprecatedImageFileExtractor, incomingDataSetDirectory,
                            channelDescriptions);
        }
        return extractor;
    }

    @Override
    protected void storeInDatabase(IImagingQueryDAO dao, DataSetInformation dataSetInformation,
            HCSImageFileExtractionResult extractedImages)
    {
        Experiment experiment = dataSetInformation.tryToGetExperiment();
        assert experiment != null : "experiment is null";
        List<AcquiredPlateImage> images = extractedImages.getImages();
        HCSImageDatasetInfo info = createImageDatasetInfo(experiment, dataSetInformation, images);

        HCSImageDatasetUploader.upload(dao, info, images, extractedImages.getChannels());
    }

    private HCSImageDatasetInfo createImageDatasetInfo(Experiment experiment,
            DataSetInformation dataSetInformation, List<AcquiredPlateImage> acquiredImages)
    {
        HCSContainerDatasetInfo info =
                HCSContainerDatasetInfo.createScreeningDatasetInfo(dataSetInformation);
        boolean hasImageSeries = hasImageSeries(acquiredImages);
        return new HCSImageDatasetInfo(info, spotGeometry.getRows(), spotGeometry.getColumns(),
                hasImageSeries);
    }
}
