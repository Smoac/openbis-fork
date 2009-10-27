/*
 * Copyright 2007 ETH Zuerich, CISD
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
 * This class lists the tables that can be found in the database.
 * 
 * @author Christian Ribeaud
 */
public final class TableNames
{

    public static final String CONTROLLED_VOCABULARY_TABLE = "controlled_vocabularies";

    public static final String CONTROLLED_VOCABULARY_TERM_TABLE = "controlled_vocabulary_terms";

    public static final String DATA_SET_RELATIONSHIPS_TABLE = "data_set_relationships";

    public static final String DATA_SET_TYPES_TABLE = "data_set_types";

    public static final String DATA_STORES_TABLE = "data_stores";

    public static final String DATA_STORE_SERVICES_TABLE = "data_store_services";

    public static final String DATA_STORE_SERVICES_DATASET_TYPES_TABLE =
            "DATA_STORE_SERVICE_DATA_SET_TYPES";

    public static final String DATA_TABLE = "data";

    public static final String DATA_SET_PROPERTIES_TABLE = "data_set_properties";

    public static final String DATA_SET_TYPE_PROPERTY_TYPE_TABLE = "data_set_type_property_types";

    public static final String DATA_TYPES_TABLE = "data_types";

    public static final String DATABASE_INSTANCES_TABLE = "database_instances";

    public static final String FILTERS_TABLE = "filters";

    public static final String GRID_CUSTOM_COLUMNS_TABLE = "grid_custom_columns";

    public static final String ATTACHMENT_CONTENT_TABLE = "attachment_contents";

    public static final String ATTACHMENTS_TABLE = "attachments";

    public static final String EXPERIMENT_PROPERTIES_TABLE = "experiment_properties";

    public static final String EXPERIMENT_TYPE_PROPERTY_TYPE_TABLE =
            "experiment_type_property_types";

    public static final String EXPERIMENT_TYPES_TABLE = "experiment_types";

    public static final String EXPERIMENTS_TABLE = "experiments";

    public static final String EXTERNAL_DATA_TABLE = "external_data";

    public static final String FILE_FORMAT_TYPES_TABLE = "file_format_types";

    public static final String GROUPS_TABLE = "groups";

    public static final String INVALIDATION_TABLE = "invalidations";

    public static final String LOCATOR_TYPES_TABLE = "locator_types";

    public static final String MATERIAL_BATCHES_TABLE = "material_batches";

    public static final String MATERIAL_PROPERTIES_TABLE = "material_properties";

    public static final String MATERIAL_TYPE_PROPERTY_TYPE_TABLE = "material_type_property_types";

    public static final String MATERIAL_TYPES_TABLE = "material_types";

    public static final String MATERIALS_TABLE = "materials";

    public static final String PERSONS_TABLE = "persons";

    public static final String PROJECTS_TABLE = "projects";

    public static final String PROPERTY_TYPES_TABLE = "property_types";

    public static final String ROLE_ASSIGNMENTS_TABLE = "role_assignments";

    public static final String SAMPLE_INPUTS_TABLE = "sample_inputs";

    public static final String SAMPLE_MATERIAL_BATCHES_TABLE = "sample_material_batches";

    public static final String SAMPLE_PROPERTIES_TABLE = "sample_properties";

    public static final String SAMPLE_TYPE_PROPERTY_TYPE_TABLE = "sample_type_property_types";

    public static final String SAMPLE_TYPES_TABLE = "sample_types";

    public static final String SAMPLES_TABLE = "samples";

    public static final String EVENTS_TABLE = "events";

    public static final String AUTHORIZATION_GROUPS_TABLE = "authorization_groups";

    public static final String AUTHORIZATION_GROUP_PERSONS_TABLE = "authorization_group_persons";

    private TableNames()
    {
        // This class can not be instantiated.
    }

}
