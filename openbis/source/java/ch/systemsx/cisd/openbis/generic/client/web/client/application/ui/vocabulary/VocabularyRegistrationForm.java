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

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Vocabulary;

/**
 * A {@link LayoutContainer} extension for registering a new vocabulary.
 * 
 * @author Christian Ribeaud
 */
public final class VocabularyRegistrationForm extends AbstractRegistrationForm
{
    private static final String PREFIX = "vocabulary-registration_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private VocabularyRegistrationFieldSet vocabularyRegistrationFieldSet;

    public VocabularyRegistrationForm(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID_PREFIX);
        this.viewContext = viewContext;
        addFields();
    }

    private final void addFields()
    {
        formPanel.add(vocabularyRegistrationFieldSet =
                new VocabularyRegistrationFieldSet(viewContext, getId() + "_", labelWidth,
                        fieldWitdh - 40));
    }

    //
    // AbstractRegistrationForm
    //

    @Override
    protected final void submitValidForm()
    {
        final Vocabulary vocabulary = vocabularyRegistrationFieldSet.createVocabulary();
        viewContext.getService().registerVocabulary(vocabulary,
                new VocabularyRegistrationCallback(viewContext, vocabulary));
    }

    //
    // Helper classes
    //

    public final class VocabularyRegistrationCallback extends AbstractAsyncCallback<Void>
    {
        private final Vocabulary vocabulary;

        VocabularyRegistrationCallback(final IViewContext<?> viewContext,
                final Vocabulary vocabulary)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
            this.vocabulary = vocabulary;
        }

        private final String createMessage()
        {
            return "Vocabulary <b>" + vocabulary.getCode() + "</b> successfully registered.";
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createMessage());
            formPanel.reset();
        }
    }
}
