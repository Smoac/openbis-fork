/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;

/**
 * Post registration task which write a message as a file. A message template is used to create the
 * message. Template place holders have the form <code>${<i>&lt;place holder name&gt;</i>}</code>.
 * The place holder name for the data set code is <code>data-set-code</code>. To refer a data set
 * property the place holder name has the form
 * <code>property:<i>&lt;property type code (ignoring case)&gt;></code>.
 * 
 * @author Franz-Josef Elmer
 */
public class NotifyingTask extends AbstractPostRegistrationTask
{
    private static final String MESSAGE_TEMPLATE_KEY = "message-template";
    private static final String DESTINATION_FILE_KEY = "destination-file";
    private static final String PROPERTY_PREFIX = "property:";
    private static final String DATA_SET_CODE_PLACE_HOLDER = "data-set-code";
    
    private final Template template;
    private final String destination;

    public NotifyingTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(properties, service);
        template = new Template(PropertyUtils.getMandatoryProperty(properties, MESSAGE_TEMPLATE_KEY));
        destination = PropertyUtils.getMandatoryProperty(properties, DESTINATION_FILE_KEY);
    }

    public boolean requiresDataStoreLock()
    {
        return false;
    }

    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
    {
        ExternalData dataSet = service.tryGetDataSet(dataSetCode);
        if (dataSet == null)
        {
            throw new IllegalArgumentException("Unknown data set " + dataSetCode);
        }
        return new Executor(dataSet, template.createFreshCopy(), destination);
    }
    
    private static final class Executor implements IPostRegistrationTaskExecutor
    {
        private final ExternalData dataSet;
        private final Template template;
        private final String destination;

        public Executor(ExternalData dataSet, Template template, String destination)
        {
            this.dataSet = dataSet;
            this.template = template;
            this.destination = destination;
        }

        public ICleanupTask createCleanupTask()
        {
            return new NoCleanupTask();
        }

        public void execute()
        {
            Set<String> placeholderNames = template.getPlaceholderNames();
            for (String placeholderName : placeholderNames)
            {
                if (placeholderName.equals(DATA_SET_CODE_PLACE_HOLDER))
                {
                    template.bind(DATA_SET_CODE_PLACE_HOLDER, dataSet.getCode());
                } else if (placeholderName.startsWith(PROPERTY_PREFIX))
                {
                    String propertyName = placeholderName.substring(PROPERTY_PREFIX.length());
                    template.bind(placeholderName, getProperty(propertyName));
                } else
                {
                    throw new IllegalArgumentException("No binding found for place holder '"
                            + placeholderName + "'.");
                }
            }
            String messageText = template.createText();
            FileUtilities.writeToFile(new File(destination), messageText);
        }
        
        private String getProperty(String propertyName)
        {
            for (IEntityProperty property : dataSet.getProperties())
            {
                if (property.getPropertyType().getCode().equalsIgnoreCase(propertyName))
                {
                    return property.tryGetAsString();
                }
            }
            throw new IllegalArgumentException("Unknown property: " + propertyName);
        }
        
    }

}
