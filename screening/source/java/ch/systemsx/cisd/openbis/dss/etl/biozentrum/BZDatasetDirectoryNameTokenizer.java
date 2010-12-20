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

import java.util.Map;

import ch.systemsx.cisd.openbis.dss.etl.UnparsedImageFileInfoLexer;

/**
 * Extracts useful information from dataset directory name specific to iBrain2.
 * 
 * @author Izabela Adamczyk
 */
public class BZDatasetDirectoryNameTokenizer
{
    private static final char EXPERIMENT_MARKER = 'i';

    private static final char MICROSCOPE_MARKER = 'm';

    private static final char PLATE_BARCODE_MARKER = 'b';

    private static final char UNIQUE_ID_MARKER = 'u';

    private final String experimentToken;

    private final String microscopeToken;

    private final String barcodeToken;

    private final String timestampToken;

    BZDatasetDirectoryNameTokenizer(String identifier)
    {
        Map<Character, String> tokensMap = UnparsedImageFileInfoLexer.extractTokensMap(identifier);
        experimentToken = tokensMap.get(EXPERIMENT_MARKER);
        microscopeToken = tokensMap.get(MICROSCOPE_MARKER);
        barcodeToken = tokensMap.get(PLATE_BARCODE_MARKER);
        timestampToken = tokensMap.get(UNIQUE_ID_MARKER);
    }

    public String getExperimentToken()
    {
        return experimentToken;
    }

    public String getMicroscopeToken()
    {
        return microscopeToken;
    }

    public String getPlateBarcodeToken()
    {
        return barcodeToken;
    }

    public String getUniqueIdToken()
    {
        return timestampToken;
    }

}