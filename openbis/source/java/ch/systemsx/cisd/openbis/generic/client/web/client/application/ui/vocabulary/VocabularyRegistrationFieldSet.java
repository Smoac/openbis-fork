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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

/**
 * A {@link FieldSet} extension for registering vocabulary.
 * 
 * @author Christian Ribeaud
 */
public final class VocabularyRegistrationFieldSet extends FieldSet
{
    public static final String ID = "vocabulary_registration_field_set";

    private CodeField vocabularyCodeField;

    private VarcharField vocabularyDescriptionField;

    private CheckBox chosenFromListCheckbox;

    private VocabularyTermsSection vocabularyTermsSection;

    private final IMessageProvider messageProvider;

    private final int labelWidth;

    private final int fieldWidth;

    private final String idPrefix;

    private final String termsSessionKey;

    public VocabularyRegistrationFieldSet(final IMessageProvider messageProvider,
            final String idPrefix, final int labelWidth, final int fieldWidth,
            final String termsSessionKey)
    {
        this.messageProvider = messageProvider;
        this.labelWidth = labelWidth;
        this.fieldWidth = fieldWidth;
        this.idPrefix = idPrefix + ID;
        this.termsSessionKey = termsSessionKey;
        createForm();
    }

    private void createForm()
    {
        setHeading(messageProvider.getMessage(Dict.VOCABULARY));
        setLayout(createFormLayout());
        setWidth(labelWidth + fieldWidth + 40);
        add(vocabularyCodeField = createCodeField());
        add(vocabularyDescriptionField =
                createDescriptionField(messageProvider.getMessage(Dict.DESCRIPTION), false));
        add(chosenFromListCheckbox = createChosenFromListCheckbox());
        createVocabularyTermsSection();
    }

    private void createVocabularyTermsSection()
    {
        vocabularyTermsSection = new VocabularyTermsSection();
    }

    private final FormLayout createFormLayout()
    {
        final FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(labelWidth);
        formLayout.setDefaultWidth(fieldWidth);
        return formLayout;
    }

    private final CodeField createCodeField()
    {
        final CodeField codeField =
                new CodeField(messageProvider, messageProvider.getMessage(Dict.CODE),
                        CodeField.CODE_PATTERN_WITH_DOT_AND_COLON);
        codeField.setId(idPrefix + "_code");
        return codeField;
    }

    private final VarcharField createDescriptionField(final String descriptionLabel,
            final boolean mandatory)
    {
        final VarcharField varcharField = new VarcharField(descriptionLabel, mandatory);
        varcharField.setMaxLength(80);
        varcharField.setId(idPrefix + "_description");
        return varcharField;
    }

    private CheckBox createChosenFromListCheckbox()
    {
        final CheckBox checkBox = new CheckBox();
        checkBox.setId(idPrefix + "_chosen-from-list");
        checkBox.setFieldLabel(messageProvider.getMessage(Dict.CHOSEN_FROM_LIST));
        checkBox.setValue(true);
        return checkBox;
    }

    public final Vocabulary createVocabulary()
    {
        final Vocabulary vocabulary = new Vocabulary();
        vocabulary.setCode(getVocabularyCodeValue());
        vocabulary.setDescription(getDescriptionValue());
        vocabulary.setChosenFromList(getChosenFromListValue());
        vocabularyTermsSection.setValues(vocabulary);
        return vocabulary;
    }

    public boolean isUploadFileDefined()
    {
        return vocabularyTermsSection.isUploadFileDefined();
    }

    private final String getVocabularyCodeValue()
    {
        final String prepend = PropertyType.USER_NAMESPACE_CODE_PREPEND;
        final String value = vocabularyCodeField.getValue();
        if (value.toUpperCase().startsWith(prepend))
        {
            return value;
        }
        return prepend + value;
    }

    private final String getDescriptionValue()
    {
        return StringUtils.trimToNull(vocabularyDescriptionField.getValue());
    }

    private final boolean getChosenFromListValue()
    {
        return chosenFromListCheckbox.getValue();
    }

    //
    // FieldSet
    //

    @Override
    public final void setVisible(final boolean visible)
    {
        super.setVisible(visible);
        FieldUtil.setVisibility(visible, vocabularyCodeField);
        vocabularyTermsSection.setVisible(visible);
    }

    // 
    // Helpers
    // 

    /** Helper class that encapsulate fields that deal with providing vocabulary terms data. */
    private class VocabularyTermsSection
    {
        private Radio freeText;

        private TextArea termsArea;

        private Radio fromFile;

        private VarcharField uriField;

        private FileUploadField uploadFileField;

        public VocabularyTermsSection()
        {
            add(createSourceRadio());
            // freeText
            add(termsArea = createTermsArea());
            // fromFile
            add(uriField = createURIField());
            add(uploadFileField = createImportFileField());
            //
            updateSection();
        }

        public void setVisible(boolean visible)
        {
            FieldUtil.setVisibility(visible, termsArea, uploadFileField);
            if (visible)
            {
                updateSection();
            }
        }

        private final RadioGroup createSourceRadio()
        {
            final RadioGroup result = new RadioGroup();
            result.setSelectionRequired(true);
            result.setFieldLabel(messageProvider.getMessage(Dict.VOCABULARY_TERMS_SOURCE));
            result.setOrientation(Orientation.HORIZONTAL);
            result.addListener(Events.Change, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        updateSection();
                    }
                });

            freeText = createRadio("specified by hand");
            fromFile = createRadio("registered from a file");
            result.add(freeText);
            result.add(fromFile);
            result.setValue(freeText);
            return result;
        }

        private final Radio createRadio(final String label)
        {
            Radio result = new Radio();
            result.setBoxLabel(label);
            return result;
        }

        private VarcharField createURIField()
        {
            return new VarcharField(messageProvider.getMessage(Dict.VOCABULARY_TERMS_SOURCE_URI),
                    false);
        }

        private FileUploadField createImportFileField()
        {
            FileFieldManager fileManager = new FileFieldManager(termsSessionKey, 1, "File");
            fileManager.setMandatory();
            return fileManager.getFields().get(0);
        }

        private final TextArea createTermsArea()
        {
            final TextArea textArea = new TextArea();
            final String fieldLabel = messageProvider.getMessage(Dict.VOCABULARY_TERMS);
            VarcharField.configureField(textArea, fieldLabel, true);
            textArea.setHeight("10em");
            textArea.setId(idPrefix + "_terms");
            textArea.setEmptyText(messageProvider.getMessage(Dict.VOCABULARY_TERMS_EMPTY));
            textArea.setValidator(new VocabularyTermValidator(messageProvider));
            return textArea;
        }

        private void updateSection()
        {
            Boolean useFreeText = freeText.getValue();
            FieldUtil.setVisibility(useFreeText, termsArea);
            FieldUtil.setVisibility(useFreeText == false, uriField, uploadFileField);
        }

        public void setValues(Vocabulary vocabulary)
        {
            vocabulary.setUploadedFromFile(fromFile.getValue());
            // from file
            vocabulary.setSourceURI(getURIValue());
            // free text
            final List<VocabularyTerm> vocabularyTerms = new ArrayList<VocabularyTerm>();
            for (final String termCode : VocabularyTermValidator.getTerms(getTermsAreaValue()))
            {
                final VocabularyTerm vocabularyTerm = new VocabularyTerm();
                vocabularyTerm.setCode(termCode);
                vocabularyTerms.add(vocabularyTerm);
            }
            vocabulary.setTerms(vocabularyTerms);
        }

        public boolean isUploadFileDefined()
        {
            return uploadFileField.isVisible() ? StringUtils.isBlank(uploadFileField.getValue()
                    .toString()) == false : false;
        }

        private String getTermsAreaValue()
        {
            return termsArea.isVisible() ? termsArea.getValue() : null;
        }

        private String getURIValue()
        {
            return uriField.isVisible() ? uriField.getValue() : null;
        }
    }

}
