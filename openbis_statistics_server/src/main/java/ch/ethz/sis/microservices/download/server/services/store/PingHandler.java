/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.microservices.download.server.services.store;

import ch.ethz.sis.microservices.download.server.services.Service;
import ch.ethz.sis.microservices.download.server.startup.StatisticsMain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PingHandler extends Service
{

    private static final long serialVersionUID = 2L;

    private static final Logger LOGGER = LogManager.getLogger(StatisticsMain.class);

    protected void doAction(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        LOGGER.trace("Ping called.");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
            IOException
    {
        doAction(request, response);
    }

}
