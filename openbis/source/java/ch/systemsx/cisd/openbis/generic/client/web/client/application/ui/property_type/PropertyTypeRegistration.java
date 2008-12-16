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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DataTypeCode;

/**
 * A {@link LayoutContainer} extension for registering a new property type.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyTypeRegistration extends AbstractRegistrationForm
{
    private static final String PREFIX = "property-type-registration_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private CodeField codeField;

    private DataTypeSelectionWidget dataTypeSelectionWidget;

    private TextArea vocabularyTerms;

    public PropertyTypeRegistration(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID_PREFIX);
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
        formPanel.add(dataTypeSelectionWidget = new DataTypeSelectionWidget(viewContext));
        formPanel.add(vocabularyTerms = createTextArea());
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
                            vocabularyTerms.setVisible(visible);
                            vocabularyTerms.setAllowBlank(!visible);
                            vocabularyTerms.reset();
                        }
                    });
    }

    private final TextArea createTextArea()
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

    //
    // AbstractRegistrationForm
    //

    @Override
    protected final void submitValidForm()
    {

    }
}
