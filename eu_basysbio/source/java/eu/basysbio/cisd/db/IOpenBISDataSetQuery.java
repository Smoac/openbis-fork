/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.db;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;

/**
 * A query for getting data sets from openBIS.
 * 
 * @author Bernd Rinn
 */
public interface IOpenBISDataSetQuery extends TransactionQuery
{

    public static class DataSetContainer
    {
        public String ds_code;
        
        public String dst_code;

        public String exp_code;

        public String exp_perm_id;

        public String ds_location;
        
        public String uploader_email;
    }

    @Select("select ds.code as ds_code, "
            + "    e.code as exp_code,                                              "
            + "    e.perm_id as exp_perm_id,                                        "
            + "    eds.location as ds_location,"
            + "    dsp.value as uploader_email,                                      "
            + "    dst.code as dst_code                                            "
            + "  from data ds                                                        "
            + "    join external_data eds on ds.id = eds.data_id "
            + "    join data_set_types dst on dst.id = ds.dsty_id                      "
            + "    join experiments e on e.id = ds.expe_id                           "
            + "    join data_set_properties dsp on dsp.ds_id = ds.id "
            + "    join data_set_type_property_types dstpt on dstpt.id = dsp.dstpt_id "
            + "    join property_types pt on pt.id = dstpt.prty_id "
            + "  where (dst.code = 'TIME_SERIES' or dst.code = 'LCA_MIC') "
            + "    and pt.code='UPLOADER_EMAIL'")
    public DataIterator<DataSetContainer> listTimeSeriesDataSets();

}
