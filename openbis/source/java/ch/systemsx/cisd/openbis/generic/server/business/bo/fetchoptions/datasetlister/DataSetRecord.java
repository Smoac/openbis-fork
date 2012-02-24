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

import java.util.Date;

/**
 * @author pkupczyk
 */
public class DataSetRecord
{

    public Long ds_id;

    public String ds_code;

    public Date ds_registration_timestamp;

    public String dt_code;

    public Boolean dt_is_container;

    public String ex_code;

    public String sa_code;

    public String sa_dbin_id;

    public String sac_code;

    public String pe_first_name;

    public String pe_last_name;

    public String pe_email;

    public String pe_user_id;

    public String pre_code;

    public String spe_code;

    public String sps_code;

    public String die_code;

    public Boolean die_is_original_source;

}
