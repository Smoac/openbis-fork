/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.business.exception;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
public class ExternalUserNotFoundInExternalAuthenticationServiceException extends UserFailureException
{

    private static final long serialVersionUID = 1L;

    private String userCode;

    public ExternalUserNotFoundInExternalAuthenticationServiceException(String externalUserCode)
    {
        super("User with code: " + externalUserCode + " is externally authenticated, but hasn't been found in the authentication service.");
        this.userCode = externalUserCode;
    }

    public String getUserCode()
    {
        return userCode;
    }

}
