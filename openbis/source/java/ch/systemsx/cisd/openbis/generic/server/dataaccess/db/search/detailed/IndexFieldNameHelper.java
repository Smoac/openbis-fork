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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.detailed;

import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.CODE;

import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * Helper for getting index field names of attributes and properties for detailed entity search.<br>
 * 
 * @author Piotr Buczek
 */
// TODO 2009-08-31, Piotr Buczek: write code for remaining entity kinds
class IndexFieldNameHelper
{
    // properties

    static String getPropertyIndexField(String propertyCode)
    {
        assert propertyCode != null : "property code is null";
        return SearchFieldConstants.PREFIX_PROPERTIES + propertyCode;
    }

    // attributes

    static IAttributeSearchFieldKind[] getAllAttributeFieldKinds(EntityKind entityKind)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return DataSetAttributeSearchFieldKind.values();
            default:
                throw new IllegalArgumentException("not implemented yet");
        }
    }

    static String getAttributeIndexField(EntityKind entityKind, String attributeCode)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return getDataSetAttributeIndexField(DataSetAttributeSearchFieldKind
                        .valueOf(attributeCode));
            default:
                throw new IllegalArgumentException("not implemented yet");
        }
    }

    private static String getDataSetAttributeIndexField(
            DataSetAttributeSearchFieldKind attributeKind)
    {
        switch (attributeKind)
        {
            case DATA_SET_CODE:
                return CODE;
            case DATA_SET_TYPE:
                return SearchFieldConstants.PREFIX_ENTITY_TYPE + CODE;
            case FILE_TYPE:
                return SearchFieldConstants.PREFIX_FILE_FORMAT_TYPE + CODE;
            default:
                throw InternalErr.error("unknown enum " + attributeKind);
        }
    }
}
