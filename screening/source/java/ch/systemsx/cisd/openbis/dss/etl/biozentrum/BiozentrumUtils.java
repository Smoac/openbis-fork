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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.dss.etl.AbstractHCSImageFileExtractor.UnparsedImageFileInfo;

/**
 * Utility class containing methods useful in iBrain integration.
 * 
 * @author Izabela Adamczyk
 */
public class BiozentrumUtils
{
    static UnparsedImageFileInfo extractImageFileInfo(File imageFile)
    {
        return extractBZImageFileInfo(FilenameUtils.getBaseName(imageFile.getPath()));
    }

    /**
     * Extracts useful information from dataset image file name specific to iBrain2.
     */
    @Private
    static UnparsedImageFileInfo extractBZImageFileInfo(String text)
    {
        // example: bDZ01-1A_wD17_s3_z0_t0_cGFP.tif
        // bDZ01-1A_ plate
        // wD17_ well
        // s3_ tile
        // z0_ depth
        // t0_ timepoint
        // cGFP.tif channel
        String[] namedParts = StringUtils.split(text, "_");
        final String plateLocationToken = extractToken(namedParts[1]);
        final String wellLocationToken = extractToken(namedParts[2]);
        final String channelToken = extractToken(namedParts[5]);
        final String timepointToken = extractToken(namedParts[4]);
        final String depthToken = extractToken(namedParts[3]);

        UnparsedImageFileInfo info = new UnparsedImageFileInfo();
        info.setPlateLocationToken(plateLocationToken);
        info.setWellLocationToken(wellLocationToken);
        info.setChannelToken(channelToken);
        info.setTimepointToken(timepointToken);
        info.setDepthToken(depthToken);
        return info;
    }

    private static String extractToken(String token)
    {
        return token.substring(1);
    }

}
