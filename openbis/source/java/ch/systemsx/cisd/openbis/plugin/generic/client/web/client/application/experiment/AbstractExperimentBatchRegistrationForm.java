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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.file.BasicFileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * The <i>abstract</i> experiment batch registration panel.
 * 
 * @author Izabela Adamczyk
 */
public abstract class AbstractExperimentBatchRegistrationForm extends AbstractRegistrationForm
{
    // TODO 2010-10-26, IA: Merge with other AbstractEntityBatchRegistratiinForm(s)

    private static final String FIELD_LABEL_TEMPLATE = "File";

    private static final int DEFAULT_NUMBER_OF_FILES = 1;

    private final BasicFileFieldManager fileFieldsManager;

    private final String sessionKey;

    public AbstractExperimentBatchRegistrationForm(final IMessageProvider messageProvider,
            String sessionKey)
    {
        super(messageProvider, createId(sessionKey));
        this.sessionKey = sessionKey;
        fileFieldsManager =
                new BasicFileFieldManager(sessionKey, DEFAULT_NUMBER_OF_FILES, FIELD_LABEL_TEMPLATE);
        fileFieldsManager.setMandatory();
        setScrollMode(Scroll.AUTO);
        addUploadFeatures(sessionKey);
    }

    /**
     * Perform registration on the service
     */
    abstract protected void save();

    /**
     * Adds additional fields to the form panel. File upload field will be added automatically after
     * specific fields.
     */
    protected abstract void addSpecificFormFields(FormPanel form);

    /**
     * Returns session key defined for given form.
     */
    protected String getSessionKey()
    {
        return sessionKey;
    }

    @Override
    protected final void submitValidForm()
    {
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
    }

    @Override
    protected void resetFieldsAfterSave()
    {
        for (FileUploadField attachmentField : fileFieldsManager.getFields())
        {
            attachmentField.reset();
        }
    }

    private static String createId(String sessionKey)
    {
        return GenericConstants.ID_PREFIX + sessionKey;
    }

    private final void addFormFields()
    {
        addSpecificFormFields(formPanel);
        for (FileUploadField attachmentField : fileFieldsManager.getFields())
        {
            formPanel.add(wrapUnaware((Field<?>) attachmentField).get());
        }
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
                    AbstractExperimentBatchRegistrationForm.this.setUploadEnabled(true);
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
                        if (fileFieldsManager.filesDefined() > 0)
                        {
                            setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            save();
                        }
                    }
                }
            });
    }

}
