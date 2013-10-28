/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.etlserver.proteomics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class Util
{
    private Util()
    {
    }
    
    /**
     * Tries to return the prot.xml file in the specified data set.
     * 
     * @return <code>null</code> if non found.
     */
    static File tryGetProtXMLFile(File dataSet)
    {
        if (dataSet.isDirectory() == false)
        {
            return dataSet;
        }
        File[] files = dataSet.listFiles();
        for (File file : files)
        {
            if (file.getName().endsWith("prot.xml"))
            {
                return file;
            }
        }
        return null;
    }

    /**
     * Returns an array of all entity properties defined by the specified entity type for which
     * values are found in the specified {@link Properties} object.
     * 
     * @throws UserFailureException if at least one mandatory property were missed.
     */
    static IEntityProperty[] getAndCheckProperties(Properties properties, EntityType entityType)
    {
        List<String> missingMandatoryProperties = new ArrayList<String>();
        List<IEntityProperty> sampleProperties =
            getProperties(properties, entityType, missingMandatoryProperties);
        if (missingMandatoryProperties.isEmpty() == false)
        {
            throw new UserFailureException("The following mandatory properties are missed: "
                    + missingMandatoryProperties);
        }
        return sampleProperties.toArray(new IEntityProperty[sampleProperties.size()]);
    }
    
    /**
     * Turns specified properties into a list of {@link IEntityProperty} instances in accordance to
     * the property types assigned to the specified entity type. Keys of missing mandatory
     * properties are added to the specified list.
     */
    static List<IEntityProperty> getProperties(Properties properties,
            EntityType entityType, List<String> missingMandatoryProperties)
    {
        List<IEntityProperty> sampleProperties = new ArrayList<IEntityProperty>();
        List<? extends EntityTypePropertyType<?>> sampleTypePropertyTypes = entityType.getAssignedPropertyTypes();
        for (EntityTypePropertyType<?> entityTypePropertyType : sampleTypePropertyTypes)
        {
            boolean mandatory = entityTypePropertyType.isMandatory();
            PropertyType propertyType = entityTypePropertyType.getPropertyType();
            String key = propertyType.getCode();
            String value = properties.getProperty(key);
            if (value == null)
            {
                if (mandatory)
                {
                    missingMandatoryProperties.add(key);
                }
            } else
            {
                EntityProperty property = new EntityProperty();
                property.setPropertyType(propertyType);
                property.setValue(value);
                sampleProperties.add(property);
            }
        }
        return sampleProperties;
    }

    static Properties loadPropertiesFile(File incomingDataSetDirectory, String propertiesFileName)
    {
        File propertiesFile = new File(incomingDataSetDirectory, propertiesFileName);
        if (propertiesFile.exists() == false)
        {
            throw new UserFailureException("Missing properties file '" + propertiesFileName + "'.");
        }
        if (propertiesFile.isFile() == false)
        {
            throw new UserFailureException("Properties file '" + propertiesFileName
                    + "' is a folder.");
        }
        return PropertyIOUtils.loadProperties(propertiesFile);
    }
}
