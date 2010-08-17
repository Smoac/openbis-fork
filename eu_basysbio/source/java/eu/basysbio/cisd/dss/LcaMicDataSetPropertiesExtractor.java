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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class LcaMicDataSetPropertiesExtractor extends DataSetPropertiesExtractor
{

    static final String GROWTH_RATE = "GROWTH_RATE";

    LcaMicDataSetPropertiesExtractor(Properties properties)
    {
        super(properties, true);
    }

    @Override
    public List<NewProperty> extractDataSetProperties(File incomingDataSetPath)
    {
        File file = HeaderUtils.getTabSeparatedValueFile(incomingDataSetPath);
        List<String> lines = FileUtilities.loadToStringList(file);
        if (lines.isEmpty())
        {
            throw new UserFailureException("Empty file: " + file);
        }
        String[] items = StringUtils.split(lines.get(0), " \t");
        NewProperty growthRate = new NewProperty(GROWTH_RATE, items[items.length - 1]);
        List<NewProperty> properties =
                HeaderUtils.extractHeaderPropertiesIgnoringTimeSeriesDataSetType(
                        incomingDataSetPath, ignoreEmptyLines, true, true);
        properties.add(growthRate);
        return properties;
    }

}
