/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.dataset;

import java.util.List;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.TechIdStringIdentifierRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectQuery;
import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;

import net.lemnik.eodsql.Select;

/**
 * @author pkupczyk
 */
public interface DataSetQuery extends ObjectQuery
{
    @Select(sql = "select id, code as identifier from data_all where code = any(?{1}) and del_id is null", parameterBindings = {
            StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<TechIdStringIdentifierRecord> listDataSetTechIdsByPermIds(String[] permIds);

}
