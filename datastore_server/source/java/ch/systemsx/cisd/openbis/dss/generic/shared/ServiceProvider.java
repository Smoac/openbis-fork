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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter;

/**
 * Provider of remote service onto openBIS.
 * 
 * @author Franz-Josef Elmer
 */
public class ServiceProvider
{
    public static final BeanFactory APPLICATION_CONTEXT =
            new ClassPathXmlApplicationContext(new String[]
                { "dssApplicationContext.xml" }, true);

    /**
     * Returns openBIS service singleton.
     */
    public static IEncapsulatedOpenBISService getOpenBISService()
    {
        return ((IEncapsulatedOpenBISService) APPLICATION_CONTEXT.getBean("openBIS-service"));
    }

    public static HttpInvokerServiceExporter getDataStoreServer()
    {
        return ((HttpInvokerServiceExporter) APPLICATION_CONTEXT.getBean("data-store-server"));
    }

    public static StreamSupportingHttpInvokerServiceExporter getDssServiceRpcNameServer()
    {
        return ((StreamSupportingHttpInvokerServiceExporter) APPLICATION_CONTEXT
                .getBean("data-store-rpc-name-server"));
    }

    public static StreamSupportingHttpInvokerServiceExporter getDssServiceRpcGeneric()
    {
        return ((StreamSupportingHttpInvokerServiceExporter) APPLICATION_CONTEXT
                .getBean("data-store-rpc-service-generic"));
    }

    private ServiceProvider()
    {
    }
}
