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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.List;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TypeMapper;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.TechIdStringIdentifierRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.ObjectQuery;
import ch.systemsx.cisd.common.db.mapper.StringArrayMapper;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface SampleQuery extends ObjectQuery
{
    @Select(sql = "select id, perm_id as identifier from samples where perm_id = any(?{1})", parameterBindings =
    { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<TechIdStringIdentifierRecord> listSampleTechIdsByPermIds(String[] permIds);

    @Select(sql = "select id, code as identifier from samples "
            + "where space_id is null and samp_id_part_of is null and code = any(?{1})", parameterBindings =
    { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<TechIdStringIdentifierRecord> listSharedSampleTechIdsByCodes(String[] codes);
    
    @Select(sql = "select s.id, s.code as identifier from samples s join samples cs on s.samp_id_part_of = cs.id "
            + "where s.space_id is null and cs.code = ?{1} and s.code = any(?{2})", parameterBindings =
        { TypeMapper.class, StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<TechIdStringIdentifierRecord> listSharedSampleTechIdsByContainerCodeAndCodes(String containerCode, 
            String[] codes);
    
    @Select(sql = "select s.id, s.code as identifier from samples s join spaces sp on s.space_id = sp.id "
            + "where sp.code = ?{1} and samp_id_part_of is null and s.code = any(?{2})", parameterBindings =
            { TypeMapper.class, StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<TechIdStringIdentifierRecord> listSpaceSampleTechIdsByCodes(String spaceCode, String[] codes);
    
    @Select(sql = "select s.id, s.code as identifier from samples s join spaces sp on s.space_id = sp.id "
            + "join samples cs on s.samp_id_part_of = cs.id "
            + "where sp.code = ?{1}  and cs.code = ?{2} and s.code = any(?{3})", parameterBindings =
        { TypeMapper.class, TypeMapper.class, StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public List<TechIdStringIdentifierRecord> listSpaceSampleTechIdsByContainerCodeAndCodes(String spaceCode, 
            String containerCode, String[] codes);
}
