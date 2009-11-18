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

import java.sql.ResultSet;
import java.sql.SQLException;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.spi.util.NonUpdateCapableDataObjectBinding;

import org.apache.commons.lang.StringEscapeUtils;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectGroupCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongSetMapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * A {@link TransactionQuery} interface for obtaining large sets of sample-related entities from the
 * database.
 * <p>
 * This interface is intended to be used only in this package. The <code>public</code> modifier is
 * needed for creating a dynamic proxy by the EOD SQL library.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
    { ExperimentProjectGroupCodeRecord.class })
@Private
public interface ISampleListingQuery extends TransactionQuery, IPropertyListingQuery
{

    public static final int FETCH_SIZE = 1000;

    /**
     * Returns the total number of all samples in the database.
     */
    @Select(sql = "select count(*) from samples s left join groups g on s.grou_id=g.id where s.dbin_id=?{1} or g.dbin_id=?{1}")
    public long getSampleCount(long dbInstanceId);

    /**
     * Returns the sample for the given <var>sampleId</var>.
     */
    @Select("select s.id, s.perm_id, s.code, s.expe_id, s.grou_id, s.dbin_id, "
            + "       s.registration_timestamp, s.pers_id_registerer, "
            + "       s.samp_id_generated_from, s.samp_id_part_of, s.saty_id, s.inva_id "
            + "   from samples s where s.id=?{1}")
    public SampleRecord getSample(long sampleId);

    /**
     * Returns all samples in the database.
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.expe_id, s.grou_id, s.dbin_id, "
            + "       s.registration_timestamp, s.pers_id_registerer, "
            + "       s.samp_id_generated_from, s.samp_id_part_of, s.saty_id, s.inva_id "
            + "   from samples s left join groups g on s.grou_id=g.id where s.dbin_id=?{1} or g.dbin_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamples(long dbInstanceId);

    //
    // Samples for group
    //

    /**
     * Returns the samples for the given <var>groupCode</var>.
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.expe_id, s.grou_id, s.dbin_id, "
            + "       s.registration_timestamp, s.pers_id_registerer, "
            + "       s.samp_id_generated_from, s.samp_id_part_of, s.saty_id, s.inva_id "
            + "   from samples s join sample_types st on s.saty_id=st.id"
            + " join groups g on s.grou_id=g.id "
            + "   where st.is_listable and g.dbin_id=?{1} and g.code=?{2} order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getGroupSamples(long dbInstanceId, String groupCode);

    /**
     * Returns the samples for the given <var>groupCode</var> that are assigned to an experiment.
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.saty_id, s.expe_id, s.grou_id, s.dbin_id, "
            + "       s.samp_id_generated_from, s.registration_timestamp, s.modification_timestamp, "
            + "       s.pers_id_registerer, s.samp_id_part_of, s.inva_id "
            + "   from samples s join groups g on s.grou_id=g.id "
            + "   where s.expe_id is not null and g.dbin_id=?{1} and g.code=?{2} "
            + "   order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getGroupSamplesWithExperiment(long dbInstanceId,
            String groupCode);

    /**
     * Returns the samples for the given <var>groupCode</var> and <var>sampleTypeId</var>
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.expe_id, s.grou_id, s.dbin_id, "
            + "       s.registration_timestamp, s.pers_id_registerer, "
            + "       s.samp_id_generated_from, s.samp_id_part_of, s.saty_id, s.inva_id "
            + "   from samples s join groups g on s.grou_id=g.id "
            + "   where g.dbin_id=?{1} and g.code=?{2} and s.saty_id=?{3}"
            + "      order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getGroupSamplesForSampleType(long dbInstanceId,
            String groupCode, long sampleTypeId);

    /**
     * Returns the samples for the given <var>groupCode</var> and <var>sampleTypeId</var> that are
     * assigned to an experiment.
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.saty_id, s.expe_id, s.grou_id, s.dbin_id, "
            + "       s.samp_id_generated_from, s.registration_timestamp, s.modification_timestamp, "
            + "       s.pers_id_registerer, s.samp_id_part_of, s.inva_id "
            + "   from samples s  join groups g on s.grou_id=g.id "
            + "   where s.expe_id is not null and g.dbin_id=?{1} and g.code=?{2}"
            + " and s.saty_id=?{3} order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getGroupSamplesForSampleTypeWithExperiment(long dbInstanceId,
            String groupCode, long sampleTypeId);

    //
    // Samples for experiment
    //

    /**
     * Returns the samples for the given <var>experimentId</var>.
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.expe_id, s.grou_id, s.dbin_id, "
            + "       s.registration_timestamp, s.pers_id_registerer, "
            + "       s.samp_id_generated_from, s.samp_id_part_of, s.saty_id, s.inva_id "
            + "   from samples s where s.expe_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesForExperiment(long experimentId);

    //
    // Samples for container
    //

    /**
     * Returns the samples for the given <var>sampleContainerId</var>.
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.expe_id, s.grou_id, s.dbin_id, "
            + "       s.registration_timestamp, s.pers_id_registerer, "
            + "       s.samp_id_generated_from, s.samp_id_part_of, s.saty_id, s.inva_id "
            + "   from samples s where s.samp_id_part_of=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesForContainer(long sampleContainerId);

    //
    // New samples of type
    //

    /**
     * Returns all samples of sample type with given <var>sampleTypeId</var> that are newer than
     * sample with given id (<var>lastSeenSampleId</var>).
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.expe_id, s.grou_id, s.dbin_id, "
            + "       s.registration_timestamp, s.pers_id_registerer, "
            + "       s.samp_id_generated_from, s.samp_id_part_of, s.saty_id, s.inva_id "
            + "   from samples s where s.saty_id=?{1} and s.id > ?{2}", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getNewSamplesForSampleType(long sampleTypeId,
            int lastSeenSampleId);

    //
    // Shared samples
    //

    /**
     * Returns the shared samples for the given <var>dbInstanceId</var>.
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.expe_id, s.dbin_id, "
            + "       s.registration_timestamp, s.pers_id_registerer, "
            + "       s.samp_id_generated_from, s.samp_id_part_of, s.saty_id, s.inva_id "
            + "   from samples s join sample_types st on s.saty_id=st.id "
            + "   where st.is_listable and s.dbin_id=?{1} order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSharedSamples(long dbInstanceId);

    /**
     * Returns the shared samples for the given <var>dbInstanceId</var> and <var>sampleTypeId</var>.
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.expe_id, s.dbin_id, "
            + "       s.registration_timestamp, s.pers_id_registerer, "
            + "       s.samp_id_generated_from, s.samp_id_part_of, s.saty_id, s.inva_id "
            + "   from samples s where s.dbin_id=?{1} and s.saty_id=?{2} order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSharedSamplesForSampleType(long dbInstanceId,
            long sampleTypeId);

    //
    // Types
    //

    /**
     * A binding for the {@link ISampleListingQuery#getPropertyTypes()} query.
     */
    static class SampleTypeDataObjectBinding extends NonUpdateCapableDataObjectBinding<SampleType>
    {
        @Override
        public void unmarshall(ResultSet row, SampleType into) throws SQLException, EoDException
        {
            into.setId(row.getLong("id"));
            into.setCode(StringEscapeUtils.escapeHtml(row.getString("code")));
            into.setGeneratedFromHierarchyDepth(row.getInt("generated_from_depth"));
            into.setShowContainer(row.getInt("part_of_depth") > 0);
        }
    }

    /**
     * Returns the sample type for the given <code>sampleCode</code>. Note that the code of the
     * result is already HTML escaped.
     */
    @Select(sql = "select id, code, generated_from_depth, part_of_depth from sample_types"
            + "      where code=?{2} and dbin_id=?{1}", resultSetBinding = SampleTypeDataObjectBinding.class)
    public SampleType getSampleType(long dbInstanceId, String sampleCode);

    /**
     * Returns all sample types.
     */
    @Select(sql = "select id, code, generated_from_depth, part_of_depth from sample_types where dbin_id=?{1}", resultSetBinding = SampleTypeDataObjectBinding.class)
    public SampleType[] getSampleTypes(long dbInstanceId);

    //
    // Samples
    //

    /**
     * Returns the samples for the given <var>sampleIds</var>.
     */
    @Select(sql = "select s.id, s.perm_id, s.code, s.expe_id, s.grou_id, s.dbin_id, "
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
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select sp.samp_id as entity_id, stpt.prty_id, sp.value from sample_properties sp"
            + "      join sample_type_property_types stpt on sp.stpt_id=stpt.id"
            + "   where sp.value is not null and sp.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet sampleIds);

    /**
     * Returns all controlled vocabulary property values of all samples specified by
     * <var>sampleIds</var>.
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select sp.samp_id as entity_id, stpt.prty_id, cvte.id, cvte.covo_id, cvte.code, cvte.label, cvte.ordinal"
            + "      from sample_properties sp"
            + "      join sample_type_property_types stpt on sp.stpt_id=stpt.id"
            + "      join controlled_vocabulary_terms cvte on sp.cvte_id=cvte.id"
            + "   where sp.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
            LongSet sampleIds);

    /**
     * Returns all material-type property values of all samples specified by <var>sampleIds</var>.
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    @Select(sql = "select sp.samp_id as entity_id, stpt.prty_id, m.id, m.code, m.maty_id"
            + "      from sample_properties sp"
            + "      join sample_type_property_types stpt on sp.stpt_id=stpt.id"
            + "      join materials m on sp.mate_prop_id=m.id where sp.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
            LongSet sampleIds);

}
