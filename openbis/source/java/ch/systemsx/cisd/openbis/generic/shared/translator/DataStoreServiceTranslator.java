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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;

/**
 * Translator for {@link DataStoreServicePE} into {@link DatastoreServiceDescription}.
 *
 * @author Franz-Josef Elmer
 */
public class DataStoreServiceTranslator
{
    public static DatastoreServiceDescription translate(DataStoreServicePE service)
    {
        String[] datasetTypeCodes = extractCodes(service.getDatasetTypes());
        String dssCode = service.getDataStore().getCode();
        DatastoreServiceDescription dssDescription =
                new DatastoreServiceDescription(service.getKey(), service.getLabel(),
                        datasetTypeCodes, dssCode);
        dssDescription.setDownloadURL(service.getDataStore().getDownloadUrl());
        return dssDescription;
    }

    private static String[] extractCodes(Set<DataSetTypePE> datasetTypes)
    {
        String[] codes = new String[datasetTypes.size()];
        int i = 0;
        for (DataSetTypePE datasetType : datasetTypes)
        {
            codes[i] = datasetType.getCode();
            i++;
        }
        return codes;
    }

    
    private DataStoreServiceTranslator()
    {
    }

}
