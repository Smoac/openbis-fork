/*
 * Copyright 2008 ETH Zuerich, CISD
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

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;

/**
 * Translates {@link DataSetTypePE} to {@link DataSetType}.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetTypeTranslator
{

    private DataSetTypeTranslator()
    {
    }

    public static DataSetType translate(DataSetTypePE entityTypeOrNull)
    {
        if (entityTypeOrNull == null)
        {
            return null;
        }
        final DataSetType result = new DataSetType();
        result.setCode(StringEscapeUtils.escapeHtml(entityTypeOrNull.getCode()));
        result.setDescription(StringEscapeUtils.escapeHtml(entityTypeOrNull.getDescription()));
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(entityTypeOrNull
                .getDatabaseInstance()));
        result.setDataSetTypePropertyTypes(DataSetTypePropertyTypeTranslator.translate(
                entityTypeOrNull.getDataSetTypePropertyTypes(), result));
        return result;
    }

    public static DataSetTypePE translate(DataSetType type)
    {
        final DataSetTypePE result = new DataSetTypePE();
        result.setCode(type.getCode());
        return result;
    }

}
