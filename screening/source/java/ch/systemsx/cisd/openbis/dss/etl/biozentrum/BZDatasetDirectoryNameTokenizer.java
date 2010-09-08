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

import org.apache.commons.lang.StringUtils;

/**
 * Extracts useful information from dataset directory name specific to iBrain2.
 * 
 * @author Izabela Adamczyk
 */
public class BZDatasetDirectoryNameTokenizer
{
    private final String experimentToken;

    private final String plateToken;

    private final String barcodeToken;

    private final String timestampToken;

    BZDatasetDirectoryNameTokenizer(String identifier)
    {
        String[] namedParts = StringUtils.split(identifier, "_");
        experimentToken = StringUtils.split(namedParts[0], "-")[1];
        plateToken = StringUtils.split(namedParts[1], "-")[1];
        barcodeToken = StringUtils.split(namedParts[2], "-")[1];
        timestampToken = StringUtils.split(namedParts[3], "-")[1];
    }

    public String getExperimentToken()
    {
        return experimentToken;
    }

    public String getPlateToken()
    {
        return plateToken;
    }

    public String getBarcodeToken()
    {
        return barcodeToken;
    }

    public String getTimestampToken()
    {
        return timestampToken;
    }

}