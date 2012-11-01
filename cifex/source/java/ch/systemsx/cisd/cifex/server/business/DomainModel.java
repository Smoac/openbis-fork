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

package ch.systemsx.cisd.cifex.server.business;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanPostProcessor;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.cifex.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.cifex.server.business.bo.BusinessObjectFactory;
import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInvocationHandler;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.security.PasswordGenerator;

/**
 * Provider of all manager objects.
 * 
 * @author Franz-Josef Elmer
 */
public final class DomainModel implements IDomainModel
{

    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DomainModel.class);

    private IUserManager userManager;

    private IFileManager fileManager;

    private ITriggerManager triggerManager;

    private final IDAOFactory daoFactory;

    private final BeanPostProcessor processor;

    private final BusinessContext businessContext;

    private final BusinessObjectFactory boFactory;

    private final IAuthenticationService externalAuthenticationService;

    /**
     * Creates an instance based on the specified DAO Factory and mail client. The specified bean
     * post processor is needed to create proxies for the various manager objects which handle
     * transactions. Corresponding manager methods are annotated with <code>@Transactional</code>.
     * In the Spring <code>applicationContext.xml</code> it is assumed that a the bean post
     * processor is correctly configured with the right TransactionInterceptor.
     */
    public DomainModel(final IDAOFactory daoFactory, final IMailClient mailClient,
            final IUserActionLog userActionLogHttp, final IUserActionLog userActionLogRpc,
            final BeanPostProcessor processor, final UserHttpSessionHolder userSessionHolder,
            final String overrideURL, IAuthenticationService externalAuthenticationService)
    {
        assert daoFactory != null : "Undefined DAO Factory";
        assert mailClient != null : "Undefined mail client";
        assert userSessionHolder != null : "Undefined user session holder";

        this.daoFactory = daoFactory;
        this.processor = processor;
        this.externalAuthenticationService = externalAuthenticationService;
        businessContext = new BusinessContext();
        businessContext.setMailClient(mailClient);
        businessContext.setPasswordGenerator(new PasswordGenerator());
        businessContext.setUserHttpSessionHolder(userSessionHolder);
        businessContext.setUserActionLogHttp(userActionLogHttp);
        businessContext.setUserActionLogRpc(userActionLogRpc);
        businessContext.setOverrideURL(overrideURL);
        businessContext.setSystemVersion(BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
        boFactory = new BusinessObjectFactory(daoFactory, businessContext);
    }

    /** Sets the file store root path where the files are going to be saved. */
    public final void setFileStorePath(final String fileStorePath)
    {
        businessContext.setFileStore(new File(fileStorePath));
        File fileStore = businessContext.getFileStore();
        if (fileStore.exists() && fileStore.isDirectory() == false)
        {
            throw new EnvironmentFailureException("File store '" + fileStore.getAbsolutePath()
                    + "' already exists but is not a directory.");
        } else if (fileStore.exists() == false)
        {
            final boolean successful = fileStore.mkdirs();
            if (successful == false)
            {
                throw new EnvironmentFailureException("Couldn't create file store '"
                        + fileStore.getAbsolutePath() + "' for some unknown reason.");
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format(
                        "File store '%s' has been set and is ready to accept files.", fileStore
                                .getAbsolutePath()));
            }
        }
    }

    /**
     * Sets the session timeout (in minutes).
     */
    public void setSessionTimeoutMin(int sessionTimeoutMin)
    {
        businessContext.setSessionTimeoutMin(sessionTimeoutMin);
    }

    /**
     * Sets the file retention time in days.
     * <p>
     * This is typically set by <i>Spring</i> via injection.
     * </p>
     */
    public final void setFileRetention(final int fileRetention)
    {
        businessContext.setFileRetention(fileRetention);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("File retention set to %d days.", fileRetention));
        }
    }

    /**
     * Sets the user retention time in days.
     * <p>
     * This is typically by <i>Spring</i> via injection.
     * </p>
     */
    public final void setUserRetention(final int userRetention)
    {
        businessContext.setUserRetention(userRetention);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("User retention set to %d days.", userRetention));
        }
    }

    /**
     * Sets the maximum file retention time in days.
     * <p>
     * This is typically set by <i>Spring</i> via injection.
     * </p>
     */
    public final void setMaxFileRetention(final int maxFileRetention)
    {
        businessContext.setMaxFileRetention(maxFileRetention);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Maximum file retention set to %d days.",
                    maxFileRetention));
        }
    }

    /**
     * Sets the maximum user retention time in days.
     * <p>
     * This is typically by <i>Spring</i> via injection.
     * </p>
     */
    public final void setMaxUserRetention(final int maxUserRetention)
    {
        businessContext.setMaxUserRetention(maxUserRetention);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Maximum user retention set to %d days.",
                    maxUserRetention));
        }
    }

    /**
     * Sets the maximum number of all files allowed to be uploaded per quota group.
     * <p>
     * This is done by <i>Spring</i> via injection.
     * </p>
     */
    public void setMaxFileCountPerQuotaGroup(Integer maxFileCountPerQuotaGroup)
    {
        businessContext.setMaxFileCountPerQuotaGroup(maxFileCountPerQuotaGroup);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Quota for number of files set to %s.",
                    maxFileCountPerQuotaGroup == null ? "'no quota'" : maxFileCountPerQuotaGroup));
        }
    }

    /**
     * Sets the maximum sum of the size of all files allowed to be uploaded per quota group.
     * <p>
     * This is done by <i>Spring</i> via injection.
     * </p>
     */
    public void setMaxFileSizePerQuotaGroupInMB(Long maxFileSizePerQuotaGroupInMB)
    {
        businessContext.setMaxFileSizePerQuotaGroupInMB(maxFileSizePerQuotaGroupInMB);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Quota for sum of size of files set to %s MB.",
                    maxFileSizePerQuotaGroupInMB == null ? "'no quota'"
                            : maxFileSizePerQuotaGroupInMB));
        }
    }

    /**
     * Sets whether new externally authenticated users should start active or not.
     */
    public void setNewExternallyAuthenticatedUserStartActive(
            boolean newExternallyAuthenticatedUserStartActive)
    {
        businessContext
                .setNewExternallyAuthenticatedUserStartActive(newExternallyAuthenticatedUserStartActive);
    }

    /**
     * Sets the number of trigger permits for this server. This determines the number of
     * asynchronous triggers that can run in parallel. (More expensive triggers may use up more than
     * one permit.)
     */
    public void setTriggerPermits(int triggerPermits)
    {
        businessContext.setTriggerPermits(triggerPermits);
    }

    /**
     * Sets the allowed IPs for calling <code>setSessionUser()</code> as a comma-separated list.
     */
    public void setAllowedIPsForSetSessionUser(String allowedIPs)
    {
        businessContext.setAllowedIPsForSetSessionUser(allowedIPs);
    }

    private final <T> T createLoggingProxy(final T manager)
    {
        final Object proxy =
                processor.postProcessAfterInitialization(manager, "proxy of "
                        + manager.getClass().getName());
        final Class<? extends DomainModel> clazz = getClass();
        final InvocationHandler invocationHandler =
                new LogInvocationHandler(proxy, manager.getClass().getSimpleName(), Level.DEBUG,
                        clazz);
        final Class<?>[] interfaces = manager.getClass().getInterfaces();
        return cast(Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, invocationHandler));
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(final Object proxy)
    {
        return (T) proxy;
    }

    /**
     * Returns the one and only one instance of {@link IUserManager}.
     */
    @Override
    public final IUserManager getUserManager()
    {
        if (userManager == null)
        {
            userManager =
                    createLoggingProxy(new UserManager(daoFactory, boFactory, businessContext,
                            externalAuthenticationService));
        }
        return userManager;
    }

    /**
     * Returns the one and only one instance of {@link IFileManager}.
     */
    @Override
    public final IFileManager getFileManager()
    {
        if (fileManager == null)
        {
            fileManager =
                    createLoggingProxy(new FileManager(daoFactory, boFactory, getUserManager(),
                            businessContext, getTriggerManager()));
        }
        return fileManager;
    }

    /**
     * Returns the instance of the {@link ITriggerManager}.
     */
    public ITriggerManager getTriggerManager()
    {
        if (triggerManager == null)
        {
            triggerManager = createLoggingProxy(new TriggerManager(businessContext));
        }
        return triggerManager;
    }

    /**
     * Returns the one and only instance of {@link IMailClient}.
     */
    @Override
    public final IMailClient getMailClient()
    {
        return businessContext.getMailClient();
    }

    /**
     * Returns the business context (server configuration).
     */
    @Override
    public final IBusinessContext getBusinessContext()
    {
        return businessContext;
    }

    /**
     * Returns the {@link PasswordGenerator}.
     */
    @Override
    public final PasswordGenerator getPasswordGenerator()
    {
        return businessContext.getPasswordGenerator();
    }

}
