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

import java.util.Collection;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * @author pkupczyk
 */
public class ExternalAuthenticationServiceNotConfiguredForExternalUsersException extends ConfigurationFailureException
{

    private static final long serialVersionUID = 1L;

    private Collection<String> externalUsersCodes;

    public ExternalAuthenticationServiceNotConfiguredForExternalUsersException(Collection<String> externalUsersCodes)
    {
        super("Cannot load information for the following users: " + externalUsersCodes
                + ". They are externally authenticated, but the authentication service hasn't been specified.");
        this.externalUsersCodes = externalUsersCodes;
    }

    public Collection<String> getExternalUsersCodes()
    {
        return externalUsersCodes;
    }

}
