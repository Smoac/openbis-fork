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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;

/**
 * The <i>GWT</i> equivalent to {@link DataSetTypePE}.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetType extends EntityType
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private List<DataSetTypePropertyType> dataSetTypePropertyTypes;

    public DataSetType()
    {
    }

    public DataSetType(final String code)
    {
        setCode(code);
    }

    @Override
    public List<DataSetTypePropertyType> getAssignedPropertyTypes()
    {
        return dataSetTypePropertyTypes;
    }

    public void setDataSetTypePropertyTypes(List<DataSetTypePropertyType> dataSetTypePropertyTypes)
    {
        this.dataSetTypePropertyTypes = dataSetTypePropertyTypes;
    }
}
