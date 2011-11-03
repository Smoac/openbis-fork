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

package ch.systemsx.cisd.openbis.etlserver.proteomics;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.etlserver.proteomics.dto.ProteinAnnotation;

final class ProteinDescription
{
    static final String DESCRIPTION_KEY = "DE";

    static final String SEQUENCE_KEY = "SEQ";

    static String createKeyValuePair(String key, String value)
    {
        return "\\" + key + "=" + value;
    }

    private String accessionNumber;

    private String description;

    private String sequence;

    public ProteinDescription(ProteinAnnotation annotation, long proteinID,
            boolean assumingExtendedProtXML)
    {
        String proteinDescription = annotation.getDescription();
        String[] items = proteinDescription.split("\\\\");
        accessionNumber = tryToGetAccessionNumber(items);
        description = tryToGetValue(items, DESCRIPTION_KEY);
        sequence = tryToGetValue(items, SEQUENCE_KEY);
        if (sequence == null)
        {
            if (assumingExtendedProtXML)
            {
                throw new UserFailureException(
                        "Can not find a amino-acid sequence in following protein description: "
                                + proteinDescription);
            } else
            {
                sequence = "";
                accessionNumber = getAccessionNumber(annotation, proteinID);
            }
        }
        if (description == null && assumingExtendedProtXML == false)
        {
            description = proteinDescription;
        }
    }
    
    private String getAccessionNumber(ProteinAnnotation annotation, long proteinID)
    {
        if (annotation.getSwissprotName() != null)
        {
            return "sp|" + annotation.getSwissprotName();
        }
        if (annotation.getTremblName() != null)
        {
            return "tr|" + annotation.getTremblName();
        }
        if (annotation.getIpiName() != null)
        {
            return "ipi|" + annotation.getIpiName();
        }
        if (annotation.getEnsemblName() != null)
        {
            return "ens|" + annotation.getEnsemblName();
        }
        if (annotation.getRefseqName() != null)
        {
            return "rs|" + annotation.getRefseqName();
        }
        if (annotation.getLocusLinkName() != null)
        {
            return "ll|" + annotation.getLocusLinkName();
        }
        if (annotation.getFlybase() != null)
        {
            return "fb|" + annotation.getFlybase();
        }
        return "unknown|" + proteinID;
    }

    public final String getAccessionNumber()
    {
        return accessionNumber;
    }

    public final String getDescription()
    {
        return description;
    }

    public final String getSequence()
    {
        return sequence;
    }

    private String tryToGetAccessionNumber(String[] items)
    {
        return items == null || items.length == 0 ? null : items[0].trim();
    }

    private String tryToGetValue(String[] items, String key)
    {
        for (String item : items)
        {
            int indexOfEqualSign = item.indexOf('=');
            if (indexOfEqualSign > 0
                    && item.substring(0, indexOfEqualSign).trim().equalsIgnoreCase(key))
            {
                return item.substring(indexOfEqualSign + 1).trim();
            }
        }
        return null;
    }
}