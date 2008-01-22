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

package ch.systemsx.cisd.cifex.server;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.UserFailureException;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.cifex.server.business.DomainModel;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.logging.IRemoteHostProvider;
import ch.systemsx.cisd.common.logging.LoggingContextHandler;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * The real <code>ICifexService</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public final class CIFEXServiceImpl implements ICIFEXService
{
    private final DomainModel domainModel;

    private final LoggingContextHandler loggingContextHandler;

    private final IAuthenticationService externalAuthenticationService;

    public CIFEXServiceImpl(final DomainModel domainModel, final IRequestContextProvider requestContextProvider,
            final IAuthenticationService externalAuthenticationService)
    {
        this.domainModel = domainModel;
        this.externalAuthenticationService = externalAuthenticationService;
        loggingContextHandler = new LoggingContextHandler(new IRemoteHostProvider()
            {
                public String getRemoteHost()
                {
                    return requestContextProvider.getHttpServletRequest().getRemoteHost();
                }
            });
    }

    private boolean hasExternalAuthenticationService()
    {
        return externalAuthenticationService != null
                && externalAuthenticationService instanceof NullAuthenticationService == false;
    }

    //
    // ICifexService
    //

    public final boolean isAuthenticated()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public final User login(final String user, final String password) throws UserFailureException
    {
        IUserManager userManager = domainModel.getUserManager();
        if (hasExternalAuthenticationService())
        {
            String applicationToken = externalAuthenticationService.authenticateApplication();
            boolean authenticated = externalAuthenticationService.authenticateUser(applicationToken, user, password);
            if (authenticated == false)
            {
                return null;
            }
            Principal principal = externalAuthenticationService.getPrincipal(applicationToken, user);
            UserDTO userDTO = userManager.tryToFindUser(user);
            if (userDTO == null)
            {
                userDTO = new UserDTO();
                userDTO.setUserID(user);
                userDTO.setEmail(principal.getEmail());
                userDTO.setEncryptedPassword(StringUtilities.encrypt(password));
                userDTO.setAdmin(false);
                userDTO.setPermanent(true);
                userManager.createUser(userDTO);
            }
            return BeanUtils.createBean(User.class, userDTO);
        } else
        {
            String encryptedPassword = StringUtilities.encrypt(password);
            UserDTO userDTO = userManager.tryToFindUser(user);
            if (userDTO == null || encryptedPassword.equals(userDTO.getEncryptedPassword()) == false)
            {
                return null;
            }
            return BeanUtils.createBean(User.class, userDTO);
        }
    }

    public final void logout()
    {
        // TODO Auto-generated method stub

    }
}
