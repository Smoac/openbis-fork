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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * A {@link LayoutContainer} extension for registering a new vocabulary.
 * 
 * @author Christian Ribeaud
 */
public final class VocabularyRegistrationForm extends AbstractRegistrationForm
{
    private static final String PREFIX = "vocabulary-registration_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String ID = ID_PREFIX + "form";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final String termsSessionKey;

    private VocabularyRegistrationFieldSet vocabularyRegistrationFieldSet;

    public VocabularyRegistrationForm(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID);
        this.viewContext = viewContext;
        termsSessionKey = ID + "_terms";
        addFields();
        addUploadFeatures();
    }

    private final void addFields()
    {
        formPanel.add(vocabularyRegistrationFieldSet =
                new VocabularyRegistrationFieldSet(viewContext, getId(), labelWidth,
                        fieldWidth - 40, termsSessionKey));
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
                    VocabularyRegistrationForm.this.setUploadEnabled(true);
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

    //
    // AbstractRegistrationForm
    //

    @Override
    protected final void submitValidForm()
    {
        final Vocabulary vocabulary = vocabularyRegistrationFieldSet.createVocabulary();
        viewContext.getService().registerVocabulary(termsSessionKey, vocabulary,
                new VocabularyRegistrationCallback(viewContext, vocabulary));
    }

    //
    // Helper classes
    //

    public final class VocabularyRegistrationCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Void>
    {
        private final Vocabulary vocabulary;

        VocabularyRegistrationCallback(final IViewContext<?> viewContext,
                final Vocabulary vocabulary)
        {
            super(viewContext);
            this.vocabulary = vocabulary;
        }

        @Override
        protected String createSuccessfullRegistrationInfo(Void result)
        {
            return "Vocabulary <b>" + vocabulary.getCode() + "</b> successfully registered.";
        }
    }
}
