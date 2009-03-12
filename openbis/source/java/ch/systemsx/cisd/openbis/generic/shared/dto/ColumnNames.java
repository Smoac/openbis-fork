/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

/**
 * This class lists database field names commonly used.
 * 
 * @author Christian Ribeaud
 */
public final class ColumnNames
{

    public static final String AMOUNT_COLUMN = "amount";

    public static final String CODE_COLUMN = "code";

    public static final String CONTROL_LAYOUT_SAMPLE_COLUMN = "samp_id_control_layout";

    public static final String CONTROLLED_VOCABULARY_COLUMN = "covo_id";

    public static final String DATA_CHILD_COLUMN = "data_id_child";

    public static final String DATA_ID_COLUMN = "data_id";

    public static final String DATA_PARENT_COLUMN = "data_id_parent";

    public static final String DATA_PRODUCER_CODE_COLUMN = "data_producer_code";

    public static final String DATA_SET_TYPE_COLUMN = "dsty_id";

    public static final String DATA_STORE_COLUMN = "dast_id";

    public static final String DATA_TYPE_COLUMN = "daty_id";

    public final static String DATABASE_INSTANCE_COLUMN = "dbin_id";

    public static final String DESCRIPTION_COLUMN = "description";

    public static final String DOWNLOAD_URL_COLUMN = "download_url";

    public static final String EXPERIMENT_ATTACHMENT_CONTENT_COLUMN = "exac_id";

    public static final String EXPERIMENT_COLUMN = "expe_id";

    public static final String EXPERIMENT_TYPE_COLUMN = "exty_id";

    public static final String EXPERIMENT_TYPE_PROPERTY_TYPE_COLUMN = "etpt_id";

    public static final String FILE_FORMAT_TYPE = "ffty_id";

    public static final String FILE_NAME_COLUMN = "file_name";

    public final static String FIRST_NAME_COLUMN = "first_name";

    public static final String GENERATED_FROM_DEPTH = "generated_from_depth";

    public static final String GENERATED_FROM_SAMPLE_COLUMN = "samp_id_generated_from";

    public static final String GROUP_COLUMN = "grou_id";

    public static final String GROUP_PARENT_COLUMN = "grou_id_parent";

    public static final String ID_COLUMN = "id";

    public static final String INHIBITOR_OF_COLUMN = "mate_id_inhibitor_of";

    public static final String INVALIDATION_COLUMN = "inva_id";

    public static final String IS_COMPLETE_COLUMN = "is_complete";

    public static final String IS_DATA_ACQUSITION = "is_data_acquisition";

    public static final String IS_DISPLAYED = "is_displayed";

    public static final String IS_INTERNAL_NAMESPACE = "is_internal_namespace";

    public static final String IS_LISTABLE = "IS_LISTABLE";

    public static final String IS_MANAGED_INTERNALLY = "is_managed_internally";

    public static final String IS_MANDATORY = "is_mandatory";

    public static final String IS_ORIGINAL_SOURCE_COLUMN = "is_original_source";

    public static final String IS_PLACEHOLDER_COLUMN = "is_placeholder";

    public static final String LABEL_COLUMN = "label";

    public final static String LAST_NAME_COLUMN = "last_name";

    public static final String LOCATION_COLUMN = "location";

    public static final String LOCATOR_TYPE_COLUMN = "loty_id";

    public static final String MATERIAL_BATCH_COLUMN = "maba_id";

    public static final String MATERIAL_COLUMN = "mate_id";

    public static final String MATERIAL_TYPE_COLUMN = "maty_id";

    public static final String MATERIAL_TYPE_PROPERTY_TYPE_COLUMN = "mtpt_id";

    public static final String PARENT_DATA_SET_CODE_COLUMN = "data_producer_code";

    public static final String PART_OF_DEPTH = "part_of_depth";

    public static final String PART_OF_SAMPLE_COLUMN = "samp_id_part_of";

    public static final String PERSON_GRANTEE_COLUMN = "pers_id_grantee";

    public static final String PERSON_LEADER_COLUMN = "pers_id_leader";

    public static final String PERSON_REGISTERER_COLUMN = "pers_id_registerer";

    public static final String PROCEDURE_COLUMN = "proc_id";

    public static final String PROCEDURE_PRODUCED_BY_COLUMN = "proc_id_produced_by";

    public static final String PROCEDURE_TYPE_COLUMN = "pcty_id";

    public static final String PRODUCTION_TIMESTAMP_COLUMN = "production_timestamp";

    public static final String PROJECT_COLUMN = "proj_id";

    public static final String PROPERTY_TYPE_COLUMN = "prty_id";

    public static final String REGISTRATION_TIMESTAMP_COLUMN = "registration_timestamp";

    public final static String ROLE_COLUMN = "role_code";

    public static final String SAMPLE_ACQUIRED_FROM = "samp_id_acquired_from";

    public static final String SAMPLE_COLUMN = "samp_id";

    public static final String SAMPLE_DERIVED_FROM = "samp_id_derived_from";

    public static final String SAMPLE_TYPE_COLUMN = "saty_id";

    public static final String SAMPLE_TYPE_PROPERTY_TYPE_COLUMN = "stpt_id";

    public static final String STORAGE_FORMAT_COLUMN = "cvte_id_stor_fmt";

    public static final String STUDY_OBJECT_COLUMN = "mate_id_study_object";

    public static final String TOP_SAMPLE_COLUMN = "samp_id_top";

    public final static String USER_COLUMN = "user_id";

    public static final String UUID_COLUMN = "uuid";

    public static final String VALUE_COLUMN = "value";

    public static final String VERSION_COLUMN = "version";

    public static final String VOCABULARY_TERM_COLUMN = "cvte_id";

    public static final String PROPERTY_MATERIAL_TYPE_COLUMN = "maty_prop_id";
    
    public static final String EVENT_TYPE = "event_type";
    
    private ColumnNames()
    {
        // Can not be instantiated.
    }
}
