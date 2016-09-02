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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import ch.systemsx.cisd.common.collection.MapBuilder;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;

/**
 * Configuration helper class for {@link CifsServer}
 *
 * @author Franz-Josef Elmer
 */
class CifsServerConfig
{
    private static final String SECTION_NAME = "cifs.server";

    private static final Template CONFIG_TEMPLATE_XML = new Template("<jlanserver>\n"
            + "<servers><SMB/></servers>\n"
            + "<SMB>\n"
            + "  <host name='${host.name}' domain='${host.domain}'>\n"
            + "    <broadcast>${broadcast-mask}</broadcast>\n"
            + "    <smbdialects>${smb-dialects}</smbdialects>\n"
            + "    <tcpipSMB port='${smb-port}' platforms='linux,macosx,solaris'/>\n"
            + "  </host>\n"
            + "  <authenticator>\n"
            + "    <class>ch.systemsx.cisd.openbis.dss.generic.server.cifs.OpenBisAuthenticator</class>\n"
            + "    <mode>USER</mode>\n"
            + "  </authenticator>\n"
            + "  <sessionDebug flags='${session-log-flags}'/>"
            + "</SMB>\n"
            + "<shares>\n"
            + "  <diskshare name='${share-name}'>\n"
            + "    <driver>\n"
            + "      <class>ch.systemsx.cisd.openbis.dss.generic.server.cifs.DataSetCifsView</class>\n"
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
            + "  <JCEProvider>org.bouncycastle.jce.provider.BouncyCastleProvider</JCEProvider>\n"
            + "  <usersInterface>\n"
            + "    <class>ch.systemsx.cisd.openbis.dss.generic.server.cifs.DummyUsersInterface</class>\n"
            + "  </usersInterface>\n"
            + "</security>\n"
            + "</jlanserver>\n");

    private static final String SMB_PORT_KEY = "smb-port";

    private static Map<String, String> CONFIG_PARAMS = new MapBuilder<String, String>()
            .entry("host.name", "UNKNOWN")
            .entry("host.domain", "OPENBIS")
            .entry("broadcast-mask", "255.255.255.255")
            .entry("smb-dialects", "LanMan,NT")
            .entry(SMB_PORT_KEY, null)
            .entry("session-log-flags", "Negotiate,Socket,Tree")
            .entry("log-level", "INFO")
            .entry("share-name", "STORE")
            .getMap();

    static Properties getServerProperties()
    {
        return PropertyParametersUtil.extractSingleSectionProperties(
                DssPropertyParametersUtil.loadServiceProperties(), SECTION_NAME, true).getProperties();
    }

    private final boolean enabled;

    private final Properties serverProperties;

    public CifsServerConfig(Properties props)
    {
        serverProperties = PropertyParametersUtil.extractSingleSectionProperties(props, SECTION_NAME, false).getProperties();
        enabled = PropertyUtils.getBoolean(serverProperties, "enable", false);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public String getPort()
    {
        return getProperty(SMB_PORT_KEY);
    }

    private String getProperty(String key)
    {
        return serverProperties.getProperty(key, CONFIG_PARAMS.get(key));
    }

    public String getServerXmlConfig()
    {
        Template template = CONFIG_TEMPLATE_XML.createFreshCopy();
        for (Entry<String, String> entry : CONFIG_PARAMS.entrySet())
        {
            String key = entry.getKey();
            template.bind(key, serverProperties.getProperty(key, entry.getValue()));
        }
        return template.createText();
    }

}
