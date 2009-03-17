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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

public class MaterialPropertyEditor extends
        PropertiesEditor<MaterialType, MaterialTypePropertyType, MaterialProperty>
{

    public MaterialPropertyEditor(List<MaterialTypePropertyType> entityTypesPropertyTypes,
            List<MaterialProperty> properties, String id,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(id, entityTypesPropertyTypes, properties, viewContext);
    }

    public MaterialPropertyEditor(List<MaterialTypePropertyType> entityTypesPropertyTypes,
            String id, IViewContext<ICommonClientServiceAsync>  viewContext)
    {
        super(id, entityTypesPropertyTypes, viewContext);
    }

    @Override
    protected MaterialProperty createEntityProperty()
    {
        return new MaterialProperty();
    }

}