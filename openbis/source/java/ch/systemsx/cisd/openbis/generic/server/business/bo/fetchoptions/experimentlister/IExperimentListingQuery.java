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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.experimentlister;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.StringArrayMapper;

/**
 * @author pkupczyk
 */
public interface IExperimentListingQuery extends BaseQuery
{

    public static final String LIST_EXPERIMENTS =
            " SELECT e.id as e_id, e.perm_id as e_perm_id, e.code as e_code, e.registration_timestamp as e_registration_timestamp, e.modification_timestamp as e_modification_timestamp,"
                    + "      et.id as et_id, et.code as et_code, et.description as et_description,"
                    + "      d.id as d_id, d.code as d_code, d.is_original_source as d_is_original_source, d.registration_timestamp as d_registration_timestamp, d.uuid as d_uuid,"
                    + "      s.id as s_id, s.code as s_code, s.description as s_description, s.registration_timestamp as s_registration_timestamp,"
                    + "      pr.id as pr_id, pr.code as pr_code, pr.registration_timestamp as pr_registration_timestamp, pr.description as pr_description, pr.modification_timestamp as pr_modification_timestamp,"
                    + "      pe.id as pe_id, pe.first_name as pe_first_name, pe.last_name as pe_last_name, pe.user_id as pe_user_id, pe.email as pe_email, pe.registration_timestamp as pe_registration_timestamp"
                    + " FROM experiments e, experiment_types et, database_instances d, spaces s, projects pr, persons pe WHERE"
                    + "      e.exty_id = et.id AND e.proj_id = pr.id AND"
                    + "      pr.space_id = s.id AND s.dbin_id = d.id AND e.pers_id_registerer = pe.id";

    @Select(sql = LIST_EXPERIMENTS
            + " AND d.code = any(?{1}) AND s.code = any(?{2}) AND pr.code = any(?{3}) AND e.code = any(?{4})", parameterBindings =
        { StringArrayMapper.class, StringArrayMapper.class, StringArrayMapper.class,
                StringArrayMapper.class })
    public DataIterator<ExperimentRecord> listExperiments(String[] databaseInstanceCodes,
            String[] spaceCodes, String[] projectCodes, String[] experimentCodes);

    @Select(sql = LIST_EXPERIMENTS
            + " AND d.code = any(?{1}) AND s.code = any(?{2}) AND pr.code = any(?{3})", parameterBindings =
        { StringArrayMapper.class, StringArrayMapper.class, StringArrayMapper.class })
    public DataIterator<ExperimentRecord> listExperimentsForProjects(
            String[] databaseInstanceCodes, String[] spaceCodes, String[] projectCodes);

}
