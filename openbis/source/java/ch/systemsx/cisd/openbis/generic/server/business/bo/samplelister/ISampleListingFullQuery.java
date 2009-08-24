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

package ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongSetMapper;

/**
 * This extension of {@link ISampleListingQuery} provides set-based query methods. As not all
 * database engines support this, it shouldn't be called directly in a BO but only via
 * {@link ISampleSetListingQuery} implementation.
 * <p>
 * This interface is intended to be used only in this package. The <code>public</code> modifier
 * is needed for creating a dynamic proxy by the EOD SQL library.
 * 
 * @author Bernd Rinn
 */
@Private
public interface ISampleListingFullQuery extends ISampleListingQuery
{

    //
    // Samples
    //

    /**
     * Returns the samples for the given <var>sampleIds</var>.
     * <p>
     * <em>Do not call directly, call via {@link ISampleSetListingQuery}</em>
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.expe_id, "
            + "       s.registration_timestamp, s.pers_id_registerer, "
            + "       s.samp_id_generated_from, s.samp_id_part_of, s.saty_id, s.inva_id "
            + "   from samples s where s.id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamples(LongSet sampleIds);

    //
    // Sample Properties
    //

    /**
     * Returns all generic property values of all samples specified by <var>sampleIds</var>.
     * <p>
     * <em>Do not call directly, call via {@link ISampleSetListingQuery}</em>
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select sp.samp_id as entity_id, stpt.prty_id, sp.value from sample_properties sp"
            + "      join sample_type_property_types stpt on sp.stpt_id=stpt.id"
            + "   where sp.value is not null and sp.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getSamplePropertyGenericValues(LongSet sampleIds);

    /**
     * Returns all controlled vocabulary property values of all samples specified by
     * <var>sampleIds</var>.
     * <p>
     * <em>Do not call directly, call via {@link ISampleSetListingQuery}</em>
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select sp.samp_id as entity_id, stpt.prty_id, cvte.id, cvte.covo_id, cvte.code, cvte.label"
            + "      from sample_properties sp"
            + "      join sample_type_property_types stpt on sp.stpt_id=stpt.id"
            + "      join controlled_vocabulary_terms cvte on sp.cvte_id=cvte.id"
            + "   where sp.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<VocabularyTermRecord> getSamplePropertyVocabularyTermValues(
            LongSet sampleIds);

    /**
     * Returns all material-type property values of all samples specified by <var>sampleIds</var>.
     * <p>
     * <em>Do not call directly, call via {@link ISampleSetListingQuery}</em>
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select sp.samp_id as entity_id, stpt.prty_id, m.id, m.code, m.maty_id"
            + "      from sample_properties sp"
            + "      join sample_type_property_types stpt on sp.stpt_id=stpt.id"
            + "      join materials m on sp.mate_prop_id=m.id where sp.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<MaterialEntityPropertyRecord> getSamplePropertyMaterialValues(LongSet sampleIds);

}
