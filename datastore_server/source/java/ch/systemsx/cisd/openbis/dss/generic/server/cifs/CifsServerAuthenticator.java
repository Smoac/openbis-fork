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
import org.alfresco.jlan.server.auth.CifsAuthenticator;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.UserAccount;
import org.alfresco.jlan.server.core.SharedDevice;

/**
 * @author Franz-Josef Elmer
 */
public class CifsServerAuthenticator extends CifsAuthenticator
{
    public CifsServerAuthenticator()
    {
        setAccessMode(USER_MODE);
    }

    @Override
    public int authenticateShareConnect(ClientInfo client, SharedDevice share, String pwd, SrvSession sess)
    {
        System.out.println("CifsServerAuthenticator.authenticateShareConnect() >"+client+ "< >" + pwd + "< >"+sess+"<");
        System.out.println(sess.getClientInformation());
        System.out.println(sess.getAuthenticationContext());
        System.out.println(client.getANSIPasswordAsString());
        System.out.println(client.getPasswordAsString());
        System.out.println(client.getUserName());
        return Writeable;
    }

    @Override
    public int authenticateUser(ClientInfo client, SrvSession sess, int alg)
    {
        System.out.println("CifsServerAuthenticator.authenticateUser() "+client+" "+sess+" "+alg);
        System.out.println(sess.getClientInformation());
        System.out.println(sess.getAuthenticationContext());
        UserAccount userAcc = getUserDetails(client.getUserName());
        System.out.println("user account:"+userAcc);
        return AUTH_ALLOW;
    }

}
