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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common;

import net.lemnik.eodsql.Select;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Query methods for retrieving property types, material types, and vocabulary URL templates.
 * 
 * @author Bernd Rinn
 */
public interface IPropertyListingQuery
{
    /**
     * Returns all property types. Fills only <code>id</code>, <code>code</code>,
     * <code>label</var> and <code>DataType</code>. Note that code and label are already HTML
     * escaped.
     */
    @Select(sql = "select pt.id as pt_id, pt.code as pt_code, dt.code as dt_code,"
            + "      pt.label as pt_label, pt.is_internal_namespace, pt.schema, pt.transformation"
            + "    from property_types pt join data_types dt on pt.daty_id=dt.id", resultSetBinding = PropertyTypeDataObjectBinding.class)
    public PropertyType[] getPropertyTypes();

    /**
     * Returns id and url template of all vocabularies.
     */
    @Select("select id, source_uri as code from controlled_vocabularies")
    public CodeRecord[] getVocabularyURLTemplates();

    /**
     * Returns id and code of all material types.
     */
    @Select("select id, code from material_types")
    public CodeRecord[] getMaterialTypes();

}
