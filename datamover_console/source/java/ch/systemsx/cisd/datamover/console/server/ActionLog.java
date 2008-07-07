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

import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.common.servlet.AbstractActionLog;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.datamover.console.client.dto.User;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ActionLog extends AbstractActionLog implements IConsoleActionLog
{
    public ActionLog(IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
    }

    public void logShutdownDatamover(String datamover)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("Shutdown Datamover '%s'.", datamover));
        }
    }

    public void logStartDatamover(String datamover, String targetName)
    {
        if (trackingLog.isInfoEnabled())
        {
            trackingLog.info(getUserHostSessionDescription()
                    + String.format("Start Datamover '%s' with target '%s'.", datamover, targetName));
        }
    }

    @Override
    protected String getUserCode(HttpSession httpSession)
    {
        return ((User) httpSession.getAttribute(DatamoverConsoleService.SESSION_USER)).getUserCode();
    }

}
