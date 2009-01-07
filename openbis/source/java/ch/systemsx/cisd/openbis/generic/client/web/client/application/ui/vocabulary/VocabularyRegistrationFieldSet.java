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

import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTerm;

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

    private TextArea vocabularyTermsField;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final int labelWidth;

    private final int fieldWith;

    private final String idPrefix;

    public VocabularyRegistrationFieldSet(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String idPrefix,
            final int labelWidth, final int fieldWidth)
    {
        this.viewContext = viewContext;
        this.labelWidth = labelWidth;
        this.fieldWith = fieldWidth;
        this.idPrefix = idPrefix + ID;
        setHeading(viewContext.getMessage(Dict.VOCABULARY));
        setLayout(createFormLayout());
        setWidth(labelWidth + fieldWidth + 40);
        add(vocabularyCodeField = createCodeField());
        add(vocabularyDescriptionField =
                createDescriptionField(viewContext.getMessage(Dict.DESCRIPTION), false));
        add(vocabularyTermsField = createVocabularyTermsField());
    }

    private final FormLayout createFormLayout()
    {
        final FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(labelWidth);
        formLayout.setDefaultWidth(fieldWith);
        return formLayout;
    }

    private final CodeField createCodeField()
    {
        final CodeField codeField =
                new CodeField(viewContext, viewContext.getMessage(Dict.CODE),
                        CodeField.CODE_PATTERN_WITH_DOT);
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

    private final TextArea createVocabularyTermsField()
    {
        final TextArea textArea = new TextArea();
        final String fieldLabel = viewContext.getMessage(Dict.VOCABULARY_TERMS);
        VarcharField.configureField(textArea, fieldLabel, true);
        textArea.setId(idPrefix + "_terms");
        textArea.setEmptyText(viewContext.getMessage(Dict.VOCABULARY_TERMS_EMPTY));
        textArea.setValidator(new VocabularyTermValidator(viewContext));
        return textArea;
    }

    private final String getVocabularyCode()
    {
        final String prepend = PropertyType.USER_NAMESPACE_CODE_PREPEND;
        final String value = vocabularyCodeField.getValue();
        if (value.toUpperCase().startsWith(prepend))
        {
            return value;
        }
        return prepend + value;
    }

    public final Vocabulary createVocabulary()
    {
        final Vocabulary vocabulary = new Vocabulary();
        vocabulary.setCode(getVocabularyCode());
        vocabulary.setDescription(StringUtils.trimToNull(vocabularyDescriptionField.getValue()));
        final List<VocabularyTerm> vocabularyTerms = new ArrayList<VocabularyTerm>();
        for (final String termCode : VocabularyTermValidator.getTerms(vocabularyTermsField
                .getValue()))
        {
            final VocabularyTerm vocabularyTerm = new VocabularyTerm();
            vocabularyTerm.setCode(termCode);
            vocabularyTerms.add(vocabularyTerm);
        }
        vocabulary.setTerms(vocabularyTerms);
        return vocabulary;
    }

    //
    // FieldSet
    //

    @Override
    public final void setVisible(final boolean visible)
    {
        vocabularyCodeField.setVisible(visible);
        vocabularyCodeField.setAllowBlank(!visible);
        vocabularyCodeField.reset();
        vocabularyDescriptionField.setVisible(visible);
        vocabularyDescriptionField.reset();
        vocabularyTermsField.setVisible(visible);
        vocabularyTermsField.setAllowBlank(!visible);
        vocabularyTermsField.reset();
        super.setVisible(visible);
    }
}
