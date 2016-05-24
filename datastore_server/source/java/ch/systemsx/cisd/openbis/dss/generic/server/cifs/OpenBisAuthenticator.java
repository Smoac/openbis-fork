/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.cifs;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.AuthContext;
import org.alfresco.jlan.server.auth.CifsAuthenticator;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.NTLanManAuthContext;
import org.alfresco.jlan.server.core.SharedDevice;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.CifsAuthenticationUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * Authenticator using openBIS. 
 * 
 * @author Franz-Josef Elmer
 */
public class OpenBisAuthenticator extends CifsAuthenticator
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, CifsServer.class);
    
    static
    {
        ClientInfo.setFactory(new OpenBisClientInfoFactory());
    }

    private IGeneralInformationService generalInfoService;

    public OpenBisAuthenticator()
    {
        setAccessMode(USER_MODE);
    }

    @Override
    public int authenticateShareConnect(ClientInfo client, SharedDevice share, String pwd, SrvSession sess)
    {
        return ReadOnly;
    }

    @Override
    public int authenticateUser(ClientInfo client, SrvSession sess, int alg)
    {
        String userName = client.getUserName();
        byte[] challenge = new byte[0];
        AuthContext authenticationContext = sess.getAuthenticationContext();
        if (authenticationContext instanceof NTLanManAuthContext)
        {
            NTLanManAuthContext new_name = (NTLanManAuthContext) authenticationContext;
            challenge = new_name.getChallenge();
        }
        String passwordInfo = CifsAuthenticationUtils.createPasswordInfo(client.getPassword(), alg, challenge);
        String sessionToken = getGeneralInfoService().tryToAuthenticateForAllServices(userName, passwordInfo);
        if (sessionToken == null)
        {
            operationLog.info("CIFS authentication for user '" + userName + "' failed.");
            return AUTH_DISALLOW;
        }
        if (client instanceof OpenBisClientInfo)
        {
            ((OpenBisClientInfo) client).setSessionToken(sessionToken);
        }
        operationLog.info("CIFS authentication for user '" + userName + "' was successful. Session token: " + sessionToken);
        return AUTH_ALLOW;
    }

    private IGeneralInformationService getGeneralInfoService()
    {
        if (generalInfoService == null)
        {
            generalInfoService = ServiceProvider.getGeneralInformationService();
        }
        return generalInfoService;
    }
}
