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

package ch.systemsx.cisd.cifex.shared.basic;


/**
 * Some constants used through the whole web application.
 * 
 * @author Christian Ribeaud
 */
public final class Constants
{

    /** The <code>id</code> attribute value for renew action. */
    public static final String RENEW_ID = "renew";

    /** The <code>id</code> attribute value for "show comment" action. */
    public static final String SHOW_COMMENT_ID = "showcomment";

    /** The <code>id</code> attribute value for edit action. */
    public static final String EDIT_ID = "edit";

    /** The <code>id</code> attribute value for delete action. */
    public static final String DELETE_ID = "delete";

    /** The <code>id</code> attribute value for 'shared' action. */
    public static final String SHARED_ID = "shared";

    /** The <code>id</code> attribute value for 'shared' action. */
    public static final String ADD_USER_ID = "add_user";

    /** The <code>id</code> attribute value for 'change user code' action. */
    public static final String CHANGE_USER_CODE_ID = "change_user_code";

    /** Prefix to specify a user in a textfield. */
    public static final String USER_ID_PREFIX = "id:";

    /** Regular expression for allowed user codes */
    public static final String USER_CODE_REGEX = "^([a-zA-Z0-9_\\.\\-\\@])+$";

    public static final String VALID_USER_CODE_DESCRIPTION =
            "User code must not be empty and must contain only allowed characters: [a-zA-Z0-9_.-@]. Whitespaces are not allowed.";

    /** The HTTP URL parameter used to specify the file id. */
    public static final String FILE_ID_PARAMETER = "fileId";

    /** The HTTP URL parameter used to specify the email. */
    public static final String USERCODE_PARAMETER = "user";

    /** The table <code>null</code> value representation (<code>-</code>). */
    public static final String TABLE_NULL_VALUE = "-";

    /** The table <i>empty</i> value representation. */
    public static final String TABLE_EMPTY_VALUE = "";

    /** A regular expression that match email addresses. */
    public static final String EMAIL_REGEX =
            "^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";

    /** A regular expression that match user code with prefix {@link #USER_ID_PREFIX}. */
    public static final String USER_CODE_WITH_ID_PREFIX_REGEX =
            "^" + USER_ID_PREFIX + USER_CODE_REGEX.substring(1);
    
    /** The property key to set the cifex.base.url in Eclipse. */
    public static final String CIFEX_BASE_URL_PROP_KEY = "cifex.base.url";

    /**
     * The path to add to the end of the server to get the rpc service.
     */
    public static final String CIFEX_RPC_PATH = "/cifex/rpc-service";

    /**
     * Returns an error message for a file that is not found in CIFEX.
     */
    public static String getErrorMessageForFileNotFound(final long fileId)
    {
        return "File [id=" + fileId + "] not found in CIFEX database.";
    }

    private Constants()
    {
        // Can not be instantiated.
    }

}
