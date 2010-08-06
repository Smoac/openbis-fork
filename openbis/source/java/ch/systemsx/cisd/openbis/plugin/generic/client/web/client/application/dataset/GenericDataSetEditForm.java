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

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetParentsArea;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FileFormatTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.FileFormatTypeSelectionWidget.FileFormatTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataSetUpdates;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
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

    private static final String EXPERIMENT_FIELD_ID_SUFFIX = "experiment_field";

    private final String simpleId;

    private FileFormatTypeSelectionWidget fileFormatTypeSelectionWidget;

    private CheckBoxField connectedWithSampleCheckbox;

    // two options:
    // 1. connected with sample
    private SampleChooserFieldAdaptor sampleChooser;

    // 2. not connected with sample
    private ExperimentChooserFieldAdaptor experimentChooser;

    // 

    private DataSetParentsArea parentsArea;

    private ExternalData originalDataSet;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, IIdAndCodeHolder identifiable)
    {
        GenericDataSetEditForm form = new GenericDataSetEditForm(viewContext, identifiable);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericDataSetEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            IIdAndCodeHolder identifiable)
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
        result.setFileFormatTypeCode(extractFileFormatTypeCode());
        if (isConnectedWithSample())
        {
            result.setSampleIdentifierOrNull(extractSampleIdentifier());
        } else
        {
            result.setExperimentIdentifierOrNull(extractExperimentIdentifier());
        }
        result.setModifiedParentDatasetCodesOrNull(extractParentDatasetCodes());
        return result;
    }

    private Boolean isConnectedWithSample()
    {
        return connectedWithSampleCheckbox.getValue();
    }

    private String extractSampleIdentifier()
    {
        return sampleChooser.getValue();
    }

    private String extractExperimentIdentifier()
    {
        ExperimentIdentifier identifierOrNull = experimentChooser.tryToGetValue();
        return identifierOrNull == null ? null : identifierOrNull.getIdentifier();
    }

    protected String[] extractParentDatasetCodes()
    {
        return parentsArea.tryGetModifiedParentCodes();
    }

    private String extractFileFormatTypeCode()
    {
        return fileFormatTypeSelectionWidget.tryGetSelectedFileFormatType().getCode();
    }

    // public only for tests
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

    private void updateOriginalValues(List<String> parentCodes)
    {
        updatePropertyFieldsOriginalValues();
        sampleChooser.updateOriginalValue();
        experimentChooser.updateOriginalValue();
        parentsArea.setParentCodes(parentCodes);
        fileFormatTypeSelectionWidget.updateOriginalValue(fileFormatTypeSelectionWidget.getValue());
        connectedWithSampleCheckbox.updateOriginalValue(connectedWithSampleCheckbox.getValue());
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
        fields.add(wrapUnaware(connectedWithSampleCheckbox));
        fields.add(wrapUnaware(sampleChooser.getField()));
        fields.add(wrapUnaware(experimentChooser.getField()));
        fields.add(wrapUnaware(parentsArea));
        fields.add(wrapUnaware(fileFormatTypeSelectionWidget));
        return fields;
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        this.connectedWithSampleCheckbox = createConnectedWithSampleCheckbox();
        this.sampleChooser = createSampleField();
        this.experimentChooser = createExperimentChooserField();
        this.parentsArea = createParentsArea();
        this.fileFormatTypeSelectionWidget = createFileFormatTypeField();
    }

    private CheckBoxField createConnectedWithSampleCheckbox()
    {
        CheckBoxField result = new CheckBoxField("Connected with Sample", false);
        result.addListener(Events.Change, new Listener<FieldEvent>()
            {
                public void handleEvent(FieldEvent be)
                {
                    updateFieldsVisibility();
                }
            });
        return result;
    }

    private SampleChooserFieldAdaptor createSampleField()
    {
        String label = viewContext.getMessage(Dict.SAMPLE);
        String originalSampleOrNull = originalDataSet.getSampleIdentifier();
        // one cannot select a sample from shared group or a sample that has no experiment
        final SampleChooserFieldAdaptor result =
                SampleChooserField.create(label, true, originalSampleOrNull, false, false, true,
                        viewContext.getCommonViewContext(),
                        SampleTypeDisplayID.DATA_SET_EDIT_SAMPLE_CHOOSER);
        result.getField().setId(createChildId(SAMPLE_FIELD_ID_SUFFIX));
        return result;
    }

    private ExperimentChooserFieldAdaptor createExperimentChooserField()
    {
        String label = viewContext.getMessage(Dict.EXPERIMENT);
        ExperimentIdentifier originalExperiment =
                ExperimentIdentifier.createIdentifier(originalDataSet.getExperiment());
        final ExperimentChooserFieldAdaptor result =
                ExperimentChooserField.create(label, true, originalExperiment, viewContext
                        .getCommonViewContext());
        result.getField().setId(createChildId(EXPERIMENT_FIELD_ID_SUFFIX));
        return result;
    }

    private DataSetParentsArea createParentsArea()
    {
        return new DataSetParentsArea(viewContext, simpleId);
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
        boolean connectedWithSample = StringUtils.isBlank(sampleChooser.getValue()) == false;
        connectedWithSampleCheckbox.setValue(connectedWithSample);
        updateFieldsVisibility();

        propertiesEditor.initWithProperties(originalDataSet.getDataSetType()
                .getAssignedPropertyTypes(), originalDataSet.getProperties());
        codeField.setValue(originalDataSet.getCode());
        fileFormatTypeSelectionWidget.setValue(new FileFormatTypeModel(originalDataSet
                .getFileFormatType()));
        // sample and experiment fields are initialized when they are created
        parentsArea.setValue(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        loadParentsInBackground();
    }

    private void updateFieldsVisibility()
    {
        boolean connectedWithSample = isConnectedWithSample();
        FieldUtil.setVisibility(connectedWithSample, sampleChooser.getField());
        FieldUtil.setVisibility(connectedWithSample == false, experimentChooser.getField());
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

    private final class DataSetInfoCallback extends AbstractAsyncCallback<ExternalData>
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

    private class ListParentsCallback extends
            AbstractAsyncCallback<ResultSetWithEntityTypes<ExternalData>>
    {

        public ListParentsCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(ResultSetWithEntityTypes<ExternalData> result)
        {
            parentsArea.setParents(result.getResultSet().getList().extractOriginalObjects());
            if (parentsArea.isVisible())
            {
                parentsArea.setEnabled(true);
            }
        }
    }
}
