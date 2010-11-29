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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * Helper for getting index field names of attributes and properties for detailed entity search.<br>
 * 
 * @author Piotr Buczek
 */
class IndexFieldNameHelper
{
    // properties

    static String getPropertyIndexField(String propertyCode)
    {
        assert propertyCode != null : "property code is null";
        return SearchFieldConstants.PREFIX_PROPERTIES + propertyCode;
    }

    // attributes

    static String getAttributeIndexField(EntityKind entityKind, String attributeCode)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return getDataSetAttributeIndexField(DataSetAttributeSearchFieldKind
                        .valueOf(attributeCode));
            case EXPERIMENT:
                return getExperimentAttributeIndexField(ExperimentAttributeSearchFieldKind
                        .valueOf(attributeCode));
            case MATERIAL:
                return getMaterialAttributeIndexField(MaterialAttributeSearchFieldKind
                        .valueOf(attributeCode));
            case SAMPLE:
                return getSampleAttributeIndexField(SampleAttributeSearchFieldKind
                        .valueOf(attributeCode));
        }
        return null; // cannot happen
    }

    private static String getDataSetAttributeIndexField(
            DataSetAttributeSearchFieldKind attributeKind)
    {
        switch (attributeKind)
        {
            case CODE:
                return CODE;
            case DATA_SET_TYPE:
                return SearchFieldConstants.PREFIX_ENTITY_TYPE + CODE;
            case FILE_TYPE:
                return SearchFieldConstants.PREFIX_FILE_FORMAT_TYPE + CODE;
        }
        return null; // cannot happen
    }

    private static String getExperimentAttributeIndexField(
            ExperimentAttributeSearchFieldKind attributeKind)
    {
        switch (attributeKind)
        {
            case CODE:
                return CODE;
            case EXPERIMENT_TYPE:
                return SearchFieldConstants.PREFIX_ENTITY_TYPE + CODE;
            case PROJECT:
                return SearchFieldConstants.PREFIX_PROJECT + CODE;
            case PROJECT_GROUP:
                return SearchFieldConstants.PREFIX_PROJECT + SearchFieldConstants.PREFIX_SPACE
                        + CODE;
        }
        return null; // cannot happen
    }

    private static String getMaterialAttributeIndexField(
            MaterialAttributeSearchFieldKind attributeKind)
    {
        switch (attributeKind)
        {
            case CODE:
                return CODE;
            case MATERIAL_TYPE:
                return SearchFieldConstants.PREFIX_ENTITY_TYPE + CODE;
        }
        return null; // cannot happen
    }

    private static String getSampleAttributeIndexField(SampleAttributeSearchFieldKind attributeKind)
    {
        switch (attributeKind)
        {
            case CODE:
                return CODE;
            case SAMPLE_TYPE:
                return SearchFieldConstants.PREFIX_ENTITY_TYPE + CODE;
            case GROUP:
                return SearchFieldConstants.PREFIX_SPACE + CODE;
        }
        return null; // cannot happen
    }
}
