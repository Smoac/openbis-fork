/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.plugin.query.client.web.server;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ch.systemsx.cisd.common.servlet.GWTRPCServiceExporter;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientService;
import ch.systemsx.cisd.openbis.plugin.query.shared.ResourceNames;

/**
 * The {@link GWTRPCServiceExporter} for the <i>query</i> service.
 * <p>
 * <i>URL</i> mappings are: <code>/query</code> and <code>/openbis/query</code>. The encapsulated {@link ICommonClientService} service implementation
 * is expected to be defined as bean with name <code>query-plugin-service</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
@Controller
public final class QueryClientServiceServlet extends GWTRPCServiceExporter
{
    private static final long serialVersionUID = 1L;

    @Resource(name = ResourceNames.QUERY_PLUGIN_SERVICE)
    private IQueryClientService service;

    @RequestMapping({ "/query", "/openbis/query" })
    public final ModelAndView handleRequestExposed(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
    {
        return super.handleRequest(request, response);
    }

    @Override
    protected final Object getService()
    {
        return service;
    }

}
