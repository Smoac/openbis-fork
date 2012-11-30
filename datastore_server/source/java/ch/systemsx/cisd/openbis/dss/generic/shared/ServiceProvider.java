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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import org.apache.log4j.Logger;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;

/**
 * Provider of remote service onto openBIS.
 * 
 * @author Franz-Josef Elmer
 */
public class ServiceProvider
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ServiceProvider.class);

    // applicationContex it lazily initialized
    private static BeanFactory applicationContext = null;

    private static boolean buildingApplicationContext;

    /**
     * @deprecated This method should only be used from {@link ServiceProviderTestWrapper} to avoid
     *             leaving the application context uncleaned after test execution.
     */
    @Deprecated
    public static void setBeanFactory(BeanFactory applicationContext)
    {
        ServiceProvider.applicationContext = applicationContext;
    }

    /**
     * Return the application context
     * 
     * @param create <code>true</code> if the application context should be created when it does not
     *            exist.
     */
    public static BeanFactory tryGetApplicationContext(boolean create)
    {
        if (create && applicationContext == null)
        {
            synchronized (ServiceProvider.class)
            {
                if (applicationContext == null)
                {
                    if (buildingApplicationContext)
                    {
                        throw new IllegalStateException("Building application context. "
                                + "Application context hasn't been built completely. "
                                + "Beans should access other beans lazily.");
                    }
                    buildingApplicationContext = true;
                    applicationContext = new ClassPathXmlApplicationContext(new String[]
                        { "dssApplicationContext.xml" }, true);
                    buildingApplicationContext = false;
                }
            }
        }
        return applicationContext;
    }

    /**
     * Return the application context, optionally creating one if needed.
     */
    public static BeanFactory getApplicationContext()
    {
        return tryGetApplicationContext(true);
    }

    /**
     * Returns openBIS service singleton.
     */
    public static IEncapsulatedOpenBISService getOpenBISService()
    {
        return ((IEncapsulatedOpenBISService) getApplicationContext().getBean("openBIS-service"));
    }

    public static IGeneralInformationService getGeneralInformationService()
    {
        return ((IGeneralInformationService) getApplicationContext().getBean(
                "general-information-service"));
    }

    public static ISearchService getSearchService()
    {
        return ((ISearchService) getApplicationContext().getBean("search-service"));
    }

    public static IAuthorizationService getAuthorizationService()
    {
        return (IAuthorizationService) getApplicationContext().getBean("authorization-service");
    }

    public static IDataSetPathInfoProvider getDataSetPathInfoProvider()
    {
        return ((IDataSetPathInfoProvider) getApplicationContext().getBean(
                "data-set-path-infos-provider"));
    }

    public static HttpInvokerServiceExporter getServiceConversationClientManagerServer()
    {
        return (HttpInvokerServiceExporter) getApplicationContext().getBean(
                "data-store-service-conversation-client-manager-server");
    }

    public static HttpInvokerServiceExporter getServiceConversationServerManagerServer()
    {
        return (HttpInvokerServiceExporter) getApplicationContext().getBean(
                "data-store-service-conversation-server-manager-server");
    }

    public static IShareIdManager getShareIdManager()
    {
        return ((IShareIdManager) getApplicationContext().getBean("share-id-manager"));
    }

    public static IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        return ((IHierarchicalContentProvider) getApplicationContext().getBean(
                "hierarchical-content-provider"));
    }

    public static HttpInvokerServiceExporter getDataStoreServer()
    {
        return ((HttpInvokerServiceExporter) getApplicationContext().getBean("data-store-server"));
    }

    public static HttpInvokerServiceExporter getRpcNameServiceExporter()
    {
        return ((HttpInvokerServiceExporter) getApplicationContext().getBean(
                "data-store-rpc-name-server"));
    }

    public static StreamSupportingHttpInvokerServiceExporter getDssServiceRpcGeneric()
    {
        return ((StreamSupportingHttpInvokerServiceExporter) getApplicationContext().getBean(
                "data-store-rpc-service-generic"));
    }

    public static IDataSourceProvider getDataSourceProvider()
    {
        return ((IDataSourceProvider) getApplicationContext().getBean("data-source-provider"));
    }

    public static IConfigProvider getConfigProvider()
    {
        return ((IConfigProvider) getApplicationContext().getBean("config-provider"));
    }

    public static OpenBISSessionHolder getSessionHolder()
    {
        return (OpenBISSessionHolder) getApplicationContext().getBean("sessionHolder");
    }

    public static IDataStoreServiceInternal getDataStoreService()
    {
        Object bean = getApplicationContext().getBean("data-store-service");
        IDataStoreServiceInternal result = null;
        if (bean instanceof Advised)
        {
            Advised advised = (Advised) getApplicationContext().getBean("data-store-service");
            try
            {
                result = (IDataStoreServiceInternal) advised.getTargetSource().getTarget();
            } catch (Exception ex)
            {
                operationLog.error("Cannot get IDataSetDeleter instance :" + ex.getMessage(), ex);
            }
        } else
        {
            result = (IDataStoreServiceInternal) bean;
        }
        return result;
    }

    private ServiceProvider()
    {
    }
}
