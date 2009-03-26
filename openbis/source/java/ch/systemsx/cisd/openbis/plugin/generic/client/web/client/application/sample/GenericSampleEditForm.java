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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Procedure;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> sample edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericSampleEditForm
        extends
        AbstractGenericEntityEditForm<SampleType, SampleTypePropertyType, SampleProperty, EditableSample>
{
    private final Sample originalSample;

    // null if sample cannot be attached to an experiment
    private final ExperimentChooserFieldAdaptor experimentFieldOrNull;

    private final PropertyGrid specificFieldsGrid;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    public GenericSampleEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            EditableSample entity, boolean editMode)
    {
        super(viewContext, entity, editMode);
        this.viewContext = viewContext;
        this.originalSample = entity.getSample();
        this.experimentFieldOrNull =
                canAttachToExperiment(originalSample) ? createExperimentField() : null;
        this.specificFieldsGrid = new PropertyGrid(viewContext, 1);
        super.initializeComponents(viewContext);
    }

    private ExperimentChooserFieldAdaptor createExperimentField()
    {
        String label = viewContext.getMessage(Dict.EXPERIMENT);
        ExperimentIdentifier originalExperiment = tryGetOriginalExperiment();
        return ExperimentChooserField.create(label, false, originalSample.getGroup(),
                originalExperiment, viewContext.getCommonViewContext());
    }

    public static final String ID_PREFIX = createId(EntityKind.SAMPLE, "");

    @Override
    public final void submitValidForm()
    {
        final List<SampleProperty> properties = extractProperties();
        ExperimentIdentifier experimentIdent =
                experimentFieldOrNull != null ? experimentFieldOrNull.getValue() : null;
        viewContext.getCommonService().updateSample(entity.getIdentifier(), properties,
                experimentIdent, entity.getModificationDate(),
                new UpdateSampleCallback(viewContext));
    }

    public final class UpdateSampleCallback extends AbstractAsyncCallback<Void>
    {

        UpdateSampleCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        private final String createSuccessfullRegistrationInfo()
        {
            return "Sample successfully updated";
        }

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo());
            showCheckPage();
        }
    }

    @Override
    protected PropertiesEditor<SampleType, SampleTypePropertyType, SampleProperty> createPropertiesEditor(
            List<SampleTypePropertyType> entityTypesPropertyTypes, List<SampleProperty> properties,
            String id, IViewContext<ICommonClientServiceAsync> context)
    {
        return new SamplePropertyEditor(entityTypesPropertyTypes, properties, id, context);
    }

    @Override
    protected List<Field<?>> getEntitySpecificFormFields()
    {
        ArrayList<Field<?>> fields = new ArrayList<Field<?>>();
        if (experimentFieldOrNull != null)
        {
            fields.add(experimentFieldOrNull.getField());
        }
        return fields;
    }

    @Override
    protected void updateCheckPageWidgets()
    {
        if (experimentFieldOrNull != null)
        {
            experimentFieldOrNull.updateOriginalValue();
            updateSpecificPropertiesGrid();
        }
    }

    private ExperimentIdentifier tryGetOriginalExperiment()
    {
        Procedure proc = originalSample.getValidProcedure();
        if (proc == null)
        {
            return null;
        }
        return ExperimentIdentifier.createIdentifier(proc.getExperiment());
    }

    @Override
    protected List<Widget> getEntitySpecificCheckPageWidgets()
    {
        ArrayList<Widget> result = new ArrayList<Widget>();
        if (experimentFieldOrNull != null)
        {
            updateSpecificPropertiesGrid();
            result.add(specificFieldsGrid);
        }
        return result;
    }

    private void updateSpecificPropertiesGrid()
    {
        Map<String, String> valueMap = new HashMap<String, String>();
        valueMap.put(viewContext.getMessage(Dict.EXPERIMENT), tryPrintSelectedExperiment());
        this.specificFieldsGrid.setProperties(valueMap);
    }

    private String tryPrintSelectedExperiment()
    {
        if (experimentFieldOrNull == null)
        {
            return null;
        }
        ExperimentIdentifier value = experimentFieldOrNull.getValue();
        if (value == null)
        {
            return null;
        } else
        {
            return value.print();
        }
    }

    private static boolean canAttachToExperiment(Sample sample)
    {
        return sample.getGroup() != null;
    }
}
