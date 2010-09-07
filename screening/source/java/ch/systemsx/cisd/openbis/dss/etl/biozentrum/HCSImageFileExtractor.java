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

package ch.systemsx.cisd.openbis.dss.etl.biozentrum;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A <code>IHCSImageFileExtractor</code> implementation suitable for <i>BioZentrum</i>.
 * 
 * @author Izabela Adamczyk
 */
public class HCSImageFileExtractor extends ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractor
{

    public HCSImageFileExtractor(Properties properties)
    {
        super(properties);
    }

    @Override
    protected final ImageFileInfo tryExtractImageInfo(File imageFile, SampleIdentifier datasetSample)
    {
        return extractFileInfo(FilenameUtils.getBaseName(imageFile.getPath()));
    }

    static ImageFileInfo extractFileInfo(String text)
    {
        String[] namedParts = StringUtils.split(text, "_");
        final String plateLocationToken = StringUtils.split(namedParts[3], "-")[1];
        final String wellLocationToken = StringUtils.split(namedParts[4], "-")[1];
        final String timepointToken = StringUtils.split(namedParts[5], "-")[1];
        final String channelToken = StringUtils.split(namedParts[6], "-")[1];
        ImageFileInfo info = new ImageFileInfo();
        info.setPlateLocationToken(plateLocationToken);
        info.setWellLocationToken(wellLocationToken);
        info.setChannelToken(channelToken);
        info.setTimepointToken(timepointToken);
        return info;
    }

}
