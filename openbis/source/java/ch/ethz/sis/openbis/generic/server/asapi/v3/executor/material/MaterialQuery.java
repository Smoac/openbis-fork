/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material;

import java.util.List;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TypeMapper;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.TechIdStringIdentifierRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.common.ObjectQuery;
import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface MaterialQuery extends ObjectQuery
{
    @Select(sql = "select m.id, m.code as identifier from materials m join material_types t on m.maty_id = t.id "
            + "where t.code = ?{1} and m.code = any(?{2})", parameterBindings =
        { TypeMapper.class, StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<TechIdStringIdentifierRecord> listMaterialTechIdsByCodes(String typeCode, String[] codes);

}
