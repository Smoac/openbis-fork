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

package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.dao.ConcurrencyFailureException;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.openbis.dss.etl.AbstractImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.etl.AcquiredSingleImage;
import ch.systemsx.cisd.openbis.dss.etl.IImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.etl.ImageFileExtractionResult;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Works only when {@link JythonPlateDataSetHandler} is set as a 'top-level-data-set-handler'.
 * Assumes that all information which have to be extracted are accessible from
 * {@link ImageDataSetInformation} which extends {@link DataSetInformation}.
 * 
 * @author Tomasz Pylak
 */
public class JythonImageFileExtractor implements IImageFileExtractor
{
    // needed by the dropbox framework
    public JythonImageFileExtractor(Properties properties)
    {
    }

    public ImageFileExtractionResult extract(File incomingDataSetDirectory,
            DataSetInformation dataSetInformation)
    {
        if (dataSetInformation instanceof ImageDataSetInformation == false)
        {
            throw new ConcurrencyFailureException(
                    "Wrong top-level-data-set-handler has been configured, the required one is: "
                            + JythonPlateDataSetHandler.class.getCanonicalName());
        }

        ImageDataSetInformation imageDataSetInfo = (ImageDataSetInformation) dataSetInformation;
        Geometry tileGeometry =
                new Geometry(imageDataSetInfo.getTileRowsNumber(),
                        imageDataSetInfo.getTileColumnsNumber());
        List<AcquiredSingleImage> images = convertImages(imageDataSetInfo);
        List<File> invalidFiles = new ArrayList<File>(); // handles in an earlier phase
        return new ImageFileExtractionResult(images, invalidFiles, imageDataSetInfo.getChannels(),
                tileGeometry);
    }

    private List<AcquiredSingleImage> convertImages(ImageDataSetInformation imageDataSetInfo)
    {
        List<AcquiredSingleImage> images = new ArrayList<AcquiredSingleImage>();
        List<ImageFileInfo> imageInfos = imageDataSetInfo.getImages();
        for (ImageFileInfo imageInfo : imageInfos)
        {
            List<AcquiredSingleImage> image =
                    AbstractImageFileExtractor.createImagesWithNoColorComponent(imageInfo);
            images.addAll(image);
        }
        return images;
    }

}
