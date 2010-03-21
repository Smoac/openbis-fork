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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.sample;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.AbstractSampleBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo.RegistrationScope;

/**
 * The {@link ScreeningConstants#LIBRARY_PLUGIN_TYPE_CODE} sample import panel.
 * 
 * @author Izabela Adamczyk
 */
public final class LibrarySampleBatchRegistrationForm extends AbstractSampleBatchRegistrationForm
{

    private static final String SESSION_KEY = "qiagen-library-sample-batch-registration";

    private final ExperimentChooserFieldAdaptor experimentChooser;

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final Field<String> plateGeometryField;

    private final RadioGroup scopeField;

    private Radio genesOligosPlatesRadio;

    private Radio oligosPlatesRadio;

    private Radio platesRadio;

    private final TextField<String> emailField;

    public LibrarySampleBatchRegistrationForm(
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), SESSION_KEY);
        this.viewContext = viewContext;
        experimentChooser =
                ExperimentChooserField.create(viewContext.getMessage(Dict.EXPERIMENT), true, null,
                        viewContext.getCommonViewContext());
        plateGeometryField = createPlateGeometryField();
        scopeField = createScope();
        emailField =
                createEmailField(viewContext.getModel().getSessionContext().getUser()
                        .getUserEmail());
    }

    private TextField<String> createEmailField(String userEmail)
    {
        TextField<String> field = new TextField<String>();
        field.setAllowBlank(false);
        field.setFieldLabel("Email");
        FieldUtil.markAsMandatory(field);
        field.setValue(userEmail);
        field.setValidateOnBlur(true);
        field.setRegex(GenericConstants.EMAIL_REGEX);
        field.getMessages().setRegexText("Expected email address format: user@domain.com");
        AbstractImagePrototype infoIcon =
                AbstractImagePrototype.create(viewContext.getImageBundle().getInfoIcon());
        FieldUtil.addInfoIcon(field,
                "All relevant notifications will be send to this email address", infoIcon
                        .createImage());
        return field;
    }

    // FIXME: change to combobox
    private RadioGroup createScope()
    {
        RadioGroup group = new RadioGroup();
        group.setFieldLabel("Register");
        genesOligosPlatesRadio = createCheckBox("Genes + Oligos + Plates");
        oligosPlatesRadio = createCheckBox("Oligos + Plates");
        platesRadio = createCheckBox("Plates");
        group.add(genesOligosPlatesRadio);
        group.add(oligosPlatesRadio);
        group.add(platesRadio);
        group.setValue(genesOligosPlatesRadio);
        return group;
    }

    private Radio createCheckBox(String label)
    {
        Radio checkBox = new Radio();
        checkBox.setBoxLabel(label);
        return checkBox;
    }

    @Override
    protected void save()
    {
        ExperimentIdentifier experiment = experimentChooser.tryToGetValue();
        String plateGeometry = plateGeometryField.getValue();
        String userEmail = emailField.getValue();
        RegistrationScope registrationScope = extractRegistrationScope();
        LibraryRegistrationInfo libraryInfo =
                new LibraryRegistrationInfo().setSessionKey(getSessionKey()).setExperiment(
                        experiment.getIdentifier()).setPlateGeometry(plateGeometry).setUserEmail(
                        userEmail).setScope(registrationScope);
        viewContext.getService().registerLibrary(libraryInfo,
                new RegisterSamplesCallback(viewContext));
        infoBox.displayInfo("Data preprocessing started. Please wait...");
    }

    private RegistrationScope extractRegistrationScope()
    {
        if (genesOligosPlatesRadio.getValue())
        {
            return RegistrationScope.GENES_OLIGOS_PLATES;
        } else if (oligosPlatesRadio.getValue())
        {
            return RegistrationScope.OLIGOS_PLATES;
        } else if (platesRadio.getValue())
        {
            return RegistrationScope.PLATES;
        }
        return null;
    }

    @Override
    protected void addSpecificFormFields(FormPanel form)
    {
        form.add(scopeField);
        form.add(experimentChooser.getChooserField());
        form.add(plateGeometryField);
        form.add(emailField);
    }

    private final class RegisterSamplesCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        RegisterSamplesCallback(final IViewContext<IScreeningClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(final Void result)
        {
            return viewContext
                    .getMessage(
                            ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict.IMPORT_SCHEDULED_MESSAGE,
                            emailField.getValue());
        }
    }

    private Field<String> createPlateGeometryField()
    {// FIXME: change to term chooser
        Field<String> field = new TextField<String>();
        field.setFieldLabel("Plate geometry");
        FieldUtil.markAsMandatory(field);
        return field;
    }
}
