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
import net.lemnik.eodsql.TypeMapper;
import net.lemnik.eodsql.spi.util.NonUpdateCapableDataObjectBinding;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.GenericEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.IPropertyListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.MaterialEntityPropertyRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.VocabularyTermRecord;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity.ExperimentProjectGroupCodeRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongSetMapper;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.StringArrayMapper;
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

    static final String SELECT_FROM_SAMPLES_S =
            "           SELECT s.id, s.perm_id, s.code, s.expe_id, s.space_id, s.saty_id, s.dbin_id, "
                    + "   s.registration_timestamp, s.modification_timestamp, s.pers_id_registerer, "
                    + "   s.inva_id, s.samp_id_part_of                                              "
                    + " FROM samples s";

    /**
     * Returns the total number of all samples in the database.
     */
    @Select(sql = "select count(*) from samples s left join spaces g on s.space_id=g.id where s.dbin_id=?{1} or g.dbin_id=?{1}")
    public long getSampleCount(long dbInstanceId);

    // relationships

    /**
     * Returns the relationship type with given <var>code</var> and namespace.
     */
    @Select(sql = "select * from relationship_types where code=?{1} and is_internal_namespace=?{2}")
    public long getRelationshipTypeId(String code, boolean internalNamespace);

    /**
     * Returns the children sample ids of the specified samples in specified relationship.
     */
    @Select(sql = "SELECT sample_id_child FROM sample_relationships "
            + "    WHERE relationship_id=?{1} AND sample_id_parent = any(?{2})", parameterBindings =
        { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getChildrenIds(long relationshipId, LongSet parentSampleIds);

    /**
     * Returns the parent sample ids of the specified samples in specified relationship.
     */
    @Select(sql = "SELECT sample_id_parent FROM sample_relationships "
            + "    WHERE relationship_id=?{1} AND sample_id_child = any(?{2})", parameterBindings =
        { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<Long> getParentIds(long relationshipId, LongSet childrenSampleIds);

    /**
     * Returns the parent sample ids of the specified children sample ids in specified relationship.
     */
    @Select(sql = "SELECT * FROM sample_relationships "
            + "    WHERE relationship_id=?{1} AND sample_id_child = any(?{2})", parameterBindings =
        { TypeMapper.class/* default */, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRelationRecord> getParentRelations(long relationshipId,
            LongSet childrenSampleIds);

    @Select(sql = "select id, saty_id, space_id, dbin_id, expe_id from samples", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSampleSkeletons();

    @Select(sql = "select * from sample_relationships", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRelationRecord> getSampleRelationshipSkeletons();

    //

    /**
     * Returns the sample for the given <var>sampleId</var>.
     */
    @Select(SELECT_FROM_SAMPLES_S + " where s.id=?{1}")
    public SampleRecord getSample(long sampleId);

    /**
     * Returns all samples in the database.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " left join spaces g on s.space_id=g.id"
            + " where s.dbin_id=?{1} or g.dbin_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamples(long dbInstanceId);

    //
    // Samples for group
    //

    /**
     * Returns the samples for the given <var>groupCode</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join sample_types st on s.saty_id=st.id"
            + " join spaces g on s.space_id=g.id "
            + " where st.is_listable and g.dbin_id=?{1} and g.code=?{2} order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getListableGroupSamples(long dbInstanceId, String groupCode);

    /**
     * Returns the samples for the given <var>groupCode</var> that are assigned to an experiment.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id "
            + " where s.expe_id is not null and g.dbin_id=?{1} and g.code=?{2} "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getGroupSamplesWithExperiment(long dbInstanceId,
            String groupCode);

    /**
     * Returns the samples for the given <var>groupCode</var> and <var>sampleTypeId</var>
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id  "
            + " where g.dbin_id=?{1} and g.code=?{2} and s.saty_id=?{3}       "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getGroupSamplesForSampleType(long dbInstanceId,
            String groupCode, long sampleTypeId);

    /**
     * Returns the samples for the given <var>groupCode</var> and <var>sampleTypeId</var> that are
     * assigned to an experiment.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id "
            + " where s.expe_id is not null and g.dbin_id=?{1} and g.code=?{2} and s.saty_id=?{3} "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getGroupSamplesForSampleTypeWithExperiment(long dbInstanceId,
            String groupCode, long sampleTypeId);

    /**
     * Returns the samples for all spaces.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join sample_types st on s.saty_id=st.id"
            + " join spaces g on s.space_id=g.id "
            + " where st.is_listable and g.dbin_id=?{1} order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getAllListableGroupSamples(long dbInstanceId);

    /**
     * Returns the samples for all spaces that are assigned to an experiment.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id "
            + " where s.expe_id is not null and g.dbin_id=?{1}                 "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getAllGroupSamplesWithExperiment(long dbInstanceId);

    /**
     * Returns the samples for all spaces and <var>sampleTypeId</var>
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id "
            + " where g.dbin_id=?{1} and s.saty_id=?{2}                        "
            + " order by s.code", fetchSize = FETCH_SIZE, rubberstamp = true)
    public DataIterator<SampleRecord> getAllGroupSamplesForSampleType(long dbInstanceId,
            long sampleTypeId);

    /**
     * Returns the samples for all spaces and <var>sampleTypeId</var> that are assigned to an
     * experiment.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join spaces g on s.space_id=g.id "
            + " where s.expe_id is not null and g.dbin_id=?{1} and s.saty_id=?{2} "
            + " order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getAllGroupSamplesForSampleTypeWithExperiment(
            long dbInstanceId, long sampleTypeId);

    //
    // Samples for experiment
    //

    /**
     * Returns the samples for the given <var>experimentId</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " JOIN sample_types st ON s.saty_id=st.id"
            + " WHERE st.is_listable AND s.expe_id=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getListableSamplesForExperiment(long experimentId);

    //
    // Samples for container
    //

    /**
     * Returns the samples for the given <var>sampleContainerId</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.samp_id_part_of=?{1}", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesForContainer(long sampleContainerId);

    //
    // Samples for parent/child
    //

    /**
     * Returns the children samples for the given ids of relationship and parent sample.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " WHERE s.id IN "
            + "     (SELECT sample_id_child FROM sample_relationships "
            + "      WHERE relationship_id=?{1} AND sample_id_parent=?{2})", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getChildrenSamplesForParent(long relationshipId,
            long sampleParentId);

    /**
     * Returns the parent samples for the given ids of relationship and child sample.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " WHERE s.id IN "
            + "     (SELECT sample_id_parent FROM sample_relationships "
            + "      WHERE relationship_id=?{1} AND sample_id_child = any(?{2}))", parameterBindings =
        { TypeMapper.class, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getParentSamplesForChildren(long relationshipId,
            LongSet sampleChildIds);

    //
    // New samples of type
    //

    /**
     * Returns all samples with a <var>propertyType</var> attached having specified
     * <var>popertyValue</var>. Additionally id of the sample SHOULDN'T be in the specified set of
     * ids.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.saty_id=?{1} and s.id in ("
            + "       select samp_id from sample_properties sp where sp.stpt_id in ("
            + "              select id from sample_type_property_types stpt where stpt.prty_id = "
            + "                     (select id from property_types where code=?{2})"
            + "              ) and value=?{3}                                      "
            + "       ) and not id = any(?{4})", parameterBindings =
        { TypeMapper.class, TypeMapper.class, TypeMapper.class, LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesWithPropertyValue(long sampleTypeId,
            String propertyTypeCode, String propertyValue, LongSet sampleIds);

    //
    // Shared samples
    //

    /**
     * Returns the shared samples for the given <var>dbInstanceId</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " join sample_types st on s.saty_id=st.id "
            + "   where st.is_listable and s.dbin_id=?{1} order by s.code", fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getListableSharedSamples(long dbInstanceId);

    /**
     * Returns the shared samples for the given <var>dbInstanceId</var> and <var>sampleTypeId</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.dbin_id=?{1} and s.saty_id=?{2}"
            + " order by s.code", fetchSize = FETCH_SIZE)
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
            into.setCode(row.getString("code"));
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
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamples(LongSet sampleIds);

    /**
     * Returns the samples for the given <var>sampleCodes</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.code = any(?{1})", parameterBindings =
        { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesForCodes(String[] sampleCodes);

    /**
     * Returns the samples for the given <var>permIds</var>.
     */
    @Select(sql = SELECT_FROM_SAMPLES_S + " where s.perm_id = any(?{1})", parameterBindings =
        { StringArrayMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleRecord> getSamplesForPermIds(String[] permIds);

    //
    // Sample Properties
    //

    /**
     * Returns all generic property values of all samples specified by <var>sampleIds</var>.
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    @Select(sql = "SELECT sp.samp_id as entity_id, stpt.prty_id, sp.value "
            + "      FROM sample_properties sp"
            + "      JOIN sample_type_property_types stpt ON sp.stpt_id=stpt.id"
            + "     WHERE sp.value is not null AND sp.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<GenericEntityPropertyRecord> getEntityPropertyGenericValues(
            LongSet sampleIds);

    /**
     * Returns all controlled vocabulary property values of all samples specified by
     * <var>sampleIds</var>.
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    @Select(sql = "SELECT sp.samp_id as entity_id, stpt.prty_id, cvte.id, cvte.covo_id, cvte.code, cvte.label, cvte.ordinal"
            + "      FROM sample_properties sp"
            + "      JOIN sample_type_property_types stpt ON sp.stpt_id=stpt.id"
            + "      JOIN controlled_vocabulary_terms cvte ON sp.cvte_id=cvte.id"
            + "     WHERE sp.cvte_id is not null AND sp.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<VocabularyTermRecord> getEntityPropertyVocabularyTermValues(
            LongSet sampleIds);

    /**
     * Returns all material-type property values of all samples specified by <var>sampleIds</var>.
     * 
     * @param sampleIds The set of sample ids to get the property values for.
     */
    @Select(sql = "SELECT sp.samp_id as entity_id, stpt.prty_id, m.id, m.code, m.maty_id"
            + "      FROM sample_properties sp"
            + "      JOIN sample_type_property_types stpt ON sp.stpt_id=stpt.id"
            + "      JOIN materials m ON sp.mate_prop_id=m.id "
            + "     WHERE sp.mate_prop_id is not null AND sp.samp_id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<MaterialEntityPropertyRecord> getEntityPropertyMaterialValues(
            LongSet sampleIds);

}
