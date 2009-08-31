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

package ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongSetMapper;

/**
 * This extension of {@link IDatasetListingQuery} provides set-based query methods. As not all
 * database engines support this, it shouldn't be called directly in a BO but only via
 * {@link IDatasetSetListingQuery} implementation.
 * <p>
 * This interface is intended to be used only in this package. The <code>public</code> modifier is
 * needed for creating a dynamic proxy by the EOD SQL library.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { IDatasetListingQuery.class, IDatasetSetListingQuery.class })
@Private
public interface IDatasetListingFullQuery extends IDatasetListingQuery
{

    /**
     * Returns the datasets for the given <var>entityIds</var>.
     * <p>
     * <em>Do not call directly, call via {@link IDatasetSetListingQuery}</em>
     */
    @Select(sql = " * from data left outer join external_data on data.id = external_data.data_id where data.id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<DatasetRecord> getDatasets(LongSet entityIds);

    /**
     * Returns the total number of all datasets in the database.
     */
    @Select(sql = "select count(*) from data")
    public long getDatasetCount();

    //
    // Entity Properties
    //

    /**
     * Returns all generic property values of all datasets specified by <var>entityIds</var>.
     * <p>
     * <em>Do not call directly, call via {@link IDatasetSetListingQuery}</em>
     * 
     * @param entityIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select pr.ds_id as entity_id, etpt.prty_id, pr.value from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "   where pr.value is not null and pr.ds_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet entityIds);

    /**
     * Returns all controlled vocabulary property values of all datasets specified by
     * <var>entityIds</var>.
     * <p>
     * <em>Do not call directly, call via {@link IDatasetSetListingQuery}</em>
     * 
     * @param entityIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select pr.ds_id as entity_id, etpt.prty_id, cvte.id, cvte.covo_id, cvte.code, cvte.label"
            + "      from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "      join controlled_vocabulary_terms cvte on pr.cvte_id=cvte.id"
            + "   where pr.ds_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
            LongSet entityIds);

    /**
     * Returns all material-type property values of all datasets specified by <var>entityIds</var>.
     * <p>
     * <em>Do not call directly, call via {@link IDatasetSetListingQuery}</em>
     * 
     * @param entityIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select sp.ds_id as entity_id, etpt.prty_id, m.id, m.code, m.maty_id"
            + "      from data_set_properties pr"
            + "      join data_set_type_property_types etpt on pr.dstpt_id=etpt.id"
            + "      join materials m on pr.mate_prop_id=m.id where pr.ds_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
            LongSet entityIds);
}
