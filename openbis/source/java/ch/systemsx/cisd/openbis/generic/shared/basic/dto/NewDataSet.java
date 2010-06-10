/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Set;

/**
 * Basic information about data set.
 * 
 * @author Izabela Adamczyk
 */
public class NewDataSet extends Code<NewDataSet> implements Comparable<NewDataSet>
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IEntityProperty[] properties = IEntityProperty.EMPTY_ARRAY;

    private Set<String> propertiesToUpdate;

    public Set<String> getPropertiesToUpdate()
    {
        return propertiesToUpdate;
    }

    public NewDataSet()
    {
    }

    public void setPropertiesToUpdate(Set<String> propertiesToUpdate)
    {
        this.propertiesToUpdate = propertiesToUpdate;
    }

    public NewDataSet(final String code, IEntityProperty[] properties,
            Set<String> propertiesToUpdate)
    {
        setCode(code);
        this.properties = properties;
        this.propertiesToUpdate = propertiesToUpdate;
    }

    public final IEntityProperty[] getProperties()
    {
        return properties;
    }

    public final void setProperties(final IEntityProperty[] properties)
    {
        this.properties = properties;
    }

}
