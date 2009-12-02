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

package ch.systemsx.cisd.cifex.client.application.grid;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * A field to filter grid rows by the value of one specified column.
 * 
 * @author Tomasz Pylak
 */
public class ContainFilterField<M extends ModelData> extends AbstractFilterField<M>
{
    public ContainFilterField(String filteredPropertyKey, String title)
    {
        super(filteredPropertyKey, title);
    }

    @Override
    public boolean isMatching(M record)
    {
        return doSelect(record, getRawValue(), getProperty());
    }

    private static boolean doSelect(ModelData record, String filterText, String filteredPropertyKey)
    {
        if (StringUtils.isBlank(filterText))
        {
            return true;
        }
        Object rawValue = record.get(filteredPropertyKey);
        return rawValue != null
                && rawValue.toString().toLowerCase().contains(filterText.toLowerCase());

    }
}
