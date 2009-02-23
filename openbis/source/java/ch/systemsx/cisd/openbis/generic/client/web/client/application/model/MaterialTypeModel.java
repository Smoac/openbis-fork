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

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;

/**
 * {@link ModelData} for {@link MaterialType}.
 * 
 * @author Izabela Adamczyk
 */
public class MaterialTypeModel extends BaseModelData
{

    private static final long serialVersionUID = 1L;

    public MaterialTypeModel(final MaterialType type)
    {
        set(ModelDataPropertyNames.CODE, type.getCode());
        set(ModelDataPropertyNames.OBJECT, type);
    }

    public final static List<MaterialTypeModel> convert(final List<MaterialType> types)
    {
        final List<MaterialTypeModel> result = new ArrayList<MaterialTypeModel>();
        for (final MaterialType st : types)
        {
            result.add(new MaterialTypeModel(st));
        }
        return result;
    }

}
