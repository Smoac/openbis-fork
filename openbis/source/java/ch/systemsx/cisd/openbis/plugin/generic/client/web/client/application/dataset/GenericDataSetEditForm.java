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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
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

    private final String simpleId;

    private FileFormatTypeSelectionWidget fileFormatTypeSelectionWidget;

    private SampleChooserFieldAdaptor sampleField;

    private DataSetParentsArea parentsArea;

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
        simpleId = createSimpleId(identifiable, EntityKind.DATA_SET);
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
        result.setModifiedParentDatasetCodesOrNull(extractParentDatasetCodes());
        result.setSampleIdentifierOrNull(extractSampleIdentifier());
        result.setFileFormatTypeCode(extractFileFormatTypeCode());
        return result;
    }

    private String extractSampleIdentifier()
    {
        return sampleField.getValue();
    }

    protected String[] extractParentDatasetCodes()
    {
        return parentsArea.tryGetModifiedParentCodes();
    }

    private String extractFileFormatTypeCode()
    {
        return fileFormatTypeSelectionWidget.tryGetSelectedFileFormatType().getCode();
    }

    public final class UpdateDataSetCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<DataSetUpdateResult>
    {

        UpdateDataSetCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final DataSetUpdateResult result)
        {
            originalDataSet.setModificationDate(result.getModificationDate());
            updateOriginalValues(result.getParentCodes());
            super.process(result);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(DataSetUpdateResult result)
        {
            return "Data set successfully updated";
        }
    }

    private void updateOriginalValues(String[] parentCodes)
    {
        updatePropertyFieldsOriginalValues();
        sampleField.updateOriginalValue();
        parentsArea.setParentCodes(parentCodes);
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
        fields.add(wrapUnaware(parentsArea));
        fields.add(wrapUnaware(sampleField.getField()));
        fields.add(wrapUnaware(fileFormatTypeSelectionWidget));
        return fields;
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        this.parentsArea = createParentsArea();
        this.sampleField = createSampleField();
        this.fileFormatTypeSelectionWidget = createFileFormatTypeField();
    }

    private DataSetParentsArea createParentsArea()
    {
        return new DataSetParentsArea(viewContext, simpleId);
    }

    private SampleChooserFieldAdaptor createSampleField()
    {
        String label = viewContext.getMessage(Dict.SAMPLE);
        String originalSampleOrNull = originalDataSet.getSampleIdentifier();
        // one cannot select a sample from shared group or a sample that has no experiment
        final SampleChooserFieldAdaptor result =
                SampleChooserField.create(label, false, originalSampleOrNull, false, true,
                        viewContext.getCommonViewContext());
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
        fileFormatTypeSelectionWidget.setValue(new FileFormatTypeModel(originalDataSet
                .getFileFormatType()));
        // parents Area
        parentsArea.setEnabled(false);
        parentsArea.setValue(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        loadParentsInBackground();
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

    private void loadParentsInBackground()
    {
        // not best performance but the same solution that is done for experiments
        // only codes are needed but we extract 'full' object
        DefaultResultSetConfig<String, ExternalData> config =
                DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listDataSetRelationships(techIdOrNull,
                DataSetRelationshipRole.CHILD, config, new ListParentsCallback(viewContext));
    }

    public class ListParentsCallback extends
            AbstractAsyncCallback<ResultSetWithEntityTypes<ExternalData>>
    {

        public ListParentsCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(ResultSetWithEntityTypes<ExternalData> result)
        {
            parentsArea.setParents(result.getResultSet().getList());
            parentsArea.setEnabled(true);
        }
    }
}
