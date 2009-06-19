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

    /** Prefix of user namespace vocabulary code. */
    public static final String USER_NAMESPACE_PREFIX = "USER.";

    private BasicConstant()
    {
    }

}
