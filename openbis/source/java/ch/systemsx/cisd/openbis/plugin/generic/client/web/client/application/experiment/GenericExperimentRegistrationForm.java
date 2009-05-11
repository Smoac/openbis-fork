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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;

/**
 * The <i>generic</i> experiment registration form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentRegistrationForm
        extends
        AbstractGenericEntityRegistrationForm<ExperimentType, ExperimentTypePropertyType, ExperimentProperty>
{

    public static IIdentifierHolder identifier = null;

    public static final String ID = createId(identifier, EntityKind.EXPERIMENT);

    public static final String ATTACHMENTS_SESSION_KEY =
            createSimpleId(identifier, EntityKind.EXPERIMENT) + "_attachments";

    public static final String SAMPLES_SESSION_KEY =
            createSimpleId(identifier, EntityKind.EXPERIMENT) + "_samples";

    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 3;

    private final ExperimentType experimentType;

    private final FileFieldManager attachmentManager =
            new FileFieldManager(ATTACHMENTS_SESSION_KEY, DEFAULT_NUMBER_OF_ATTACHMENTS,
                    "Attachment");

    private final FileFieldManager importSamplesFileManager =
            new FileFieldManager(SAMPLES_SESSION_KEY, 1, "File");

    private final SampleTypeSelectionWidget importSampleTypeSelection;

    private final Radio importSamplesRadio;

    private final Radio existingSamplesRadio;

    private final CheckBox autoGenerateCodes;

    private ExperimentSamplesArea samplesArea;

    private ProjectSelectionWidget projectSelectionWidget;

    public GenericExperimentRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final ExperimentType experimentType)
    {
        super(viewContext, EntityKind.EXPERIMENT);
        this.experimentType = experimentType;
        importSamplesFileManager.setMandatory();
        importSampleTypeSelection = new SampleTypeSelectionWidget(viewContext, ID, false);
        FieldUtil.markAsMandatory(importSampleTypeSelection);
        existingSamplesRadio = cerateExistingSamplesRadio();
        importSamplesRadio = createImportRadio();
        autoGenerateCodes = createAutoGenerateCheckbox();
        List<String> sesionKeys = new ArrayList<String>();
        sesionKeys.add(ATTACHMENTS_SESSION_KEY);
        sesionKeys.add(SAMPLES_SESSION_KEY);
        addUploadFeatures(sesionKeys);
    }

    private final String createExperimentIdentifier()
    {
        final Project project = projectSelectionWidget.tryGetSelectedProject();
        final String code = codeField.getValue();
        final String result = project.getIdentifier() + "/" + code;
        return result.toUpperCase();
    }

    @Override
    public final void submitValidForm()
    {
    }

    public final class RegisterExperimentCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {

        RegisterExperimentCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(Void result)
        {
            return "Experiment <b>" + createExperimentIdentifier() + "</b> successfully registered";
        }

    }

    private RadioGroup createSamplesSourceRadio(Radio existing, Radio importFromFile)
    {
        RadioGroup result = new RadioGroup();
        result.setSelectionRequired(true);
        result.setFieldLabel(viewContext.getMessage(Dict.SAMPLES));
        result.setOrientation(Orientation.HORIZONTAL);
        result.add(existing);
        result.add(importFromFile);
        result.addListener(Events.Change, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    updateSamples();
                }
            });
        return result;
    }

    protected static Radio createImportRadio()
    {
        Radio importRadio = new Radio();
        importRadio.setBoxLabel("register from a file and attach");
        return importRadio;
    }

    protected static CheckBox createAutoGenerateCheckbox()
    {
        CheckBox result = new CheckBox();
        result.setFieldLabel("Create codes automatically");
        return result;
    }

    protected static Radio cerateExistingSamplesRadio()
    {
        Radio existingRadio = new Radio();
        existingRadio.setBoxLabel("specify the list of existing samples");
        existingRadio.setValue(true);
        return existingRadio;
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        projectSelectionWidget = new ProjectSelectionWidget(viewContext, getId());
        FieldUtil.markAsMandatory(projectSelectionWidget);
        projectSelectionWidget.setFieldLabel(viewContext.getMessage(Dict.PROJECT));
        samplesArea = new ExperimentSamplesArea(viewContext, ID);
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

    private void redefineSaveListeners()
    {
        saveButton.removeAllListeners();
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        if (attachmentManager.filesDefined() > 0
                                || importSamplesFileManager.filesDefined() > 0)
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
    protected void resetPanel()
    {
        super.resetPanel();
    }

    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        final ArrayList<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(projectSelectionWidget.asDatabaseModificationAware());
        fields.add(wrapUnaware(createSamplesSourceRadio(existingSamplesRadio, importSamplesRadio)));
        fields.add(wrapUnaware(samplesArea));
        fields.add(importSampleTypeSelection.asDatabaseModificationAware());
        for (FileUploadField samplesFileField : importSamplesFileManager.getFields())
        {
            fields.add(wrapUnaware((Field<?>) samplesFileField));
        }
        fields.add(wrapUnaware(autoGenerateCodes));
        for (FileUploadField attachmentField : attachmentManager.getFields())
        {
            fields.add(wrapUnaware((Field<?>) attachmentField));
        }
        return fields;
    }

    private void updateSamples()
    {
        Boolean useExistingSamples = existingSamplesRadio.getValue();
        samplesArea.setVisible(useExistingSamples);
        samplesArea.setEnabled(useExistingSamples);
        importSampleTypeSelection.setVisible(useExistingSamples == false);
        importSampleTypeSelection.setEnabled(useExistingSamples == false);
        autoGenerateCodes.setVisible(useExistingSamples == false);
        autoGenerateCodes.setEnabled(useExistingSamples == false);
        for (FileUploadField samplesFileField : importSamplesFileManager.getFields())
        {
            samplesFileField.setVisible(useExistingSamples == false);
            samplesFileField.setEnabled(useExistingSamples == false);
            samplesFileField.validate();
        }
        samplesArea.validate();
        importSampleTypeSelection.validate();
    }

    private void registerExperiment()
    {
        final NewExperiment newExp =
                new NewExperiment(createExperimentIdentifier(), experimentType.getCode());
        final List<ExperimentProperty> properties = extractProperties();
        newExp.setProperties(properties.toArray(ExperimentProperty.EMPTY_ARRAY));
        newExp.setSamples(getSamples());
        newExp.setSampleType(getSampleType());
        newExp.setGenerateCodes(autoGenerateCodes.getValue().booleanValue());
        newExp.setRegisterSamples(existingSamplesRadio.getValue() == false);
        viewContext.getService().registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY,
                newExp, new RegisterExperimentCallback(viewContext));
    }

    private String[] getSamples()
    {
        if (existingSamplesRadio.getValue())
            return samplesArea.tryGetSampleCodes();
        else
            return null;
    }

    private SampleType getSampleType()
    {
        if (existingSamplesRadio.getValue() == false)
            return importSampleTypeSelection.tryGetSelectedSampleType();
        else
            return null;
    }

    @Override
    protected PropertiesEditor<ExperimentType, ExperimentTypePropertyType, ExperimentProperty> createPropertiesEditor(
            String id, IViewContext<ICommonClientServiceAsync> context)
    {
        ExperimentPropertyEditor editor = new ExperimentPropertyEditor(id, context);
        return editor;
    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithoutProperties(experimentType.getAssignedPropertyTypes());
        updateSamples();
    }

    @Override
    protected void loadForm()
    {
        initGUI();
    }

}
