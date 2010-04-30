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

package ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongSetMapper;

/**
 * A {@link TransactionQuery} interface for obtaining large sets of material-related entities from
 * the database.
 * <p>
 * This interface is intended to be used only in this package. The <code>public</code> modifier is
 * needed for creating a dynamic proxy by the EOD SQL library.
 * 
 * @author Tomasz Pylak
 */
@Private
public interface IMaterialListingQuery extends TransactionQuery, IPropertyListingQuery
{
    public static final int FETCH_SIZE = 1000;

    /**
     * Returns the materials for the given <var>materialTypeId</var>
     */
    @Select(sql = "select m.id, m.code, m.dbin_id, m.maty_id, "
            + "           m.registration_timestamp, m.modification_timestamp, m.pers_id_registerer "
            + "      from materials m where m.dbin_id=?{1} and m.maty_id=?{2} "
            + "  order by m.code", fetchSize = FETCH_SIZE)
    public DataIterator<MaterialRecord> getMaterialsForMaterialType(long dbInstanceId,
            long materialTypeId);

    //
    // Entity Properties
    //

    /**
     * Returns all generic property values of all materials specified by <var>entityIds</var>.
     * 
     * @param entityIds The set of material ids to get the property values for.
     */
    @Select(sql = "select pr.mate_id as entity_id, etpt.prty_id, pr.value from material_properties pr"
            + "      join material_type_property_types etpt on pr.mtpt_id=etpt.id"
            + "   where pr.value is not null and pr.mate_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet entityIds);

    /**
     * Returns all controlled vocabulary property values of all materials specified by
     * <var>entityIds</var>.
     * 
     * @param entityIds The set of material ids to get the property values for.
     */
    @Select(sql = "select pr.mate_id as entity_id, etpt.prty_id, cvte.id, cvte.covo_id, cvte.code, cvte.label, cvte.ordinal"
            + "      from material_properties pr"
            + "      join material_type_property_types etpt on pr.mtpt_id=etpt.id"
            + "      join controlled_vocabulary_terms cvte on pr.cvte_id=cvte.id"
            + "   where pr.mate_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
            LongSet entityIds);

    /**
     * Returns all material-type property values of all materials specified by <var>entityIds</var>.
     * 
     * @param entityIds The set of material ids to get the property values for.
     */
    @Select(sql = "select pr.mate_id as entity_id, etpt.prty_id, m.id, m.code, m.maty_id"
            + "      from material_properties pr"
            + "      join material_type_property_types etpt on pr.mtpt_id=etpt.id"
            + "      join materials m on pr.mate_prop_id=m.id where pr.mate_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
            LongSet entityIds);

}
