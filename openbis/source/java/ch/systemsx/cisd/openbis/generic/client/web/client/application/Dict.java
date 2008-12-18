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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

/**
 * Contains keys of the localized messages.
 * <p>
 * Use these constants instead of accessing messages with hard coded keys! Consider extending this
 * class for plugin specific keys. Currently this class contains message keys of the <i>common</i>
 * and <i>generic</i> technology.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public abstract class Dict
{

    protected Dict()
    {
        // Can not be instantiated.
    }

    public static final String APPLICATION_NAME = "applicationName";

    public static final String WELCOME = "welcome";

    public static final String FOOTER = "footer";

    //
    // Common Labels
    // 

    public static final String CODE = "code";

    public static final String REGISTRATOR = "registrator";

    public static final String REGISTRATION_DATE = "registration_date";

    //
    // Field
    //

    public static final String COMBO_BOX_EMPTY = "combobox_empty";

    public static final String COMBO_BOX_CHOOSE = "combobox_choose";

    public static final String INVALID_CODE_MESSAGE = "invalid_code_message";

    //
    // MessageBox
    //

    public static final String MESSAGEBOX_ERROR = "messagebox_error";

    public static final String MESSAGEBOX_WARNING = "messagebox_warning";

    public static final String MESSAGEBOX_INFO = "messagebox_info";

    //
    // Buttons
    //

    public static final String BUTTON_SAVE = "button_save";

    public static final String BUTTON_CANCEL = "button_cancel";

    public static final String BUTTON_RESET = "button_reset";

    public static final String BUTTON_SUBMIT = "button_submit";

    public static final String BUTTON_REFRESH = "button_refresh";

    public static final String BUTTON_EXPORT_DATA = "button_exportData";

    //
    // LoginWidget
    //

    public static final String LOGIN_INVITATION = "login_invitation";

    public static final String LOGIN_USER_LABEL = "login_userLabel";

    public static final String LOGIN_PASSWORD_LABEL = "login_passwordLabel";

    public static final String LOGIN_BUTTON_LABEL = "login_buttonLabel";

    public static final String LOGIN_FAILED = "login_failed";

    //
    // AbstractAsyncCallback
    //

    public static final String EXCEPTION_INVOCATION_MESSAGE = "exception_invocationMessage";

    public static final String EXCEPTION_WITHOUT_MESSAGE = "exception_withoutMessage";

    //
    // Header
    //

    public static final String HEADER_USER_WITHOUT_HOMEGROUP = "header_userWithoutHomegroup";

    public static final String HEADER_USER_WITH_HOMEGROUP = "header_userWithHomegroup";

    public static final String HEADER_LOGOUT_BUTTON_LABEL = "header_logoutButtonLabel";

    //
    // Authorization Management Console
    // 

    public static final String PERSONS_VIEW_HEADING = "personsView_heading";

    public static final String GROUPS_VIEW_HEADING = "groupsView_heading";

    public static final String ROLES_VIEW_HEADING = "rolesView_heading";

    //
    // Search
    //

    public static final String SEARCH_BUTTON = "search_button";

    public static final String GLOBAL_SEARCH = "global_search";

    public static final String IDENTIFIER = "identifier";

    public static final String NO_MATCH = "no_match";

    public static final String ENTITY_TYPE = "entity_type";

    public static final String ENTITY_KIND = "entity_kind";

    public static final String MATCHING_TEXT = "matching_text";

    public static final String MATCHING_FIELD = "matching_field";

    public static final String TOO_GENERIC = "too_generic";

    //
    // Sample Browser
    //

    public static final String SAMPLE = "sample";

    public static final String SAMPLE_TYPE = "sample_type";

    public static final String DATABASE_INSTANCE = "database_instance";

    public static final String SAMPLE_IDENTIFIER = "sample_identifier";

    public static final String IS_INSTANCE_SAMPLE = "is_instance_sample";

    public static final String IS_INVALID = "is_invalid";

    public static final String GROUP = "group";

    public static final String PROJECT = "project";

    public static final String EXPERIMENT = "experiment";

    public static final String EXPERIMENT_IDENTIFIER = "experiment_identifier";

    public static final String GENERATED_SAMPLES = "generated_samples";

    public static final String GENERATED_FROM = "generated_from";

    public static final String PART_OF = "part_of";

    //
    // Experiment Browser
    //

    public static final String EXPERIMENT_TYPE = "experiment_type";

    //
    // Property Type Browser
    //

    public static final String LABEL = "label";

    public static final String DATA_TYPE = "data_type";

    public static final String DESCRIPTION = "description";

    public static final String SAMPLE_TYPES = "sample_types";

    public static final String MATERIAL_TYPES = "material_types";

    public static final String EXPERIMENT_TYPES = "experiment_types";

    public static final String IS_MANDATORY = "is_mandatory";

    public static final String PROPERTY_TYPE = "property_type";

    public static final String PROPERTY_TYPE_CODE = "property_type_code";

    public static final String ASSIGNED_TO = "assigned_to";

    public static final String TYPE_OF = "type_of";

    public static final String VOCABULARY = "vocabulary";

    public static final String VOCABULARY_TERMS = "vocabulary_terms";

    public static final String VOCABULARY_TERMS_EMPTY = "vocabulary_terms_empty";

    public static final String MANDATORY = "mandatory";

    public static final String DEFAULT_VALUE = "default_value";

    // -------- generic plugin dictionary -------------------

    //
    // Experiment Viewer
    //

    public static final String FILE_NAME = "file_name";

    public static final String VERSION_FILE_NAME = "version_file_name";

    public static final String VERSION = "version";

    public static final String VERSIONS = "versions";

    public static final String VERSIONS_TEMPLATE = "versions_template";

    public static final String NO_ATTACHMENTS_FOUND = "no_attachments_found";

    public static final String NO_SAMPLES_FOUND = "no_samples_found";

    public static final String PROCEDURE = "procedure";

    //
    // Sample Viewer
    //

    public static final String SAMPLE_PROPERTIES_HEADING = "sample_properties_heading";

    public static final String PART_OF_HEADING = "part_of_heading";

    public static final String EXTERNAL_DATA_HEADING = "external_data_heading";

    public static final String INVALIDATION = "invalidation";

    public static final String INVALIDATION_TEMPLATE = "invalidation_template";

    public static final String LOCATION = "location";

    public static final String FILE_FORMAT_TYPE = "file_format_type";

    //
    // Sample Registration
    //

    public static final String INSTANCE_SAMPLE = "instance_sample";

    public static final String GENERATED_FROM_SAMPLE = "generated_from_sample";

    public static final String PART_OF_SAMPLE = "part_of_sample";

    // ----- end generic ------------------
}