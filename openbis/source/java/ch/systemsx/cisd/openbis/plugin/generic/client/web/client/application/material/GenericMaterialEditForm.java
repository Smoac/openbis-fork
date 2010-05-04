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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> material edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericMaterialEditForm extends
        AbstractGenericEntityRegistrationForm<MaterialType, MaterialTypePropertyType>
{
    private Material originalMaterial;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, IIdAndCodeHolder identifiable,
            boolean editMode)
    {
        GenericMaterialEditForm form =
                new GenericMaterialEditForm(viewContext, identifiable, editMode);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericMaterialEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            IIdAndCodeHolder identifiable, boolean editMode)
    {
        super(viewContext, identifiable, EntityKind.MATERIAL);
    }

    @Override
    public final void submitValidForm()
    {
        viewContext.getService().updateMaterial(techIdOrNull, extractProperties(),
                originalMaterial.getModificationDate(), new UpdateMaterialCallback(viewContext));
    }

    private final class UpdateMaterialCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Date>
    {

        UpdateMaterialCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final Date result)
        {
            originalMaterial.setModificationDate(result);
            updateOriginalValues();
            super.process(result);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(Date result)
        {
            return "Material successfully updated";
        }

    }

    public void updateOriginalValues()
    {
        updatePropertyFieldsOriginalValues();
    }

    @Override
    protected PropertiesEditor<MaterialType, MaterialTypePropertyType> createPropertiesEditor(
            String id, IViewContext<ICommonClientServiceAsync> context)
    {
        MaterialPropertyEditor editor = new MaterialPropertyEditor(id, context);
        return editor;
    }

    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        return new ArrayList<DatabaseModificationAwareField<?>>();
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithProperties(originalMaterial.getMaterialType()
                .getAssignedPropertyTypes(), originalMaterial.getProperties());
        codeField.setValue(originalMaterial.getCode());
    }

    private void setOriginalMaterial(Material material)
    {
        this.originalMaterial = material;
    }

    @Override
    protected void loadForm()
    {
        viewContext.getService().getMaterialInfo(techIdOrNull,
                new MaterialInfoCallback(viewContext));
    }

    private final class MaterialInfoCallback extends AbstractAsyncCallback<Material>
    {

        private MaterialInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final Material result)
        {
            setOriginalMaterial(result);
            initGUI();
        }
    }

}
