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
import ch.systemsx.cisd.cifex.rpc.server.CIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.server.Session;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.cifex.server.util.FileUploadFeedbackProvider;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LoggingContextHandler;
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

    /**
     * The Crowd property for the display name.
     */
    private static final String DISPLAY_NAME_PROPERTY = "displayName";

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, AbstractCIFEXService.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractCIFEXService.class);

    protected final IDomainModel domainModel;

    protected final IRequestContextProvider requestContextProvider;

    protected final LoggingContextHandler loggingContextHandler;

    protected final IAuthenticationService externalAuthenticationService;

    protected final IUserActionLog userBehaviorLogOrNull;

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
            final IUserActionLog userBehaviorLogOrNull,
            final IAuthenticationService externalAuthenticationService,
            final LoggingContextHandler loggingContextHandler,
            final int sessionExpirationPeriodMinutes)
    {
        this.domainModel = domainModel;
        this.requestContextProvider = requestContextProvider;
        this.userBehaviorLogOrNull = userBehaviorLogOrNull;
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
                    .getAttribute(CIFEXRPCService.SESSION_ATTRIBUTE_RPC_SESSION);
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

    private UserDTO finishLogin(final UserDTO userDTO, final boolean doUserActionLog)
    {
        // Do not transfer the password or its hash value to the client (security).
        userDTO.setPassword(null);
        userDTO.setPasswordHash(null);
        // Be a bit more restrictive for the registrator. Actually only user_id, name and email
        // should be transferred.
        final UserDTO fullRegistrator = userDTO.getRegistrator();
        if (fullRegistrator != null)
        {
            final UserDTO strippedRegistrator = new UserDTO();
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
        if (doUserActionLog && userBehaviorLogOrNull != null)
        {
            userBehaviorLogOrNull.logSuccessfulLogin();
        }
        return userDTO;
    }

    protected final UserDTO tryLoginUser(final String userCode, final String plainPassword,
            final boolean doUserActionLog) throws EnvironmentFailureException
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Try to login user '" + userCode + "'.");
        }
        final IUserManager userManager = domainModel.getUserManager();
        if (userManager.isDatabaseEmpty())
        {
            final UserDTO userDTO = createAdminUserDTO(userCode, plainPassword);
            userManager.createUser(userDTO);
            return finishLogin(userDTO, doUserActionLog);
        }
        UserDTO userDTOOrNull = userManager.tryFindUserByCodeFillRegistrator(userCode);
        if (userDTOOrNull == null || userDTOOrNull.isExternallyAuthenticated())
        {
            userDTOOrNull = tryExternalAuthenticationServiceLogin(userCode, plainPassword);
            if (userDTOOrNull != null)
            {
                return finishLogin(userDTOOrNull, doUserActionLog);
            }
        } else
        {
            final Password password = new Password(plainPassword);
            if (userDTOOrNull.isActive() && password.matches(userDTOOrNull.getPasswordHash()))
            {
                return finishLogin(userDTOOrNull, doUserActionLog);
            }
            operationLog.info("User '" + userDTOOrNull.getUserCode()
                    + "' which is deactivated tried to login.");
        }
        if (doUserActionLog && userBehaviorLogOrNull != null)
        {
            userBehaviorLogOrNull.logFailedLoginAttempt(userCode);
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
        userDTO.setPermanent(true);
        return userDTO;
    }

    protected UserDTO tryExternalAuthenticationServiceLogin(final String userOrEmail,
            final String password) throws EnvironmentFailureException
    {
        if (hasExternalAuthenticationService())
        {
            final String applicationToken = externalAuthenticationService.authenticateApplication();
            if (applicationToken == null)
            {
                if (userBehaviorLogOrNull != null)
                {
                    userBehaviorLogOrNull.logFailedLoginAttempt(userOrEmail);
                }
                final String msg =
                        "User '" + userOrEmail
                                + "' couldn't be authenticated because authentication of "
                                + "the application at the external authentication service failed.";
                operationLog.error(msg);
                throw new EnvironmentFailureException(msg);
            }
            final boolean authenticated =
                    externalAuthenticationService.authenticateUser(applicationToken, userOrEmail,
                            password);
            if (authenticated == false)
            {
                return null;
            }
            final Principal principal;
            try
            {
                principal =
                        externalAuthenticationService.getPrincipal(applicationToken, userOrEmail);
            } catch (final IllegalArgumentException ex)
            {
                operationLog.error(ex.getMessage());
                throw new EnvironmentFailureException(ex.getMessage());
            }
            final String code = principal.getUserId();
            final String email = principal.getEmail();
            final String firstName = principal.getFirstName();
            final String lastName = principal.getLastName();
            final String displayName;
            if (principal.getProperty(DISPLAY_NAME_PROPERTY) != null)
            {
                displayName = principal.getProperty(DISPLAY_NAME_PROPERTY);
            } else
            {
                displayName = firstName + " " + lastName;
            }
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
                userDTO.setAdmin(false);
                userDTO.setPermanent(true);
                userDTO.setActive(domainModel.getBusinessContext()
                        .isNewExternallyAuthenticatedUserStartActive());
                try
                {
                    userManager.createUser(userDTO);
                } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException ex)
                {
                    operationLog.error(ex.getMessage(), ex);
                    // This is actually an environment failure since the user couldn't have done
                    // anything different.
                    throw new EnvironmentFailureException(ex.getMessage());
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
                        userManager.updateUser(userDTO, null);
                    } catch (final DataIntegrityViolationException ex)
                    {
                        final String msg =
                                "User '" + code + "' with email '" + email + "' cannot be updated.";
                        operationLog.error(msg, ex);
                        throw new EnvironmentFailureException(msg);
                    }
                }
            }
            if (userDTO.isActive() == false)
            {
                return null;
            }
            return userDTO;
        } else
        {
            return null;
        }
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
