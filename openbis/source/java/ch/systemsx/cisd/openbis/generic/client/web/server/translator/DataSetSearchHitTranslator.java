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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetSearchHit;
import ch.systemsx.cisd.openbis.generic.client.web.server.translator.ExperimentTranslator.LoadableFields;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetSearchHitDTO;

/**
 * Converts {@link DataSetSearchHitDTO}s to {@link DataSetSearchHit}s.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetSearchHitTranslator
{
    private DataSetSearchHitTranslator()
    {
    }

    public static List<DataSetSearchHit> translate(List<DataSetSearchHitDTO> list)
    {
        ArrayList<DataSetSearchHit> result = new ArrayList<DataSetSearchHit>(list.size());
        for (DataSetSearchHitDTO item : list)
        {
            result.add(translate(item));
        }
        return result;
    }

    public static DataSetSearchHit translate(DataSetSearchHitDTO hit)
    {
        DataSetSearchHit result = new DataSetSearchHit();
        result.setDataSet(ExternalDataTranslator.translate(hit.getDataSet(),
                LoadableFields.PROPERTIES));
        return result;
    }

}
