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

package ch.systemsx.cisd.openbis.generic.shared.basic;

/**
 * Definition of basic constants. Can be used by server and GWT client.
 * 
 * @author Franz-Josef Elmer
 */
public class BasicConstant
{
    /** Maximum length of a file name for uploading data sets to CIFEX. */
    public static final int MAX_LENGTH_OF_FILE_NAME = 250;

    /** Maximum length of a comment for uploading data sets to CIFEX. */
    public static final int MAX_LENGTH_OF_CIFEX_COMMENT = 1000;

    /** Cifex URL parameter 'comment' */
    public static final String CIFEX_URL_PARAMETER_COMMENT = "comment";

    /** Cifex URL parameter 'recipient' */
    public static final String CIFEX_URL_PARAMETER_RECIPIENT = "recipients";

    /** Prefix of internal namespace vocabulary code. */
    public static final String INTERNAL_NAMESPACE_PREFIX = "$";

    private static final char UNI_REPLACEMENT_CHAR = 0xFFFD;

    /** Prefix of property value that contains error message. */
    public static final String ERROR_PROPERTY_PREFIX = Character.toString(UNI_REPLACEMENT_CHAR);

    /** Value of dynamic property before it is evaluated. */
    public static final String DYNAMIC_PROPERTY_PLACEHOLDER_VALUE = ERROR_PROPERTY_PREFIX
            + "(pending evaluation)";

    /** Value of managed property before it is defined by user. */
    public static final String MANAGED_PROPERTY_PLACEHOLDER_VALUE = ERROR_PROPERTY_PREFIX
            + "(undefined)";

    /** Template part of Vocabulary URL that that is replaced with vocabulary term code. */
    public static final String VOCABULARY_URL_TEMPLATE_TERM_PART = "$term$";

    /** Pattern for template part of Vocabulary URL that that is replaced with vocabulary term code. */
    public static final String VOCABULARY_URL_TEMPLATE_TERM_PATTERN =
            VOCABULARY_URL_TEMPLATE_TERM_PART.replaceAll("\\$", "\\\\\\$");

    /**
     * Canonical date format pattern used to save dates in DB. Holds date, time and time zone
     * information. It is less readable then the one used in GUI (
     * {@link BasicConstant#RENDERED_CANONICAL_DATE_FORMAT_PATTERN}), but both layers cannot share
     * the more readable one (GWT fails to parse date created with that pattern on the server side).
     */
    public static final String CANONICAL_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss Z";

    /**
     * Canonical date format pattern used to render dates in GUI in a more readable way.
     */
    public static final String RENDERED_CANONICAL_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss ZZZZ";

    /**
     * Date format which does not include time zone.
     */
    public static final String DATE_WITHOUT_TIMEZONE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Date format which does not include time and time zone.
     */
    public static final String DATE_WITHOUT_TIME_FORMAT_PATTERN = "yyyy-MM-dd";

    /**
     * Date format which does not include seconds & time zone.
     */
    public static final String DATE_WITH_SHORT_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    // constants used for link creation and handling

    public static final String VIEW_MODE_KEY = "viewMode";

    public static final String ANONYMOUS_KEY = "anonymous";

    public static final String LOCATOR_ACTION_PARAMETER = "action";

    public static final String PARENT_CHILD_INTERNAL_RELATIONSHIP = "$PARENT_CHILD";

    public static final String SERVER_URL_PARAMETER = "server-url";

    public static final String CODEBASE_PARAMETER = "codebase-url";

    public static final String DATA_SET_UPLOAD_CLIENT_PATH = "data-set-uploader-launch.jnlp";

    private BasicConstant()
    {
    }

}
