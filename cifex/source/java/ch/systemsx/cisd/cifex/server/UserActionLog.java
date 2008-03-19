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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.server.IRemoteHostProvider;

/**
 * This class provides methods that are required to log the actions of CIFEX users.
 * 
 * @author Bernd Rinn
 */
public final class UserActionLog implements IUserActionLog
{
    private static final String TEMPORARY_UNTIL_STR = "TEMPORARY until ";

    private static final String REGULAR_STR = "REGULAR";

    private static final String ADMIN_STR = "ADMIN";

    private static final String FAILED = "FAILED";

    private static final String OK = "OK";

    private static final String USER_HOST_SESSION_TEMPLATE = "{USER: %s, HOST: %s, SESSION: %s} ";

    private static final Logger authenticationLog = LogFactory.getLogger(LogCategory.AUTH);

    private static final Logger accessLog = LogFactory.getLogger(LogCategory.ACCESS);

    private static final Logger trackingLog = LogFactory.getLogger(LogCategory.TRACKING);

    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss zzz";

    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);

    private final IRequestContextProvider requestContextProvider;

    private final IRemoteHostProvider remoteHostProvider;

    public UserActionLog(IRequestContextProvider requestContextProvider)
    {
        this.requestContextProvider = requestContextProvider;
        this.remoteHostProvider = new RequestContextProviderAdapter(requestContextProvider);
    }

    //
    // IUserBehaviorLog
    //

    //
    // Login / Logout
    //

    public void logFailedLoginAttempt(String userCode)
    {
        if (authenticationLog.isInfoEnabled())
        {
            final String logMessage =
                    String.format("{USER: %s, HOST: %s} login: FAILED", userCode, remoteHostProvider.getRemoteHost());
            authenticationLog.info(logMessage);
        }
    }

    public void logSuccessfulLogin()
    {
        if (authenticationLog.isInfoEnabled())
        {
            final String userHostSessionDescription = getUserHostSessionDescription();
            authenticationLog.info(userHostSessionDescription + "login: OK");
        }
    }

    public void logLogout(HttpSession httpSession)
    {
        if (authenticationLog.isInfoEnabled())
        {
            final long now = System.currentTimeMillis();
            final boolean timedOut =
                    (now - httpSession.getLastAccessedTime()) / 1000.0 >= httpSession.getMaxInactiveInterval();
            final UserDTO user = (UserDTO) httpSession.getAttribute(CIFEXServiceImpl.SESSION_NAME);
            final String logoutMsg =
                    String.format("{USER: %s, SESSION: %s} logout%s", user.getUserCode(), httpSession.getId(),
                            timedOut ? " (session timeout)" : "");
            authenticationLog.info(logoutMsg);
        }
    }

    //
    // Users
    //

    public void logCreateUser(UserDTO user, boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("create_user '%s': %s", getUserDescription(user), getSuccessString(success)));
        }
    }

    /**
     * The states of a user.
     */
    private enum UserState
    {
        TEMPORARY, REGULAR, ADMIN;

        static UserState getState(UserDTO user)
        {
            if (user.isAdmin())
            {
                return ADMIN;
            } else if (user.isPermanent())
            {
                return REGULAR;
            } else
            {
                return TEMPORARY;
            }
        }
    }

    private static String getMessageWhatChanged(final UserDTO oldUser, final UserDTO newUser)
    {
        assert oldUser != null;
        assert newUser != null;

        final StringBuilder result = new StringBuilder();
        final UserState oldState = UserState.getState(oldUser);
        final UserState newState = UserState.getState(newUser);
        if (newState.equals(oldState) == false)
        {
            result.append("STATE: ");
            result.append(oldState);
            result.append("->");
            result.append(newState);
            result.append(", ");
        }
        if (StringUtils.equals(oldUser.getEncryptedPassword(), newUser.getEncryptedPassword()) == false)
        {
            result.append("PASSWORD, ");
        }
        if (StringUtils.equals(oldUser.getUserFullName(), newUser.getUserFullName()) == false)
        {
            result.append("FULLNAME, ");
        }
        if (StringUtils.equals(oldUser.getEmail(), newUser.getEmail()) == false)
        {
            result.append("EMAIL, ");
        }
        if (UserState.TEMPORARY.equals(oldState) && UserState.TEMPORARY.equals(newState))
        {
            final Date oldExpirationDate = oldUser.getExpirationDate();
            final Date newExpirationDate = newUser.getExpirationDate();
            if (newExpirationDate != null && newExpirationDate.equals(oldExpirationDate) == false)
            {
                result.append("RENEW, ");
            }
        }
        if (result.length() == 0)
        {
            result.append("UNCHANGED");
        } else
        {
            result.setLength(result.length() - 2);
        }
        return result.toString();
    }

    public void logUpdateUser(UserDTO oldUser, UserDTO newUser, boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            final String whatChanged = getMessageWhatChanged(oldUser, newUser);
            trackingLog.info(String.format(getUserHostSessionDescription() + "update_user '%s' [%s]: %s",
                    getUserDescription(newUser), whatChanged, getSuccessString(success)));
        }
    }

    public void logDeleteUser(UserDTO user, boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("delete_user '%s': %s", getUserDescription(user), getSuccessString(success)));
        }
    }

    public void logExpireUser(UserDTO user, boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(String.format("{SYSTEM} delete_user '%s': %s", getUserDescription(user),
                    getSuccessString(success)));
        }
    }

    private String getUserHostSessionDescription()
    {
        final HttpSession httpSession = getHttpSession();
        if (httpSession == null)
        {
            return String.format(USER_HOST_SESSION_TEMPLATE, "UNKNOWN", remoteHostProvider.getRemoteHost(), "UNKNOWN");
        }
        final UserDTO user = (UserDTO) httpSession.getAttribute(CIFEXServiceImpl.SESSION_NAME);
        return String.format(USER_HOST_SESSION_TEMPLATE, user.getUserCode(), remoteHostProvider.getRemoteHost(),
                httpSession.getId());
    }

    private HttpSession getHttpSession()
    {
        return requestContextProvider.getHttpServletRequest().getSession(false);
    }

    private static String getSuccessString(boolean success)
    {
        return success ? OK : FAILED;
    }

    private static String getUserDescription(UserDTO user)
    {
        String state;
        if (user.isAdmin())
        {
            state = ADMIN_STR;
        } else if (user.isPermanent())
        {
            state = REGULAR_STR;
        } else
        {
            final Date expirationDateOrNull = user.getExpirationDate();
            if (expirationDateOrNull != null)
            {
                state = TEMPORARY_UNTIL_STR + dateTimeFormat.format(expirationDateOrNull);
            } else
            {
                state = TEMPORARY_UNTIL_STR + "???";
            }
        }
        return user.getUserCode() + " [" + state + "]";
    }

    //
    // Files
    //

    public void logUploadFile(String filename, boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("upload_file '%s': %s", filename, getSuccessString(success)));
        }
    }

    public void logShareFiles(Collection<FileDTO> files, Collection<UserDTO> usersToShareWith,
            Collection<String> emailsOfUsersToShareWith, Collection<String> invalidEmailAddresses, boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            final String invalidEmailDesc =
                    (invalidEmailAddresses.size() > 0) ? " [invalid: " + getStringDescription(invalidEmailAddresses)
                            + "]" : "";
            if (success)
            {
                trackingLog.info(getUserHostSessionDescription()
                        + String.format("share_files %s with %s: %s%s", getFileDescriptions(files),
                                getUserDescription(usersToShareWith), getSuccessString(success), invalidEmailDesc));
            } else
            {
                trackingLog.info(getUserHostSessionDescription()
                        + String.format("share_files %s with %s: %s", getFileDescriptions(files),
                                getStringDescription(emailsOfUsersToShareWith), getSuccessString(success),
                                invalidEmailDesc));
            }
        }
    }

    /** Roles that can describe some kind of object. */
    private interface Descriptor<T>
    {
        public String getDescription(T object);
    }

    private <T> String getDescription(Iterable<T> objects, Descriptor<T> descriptor)
    {
        final StringBuilder b = new StringBuilder();
        b.append('{');
        for (T object : objects)
        {
            b.append(descriptor.getDescription(object));
            b.append(',');
        }
        if (b.length() > 1)
        {
            b.setLength(b.length() - 1);
        }
        b.append('}');
        return b.toString();
    }

    private String getFileDescription(FileDTO file)
    {
        assert file != null;
        
        final UserDTO registratorOrNull = file.getRegisterer();
        if (registratorOrNull != null)
        {
            return registratorOrNull.getUserCode() + "::" + file.getName();
        } else
        {
            return file.getName();
        }
    }

    private String getFileDescriptions(Iterable<FileDTO> files)
    {
        return getDescription(files, new Descriptor<FileDTO>()
            {
                public String getDescription(FileDTO file)
                {
                    return getFileDescription(file);
                }
            });
    }

    private String getUserDescription(Iterable<UserDTO> users)
    {
        return getDescription(users, new Descriptor<UserDTO>()
            {
                public String getDescription(UserDTO user)
                {
                    return user.getUserCode();
                }
            });
    }

    private String getStringDescription(Iterable<String> users)
    {
        return getDescription(users, new Descriptor<String>()
            {
                public String getDescription(String str)
                {
                    return str;
                }
            });
    }

    public void logDeleteFile(FileDTO file, boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("delete_file '%s': %s", getFileDescription(file), getSuccessString(success)));
        }
    }

    public void logExpireFile(FileDTO file, boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(String.format("{SYSTEM} delete_file '%s': %s", getFileDescription(file),
                    getSuccessString(success)));
        }
    }

    public void logRenewFile(FileDTO file, boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("renew_file '%s' until %s: %s", getFileDescription(file), dateTimeFormat
                            .format(file.getExpirationDate()), success));
        }
    }

    public void logDownloadFile(FileDTO file, boolean success)
    {
        if (accessLog.isInfoEnabled())
        {
            accessLog.info(getUserHostSessionDescription()
                    + String.format("download_file '%s': %s", getFileDescription(file), success));
        }
    }

}
