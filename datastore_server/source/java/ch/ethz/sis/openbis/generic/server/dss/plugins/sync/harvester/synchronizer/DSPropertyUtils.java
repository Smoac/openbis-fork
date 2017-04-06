/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class DSPropertyUtils
{
    static Set<String> extractPropertyNames(List<NewProperty> dataSetProperties)
    {
        Set<String> existingPropertyNames = new HashSet<String>();
        for (NewProperty prop : dataSetProperties)
        {
            existingPropertyNames.add(prop.getPropertyCode());
        }
        return existingPropertyNames;
    }

    static IEntityProperty[] convertToEntityProperty(List<NewProperty> dataSetProperties)
    {
        ArrayList<IEntityProperty> list = new ArrayList<IEntityProperty>();
        for (NewProperty prop : dataSetProperties)
        { // if value is a material it will have been properly parsed in the ResourceListParser so we do not need to
          // do anything special about it here.
            String propertyCode = prop.getPropertyCode();
            String value = prop.getValue();
            list.add(new PropertyBuilder(propertyCode).value(value).getProperty());
        }
        return list.toArray(new IEntityProperty[list.size()]);
    }
}
