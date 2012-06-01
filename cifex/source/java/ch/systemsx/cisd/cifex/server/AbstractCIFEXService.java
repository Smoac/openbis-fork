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

package ch.systemsx.cisd.cifex.server;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.rpc.server.Session;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.UserUtils;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.cifex.server.util.FileUploadFeedbackProvider;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LoggingContextHandler;
import ch.systemsx.cisd.common.server.IRemoteHostProvider;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.servlet.RequestContextProviderAdapter;

/**
 * Base class for classes implementing services. Allows login and logout.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractCIFEXService
{
    /** The attribute name under which the user can be found for HTTP / AJAX sessions . */
    public static final String SESSION_ATTRIBUTE_USER_NAME = "cifex-user";

    /** The attribute name under which the RPC session could be found for RPC sessions. */
    public static final String SESSION_ATTRIBUTE_RPC_SESSION = "rpc-session";

    /** The attribute name that holds the queue that has the feedbacks of the upload. */
    static final String UPLOAD_FEEDBACK_QUEUE = "upload-feedback-queue";

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, AbstractCIFEXService.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractCIFEXService.class);

    protected final IDomainModel domainModel;

    protected final IRequestContextProvider requestContextProvider;

    protected final IRemoteHostProvider remoteHostProvider;

    protected final LoggingContextHandler loggingContextHandler;

    protected final IAuthenticationService externalAuthenticationService;

    protected final IUserActionLog userActionLog;

    /** Session timeout in seconds. */
    private final int sessionExpirationPeriodInSeconds;

    protected static LoggingContextHandler createLoggingContextHandler(
            final IRequestContextProvider requestContextProvider)
    {
        if (requestContextProvider != null)
        {
            return new LoggingContextHandler(new RequestContextProviderAdapter(
                    requestContextProvider));
        } else
        {
            return null;
        }
    }

    protected AbstractCIFEXService(final IDomainModel domainModel,
            final IRequestContextProvider requestContextProvider,
            final IUserActionLog userActionLog,
            final IAuthenticationService externalAuthenticationService,
            final LoggingContextHandler loggingContextHandler,
            final int sessionExpirationPeriodMinutes)
    {
        this.domainModel = domainModel;
        this.requestContextProvider = requestContextProvider;
        this.remoteHostProvider = new RequestContextProviderAdapter(requestContextProvider);
        this.userActionLog = userActionLog;
        this.externalAuthenticationService = externalAuthenticationService;
        this.loggingContextHandler = loggingContextHandler;
        this.sessionExpirationPeriodInSeconds = sessionExpirationPeriodMinutes * 60;
        checkAuthentication();
    }

    /** Returns the rpc session for an <var>httpSession</var>, if any. */
    public static Session tryGetRPCSession(HttpSession httpSessionOrNull)
    {
        if (httpSessionOrNull != null)
        {
            return (Session) httpSessionOrNull
                    .getAttribute(AbstractCIFEXService.SESSION_ATTRIBUTE_RPC_SESSION);
        } else
        {
            return null;
        }
    }

    private void checkAuthentication() throws HighLevelException
    {
        if (hasExternalAuthenticationService())
        {
            try
            {
                this.externalAuthenticationService.check();
                operationLog.info("Configuration check of external authentication service "
                        + externalAuthenticationService.getClass().getSimpleName() + " passed.");
            } catch (HighLevelException ex)
            {
                if (externalAuthenticationService.isRemote()
                        && ex instanceof ch.systemsx.cisd.common.exceptions.EnvironmentFailureException)
                {
                    notificationLog
                            .error(
                                    "Self-test failed for external authentication service '"
                                            + externalAuthenticationService.getClass()
                                                    .getSimpleName()
                                            + "'. This authentication service is remote and the resource may become "
                                            + "available later, thus continuing anyway.", ex);
                } else
                {
                    notificationLog.error("Self-test failed for external authentication service '"
                            + externalAuthenticationService.getClass().getSimpleName() + "'.", ex);
                    throw ex;
                }
            }
        }
    }

    protected final boolean hasExternalAuthenticationService()
    {
        return externalAuthenticationService != null
                && externalAuthenticationService instanceof NullAuthenticationService == false;
    }

    private final String createSession(final UserDTO user)
    {
        final HttpSession httpSession = getSession(true);
        // A negative time (in seconds) indicates the session should never timeout.
        httpSession.setMaxInactiveInterval(sessionExpirationPeriodInSeconds);
        httpSession.setAttribute(SESSION_ATTRIBUTE_USER_NAME, user);
        httpSession.setAttribute(UPLOAD_FEEDBACK_QUEUE, new FileUploadFeedbackProvider());
        return httpSession.getId();
    }

    protected final HttpSession getSession(final boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }

    private UserDTO finishLogin(final UserDTO userDTO)
    {
        // Do not transfer the password or its hash value to the client (security).
        userDTO.setPassword(null);
        userDTO.setPasswordHash(null);
        // Be a bit more restrictive for the registrator. Actually only user_code, name and email
        // should be transferred.
        final UserDTO fullRegistrator = userDTO.getRegistrator();
        if (fullRegistrator != null)
        {
            final UserDTO strippedRegistrator = new UserDTO();
            strippedRegistrator.setID(fullRegistrator.getID());
            strippedRegistrator.setUserCode(fullRegistrator.getUserCode());
            strippedRegistrator.setEmail(fullRegistrator.getEmail());
            strippedRegistrator.setUserFullName(fullRegistrator.getUserFullName());
            userDTO.setRegistrator(strippedRegistrator);
        }
        final String sessionToken = createSession(userDTO);
        loggingContextHandler.addContext(sessionToken, "user (email):" + userDTO.getEmail()
                + ", session start:" + DateFormatUtils.format(new Date(), DATE_FORMAT_PATTERN));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Successfully created session for user " + userDTO);
        }
        return userDTO;
    }

    protected final UserDTO tryLoginUser(final String userCode, final String plainPassword)
            throws EnvironmentFailureException
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Try to login user '" + userCode + "'.");
        }
        final IUserManager userManager = domainModel.getUserManager();
        if (userManager.isDatabaseEmpty())
        {
            final UserDTO userDTO = createAdminUserDTO(userCode, plainPassword);
            boolean success = false;
            try
            {
                userManager.createUser(userDTO, null);
                success = true;
            } finally
            {
                userActionLog.logCreateUser(userDTO, success);
            }
            return finishLogin(userDTO);
        }
        UserDTO userDTOOrNull = userManager.tryFindUserByCode(userCode);
        if (userDTOOrNull == null || userDTOOrNull.isExternallyAuthenticated())
        {
            userDTOOrNull = tryExternalAuthenticationServiceLogin(userCode, plainPassword);
            if (userDTOOrNull != null)
            {
                return finishLogin(userDTOOrNull);
            }
        } else
        {
            final Password password = new Password(plainPassword);
            if (userDTOOrNull.isActive() && password.matches(userDTOOrNull.getPasswordHash()))
            {
                return finishLogin(userDTOOrNull);
            }
            operationLog.info(String.format("User '%s' failed to login: %s.", userDTOOrNull
                    .getUserCode(), userDTOOrNull.isActive() ? "password mismatch"
                    : "account deactivated"));
        }
        return null;
    }

    private static UserDTO createAdminUserDTO(final String userCode, final String plainPassword)
    {
        final UserDTO userDTO = new UserDTO();
        userDTO.setUserCode(userCode);
        userDTO.setEmail(userCode);
        userDTO.setPassword(new Password(plainPassword));
        userDTO.setAdmin(true);
        return userDTO;
    }

    protected UserDTO tryExternalAuthenticationServiceLogin(final String userId,
            final String password) throws EnvironmentFailureException
    {
        if (hasExternalAuthenticationService() == false)
        {
            return null;
        }
        final Principal principalOrNull =
                externalAuthenticationService.tryGetAndAuthenticateUser(userId, password);
        final boolean authenticated = Principal.isAuthenticated(principalOrNull);
        if (authenticated == false)
        {
            return null;
        }
        final UserDTO userDTO =
                createOrUpdateUserFromExternalAuthenticationService(principalOrNull, null);
        if (userDTO.isActive() == false)
        {
            return null;
        }
        return userDTO;
    }

    protected UserDTO createOrUpdateUserFromExternalAuthenticationService(
            final Principal principal, final UserInfoDTO userOrNull)
    {
        final String code = principal.getUserId();
        final String email = principal.getEmail();
        final String displayName = UserUtils.extractDisplayName(principal);
        final IUserManager userManager = domainModel.getUserManager();
        UserDTO userDTO = userManager.tryFindUserByCode(code);
        if (userDTO == null)
        {
            userDTO = new UserDTO();
            userDTO.setUserCode(code);
            userDTO.setUserFullName(displayName);
            userDTO.setEmail(email);
            userDTO.setPassword(null);
            userDTO.setExternallyAuthenticated(true);
            if (userOrNull != null)
            {
                userDTO.setAdmin(userOrNull.isAdmin());
                userDTO.setExpirationDate(userOrNull.getExpirationDate());
                userDTO.setActive(userOrNull.isActive());

            } else
            {
                userDTO.setAdmin(false);
                userDTO.setActive(domainModel.getBusinessContext()
                        .isNewExternallyAuthenticatedUserStartActive());
            }
            boolean success = false;
            try
            {
                userManager.createUser(userDTO, null);
                success = true;
            } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException ex)
            {
                operationLog.error(ex.getMessage(), ex);
                // This is actually an environment failure since the user couldn't have done
                // anything different.
                throw new EnvironmentFailureException(ex.getMessage());
            } finally
            {
                userActionLog.logCreateUser(userDTO, success);
            }
        } else
        { // check whether name or email of the principal have changed, and update, if
            // necessary
            boolean changed = false;
            if (StringUtils.equals(displayName, userDTO.getUserFullName()) == false)
            {
                userDTO.setUserFullName(displayName);
                changed = true;
            }
            if (StringUtils.equals(email, userDTO.getEmail()) == false)
            {
                userDTO.setEmail(email);
                changed = true;
            }
            if (changed)
            {
                try
                {
                    userManager.updateUser(userDTO, null, null, userActionLog);
                } catch (final DataIntegrityViolationException ex)
                {
                    final String msg =
                            "User '" + code + "' with email '" + email + "' cannot be updated.";
                    operationLog.error(msg, ex);
                    throw new EnvironmentFailureException(msg);
                }
            }
        }
        return userDTO;
    }

    public final void logout()
    {
        final HttpSession httpSession = getSession(false);
        if (httpSession != null)
        {
            loggingContextHandler.destroyContext(httpSession.getId());
            // This unbinds all the attributes as well. So do not do clever cleaning here.
            httpSession.invalidate();
        }
    }
}
