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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> data set edit form.
 * 
 * @author Piotr Buczek
 */
public final class GenericDataSetEditForm
        extends
        AbstractGenericEntityRegistrationForm<DataSetType, DataSetTypePropertyType, DataSetProperty>
{

    private SampleChooserFieldAdaptor sampleField;

    private ExternalData originalDataSet;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, IIdentifierHolder identifierHolder)
    {
        GenericDataSetEditForm form = new GenericDataSetEditForm(viewContext, identifierHolder);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericDataSetEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            IIdentifierHolder identifierHolder)
    {
        super(viewContext, identifierHolder, EntityKind.DATA_SET);
    }

    @Override
    public final void submitValidForm()
    {
        final List<DataSetProperty> properties = extractProperties();
        final String sampleIdentifier = extractSampleIdentifier();
        viewContext.getService().updateDataSet(
                // TODO 2009-05-11, IA: use code
                identifierHolderOrNull.getIdentifier(), sampleIdentifier, properties,
                originalDataSet.getModificationDate(), new UpdateDataSetCallback(viewContext));
    }

    private String extractSampleIdentifier()
    {
        return sampleField.getValue();
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

    private SampleChooserFieldAdaptor createSampleField()
    {
        String label = viewContext.getMessage(Dict.SAMPLE);
        String originalSample = originalDataSet.getSampleIdentifier();
        // one cannot select a sample from shared group or a sample that has no experiment
        return SampleChooserField.create(label, true, originalSample, false, true, viewContext
                .getCommonViewContext());
    }

    private void updateOriginalValues()
    {
        updatePropertyFieldsOriginalValues();
    }

    @Override
    protected PropertiesEditor<DataSetType, DataSetTypePropertyType, DataSetProperty> createPropertiesEditor(
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
        fields.add(wrapUnaware(sampleField.getField()));
        return fields;
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        this.sampleField = createSampleField();
    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithProperties(originalDataSet.getDataSetType()
                .getAssignedPropertyTypes(), originalDataSet.getProperties());
        codeField.setValue(originalDataSet.getCode());
    }

    private void setOriginalData(ExternalData data)
    {
        this.originalDataSet = data;
    }

    @Override
    protected void loadForm()
    {
        // TODO 2009-05-11, IA: use code
        viewContext.getService().getDataSetInfo(identifierHolderOrNull.getIdentifier(),
                GWTUtils.getBaseIndexURL(), new DataSetInfoCallback(viewContext));

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
