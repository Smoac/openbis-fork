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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.mock.web.MockHttpServletRequest;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.cifex.rpc.server.Session;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.common.servlet.AbstractActionLog;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;

/**
 * This class provides methods that are required to log the actions of CIFEX users.
 * 
 * @author Bernd Rinn
 */
public final class UserActionLog extends AbstractActionLog implements IUserActionLog
{

    private static final String USER_SESSION_RPC_TEMPLATE = "{USER: %s, RPCSESSION: %s} logout%s";

    private static final String USER_HOST_RPC_SESSION_TEMPLATE =
            "{USER: %s, HOST: %s, RPCSESSION: %s} ";

    private static final String TEMPORARY_UNTIL_STR = "TEMPORARY until ";

    private static final String REGULAR_STR = "REGULAR";

    private static final String ADMIN_STR = "ADMIN";

    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss zzz";

    private static final SimpleDateFormat dateTimeFormat =
            new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);

    private final boolean useRPCSession;

    public UserActionLog(final IRequestContextProvider requestContextProvider,
            boolean useRPCSession, String testingFlag)
    {
        super("true".equals(testingFlag) ? new IRequestContextProvider()
            {

                public HttpServletRequest getHttpServletRequest()
                {
                    return new MockHttpServletRequest();
                }

            } : requestContextProvider);
        this.useRPCSession = useRPCSession;
    }

    //
    // AbstractActionLog
    //

    @Override
    protected String getUserCode(HttpSession httpSession)
    {
        final Session sessionOrNull = AbstractCIFEXService.tryGetRPCSession(httpSession);
        if (useRPCSession && sessionOrNull != null)
        {
            return sessionOrNull.getUser().getUserCode();
        } else
        {
            final UserDTO userDTOOrNull =
                    (UserDTO) httpSession
                            .getAttribute(CIFEXServiceImpl.SESSION_ATTRIBUTE_USER_NAME);
            if (userDTOOrNull != null)
            {
                return userDTOOrNull.getUserCode();
            } else
            {
                return "-";
            }
        }
    }

    //
    // Users
    //

    @Override
    public String getUserHostSessionDescription()
    {
        final Session sessionOrNull = AbstractCIFEXService.tryGetRPCSession(getHttpSession());
        if (useRPCSession && sessionOrNull != null)
        {
            final String remoteHost = remoteHostProvider.getRemoteHost();
            final String userName = sessionOrNull.getUser().getUserCode();
            final String id = sessionOrNull.getSessionID();
            return String.format(USER_HOST_RPC_SESSION_TEMPLATE, userName, remoteHost, id);
        } else
        {
            return super.getUserHostSessionDescription();
        }
    }

    public void logCreateUser(final UserDTO user, final boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("create_user '%s': %s", getUserDescription(user),
                            getSuccessString(success)));
        }
    }

    /**
     * The states of a user.
     */
    private enum UserState
    {
        TEMPORARY, REGULAR, ADMIN;

        static UserState getState(final UserDTO user)
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
        if (Password.equals(oldUser.getPassword(), newUser.getPassword()) == false)
        {
            result.append("PASSWORD, ");
        }
        if (changed(oldUser.getUserFullName(), newUser.getUserFullName()))
        {
            result.append("FULLNAME, ");
        }
        if (changed(oldUser.getEmail(), newUser.getEmail()))
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

    @Private
    static boolean changed(String string1OrNull, String string2OrNull)
    {
        return StringUtils.equals(string1OrNull, string2OrNull) == false
                && (StringUtils.isBlank(string1OrNull) && StringUtils.isBlank(string2OrNull)) == false;
    }

    public void logUpdateUser(final UserDTO oldUser, final UserDTO newUser, final boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            final String whatChanged = getMessageWhatChanged(oldUser, newUser);
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("update_user '%s' [%s]: %s", getUserDescription(newUser),
                            whatChanged, getSuccessString(success)));
        }
    }

    public void logDeleteUser(final UserDTO user, final boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("delete_user '%s': %s", getUserDescription(user),
                            getSuccessString(success)));
        }
    }

    public void logExpireUser(final UserDTO user, final boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(String.format("{SYSTEM} delete_user '%s': %s",
                    getUserDescription(user), getSuccessString(success)));
        }
    }

    private static String getUserDescription(final UserDTO user)
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

    public void logUploadFileStart(String filename, FileDTO fileOrNull, long startPosition)
    {
        if (trackingLog.isInfoEnabled())
        {
            if (startPosition > 0L)
            {
                trackingLog.info(getUserHostSessionDescription()
                        + String.format("upload_file_resume %s [pos: %d]", getFileDescription(
                                fileOrNull, filename), startPosition));
            } else
            {
                trackingLog.info(getUserHostSessionDescription()
                        + String.format("upload_file_start %s", getFileDescription(fileOrNull,
                                filename)));
            }
        }
    }

    public void logUploadFileFinished(String filename, FileDTO fileOrNull, boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("upload_file_finished %s: %s", getFileDescription(fileOrNull,
                            filename), getSuccessString(success)));
        }
    }

    public void logShareFiles(final Collection<FileDTO> files,
            final Collection<UserDTO> usersToShareWith,
            final Collection<String> emailsOfUsersToShareWith,
            final Collection<String> invalidEmailAddresses, final boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            final String invalidEmailDesc =
                    invalidEmailAddresses.size() > 0 ? " [invalid: "
                            + getStringDescription(invalidEmailAddresses) + "]" : "";
            if (success)
            {
                trackingLog.info(getUserHostSessionDescription()
                        + String.format("share_files %s with %s: %s%s", getFileDescriptions(files),
                                getUserDescription(usersToShareWith), getSuccessString(success),
                                invalidEmailDesc));
            } else
            {
                trackingLog.info(getUserHostSessionDescription()
                        + String.format("share_files %s with %s: %s%s", getFileDescriptions(files),
                                getStringDescription(emailsOfUsersToShareWith),
                                getSuccessString(success), invalidEmailDesc));
            }
        }
    }

    public void logShareFilesAuthorizationFailure(Collection<FileDTO> files,
            Collection<String> recipientsToShareWith)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String
                            .format("share_files %s with %s: FAILED [insufficient privileges]",
                                    getFileDescriptions(files),
                                    getStringDescription(recipientsToShareWith)));
        }
    }

    /** Roles that can describe some kind of object. */
    private interface Descriptor<T>
    {
        public String getDescription(T object);
    }

    private <T> String getDescription(final Iterable<T> objects, final Descriptor<T> descriptor)
    {
        final StringBuilder b = new StringBuilder();
        b.append('{');
        for (final T object : objects)
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

    private String getFileDescription(final FileDTO fileOrNull, String filenameOrNull)
    {
        assert fileOrNull != null || filenameOrNull != null;

        if (fileOrNull != null)
        {
            final UserDTO registratorOrNull = fileOrNull.getOwner();
            if (registratorOrNull != null)
            {
                return "'" + registratorOrNull.getUserCode() + "::" + fileOrNull.getName() + "' ("
                        + fileOrNull.getID() + ")";
            } else
            {
                return fileOrNull.getName();
            }
        } else
        {
            return "'" + filenameOrNull + "'";
        }
    }

    private String getFileDescriptions(final Iterable<FileDTO> files)
    {
        return getDescription(files, new Descriptor<FileDTO>()
            {
                public String getDescription(final FileDTO file)
                {
                    return getFileDescription(file, null);
                }
            });
    }

    private String getUserDescription(final Iterable<UserDTO> users)
    {
        return getDescription(users, new Descriptor<UserDTO>()
            {
                public String getDescription(final UserDTO user)
                {
                    return user.getUserCode();
                }
            });
    }

    private String getStringDescription(final Iterable<String> users)
    {
        return getDescription(users, new Descriptor<String>()
            {
                public String getDescription(final String str)
                {
                    return str;
                }
            });
    }

    public void logDeleteFile(final FileDTO file, final boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("delete_file %s: %s", getFileDescription(file, null),
                            getSuccessString(success)));
        }
    }

    public void logExpireFile(final FileDTO file, final boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(String.format("{SYSTEM} delete_file %s: %s", getFileDescription(file,
                    null), getSuccessString(success)));
        }
    }

    public void logEditFile(long fileId, String newName, Date fileExpirationDateOrNull,
            boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("edit_file '%s' (%d) until %s: %s", newName, fileId,
                            (fileExpirationDateOrNull == null) ? "UNKNOWN" : dateTimeFormat
                                    .format(fileExpirationDateOrNull), getSuccessString(success)));
        }
    }

    public void logDownloadFileStart(FileDTO file, long startPosition)
    {
        if (accessLog.isInfoEnabled())
        {
            if (startPosition > 0L)
            {
                accessLog.info(getUserHostSessionDescription()
                        + String.format("download_file_resume %s [pos: %d]", getFileDescription(
                                file, null), startPosition));
            } else
            {
                accessLog.info(getUserHostSessionDescription()
                        + String.format("download_file_start %s", getFileDescription(file, null)));
            }
        }
    }

    public void logDownloadFileFinished(FileDTO file, boolean success)
    {
        if (accessLog.isInfoEnabled())
        {
            accessLog.info(getUserHostSessionDescription()
                    + String.format("download_file_finished %s: %s",
                            getFileDescription(file, null), getSuccessString(success)));
        }
    }

    public void logDownloadFileFailedNotAuthorized(FileDTO file)
    {
        if (accessLog.isInfoEnabled())
        {
            accessLog.info(getUserHostSessionDescription()
                    + String.format("download_file %s: NOT AUTHORIZED", getFileDescription(file,
                            null)));
        }
    }

    public void logDownloadFileFailedNotFound(FileDTO file)
    {
        if (accessLog.isInfoEnabled())
        {
            accessLog.info(getUserHostSessionDescription()
                    + String.format("download_file %s: NOT FOUND", getFileDescription(file, null)));
        }
    }

    public void logDeleteSharingLink(final long fileId, final String userCode, final boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("delete_sharing_link between file id %d and user '%s': %s",
                            fileId, userCode, getSuccessString(success)));
        }

    }

    public void logChangeUserCode(final String before, final String after, final boolean success)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("change_user_code from '%s' to '%s': %s", before, after,
                            getSuccessString(success)));
        }

    }

    public void logSwitchToExternalAuthentication(final String userCode, final boolean success)
    {
        if (accessLog.isInfoEnabled())
        {
            accessLog.info(getUserHostSessionDescription()
                    + String.format("switch_to_external_authentication user '%s': %s", userCode,
                            getSuccessString(success)));
        }
    }

    public void logLogout(Session session, LogoutReason reason)
    {
        if (authenticationLog.isInfoEnabled())
        {
            final UserDTO userOrNull = session.getUser();
            final String userName = (userOrNull == null) ? "UNKNOWN" : userOrNull.getUserCode();
            final String id = session.getSessionID();
            final String logoutMsg =
                    String.format(USER_SESSION_RPC_TEMPLATE, userName, id, reason.getLogText());
            authenticationLog.info(logoutMsg);
        }
    }

}
