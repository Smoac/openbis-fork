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

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;

/**
 * This class lists the validation messages.
 * <p>
 * Each field definition follows schema:
 * <code>&lt;field-name&gt;_&lt;validation-annotation&gt;_MESSAGE</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ValidationMessages
{

    private static final String CAN_NOT_BE_NULL = " can not be null.";

    private static final String LENGTH_PREFIX = "Given ";

    private static final String LENGTH_SUFFIX =
            " '%s' is too long (maximal length: {max} characters).";

    public static final String ATTACHMENT_CONTENT_NOT_NULL_MESSAGE =
            "The content of the attachment" + CAN_NOT_BE_NULL;

    public static final String CODE_LENGTH_MESSAGE =
            "Given code '%s' is either too short (minimal length: {min} character) "
                    + "or too long (maximal length: {max} characters).";

    public static final String CODE_NOT_NULL_MESSAGE = "Code" + CAN_NOT_BE_NULL;

    public static final String CODE_PATTERN_MESSAGE =
            "Given code '%s' contains illegal characters (allowed: A-Z, a-z, 0-9 and _, -, .)";

    public static final String TERM_CODE_PATTERN_MESSAGE =
            "Given term code '%s' contains illegal characters (allowed: A-Z, a-z, 0-9 and '_', '-', ':', '.')";

    public static final String DATA_STORE_NOT_NULL_MESSAGE = "Data store" + CAN_NOT_BE_NULL;

    public static final String DATA_STORE_SERVICES_NOT_NULL_MESSAGE =
            "Data store services" + CAN_NOT_BE_NULL;

    public static final String DATA_STORE_SERVICE_KIND_NOT_NULL_MESSAGE =
            "Data Store Service Kind " + CAN_NOT_BE_NULL;

    public static final String DATA_SET_TYPE_NOT_NULL_MESSAGE = "Data set type" + CAN_NOT_BE_NULL;

    public static final String DATA_TYPE_NOT_NULL_MESSAGE = "Data type" + CAN_NOT_BE_NULL;

    public static final String DATABASE_INSTANCE_NOT_NULL_MESSAGE =
            "Database instance" + CAN_NOT_BE_NULL;

    public final static String DESCRIPTION_LENGTH_MESSAGE =
            LENGTH_PREFIX + "description" + LENGTH_SUFFIX;

    public static final String DESCRIPTION_NOT_NULL_MESSAGE = "Description" + CAN_NOT_BE_NULL;

    public static final String DOWNLOAD_URL_NOT_NULL_MESSAGE = "Download URL" + CAN_NOT_BE_NULL;

    public static final String REMOTE_URL_NOT_NULL_MESSAGE = "Remote URL" + CAN_NOT_BE_NULL;

    public static final String SESSION_TOKEN_NOT_NULL_MESSAGE = "Session token" + CAN_NOT_BE_NULL;

    public static final String EMAIL_EMAIL_MESSAGE = "Given email address '%s' is not a valid one.";

    public static final String EMAIL_LENGTH_MESSAGE =
            LENGTH_PREFIX + "email address" + LENGTH_SUFFIX;

    public static final String EXPERIMENT_NOT_NULL_MESSAGE = "Experiment " + CAN_NOT_BE_NULL;

    public static final String EXPERIMENT_TYPE_NOT_NULL_MESSAGE =
            "Experiment type" + CAN_NOT_BE_NULL;

    public static final String EXPERIMENT_TYPE_PROPERTY_TYPE_NOT_NULL_MESSAGE =
            "Experiment type - property type" + CAN_NOT_BE_NULL;

    public static final String FILE_FORMAT_TYPE_NOT_NULL_MESSAGE =
            "File format type" + CAN_NOT_BE_NULL;

    public static final String FILE_NAME_LENGTH_MESSAGE =
            LENGTH_PREFIX + "file name" + LENGTH_SUFFIX;

    public static final String FILE_NAME_NOT_NULL_MESSAGE = "File name" + CAN_NOT_BE_NULL;

    public static final String TITLE_LENGTH_MESSAGE = LENGTH_PREFIX + "title" + LENGTH_SUFFIX;

    public static final String FIRST_NAME_LENGTH_MESSAGE =
            LENGTH_PREFIX + "first name" + LENGTH_SUFFIX;

    public static final String GROUP_NOT_NULL_MESSAGE = "Group" + CAN_NOT_BE_NULL;

    public static final String IS_COMPLETE_NOT_NULL_MESSAGE = "Complete flag" + CAN_NOT_BE_NULL;

    public static final String LABEL_LENGTH_MESSAGE = LENGTH_PREFIX + "label" + LENGTH_SUFFIX;

    public static final String LABEL_NOT_NULL_MESSAGE = "Label" + CAN_NOT_BE_NULL;

    public static final String LAST_NAME_LENGTH_MESSAGE =
            LENGTH_PREFIX + "last name" + LENGTH_SUFFIX;

    public static final String LOCATION_LENGTH_MESSAGE = LENGTH_PREFIX + "location" + LENGTH_SUFFIX;

    public static final String LOCATION_NOT_NULL_MESSAGE = "Location" + CAN_NOT_BE_NULL;

    public static final String LOCATION_NOT_RELATIVE = "Location is not relative";

    public static final String CODE_NOT_USER_NAMESPACE =
            "Code does not contain '" + BasicConstant.USER_NAMESPACE_PREFIX + "' prefix.";

    public static final String LOCATOR_TYPE_NOT_NULL_MESSAGE = "Locator type" + CAN_NOT_BE_NULL;

    public static final String MATERIAL_NOT_NULL_MESSAGE = "Material" + CAN_NOT_BE_NULL;

    public static final String MATERIAL_TYPE_NOT_NULL_MESSAGE = "Material type" + CAN_NOT_BE_NULL;

    public static final String MATERIAL_TYPE_PROPERTY_TYPE_NOT_NULL_MESSAGE =
            "Material type - property type" + CAN_NOT_BE_NULL;

    public static final String PERSON_NOT_NULL_MESSAGE = "Person" + CAN_NOT_BE_NULL;

    public static final String PROJECT_NOT_NULL_MESSAGE = "Project" + CAN_NOT_BE_NULL;

    public static final String SAMPLE_NOT_NULL_MESSAGE = "Sample" + CAN_NOT_BE_NULL;

    public static final String PROPERTY_TYPE_NOT_NULL_MESSAGE = "Property type" + CAN_NOT_BE_NULL;

    public static final String ROLE_NOT_NULL_MESSAGE = "Role" + CAN_NOT_BE_NULL;

    public static final String SAMPLE_TYPE_NOT_NULL_MESSAGE = "Sample type" + CAN_NOT_BE_NULL;

    public static final String SAMPLE_TYPE_PROPERTY_TYPE_NOT_NULL_MESSAGE =
            "Sample type - property type" + CAN_NOT_BE_NULL;

    public static final String STORAGE_FORMAT_NOT_NULL_MESSAGE = "Storage format" + CAN_NOT_BE_NULL;

    public static final String USER_ID_LENGTH_MESSAGE = LENGTH_PREFIX + "user id" + LENGTH_SUFFIX;

    public static final String USER_ID_NOT_NULL_MESSAGE = "User id" + CAN_NOT_BE_NULL;

    public static final String UUID_NOT_NULL_MESSAGE = "UUID" + CAN_NOT_BE_NULL;

    public static final String VALUE_LENGTH_MESSAGE = LENGTH_PREFIX + "value" + LENGTH_SUFFIX;

    public static final String VALUE_NOT_NULL_MESSAGE = "Value" + CAN_NOT_BE_NULL;

    public static final String VERSION_NOT_NULL_MESSAGE = "Version" + CAN_NOT_BE_NULL;

    public static final String VOCABULARY_NOT_NULL_MESSAGE = "Vocabulary" + CAN_NOT_BE_NULL;

    public static final String VALID_USER_CODE_DESCRIPTION =
            "User code must not be empty and must contain only allowed characters: "
                    + "letters, digits, '_', '.', '-', '@'. Note that whitespaces are not allowed.";

    public static final String EVENT_TYPE_NOT_NULL_MESSAGE = "Event Type" + CAN_NOT_BE_NULL;

    public static final String ENTITY_TYPE_NOT_NULL_MESSAGE = "Entity Type" + CAN_NOT_BE_NULL;

    public static final String IDENTIFIER_NOT_NULL_MESSAGE = "Identifier" + CAN_NOT_BE_NULL;

    public static final String IDENTIFIER_LENGTH_MESSAGE =
            LENGTH_PREFIX + "identifier" + LENGTH_SUFFIX;

    public static final String DATA_NOT_NULL_MESSAGE = "Data " + CAN_NOT_BE_NULL;

    private ValidationMessages()
    {
        // Can not be instantiated.
    }
}
