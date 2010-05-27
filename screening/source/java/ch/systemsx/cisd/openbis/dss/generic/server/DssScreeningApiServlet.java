/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter;

/**
 * A servlet which exposes the spring bean with the HTTP Invoker
 * 
 * @author Tomasz Pylak
 */
public class DssScreeningApiServlet extends HttpInvokerServlet
{
    private static final long serialVersionUID = 1L;

    private static final BeanFactory APPLICATION_CONTEXT =
            new ClassPathXmlApplicationContext(new String[]
                { "screening-dssApplicationContext.xml" }, true);

    private static StreamSupportingHttpInvokerServiceExporter getDssScreeningService()
    {
        return ((StreamSupportingHttpInvokerServiceExporter) APPLICATION_CONTEXT
                .getBean("data-store-rpc-service-screening"));
    }

    public DssScreeningApiServlet()
    {
        super(getDssScreeningService(), "/rmi-datastore-server-screening-api-v1");
    }
}
