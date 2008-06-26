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

package ch.systemsx.cisd.datamover.console.server;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.GWTSpringController;
import ch.systemsx.cisd.datamover.console.client.EnvironmentFailureException;
import ch.systemsx.cisd.datamover.console.client.IDatamoverConsoleService;
import ch.systemsx.cisd.datamover.console.client.UserFailureException;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DatamoverConsoleServlet extends GWTSpringController implements IDatamoverConsoleService
{
    private static final long serialVersionUID = 1L;

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DatamoverConsoleServlet.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatamoverConsoleServlet.class);

    public void logout()
    {
        // TODO Auto-generated method stub
        
    }

    public User tryLogin(String user, String password) throws UserFailureException,
            EnvironmentFailureException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
