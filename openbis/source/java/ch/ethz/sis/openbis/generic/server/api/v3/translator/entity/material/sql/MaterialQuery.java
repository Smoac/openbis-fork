/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import net.lemnik.eodsql.Select;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectQuery;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.common.sql.ObjectRelationRecord;
import ch.systemsx.cisd.common.db.mapper.LongSetMapper;

/**
 * @author pkupczyk
 */
public interface MaterialQuery extends ObjectQuery
{

    @Select(sql = "select m.id, m.code, mt.code as typeCode, m.pers_id_registerer as registererId, m.registration_timestamp as registrationDate, m.modification_timestamp as modificationDate "
            + "from materials m, material_types mt "
            + "where m.maty_id = mt.id and m.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialBaseRecord> getMaterials(LongSet materialIds);

    @Select(sql = "select m.id as objectId, m.maty_id as relatedId from materials m where m.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTypeIds(LongSet materialIds);

    @Select(sql = "select mt.id, mt.code, mt.description, mt.modification_timestamp as modificationDate from material_types mt where mt.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialTypeBaseRecord> getTypes(LongSet materialTypeIds);

    @Select(sql = "select mp.mate_id as materialId, pt.code as propertyCode, mp.value as propertyValue, m.code as materialPropertyValueCode, mt.code as materialPropertyValueTypeCode, cvt.code as vocabularyPropertyValue "
            + "from material_properties mp "
            + "left outer join materials m on mp.mate_prop_id = m.id "
            + "left outer join controlled_vocabulary_terms cvt on mp.cvte_id = cvt.id "
            + "left join material_types mt on m.maty_id = mt.id "
            + "left join material_type_property_types mtpt on mp.mtpt_id = mtpt.id "
            + "left join property_types pt on mtpt.prty_id = pt.id "
            + "where mp.mate_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialPropertyRecord> getProperties(LongSet materialIds);

    @Select(sql = "select mp.mate_id as materialId, pt.code as propertyCode, mp.mate_prop_id as propertyValue "
            + "from material_properties mp "
            + "left join material_type_property_types mtpt on mp.mtpt_id = mtpt.id "
            + "left join property_types pt on mtpt.prty_id = pt.id "
            + "where mp.mate_prop_id is not null and mp.mate_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<MaterialMaterialPropertyRecord> getMaterialProperties(LongOpenHashSet materialIds);

    @Select(sql = "select m.id as objectId, m.pers_id_registerer as relatedId from materials m where m.id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getRegistratorIds(LongSet materialIds);

    @Select(sql = "select ma.mate_id as objectId, ma.mepr_id as relatedId from metaproject_assignments ma where ma.mate_id = any(?{1})", parameterBindings = { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public List<ObjectRelationRecord> getTagIds(LongSet materialIds);

}
