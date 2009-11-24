/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.dsu.tracking.main;

import static ch.systemsx.cisd.common.utilities.PropertyUtils.getMandatoryProperty;

import java.util.Properties;

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;

/**
 * @author Tomasz Pylak
 */
public class Parameters
{
    private static final String OPENBIS_USER = "openbis-user";

    private static final String OPENBIS_PASSWORD = "openbis-password";

    private static final String OPENBIS_SERVER_URL = "openbis-server-url";

    private final String openbisUser;

    private final String openbisPassword;

    private final String openbisServerURL;

    private final IMailClient mailClient;

    public Parameters(Properties props)
    {
        this.openbisUser = getMandatoryProperty(props, OPENBIS_USER);
        this.openbisPassword = getMandatoryProperty(props, OPENBIS_PASSWORD);
        this.openbisServerURL = getMandatoryProperty(props, OPENBIS_SERVER_URL);
        this.mailClient = new MailClient(props);
    }

    public String getOpenbisUser()
    {
        return openbisUser;
    }

    public String getOpenbisPassword()
    {
        return openbisPassword;
    }

    public String getOpenbisServerURL()
    {
        return openbisServerURL;
    }

    public IMailClient getMailClient()
    {
        return mailClient;
    }
}
