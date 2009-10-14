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

package ch.systemsx.cisd.openbis.generic.server.business.bo.common.entity;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;

import ch.rinn.restrictions.Friend;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.LongSetMapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * Interfaces to query basic information about samples and experiments referenced from other
 * objects. Note that this interface is not intended to be used to fetch primary entities.
 * 
 * @author Tomasz Pylak
 */
@Private
@Friend(toClasses =
    { SampleReferenceRecord.class })
public interface ISecondaryEntityListingQuery extends TransactionQuery
{
    public static final int FETCH_SIZE = 1000;

    //
    // Experiments
    //

    /**
     * Returns the code of an experiment and its project by the experiment <var>id</var>.
     * 
     * @param experimentId The id of the experiment to get the code for.
     */
    @Select("select e.code as e_code, et.code as et_code, p.code as p_code, g.code as g_code, g.dbin_id as dbin_id from experiments e "
            + "join experiment_types et on e.exty_id=et.id join projects p on e.proj_id=p.id "
            + "join groups g on p.grou_id=g.id where e.id=?{1}")
    public ExperimentProjectGroupCodeRecord getExperimentAndProjectAndGroupCodeForId(
            long experimentId);

    //
    // Samples
    //

    /**
     * Returns the samples for the given ids.
     */
    @Select(sql = "select s.id as id, s.perm_id as perm_id, s.code as s_code, s.inva_id as inva_id, "
            + "           st.code as st_code, g.code as g_code, c.code as c_code"
            + "   from samples s join sample_types st on s.saty_id=st.id"
            + "                  join groups g on s.grou_id=g.id "
            + "                  left join samples c on s.samp_id_part_of=c.id "
            + "        where s.id = any(?{1})", parameterBindings =
        { LongSetMapper.class }, fetchSize = FETCH_SIZE)
    public DataIterator<SampleReferenceRecord> getSamples(LongSet sampleIds);

    //
    // Persons
    //

    /**
     * Returns the person for the given <var>personId</var>
     * 
     * @param personId The id of the Person you want to get.
     */
    @Select("select first_name as firstName, last_name as lastName, email, user_id as userId from persons where id=?{1}")
    public Person getPersonById(long personId);

    /**
     * Returns all groups of this data base instance.
     * 
     * @param databaseInstanceId The id of the ddatabase to get the groups for.
     */
    @Select("select id, code from groups where dbin_id=?{1}")
    public Group[] getAllGroups(long databaseInstanceId);

    /**
     * Returns the technical id of a group for given <var>groupCode</code>.
     */
    @Select("select id from groups where code=?{1}")
    public long getGroupIdForCode(String groupCode);
}
