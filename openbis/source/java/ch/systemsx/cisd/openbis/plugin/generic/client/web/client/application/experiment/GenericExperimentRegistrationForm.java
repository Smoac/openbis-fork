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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.TextArea;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.FormPanelListener;

/**
 * The <i>generic</i> experiment registration form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentRegistrationForm
        extends
        AbstractGenericEntityRegistrationForm<ExperimentType, ExperimentTypePropertyType, ExperimentProperty>
{

    public static final String ID_SUFFIX_SAMPLES = "_samples";

    public static final String ID = createId(EntityKind.EXPERIMENT);

    public static final String SESSION_KEY = createSimpleId(EntityKind.EXPERIMENT);

    private static final String FIELD_LABEL_TEMPLATE = "Attachment" + " {0}";

    private static final String FIELD_NAME_TEMPLATE = SESSION_KEY + "_{0}";

    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 3;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final ExperimentType experimentType;

    private ProjectSelectionWidget projectSelectionWidget;

    private List<FileUploadField> attachmentFields = new ArrayList<FileUploadField>();

    private TextArea samplesArea;

    public GenericExperimentRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final ExperimentType experimentType)
    {
        super(viewContext, experimentType.getExperimentTypePropertyTypes(), EntityKind.EXPERIMENT);
        this.viewContext = viewContext;
        this.experimentType = experimentType;
        addUploadFeatures(formPanel, SESSION_KEY);
    }

    private final String createExpeimentIdentifier()
    {
        final Project project = projectSelectionWidget.tryGetSelectedProject();
        final String code = codeField.getValue();
        final String result = project.getIdentifier() + "/" + code;
        return result.toUpperCase();
    }

    private final String[] extractSamples()
    {
        String text = samplesArea.getValue();
        if (StringUtils.isBlank(text) == false)
        {
            return text.split("\n|\r\n|, *");
        } else
        {
            return new String[0];
        }
    }

    @Override
    public final void submitValidForm()
    {
    }

    public final class RegisterExperimentCallback extends AbstractAsyncCallback<Void>
    {

        RegisterExperimentCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        private final String createSuccessfullRegistrationInfo()
        {
            return "Experiment <b>" + createExpeimentIdentifier() + "</b> successfully registered";
        }

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo());
            resetPanel();
            setUploadEnabled(true);
        }

        @Override
        protected final void finishOnFailure(final Throwable caught)
        {
            setUploadEnabled(true);
        }

    }

    @Override
    protected void createEntitySpecificFields()
    {

        projectSelectionWidget = new ProjectSelectionWidget(viewContext, getId());
        FieldUtil.markAsMandatory(projectSelectionWidget);
        projectSelectionWidget.setFieldLabel(viewContext.getMessage(Dict.PROJECT));

        samplesArea = new TextArea();
        samplesArea.setFieldLabel(viewContext.getMessage(Dict.SAMPLES));
        samplesArea.setHeight("10em");
        samplesArea.setEmptyText(viewContext.getMessage(Dict.SAMPLES_LIST));
        samplesArea.setId(ID + ID_SUFFIX_SAMPLES);

        for (int i = 0; i < DEFAULT_NUMBER_OF_ATTACHMENTS; i++)
        {
            attachmentFields.add(createFileUploadField(i));
        }

        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    registerExperiment();
                }

                @Override
                protected void setUploadEnabled()
                {
                    GenericExperimentRegistrationForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

    protected void setUploadEnabled(boolean enabled)
    {
        saveButton.setEnabled(enabled);
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
                        if (attachmentsDefined())
                        {
                            setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            registerExperiment();
                        }
                    }
                }
            });
    }

    @Override
    protected List<Field<?>> getEntitySpecificFields()
    {
        final ArrayList<Field<?>> fields = new ArrayList<Field<?>>();
        fields.add(projectSelectionWidget);
        fields.add(samplesArea);
        for (FileUploadField attachmentField : attachmentFields)
        {
            fields.add(attachmentField);
        }
        return fields;
    }

    private final FileUploadField createFileUploadField(final int counter)
    {
        final FileUploadField file = new FileUploadField();
        final int number = counter + 1;
        file.setFieldLabel(Format.substitute(FIELD_LABEL_TEMPLATE, number));
        file.setName(Format.substitute(FIELD_NAME_TEMPLATE, number));
        return file;
    }

    private void registerExperiment()
    {
        final NewExperiment newExp =
                new NewExperiment(createExpeimentIdentifier(), experimentType.getCode());
        final List<ExperimentProperty> properties = extractProperties();
        newExp.setProperties(properties.toArray(ExperimentProperty.EMPTY_ARRAY));
        newExp.setSamples(extractSamples());
        viewContext.getService().registerExperiment(SESSION_KEY, newExp,
                new RegisterExperimentCallback(viewContext));
    }

    private boolean attachmentsDefined()
    {
        for (FileUploadField field : attachmentFields)
        {
            Object value = field.getValue();
            if (value != null && String.valueOf(value).length() > 0)
            {
                return true;
            }
        }
        return false;
    }

    private void resetPanel()
    {
        formPanel.reset();
    }

    @Override
    protected PropertiesEditor<ExperimentType, ExperimentTypePropertyType, ExperimentProperty> createPropertiesEditor(
            List<ExperimentTypePropertyType> etpt, String id)
    {
        return new ExperimentPropertyEditor<ExperimentType, ExperimentTypePropertyType, ExperimentProperty>(
                etpt, id);
    }

}
