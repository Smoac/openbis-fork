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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
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
    public static final String ID = createId(EntityKind.EXPERIMENT);

    public static final String SESSION_KEY = createSimpleId(EntityKind.EXPERIMENT);

    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 3;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final ExperimentType experimentType;

    private ProjectSelectionWidget projectSelectionWidget;

    private AttachmentManager attachmentManager =
            new AttachmentManager(SESSION_KEY, DEFAULT_NUMBER_OF_ATTACHMENTS, "Attachment");

    private ExperimentSamplesArea samplesArea;

    public GenericExperimentRegistrationForm(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final ExperimentType experimentType)
    {
        super(viewContext, experimentType.getExperimentTypePropertyTypes(), EntityKind.EXPERIMENT);
        this.viewContext = viewContext;
        this.experimentType = experimentType;
        addUploadFeatures(formPanel, SESSION_KEY);
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
            AbstractRegistrationForm.AbstractRegistrationCallback
    {

        RegisterExperimentCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected String createSuccessfullRegistrationInfo()
        {
            return "Experiment <b>" + createExperimentIdentifier() + "</b> successfully registered";
        }

    }

    @Override
    protected void createEntitySpecificFields()
    {

        projectSelectionWidget = new ProjectSelectionWidget(viewContext, getId());
        FieldUtil.markAsMandatory(projectSelectionWidget);
        projectSelectionWidget.setFieldLabel(viewContext.getMessage(Dict.PROJECT));

        samplesArea = new ExperimentSamplesArea(viewContext, ID + createExperimentIdentifier());

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
                        if (attachmentManager.attachmentsDefined() > 0)
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
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFields()
    {
        final ArrayList<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(projectSelectionWidget.asDatabaseModificationAware());
        fields.add(wrapUnaware(samplesArea));
        for (FileUploadField attachmentField : attachmentManager.getFields())
        {
            fields.add(wrapUnaware((Field<?>) attachmentField));
        }
        return fields;
    }

    private void registerExperiment()
    {
        final NewExperiment newExp =
                new NewExperiment(createExperimentIdentifier(), experimentType.getCode());
        final List<ExperimentProperty> properties = extractProperties();
        newExp.setProperties(properties.toArray(ExperimentProperty.EMPTY_ARRAY));
        newExp.setSamples(samplesArea.getSampleCodes());
        viewContext.getService().registerExperiment(SESSION_KEY, newExp,
                new RegisterExperimentCallback(viewContext));
    }

    @Override
    protected PropertiesEditor<ExperimentType, ExperimentTypePropertyType, ExperimentProperty> createPropertiesEditor(
            List<ExperimentTypePropertyType> etpt, String id,
            IViewContext<ICommonClientServiceAsync> context)
    {
        return new ExperimentPropertyEditor(etpt, id, context);
    }

}
