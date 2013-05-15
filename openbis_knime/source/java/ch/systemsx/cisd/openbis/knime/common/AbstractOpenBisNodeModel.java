/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.ICredentials;

/**
 * Abstract super class of start nodes getting some data from openBIS.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractOpenBisNodeModel extends NodeModel
{
    public static final String URL_KEY = "url";
    public static final String CREDENTIALS_KEY = "credentials";
    public static final String USER_KEY = "user";
    public static final String PASSWORD_KEY = "password";
    
    protected NodeLogger logger;
    protected String url;
    protected String userID;
    protected String password;
    private String credentialsName;

    protected AbstractOpenBisNodeModel()
    {
        super(0, 1);
        logger = NodeLogger.getLogger(getClass());
    }
    
    protected AbstractOpenBisNodeModel(PortType[] inPortTypes, PortType[] outPortTypes)
    {
        super(inPortTypes, outPortTypes);
        logger = NodeLogger.getLogger(getClass());
    }

    protected abstract void loadAdditionalValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException;

    protected abstract void saveAdditionalSettingsTo(NodeSettingsWO settings);

    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException
    {
        url = settings.getString(URL_KEY);
        credentialsName = settings.getString(CREDENTIALS_KEY, "");
        if (StringUtils.isNotBlank(credentialsName))
        {
            ICredentials credentials = getCredentials(credentialsName);
            if (credentials == null)
            {
                throw new InvalidSettingsException("Unknown credentials '" + credentialsName + "'.");
            }
            userID = credentials.getLogin();
            password = credentials.getPassword();
        } else
        {
            userID = settings.getString(USER_KEY);
            password = Util.getDecryptedPassword(settings);
        }
        loadAdditionalValidatedSettingsFrom(settings);
    }

    protected ICredentials getCredentials(String name)
    {
        return getCredentialsProvider().get(name);
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings)
    {
        settings.addString(URL_KEY, url);
        settings.addString(CREDENTIALS_KEY, credentialsName);
        if (StringUtils.isBlank(credentialsName))
        {
            settings.addString(USER_KEY, userID);
            settings.addString(PASSWORD_KEY,
                    password == null ? null : Util.getEncryptedPassword(password.toCharArray()));
        }
        saveAdditionalSettingsTo(settings);
    }

    @Override
    protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException
    {
    }
    
    @Override
    protected void reset()
    {
    }

    @Override
    protected void saveInternals(File arg0, ExecutionMonitor arg1) throws IOException,
            CanceledExecutionException
    {
    }

    @Override
    protected void loadInternals(File arg0, ExecutionMonitor arg1) throws IOException,
    CanceledExecutionException
    {
    }
    
}