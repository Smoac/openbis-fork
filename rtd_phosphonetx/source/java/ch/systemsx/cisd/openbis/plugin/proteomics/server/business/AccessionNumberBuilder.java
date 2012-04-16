/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.business;

/**
 * Parses accession numbers of the form
 * <pre>
 * ['DECOY_'][&lt;type&gt;'|']&lt;accession number&gt;['|'&lt;some string&gt;]
 * </pre>
 *
 * @author Franz-Josef Elmer
 */
public class AccessionNumberBuilder
{
    static final String DECOY_PREFIX = "DECOY_";
    
    private static final char SEPARATOR = '|';

    private final String typeOrNull;
    
    private final String accessionNumber;

    
    public AccessionNumberBuilder(String fullAccessionNumber)
    {
        int indexOfFirstSeparator = fullAccessionNumber.indexOf(SEPARATOR);
        if (indexOfFirstSeparator < 0)
        {
            typeOrNull = null;
            accessionNumber = fullAccessionNumber;
        } else
        {
            String firstItem = fullAccessionNumber.substring(0, indexOfFirstSeparator);
            int decoyPrefixIndex = firstItem.startsWith(DECOY_PREFIX) ? DECOY_PREFIX.length() : 0;
            String prefix = firstItem.substring(0, decoyPrefixIndex);
            typeOrNull = firstItem.substring(decoyPrefixIndex);
            int startIndex = indexOfFirstSeparator + 1;
            int indexOfSecondSeparator = fullAccessionNumber.indexOf(SEPARATOR, startIndex);
            if (indexOfSecondSeparator < 0)
            {
                accessionNumber =
                        addPrefixIfNecessary(prefix, fullAccessionNumber.substring(startIndex));
            } else
            {
                accessionNumber =
                        addPrefixIfNecessary(prefix,
                                fullAccessionNumber.substring(startIndex, indexOfSecondSeparator));
            }
        }
    }
    
    private String addPrefixIfNecessary(String prefix, String string)
    {
        return string.startsWith(prefix) ? string : prefix + string;
    }

    public final String getTypeOrNull()
    {
        return typeOrNull;
    }

    public final String getAccessionNumber()
    {
        return accessionNumber;
    }
}
