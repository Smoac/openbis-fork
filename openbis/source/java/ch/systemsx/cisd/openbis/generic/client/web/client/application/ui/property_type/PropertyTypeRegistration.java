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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.Validator;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTerm;

/**
 * A {@link LayoutContainer} extension for registering a new property type.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyTypeRegistration extends AbstractRegistrationForm
{
    private static final String PREFIX = "property-type-registration";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private CodeField codeField;

    private VarcharField labelField;

    private VarcharField descriptionField;

    private DataTypeSelectionWidget dataTypeSelectionWidget;

    private TextArea vocabularyTermsField;

    public PropertyTypeRegistration(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID_PREFIX, DEFAULT_LABEL_WIDTH + 20, DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;
        addFields();
    }

    private final static List<String> getTerms(final String value)
    {
        final String[] split = value.split("[,\n\r\t\f ]");
        final List<String> terms = new ArrayList<String>();
        for (final String text : split)
        {
            if (StringUtils.isBlank(text) == false)
            {
                terms.add(text);
            }
        }
        return terms;
    }

    private final void addFields()
    {
        formPanel.add(codeField = new CodeField(viewContext, viewContext.getMessage(Dict.CODE)));
        formPanel.add(labelField = createLabelField());
        formPanel.add(descriptionField = createDescriptionField());
        formPanel.add(dataTypeSelectionWidget = new DataTypeSelectionWidget(viewContext));
        formPanel.add(vocabularyTermsField = createVocabularyTermsTextArea());
        dataTypeSelectionWidget
                .addSelectionChangedListener(new SelectionChangedListener<DataTypeModel>()
                    {

                        //
                        // SelectionChangedListener
                        //

                        @Override
                        public final void selectionChanged(
                                final SelectionChangedEvent<DataTypeModel> se)
                        {
                            final DataTypeModel selectedItem = se.getSelectedItem();
                            final boolean visible;
                            if (selectedItem != null)
                            {
                                visible =
                                        selectedItem.get(ModelDataPropertyNames.CODE).equals(
                                                DataTypeCode.CONTROLLEDVOCABULARY.name());
                            } else
                            {
                                visible = false;
                            }
                            vocabularyTermsField.setVisible(visible);
                            vocabularyTermsField.setAllowBlank(!visible);
                            vocabularyTermsField.reset();
                        }
                    });
    }

    private final VarcharField createDescriptionField()
    {
        final VarcharField varcharField =
                new VarcharField(viewContext.getMessage(Dict.DESCRIPTION), true);
        varcharField.setMaxLength(80);
        return varcharField;
    }

    private final VarcharField createLabelField()
    {
        final VarcharField varcharField =
                new VarcharField(viewContext.getMessage(Dict.LABEL), true);
        varcharField.setMaxLength(40);
        return varcharField;
    }

    private final TextArea createVocabularyTermsTextArea()
    {
        final TextArea textArea = new TextArea();
        final String fieldLabel = viewContext.getMessage(Dict.VOCABULARY_TERMS);
        VarcharField.configureField(textArea, fieldLabel, true);
        textArea.setEmptyText(viewContext.getMessage(Dict.VOCABULARY_TERMS_EMPTY));
        textArea.setVisible(false);
        textArea.setAllowBlank(true);
        textArea.setValidator(new Validator<String, TextArea>()
            {

                //
                // Validator
                //

                public final String validate(final TextArea field, final String value)
                {
                    if (StringUtils.isBlank(value))
                    {
                        return null;
                    }
                    final List<String> terms = getTerms(value);
                    if (terms.size() == 0)
                    {
                        return null;
                    }
                    for (final String term : terms)
                    {
                        if (term.matches(CodeField.CODE_PATTERN) == false)
                        {
                            return viewContext.getMessage(Dict.INVALID_CODE_MESSAGE, "Term '"
                                    + term + "'");
                        }
                    }
                    return null;
                }

            });
        return textArea;
    }

    private final PropertyType createPropertyType()
    {
        final PropertyType propertyType = new PropertyType();
        propertyType.setSimpleCode(codeField.getValue());
        propertyType.setLabel(labelField.getValue());
        propertyType.setDescription(descriptionField.getValue());
        propertyType.setInternalNamespace(false);
        propertyType.setManagedInternally(false);
        if (DataTypeCode.CONTROLLEDVOCABULARY.equals(dataTypeSelectionWidget
                .tryGetSelectedDataType().getCode()))
        {
            final Vocabulary vocabulary = new Vocabulary();
            List<VocabularyTerm> vocabularyTerms = new ArrayList<VocabularyTerm>();
            for (final String termCode : getTerms(vocabularyTermsField.getValue()))
            {
                final VocabularyTerm vocabularyTerm = new VocabularyTerm();
                vocabularyTerm.setCode(termCode);
                vocabularyTerms.add(vocabularyTerm);
            }
            vocabulary.setTerms(vocabularyTerms);
            propertyType.setVocabulary(vocabulary);
        }
        return propertyType;
    }

    //
    // AbstractRegistrationForm
    //

    @Override
    protected final void submitValidForm()
    {
        final PropertyType propertyType = createPropertyType();
        viewContext.getService().registerPropertyType(propertyType,
                new PropertyTypeRegistrationCallback(viewContext, propertyType));
    }

    //
    // Helper classes
    //

    private final class PropertyTypeRegistrationCallback extends AbstractAsyncCallback<Void>
    {
        private final PropertyType propertyType;

        PropertyTypeRegistrationCallback(final IViewContext<?> viewContext,
                final PropertyType propertyType)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
            this.propertyType = propertyType;
        }

        private final String createMessage()
        {
            return "Property type <b>" + propertyType.getSimpleCode()
                    + "</b> successfully registered.";
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
