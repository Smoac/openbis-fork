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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEditableEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> material edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericMaterialEditForm extends
        AbstractGenericEntityEditForm<MaterialType, MaterialTypePropertyType, MaterialProperty>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public GenericMaterialEditForm(IViewContext<ICommonClientServiceAsync> viewContext,
            IEditableEntity<MaterialType, MaterialTypePropertyType, MaterialProperty> entity,
            boolean editMode)
    {
        super(viewContext, entity, editMode);
        this.viewContext = viewContext;
    }

    public static final String ID_PREFIX = createId(EntityKind.MATERIAL, "");

    @Override
    public final void submitValidForm()
    {
        final List<MaterialProperty> properties = extractProperties();
        viewContext.getService().updateMaterial(entity.getIdentifier(), properties,
                new RegisterMaterialCallback(viewContext));
    }

    public final class RegisterMaterialCallback extends AbstractAsyncCallback<Void>
    {

        RegisterMaterialCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        private final String createSuccessfullRegistrationInfo()
        {
            return "Material successfully updated";
        }

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo());
            showPropertyGrid();
        }
    }

    @Override
    protected PropertiesEditor<MaterialType, MaterialTypePropertyType, MaterialProperty> createPropertiesEditor(
            List<MaterialTypePropertyType> entityTypesPropertyTypes,
            List<MaterialProperty> properties, String id)
    {
        return new MaterialPropertyEditor<MaterialType, MaterialTypePropertyType, MaterialProperty>(
                entityTypesPropertyTypes, properties, id, viewContext);
    }

}
