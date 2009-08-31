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
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FileFormatTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FileFormatTypeSelectionWidget.FileFormatTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> data set edit form.
 * 
 * @author Piotr Buczek
 */
public final class GenericDataSetEditForm extends
        AbstractGenericEntityRegistrationForm<DataSetType, DataSetTypePropertyType>
{
    private static final String SAMPLE_FIELD_ID_SUFFIX = "sample_field";

    private FileFormatTypeSelectionWidget fileFormatTypeSelectionWidget;

    private SampleChooserFieldAdaptor sampleField;

    private CodeField parentField;

    private ExternalData originalDataSet;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, IIdentifiable identifiable)
    {
        GenericDataSetEditForm form = new GenericDataSetEditForm(viewContext, identifiable);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericDataSetEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            IIdentifiable identifiable)
    {
        super(viewContext, identifiable, EntityKind.DATA_SET);
    }

    private String createChildId(String childSuffix)
    {
        return getId() + childSuffix;
    }

    @Override
    public final void submitValidForm()
    {
        viewContext.getService().updateDataSet(createUpdates(),
                new UpdateDataSetCallback(viewContext));
    }

    private DataSetUpdates createUpdates()
    {
        final DataSetUpdates result = new DataSetUpdates();
        result.setDatasetId(techIdOrNull);
        result.setProperties(extractProperties());
        result.setVersion(originalDataSet.getModificationDate());
        result.setParentDatasetCodeOrNull(extractParentDatasetCode());
        result.setSampleIdentifier(extractSampleIdentifier());
        result.setFileFormatTypeCode(extractFileFormatTypeCode());
        return result;
    }

    private String extractSampleIdentifier()
    {
        return sampleField.getValue();
    }

    private String extractParentDatasetCode()
    {
        return parentField.getValue();
    }

    private String extractFileFormatTypeCode()
    {
        return fileFormatTypeSelectionWidget.tryGetSelectedFileFormatType().getCode();
    }

    public final class UpdateDataSetCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Date>
    {

        UpdateDataSetCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final Date result)
        {
            originalDataSet.setModificationDate(result);
            updateOriginalValues();
            super.process(result);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(Date result)
        {
            return "Data set successfully updated";
        }
    }

    private void updateOriginalValues()
    {
        updatePropertyFieldsOriginalValues();
        sampleField.updateOriginalValue();
        parentField.updateOriginalValue(parentField.getValue());
        fileFormatTypeSelectionWidget.updateOriginalValue(fileFormatTypeSelectionWidget.getValue());
    }

    @Override
    protected PropertiesEditor<DataSetType, DataSetTypePropertyType> createPropertiesEditor(
            String id, IViewContext<ICommonClientServiceAsync> context)
    {
        DataSetPropertyEditor editor = new DataSetPropertyEditor(id, context);
        return editor;
    }

    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        ArrayList<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        // TODO 2009-08-01, Piotr Buczek: add other fields specified in LMS-1003
        fields.add(wrapUnaware(parentField));
        fields.add(wrapUnaware(sampleField.getField()));
        fields.add(wrapUnaware(fileFormatTypeSelectionWidget));
        return fields;
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        this.parentField = createParentField();
        this.sampleField = createSampleField();
        this.fileFormatTypeSelectionWidget = createFileFormatTypeField();
    }

    private CodeField createParentField()
    {
        final CodeField result = new CodeField(viewContext, viewContext.getMessage(Dict.PARENT));
        result.setEmptyText("Parent data set code");
        // by default CodeField is mandatory
        result.setLabelSeparator("");
        result.setAllowBlank(true);
        return result;
    }

    private SampleChooserFieldAdaptor createSampleField()
    {
        String label = viewContext.getMessage(Dict.SAMPLE);
        String originalSample = originalDataSet.getSampleIdentifier();
        // one cannot select a sample from shared group or a sample that has no experiment
        final SampleChooserFieldAdaptor result =
                SampleChooserField.create(label, true, originalSample, false, true, viewContext
                        .getCommonViewContext());
        result.getField().setId(createChildId(SAMPLE_FIELD_ID_SUFFIX));
        return result;
    }

    private FileFormatTypeSelectionWidget createFileFormatTypeField()
    {
        final FileFormatTypeSelectionWidget result =
                new FileFormatTypeSelectionWidget(viewContext.getCommonViewContext(), getId());
        FieldUtil.markAsMandatory(result);
        return result;
    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithProperties(originalDataSet.getDataSetType()
                .getAssignedPropertyTypes(), originalDataSet.getProperties());
        codeField.setValue(originalDataSet.getCode());
        parentField.setValue(originalDataSet.getParentCode());
        fileFormatTypeSelectionWidget.setValue(new FileFormatTypeModel(originalDataSet
                .getFileFormatType()));
    }

    private void setOriginalData(ExternalData data)
    {
        this.originalDataSet = data;
    }

    @Override
    protected void loadForm()
    {
        viewContext.getService().getDataSetInfo(techIdOrNull, new DataSetInfoCallback(viewContext));

    }

    public final class DataSetInfoCallback extends AbstractAsyncCallback<ExternalData>
    {

        private DataSetInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final ExternalData result)
        {
            setOriginalData(result);
            initGUI();
        }
    }
}
