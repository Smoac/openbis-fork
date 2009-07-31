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

    public final UserInfoDTO getCurrentUser() throws InvalidSessionException
    {
        return cifexServiceDelegate.getCurrentUser();
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

    public FileInfoDTO[] listDownloadFiles() throws InvalidSessionException
    {
        return cifexServiceDelegate.listDownloadFiles();
    }

    public FileInfoDTO[] listUploadedFiles() throws InvalidSessionException
    {
        return cifexServiceDelegate.listUploadedFiles();
    }

    public FileInfoDTO[] listFiles() throws InvalidSessionException, InsufficientPrivilegesException
    {
        return cifexServiceDelegate.listFiles();
    }

    public UserInfoDTO[] listUsers() throws InvalidSessionException, InsufficientPrivilegesException
    {
        return cifexServiceDelegate.listUsers();
    }

    public void createUser(final UserInfoDTO user, final String password, final UserInfoDTO registratorOrNull,
            final String comment) throws EnvironmentFailureException, UserFailureException,
            InvalidSessionException, InsufficientPrivilegesException
    {
        cifexServiceDelegate.createUser(user, password, registratorOrNull, comment);

    }

    public void deleteUser(final String code) throws InvalidSessionException,
            InsufficientPrivilegesException, UserNotFoundException
    {
        cifexServiceDelegate.deleteUser(code);

    }

    public void deleteFile(final String idStr) throws InvalidSessionException,
            InsufficientPrivilegesException, FileNotFoundException
    {
        cifexServiceDelegate.deleteFile(idStr);
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

    public void updateUser(final UserInfoDTO user, final String password,
            final boolean sendUpdateInformationToUser) throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException
    {
        cifexServiceDelegate.updateUser(user, password, sendUpdateInformationToUser);
    }

    public UserInfoDTO tryFindUserByUserCode(final String userCode) throws InvalidSessionException
    {
        return cifexServiceDelegate.tryFindUserByUserCode(userCode);
    }

    public UserInfoDTO[] tryFindUserByEmail(final String email) throws InvalidSessionException
    {
        return cifexServiceDelegate.tryFindUserByEmail(email);
    }

    public UserInfoDTO[] listUsersRegisteredBy(final String userCode) throws InvalidSessionException
    {
        return cifexServiceDelegate.listUsersRegisteredBy(userCode);
    }

    public void updateFileExpiration(final String idStr) throws InvalidSessionException,
            InsufficientPrivilegesException, FileNotFoundException
    {
        cifexServiceDelegate.updateFileExpiration(idStr);
    }

    public UserInfoDTO[] listUsersFileSharedWith(final String fileId) throws InvalidSessionException
    {
        return cifexServiceDelegate.listUsersFileSharedWith(fileId);
    }

    public void deleteSharingLink(final String fileId, final String userCode)
            throws InvalidSessionException, InsufficientPrivilegesException, FileNotFoundException
    {
        cifexServiceDelegate.deleteSharingLink(fileId, userCode);

    }

    public void createSharingLink(final String fileId, final String emailsOfUsers)
            throws UserFailureException, InvalidSessionException, InsufficientPrivilegesException,
            FileNotFoundException
    {

        cifexServiceDelegate.createSharingLink(fileId, emailsOfUsers);
    }

    public void changeUserCode(final String before, final String after)
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException
    {
        cifexServiceDelegate.changeUserCode(before, after);

    }

    public UserInfoDTO trySwitchToExternalAuthentication(final String userCode, final String plainPassword)
            throws EnvironmentFailureException, InvalidSessionException,
            InsufficientPrivilegesException
    {
        return cifexServiceDelegate.trySwitchToExternalAuthentication(userCode, plainPassword);
    }

    public boolean showSwitchToExternalOption(final UserInfoDTO user)
    {
        return cifexServiceDelegate.showSwitchToExternalOption(user);
    }

}
