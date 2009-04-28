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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableDataSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> data set edit form.
 * 
 * @author Piotr Buczek
 */
public final class GenericDataSetEditForm
        extends
        AbstractGenericEntityEditForm<DataSetType, DataSetTypePropertyType, DataSetProperty, EditableDataSet>
{

    private final ExternalData originalDataSet;

    private final SampleChooserFieldAdaptor sampleField;

    private final PropertyGrid specificFieldsGrid;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, EditableDataSet entity,
            boolean editMode)
    {
        GenericDataSetEditForm form = new GenericDataSetEditForm(viewContext, entity, editMode);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericDataSetEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            EditableDataSet entity, boolean editMode)
    {
        super(viewContext, entity, editMode);
        this.viewContext = viewContext;
        this.originalDataSet = entity.getDataSet();
        this.sampleField = createSampleField();
        this.specificFieldsGrid = new PropertyGrid(viewContext, 1);
        super.initializeComponents(viewContext);
    }

    public static final String ID_PREFIX = createId(EntityKind.DATA_SET, "");

    @Override
    public final void submitValidForm()
    {
        final List<DataSetProperty> properties = extractProperties();
        final String sampleIdentifier = extractSampleIdentifier();
        viewContext.getService().updateDataSet(entity.getIdentifier(), sampleIdentifier,
                properties, entity.getModificationDate(), new RegisterDataSetCallback(viewContext));
    }

    private String extractSampleIdentifier()
    {
        return sampleField.getValue();
    }

    public final class RegisterDataSetCallback extends AbstractAsyncCallback<Void>
    {

        RegisterDataSetCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        private final String createSuccessfullRegistrationInfo()
        {
            return "DataSet successfully updated";
        }

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo());
            showCheckPage();
        }
    }

    private SampleChooserFieldAdaptor createSampleField()
    {
        String label = viewContext.getMessage(Dict.SAMPLE);
        String originalSample = originalDataSet.getSampleIdentifier();
        return SampleChooserField.create(label, false, originalSample, viewContext
                .getCommonViewContext());
    }

    @Override
    protected PropertiesEditor<DataSetType, DataSetTypePropertyType, DataSetProperty> createPropertiesEditor(
            List<DataSetTypePropertyType> entityTypesPropertyTypes, // brak
            List<DataSetProperty> properties, String id,
            IViewContext<ICommonClientServiceAsync> context)
    {
        return new DataSetPropertyEditor(entityTypesPropertyTypes, properties, id, context);
    }

    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        ArrayList<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(wrapUnaware(sampleField.getField()));
        return fields;
    }

    @Override
    protected List<Widget> getEntitySpecificCheckPageWidgets()
    {
        ArrayList<Widget> result = new ArrayList<Widget>();
        updateSpecificPropertiesGrid();
        result.add(specificFieldsGrid);
        return result;
    }

    @Override
    protected void updateCheckPageWidgets()
    {
        sampleField.updateOriginalValue();
        updateSpecificPropertiesGrid();
    }

    private void updateSpecificPropertiesGrid()
    {
        Map<String, String> valueMap = new HashMap<String, String>();
        valueMap.put(viewContext.getMessage(Dict.SAMPLE), sampleField.getValue());
        this.specificFieldsGrid.setProperties(valueMap);
    }

}
