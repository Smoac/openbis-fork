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

package ch.systemsx.cisd.openbis.generic.server;

import ch.systemsx.cisd.authentication.ILogMessagePrefixGenerator;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Log message prefix generator based on user information found in a {@link Session} object.
 *
 * @author Franz-Josef Elmer
 */
class LogMessagePrefixGenerator implements ILogMessagePrefixGenerator<Session>
{
    static final String UNDEFINED = "<UNDEFINED>";

    public String createPrefix(Session session)
    {
        String userName = session.getUserName();
        String groupCode = null;
        PersonPE person = session.tryGetPerson();
        if (person != null)
        {
            GroupPE homeGroup = person.getHomeGroup();
            if (homeGroup != null)
            {
                groupCode = homeGroup.getCode();
            }
        }
        String remoteHost = session.getRemoteHost();
        return createPrefix(userName, groupCode, remoteHost);
    }

    public String createPrefix(String user, String remoteHost)
    {
        return createPrefix(user, null, remoteHost);
    }
    
    private String createPrefix(String user, String groupCodeOrNull, String remoteHost)
    {
        return String.format("[USER:%s GROUP:%s HOST:%s]", cite(user),
        cite(groupCodeOrNull), cite(remoteHost));
    }
    
    private String cite(final String text)
    {
        return text == null ? UNDEFINED : "'" + text + "'";
    }
}
