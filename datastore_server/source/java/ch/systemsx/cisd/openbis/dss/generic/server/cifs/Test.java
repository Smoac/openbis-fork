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

import java.io.StringReader;
import java.util.StringTokenizer;

import javax.xml.bind.DatatypeConverter;

import org.alfresco.jlan.app.XMLServerConfiguration;
import org.alfresco.jlan.server.auth.PasswordEncryptor;
import org.alfresco.jlan.server.auth.UserAccount;
import org.alfresco.jlan.server.config.ConfigurationListener;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.smb.Dialect;
import org.alfresco.jlan.smb.DialectSelector;
import org.alfresco.jlan.smb.server.SMBServer;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.string.Template;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Test
{
    private static final Template CONFIG_TEMPLATE_XML = new Template("<jlanserver>\n"
            + "<servers><SMB/></servers>\n"
            + "<SMB>\n"
            + "  <host name='${host.name}' domain='${host.domain}'>\n"
            + "    <broadcast>${broadcast-mask}</broadcast>\n"
            + "    <smbdialects>${smb-dialects}</smbdialects>\n"
            + "    <tcpipSMB port='${smb-port}'/>\n"
            + "  </host>\n"
            + "  <authenticator type='enterprise'>\n"
            + "    <mode>USER</mode>\n"
            + "  </authenticator>\n"
            + "  <sessionDebug flags='${session-log-flags}'/>"
            + "</SMB>\n"
            + "<shares>\n"
            + "  <diskshare name='${share-name}'>\n"
            + "    <driver>\n"
            + "      <!--class>ch.systemsx.cisd.openbis.dss.generic.server.cifs.DataSetCifsView</class-->\n"
            + "      <class>org.alfresco.jlan.smb.server.disk.JavaFileDiskDriver</class>\n"
            + "      <LocalPath>/Users/felmer/tmp/playgrounds/unix</LocalPath>\n"
            + "    </driver>\n"
            + "  </diskshare>\n"
            + "</shares>\n"
            + "<debug>\n"
            + "  <output>\n"
            + "    <class>ch.systemsx.cisd.openbis.dss.generic.server.cifs.CifsServerLogger</class>\n"
            + "    <log-level>${log-level}</log-level>\n"
            + "  </output>\n"
            + "</debug>\n"
            + "<security>\n"
            + "    <JCEProvider>org.bouncycastle.jce.provider.BouncyCastleProvider</JCEProvider>\n"
            + "    <authenticator>\n"
            + "      <class>org.alfresco.jlan.server.auth.LocalAuthenticator</class>\n"
            + "      <mode>USER</mode>\n"
            + "    </authenticator>\n" +
            "    <users>\n" + 
            "      <user name=\"jlansrv\">\n" + 
            "        <password>jlan</password>\n" + 
            "        <comment>System administrator</comment>\n" + 
            "        <administrator/>\n" + 
            "      </user>\n" + 
            "      <user name=\"felmer\">\n" + 
            "        <md4>e0fba38268d0ec66ef1cb452d5885e53</md4>\n" + 
            "      </user>\n" + 
            "      <user name=\"normal\">\n" + 
            "        <password>normal</password>\n" + 
            "      </user>\n" + 
            "    </users>\n" + 
            "  </security>\n" + 
            "</jlanserver>\n" + 
            "");


    public static void main(String[] args) throws Exception
    {
        LogInitializer.init();
        Template template = CONFIG_TEMPLATE_XML.createFreshCopy();
        template.bind("host.name", "ETHZ");
        template.bind("host.domain", "OPENBIS");
        template.bind("broadcast-mask", "255.255.255.255");
        template.bind("smb-dialects", "LanMan,NT");
        template.bind("smb-port", "1445");
        template.bind("session-log-flags", "Negotiate,Socket,Tree");
        template.bind("log-level", "INFO");
        template.bind("share-name", "STORE");
        XMLServerConfiguration configuration = new XMLServerConfiguration();
        configuration.loadConfiguration(new StringReader(template.createText()));
        SMBServer server = new SMBServer(configuration);
//        server.startServer();
        byte[] encryptedPassword = new PasswordEncryptor().generateEncryptedPassword("abc", null, PasswordEncryptor.MD4, "felmer", null);
        UserAccount userAccount = new UserAccount();
        userAccount.setMD4Password(encryptedPassword);
        System.out.println(userAccount);
        byte[] bytes = DatatypeConverter.parseHexBinary("0f6f34abcd");
        System.out.println(DatatypeConverter.printHexBinary(bytes));
        System.out.println(">"+DatatypeConverter.printHexBinary(new byte[0])+"<");
    }
    
}
