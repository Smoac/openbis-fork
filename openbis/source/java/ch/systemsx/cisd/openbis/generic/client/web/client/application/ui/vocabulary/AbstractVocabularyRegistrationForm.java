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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;

/**
 * An abstract {@link LayoutContainer} extension for registering a new vocabulary.
 * 
 * @author Piotr Buczek
 * @author Christian Ribeaud
 */
public abstract class AbstractVocabularyRegistrationForm extends AbstractRegistrationForm
{
    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    protected final String termsSessionKey;

    protected final VocabularyRegistrationFieldSet vocabularyRegistrationFieldSet;

    public AbstractVocabularyRegistrationForm(
            final IViewContext<ICommonClientServiceAsync> viewContext, String id)
    {
        super(viewContext, id);
        this.viewContext = viewContext;
        termsSessionKey = id + "_terms";
        this.vocabularyRegistrationFieldSet = createVocabularySelectionWidget();
        addUploadFeatures();
    }

    private final VocabularyRegistrationFieldSet createVocabularySelectionWidget()
    {
        return new VocabularyRegistrationFieldSet(viewContext, getId(), labelWidth,
                fieldWidth - 40, termsSessionKey);
    }

    private void addUploadFeatures()
    {
        addFormSubmitListener();
        redefineSaveListeners();
        addUploadFeatures(termsSessionKey);
    }

    private void addFormSubmitListener()
    {
        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    submitValidForm();
                }

                @Override
                protected void setUploadEnabled()
                {
                    AbstractVocabularyRegistrationForm.this.setUploadEnabled(true);
                }
            });
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
                        if (vocabularyRegistrationFieldSet.isUploadFileDefined())
                        {
                            setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            submitValidForm();
                        }
                    }
                }
            });
    }

}
