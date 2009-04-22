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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
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
    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 3;

    private final FileFieldManager attachmentManager;

    private String sessionKey;

    private Html attachmentsInfo;

    private final Sample originalSample;

    // null if sample cannot be attached to an experiment
    private final ExperimentChooserFieldAdaptor experimentFieldOrNull;

    private final PropertyGrid specificFieldsGrid;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, EditableSample entity,
            boolean editMode)
    {
        GenericSampleEditForm form = new GenericSampleEditForm(viewContext, entity, editMode);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericSampleEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            EditableSample entity, boolean editMode)
    {
        super(viewContext, entity, editMode);
        this.viewContext = viewContext;
        this.originalSample = entity.getSample();
        this.experimentFieldOrNull =
                canAttachToExperiment(originalSample) ? createExperimentField() : null;
        this.specificFieldsGrid = new PropertyGrid(viewContext, 1);
        super.initializeComponents(viewContext);
        sessionKey = createSimpleId(EntityKind.SAMPLE, entity.getId() + "");
        attachmentManager =
                new FileFieldManager(sessionKey, DEFAULT_NUMBER_OF_ATTACHMENTS, "New Attachment");
        addUploadFeatures(sessionKey);
        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    save();
                }

                @Override
                protected void setUploadEnabled()
                {
                    // GenericExperimentRegistrationForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

    void redefineSaveListeners()
    {
        saveButton.removeAllListeners();
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        if (attachmentManager.filesDefined() > 0)
                        {
                            formPanel.submit();
                        } else
                        {
                            save();
                        }
                    }
                }
            });
    }

    private ExperimentChooserFieldAdaptor createExperimentField()
    {
        String label = viewContext.getMessage(Dict.EXPERIMENT);
        Experiment experiment = originalSample.getExperiment();
        ExperimentIdentifier originalExperiment =
                experiment == null ? null : ExperimentIdentifier.createIdentifier(experiment);
        return ExperimentChooserField.create(label, false, originalSample.getGroup(),
                originalExperiment, viewContext.getCommonViewContext());
    }

    public static final String ID_PREFIX = createId(EntityKind.SAMPLE, "");

    @Override
    public final void submitValidForm()
    {
    }

    private void save()
    {
        final List<SampleProperty> properties = extractProperties();
        ExperimentIdentifier experimentIdent =
                experimentFieldOrNull != null ? experimentFieldOrNull.getValue() : null;
        viewContext.getService().updateSample(sessionKey, entity.getIdentifier(), properties,
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

    @SuppressWarnings("unchecked")
    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        ArrayList<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        if (experimentFieldOrNull != null)
        {
            fields.add(wrapUnaware(experimentFieldOrNull.getField()));
        }
        for (FileUploadField f : attachmentManager.getFields())
        {
            fields.add(DatabaseModificationAwareField.wrapUnaware(f));
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
            attachmentsInfo.setHtml(getAttachmentInfoText(attachmentManager.filesDefined()));
            updateHeader();
        }
    }

    @Override
    protected List<Widget> getEntitySpecificCheckPageWidgets()
    {
        ArrayList<Widget> result = new ArrayList<Widget>();
        if (experimentFieldOrNull != null)
        {
            updateSpecificPropertiesGrid();
            result.add(specificFieldsGrid);
            result.add(attachmentsInfo = new Html());
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

    public String getAttachmentInfoText(int attachmentDefined)
    {
        if (attachmentDefined > 0)
        {
            return Format.substitute("Added {0} new attachment{1}.", attachmentDefined,
                    attachmentDefined == 1 ? "" : "s");

        } else
        {
            return "No new attachments added.";
        }
    }

    private void updateHeader()
    {
        setHeading("Sample " + entity.getIdentifier());
    }
}
