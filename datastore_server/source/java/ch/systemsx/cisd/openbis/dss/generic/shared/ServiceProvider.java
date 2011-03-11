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

    public static void setBeanFactory(BeanFactory applicationContext)
    {
        ServiceProvider.applicationContext = applicationContext;
    }

    public static BeanFactory getApplicationContext()
    {
        if (applicationContext == null)
        {
            applicationContext = new ClassPathXmlApplicationContext(new String[]
                { "dssApplicationContext.xml" }, true);
        }
        return applicationContext;
    }

    /**
     * Returns openBIS service singleton.
     */
    public static IEncapsulatedOpenBISService getOpenBISService()
    {
        return ((IEncapsulatedOpenBISService) getApplicationContext().getBean("openBIS-service"));
    }

    public static IShareIdManager getShareIdManager()
    {
        return ((IShareIdManager) getApplicationContext().getBean("share-id-manager"));
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

    public static DataSourceProvider getDataSourceProvider()
    {
        return ((DataSourceProvider) getApplicationContext().getBean("data-source-provider"));
    }

    public static IDataSetDeleter getDataSetDeleter()
    {
        Advised advised = (Advised) getApplicationContext().getBean("data-store-service");
        try
        {
            IDataSetDeleterProvider dssService =
                    (IDataSetDeleterProvider) advised.getTargetSource().getTarget();
            return dssService.getDataSetDeleter();
        } catch (Exception ex)
        {
            operationLog.error("Cannot get IDataSetDeleter instance :" + ex.getMessage(), ex);
            return null;
        }
    }

    private ServiceProvider()
    {
    }
}
