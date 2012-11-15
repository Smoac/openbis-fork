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

package ch.systemsx.cisd.openbis.generic.server.business.bo.fetchoptions.datasetlister;

import java.util.List;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.StringArrayMapper;

/**
 * @author pkupczyk
 */
public interface IDataSetListingQuery extends BaseQuery
{

    public static final String RELATIONS_SQL =
            "select dp.code as dp_code, dc.code as dc_code from data_set_relationships r inner join data dp on r.data_id_parent = dp.id inner join data dc on r.data_id_child = dc.id";

    @Select(sql = "select"
            + " ds.id as ds_id, ds.code as ds_code, ds.registration_timestamp as ds_registration_timestamp,"
            + " ds.modification_timestamp as ds_modification_timestamp,"
            + " dt.code as dt_code, dt.data_set_kind as dt_data_set_kind,"
            + " ctnr.id as ctnr_id, ctnr.code as ctnr_code,"
            + " ex.code as ex_code, "
            + " ed.storage_confirmation as ed_sc,"
            + " sa.code as sa_code, sa.dbin_id as sa_dbin_id, sac.code as sac_code,"
            + " pe.first_name as pe_first_name, pe.last_name as pe_last_name, pe.email as pe_email, pe.user_id as pe_user_id,"
            + " mod.first_name as mod_first_name, mod.last_name as mod_last_name, mod.email as mod_email, mod.user_id as mod_user_id,"
            + " pre.code as pre_code, spe.code as spe_code, sps.code as sps_code,"
            + " die.id as die_id, die.code as die_code, die.uuid as die_uuid, die.is_original_source as die_is_original_source,"
            + " ld.external_code as ld_external_code, edms.id as edms_id, edms.code as edms_code, edms.label as edms_label, edms.url_template as edms_url_template, edms.is_openbis as edms_is_openbis"
            + " from data ds inner join data_set_types dt on ds.dsty_id = dt.id"
            + " inner join experiments ex on ds.expe_id = ex.id"
            + " left outer join external_data ed on ds.id = ed.data_id"
            + " left outer join link_data ld on ds.id = ld.data_id"
            + " left outer join external_data_management_systems edms on ld.edms_id = edms.id"
            + " left outer join samples sa on ds.samp_id = sa.id"
            + " left outer join persons pe on ds.pers_id_registerer = pe.id"
            + " left outer join persons mod on ds.pers_id_modifier = mod.id"
            + " left outer join data ctnr on ds.ctnr_id = ctnr.id"
            + " inner join projects pre on ex.proj_id = pre.id"
            + " inner join spaces spe on pre.space_id = spe.id"
            + " inner join database_instances die on spe.dbin_id = die.id"
            + " left outer join spaces sps on sa.space_id = sps.id"
            + " left outer join samples sac on sa.samp_id_part_of = sac.id"
            + " where ds.code = any(?{1})", parameterBindings =
        { StringArrayMapper.class })
    public List<DataSetRecord> getDataSetMetaData(String[] dataSetCodes);

    @Select(sql = RELATIONS_SQL + " where dc.code = any(?{1})", parameterBindings =
        { StringArrayMapper.class })
    public List<DataSetRelationRecord> getDataSetParentsCodes(String[] dataSetCodes);

    @Select(sql = RELATIONS_SQL + " where dp.code = any(?{1})", parameterBindings =
        { StringArrayMapper.class })
    public List<DataSetRelationRecord> getDataSetChildrenCodes(String[] dataSetCodes);

    @Select(sql = " select ds.download_url as url, array_agg(d.code::text) as data_set_codes"
            + " from data d left join data_stores ds on ds.id = d.dast_id"
            + " where d.code = any(?{1}) group by ds.download_url", parameterBindings =
        { StringArrayMapper.class })
    public List<DataSetDownloadRecord> getDownloadURLs(String[] dataSetCodes);

    @Select(sql = " select ds.remote_url as url, array_agg(d.code::text) as data_set_codes"
            + " from data d left join data_stores ds on ds.id = d.dast_id"
            + " where d.code = any(?{1}) group by ds.remote_url", parameterBindings =
        { StringArrayMapper.class })
    public List<DataSetDownloadRecord> getRemoteURLs(String[] dataSetCodes);

}
