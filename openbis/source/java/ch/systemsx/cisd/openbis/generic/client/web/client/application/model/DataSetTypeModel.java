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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.TooltipRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * {@link ModelData} for {@link DataSetType}.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetTypeModel extends NonHierarchicalBaseModelData
{

    private static final long serialVersionUID = 1L;

    public DataSetTypeModel(final DataSetType dataSetType)
    {
        set(ModelDataPropertyNames.CODE, dataSetType.getCode());
        set(ModelDataPropertyNames.OBJECT, dataSetType);
        set(ModelDataPropertyNames.TOOLTIP, TooltipRenderer.renderAsTooltip(dataSetType.getCode(),
                dataSetType.getDescription()));
    }

    public final static List<DataSetTypeModel> convert(final List<DataSetType> dataSetTypes)
    {
        final List<DataSetTypeModel> result = new ArrayList<DataSetTypeModel>();
        for (final DataSetType st : dataSetTypes)
        {
            result.add(new DataSetTypeModel(st));
        }
        return result;
    }

}
