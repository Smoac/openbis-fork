/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.dssapi.v3;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.openbis.common.api.server.AbstractStreamSupportingApiServiceExporter;

/**
 * @author Jakub Straszewski
 */
@Controller(value = DataStoreServerApiServer.INTERNAL_BEAN_NAME)
public class DataStoreServerApiServer extends AbstractStreamSupportingApiServiceExporter
{

    public static final String INTERNAL_BEAN_NAME = "v3-exporter";

    /**
     *
     *
     */
    public DataStoreServerApiServer()
    {
        System.out.println();
    }

    @Resource(name = DataStoreServerApi.INTERNAL_SERVICE_NAME)
    private IDataStoreServerApi service;

    @Override
    public void afterPropertiesSet()
    {
        establishService(IDataStoreServerApi.class, service, IDataStoreServerApi.SERVICE_NAME,
                IDataStoreServerApi.SERVICE_URL);
        super.afterPropertiesSet();
    }

    @RequestMapping(
    { IDataStoreServerApi.SERVICE_URL, "/datastore_server" + IDataStoreServerApi.SERVICE_URL })
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        super.handleRequest(request, response);
    }
}
