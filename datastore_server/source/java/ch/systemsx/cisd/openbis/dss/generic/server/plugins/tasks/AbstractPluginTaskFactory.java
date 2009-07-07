/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginTaskDescription;

/**
 * Abstract class for a factory of plugin tasks.
 * 
 * @author Tomasz Pylak
 */
public abstract class AbstractPluginTaskFactory<T>
{
    /** Creates a new instance of a plugin task */
    abstract public T createPluginInstance();

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    abstract public void logConfiguration();

    /** Property name which stores a plugin label. */
    @Private
    public final static String LABEL_PROPERTY_NAME = "label";

    /** Property name which stores a list of dataset type codes. */
    @Private
    public final static String DATASET_CODES_PROPERTY_NAME = "dataset-types";

    /** Property name which stores a plugin class name. */
    @Private
    public final static String CLASS_PROPERTY_NAME = "class";

    /**
     * Property name which stores a file path. The file should contain properties which are plugin
     * parameters.
     */
    @Private
    public final static String PARAMS_FILE_PATH_PROPERTY_NAME = "properties-file";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractPluginTaskFactory.class);

    private final PluginTaskDescription description;

    private final String className;

    private final Properties instanceParameters;

    protected AbstractPluginTaskFactory(SectionProperties sectionProperties)
    {
        Properties pluginProperties = sectionProperties.getProperties();
        String pluginKey = sectionProperties.getKey();
        String label = PropertyUtils.getMandatoryProperty(pluginProperties, LABEL_PROPERTY_NAME);
        String[] datasetCodes = extractDatasetCodes(pluginProperties);
        this.description = new PluginTaskDescription(pluginKey, label, datasetCodes);
        this.className = PropertyUtils.getMandatoryProperty(pluginProperties, CLASS_PROPERTY_NAME);
        this.instanceParameters = extractInstanceParameters(pluginProperties);
    }

    protected T createPluginInstance(Class<T> clazz)
    {
        try
        {
            return ClassUtils.create(clazz, className, instanceParameters);
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find the plugin class '" + className
                    + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

    private static Properties extractInstanceParameters(final Properties pluginProperties)
    {
        String parametersFilePath = pluginProperties.getProperty(PARAMS_FILE_PATH_PROPERTY_NAME);
        if (parametersFilePath == null)
        {
            return PropertyUtils.loadProperties(parametersFilePath);
        } else
        {
            return new Properties();
        }
    }

    private static String[] extractDatasetCodes(final Properties pluginProperties)
    {
        String datasetCodesValues =
                PropertyUtils.getMandatoryProperty(pluginProperties, DATASET_CODES_PROPERTY_NAME);
        return PropertyParametersUtil.parseItemisedProperty(datasetCodesValues,
                DATASET_CODES_PROPERTY_NAME);
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    protected final void logPropertiesConfiguration()
    {
        if (operationLog.isInfoEnabled())
        {
            logLine(LABEL_PROPERTY_NAME, description.getLabel());
            logLine(DATASET_CODES_PROPERTY_NAME, CollectionUtils.abbreviate(description
                    .getDatasetTypeCodes(), -1));
            logLine(CLASS_PROPERTY_NAME, className);
            logLine(PARAMS_FILE_PATH_PROPERTY_NAME, instanceParameters.toString());
        }
    }

    private void logLine(String propertyName, String value)
    {
        operationLog.info(String.format("%s.%s = %s", description.getKey(), propertyName, value));
    }

    /** Ensures that the factory configuration is correct and it is able to create plugins instances */
    public void check()
    {
        createPluginInstance(); // just to see if it is possible
    }

    /** @return description of a plugin task. It is the same for all plugin instances. */
    public PluginTaskDescription getPluginDescription()
    {
        return description;
    }
}
