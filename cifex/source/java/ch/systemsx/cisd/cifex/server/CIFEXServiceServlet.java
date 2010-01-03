package ch.systemsx.cisd.cifex.server;

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

import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ch.systemsx.cisd.cifex.client.Configuration;
import ch.systemsx.cisd.cifex.client.FileNotFoundException;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.InsufficientPrivilegesException;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.UserNotFoundException;
import ch.systemsx.cisd.cifex.shared.basic.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.shared.basic.UserFailureException;
import ch.systemsx.cisd.cifex.shared.basic.dto.CurrentUserInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.OwnerFileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.GWTSpringController;

/**
 * The server <code>ICifexService</code> implementation.
 * <p>
 * Note that this is a servlet. It is usually good practice to let the constructor body empty and to
 * put initialization stuff in {@link #init(ServletConfig)}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class CIFEXServiceServlet extends GWTSpringController implements ICIFEXService
{
    private static final String CIFEX_SERVICE_BEAN_NAME = "cifex-service";

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, CIFEXServiceServlet.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CIFEXServiceServlet.class);

    private static final long serialVersionUID = 1L;

    /** The encapsulated {@link ICIFEXService} which is doing the real work. */
    private ICIFEXService cifexServiceDelegate;

    private final void initService(final ServletContext servletContext)
    {
        final BeanFactory context =
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        cifexServiceDelegate = (ICIFEXService) context.getBean(CIFEX_SERVICE_BEAN_NAME);
    }

    //
    // ICifexService
    //

    public Configuration getConfiguration() throws InvalidSessionException
    {
        return cifexServiceDelegate.getConfiguration();
    }

    public final CurrentUserInfoDTO getCurrentUser() throws InvalidSessionException
    {
        return cifexServiceDelegate.getCurrentUser();
    }

    public UserInfoDTO refreshQuotaInformationOfCurrentUser() throws InvalidSessionException
    {
        return cifexServiceDelegate.refreshQuotaInformationOfCurrentUser();
    }

    public final UserInfoDTO tryLogin(final String user, final String password)
            throws UserFailureException, EnvironmentFailureException
    {
        return cifexServiceDelegate.tryLogin(user, password);
    }

    public final void logout()
    {
        cifexServiceDelegate.logout();
    }

    public List<FileInfoDTO> listDownloadFiles() throws InvalidSessionException
    {
        return cifexServiceDelegate.listDownloadFiles();
    }

    public List<OwnerFileInfoDTO> listOwnedFiles() throws InvalidSessionException
    {
        return cifexServiceDelegate.listOwnedFiles();
    }

    public List<OwnerFileInfoDTO> listFiles() throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        return cifexServiceDelegate.listFiles();
    }

    public List<UserInfoDTO> listUsers() throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        return cifexServiceDelegate.listUsers();
    }

    public UserInfoDTO createUser(final UserInfoDTO user, final String password,
            final String comment) throws EnvironmentFailureException, UserFailureException,
            InvalidSessionException, InsufficientPrivilegesException
    {
        return cifexServiceDelegate.createUser(user, password, comment);
    }

    public void deleteUser(final long code) throws InvalidSessionException,
            InsufficientPrivilegesException, UserNotFoundException
    {
        cifexServiceDelegate.deleteUser(code);
    }

    public FileInfoDTO getFile(long fileId) throws InvalidSessionException,
            InsufficientPrivilegesException, IllegalArgumentException
    {
        return cifexServiceDelegate.getFile(fileId);
    }

    public void deleteFile(final long id) throws InvalidSessionException,
            InsufficientPrivilegesException, FileNotFoundException
    {
        cifexServiceDelegate.deleteFile(id);
    }

    public void registerFilenamesForUpload(final String[] filenamesForUpload)
            throws InvalidSessionException
    {
        cifexServiceDelegate.registerFilenamesForUpload(filenamesForUpload);
    }

    public FileUploadFeedback getFileUploadFeedback() throws InvalidSessionException
    {
        return cifexServiceDelegate.getFileUploadFeedback();
    }

    //
    // RemoteServiceServlet
    //

    @Override
    public final void init(final ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            initService(config.getServletContext());
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("'%s' successfully initialized.", getClass()
                        .getName()));
            }
        } catch (final Exception ex)
        {
            notificationLog.fatal("Failure during CIFEX service servlet initialization.", ex);
            throw new ServletException(ex);
        }
    }

    public UserInfoDTO updateUser(final UserInfoDTO user, final String password,
            final boolean sendUpdateInformationToUser) throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException
    {
        return cifexServiceDelegate.updateUser(user, password, sendUpdateInformationToUser);
    }

    public UserInfoDTO getUser(long id) throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        return cifexServiceDelegate.getUser(id);
    }

    public UserInfoDTO tryFindUserByUserCode(final String userCode) throws InvalidSessionException
    {
        return cifexServiceDelegate.tryFindUserByUserCode(userCode);
    }

    public List<UserInfoDTO> findUserByEmail(final String email) throws InvalidSessionException
    {
        return cifexServiceDelegate.findUserByEmail(email);
    }

    public List<UserInfoDTO> listUsersOwnedBy(final long userId) throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        return cifexServiceDelegate.listUsersOwnedBy(userId);
    }

    public Date updateFileUserData(long fileId, String name, String commentOrNull,
            Date expirationDate) throws InvalidSessionException, InsufficientPrivilegesException
    {
        return cifexServiceDelegate.updateFileUserData(fileId, name, commentOrNull, expirationDate);
    }

    public List<UserInfoDTO> listUsersFileSharedWith(final long fileId)
            throws InvalidSessionException, InsufficientPrivilegesException
    {
        return cifexServiceDelegate.listUsersFileSharedWith(fileId);
    }

    public void updateSharingLinks(long fileId, List<String> usersToAdd, List<String> usersToRemove)
            throws InvalidSessionException, InsufficientPrivilegesException, FileNotFoundException,
            UserFailureException
    {
        cifexServiceDelegate.updateSharingLinks(fileId, usersToAdd, usersToRemove);
    }

    public void changeUserCode(final String before, final String after)
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException
    {
        cifexServiceDelegate.changeUserCode(before, after);

    }

    public UserInfoDTO trySwitchToExternalAuthentication(final String userCode,
            final String plainPassword) throws EnvironmentFailureException,
            InvalidSessionException, InsufficientPrivilegesException
    {
        return cifexServiceDelegate.trySwitchToExternalAuthentication(userCode, plainPassword);
    }

    public Boolean showSwitchToExternalOption(final UserInfoDTO user)
    {
        return cifexServiceDelegate.showSwitchToExternalOption(user);
    }

}
