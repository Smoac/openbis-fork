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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter;

import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;

/**
 * A {@link Window} extension for registering and editing grid custom filters or columns.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
abstract public class AbstractGridCustomExpressionEditRegisterDialog extends
        AbstractRegistrationDialog
{

    public static int FIELD_WIDTH = 400;

    public static int LABEL_WIDTH = 100;

    public static final String PUBLIC_FIELD = "public-field";

    public static final String EXPRESSION_FIELD = "expression-field";

    public static final String DESCRIPTION_FIELD = "description-field";

    public static final String NAME_FIELD = "name-field";

    public static final String INSERT_COLUMNS_LINK = "insert-columns-link";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final TextField<String> nameField;

    private final DescriptionField descriptionField;

    private final MultilineVarcharField expressionField;

    private final CheckBoxField publicField;

    private final LabelField insertColumnsLink;

    protected final String gridId;

    public AbstractGridCustomExpressionEditRegisterDialog(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String title,
            final IDelegatedAction postRegistrationCallback, final String gridId,
            final List<ColumnDataModel> columnModels)
    {
        super(viewContext, title, postRegistrationCallback);
        this.viewContext = viewContext;
        this.gridId = gridId;
        addField(nameField = createTextField(viewContext.getMessage(Dict.NAME), true));
        nameField.setId(createId(gridId, NAME_FIELD));
        addField(descriptionField = createDescriptionField(viewContext, true));
        descriptionField.setId(createId(gridId, DESCRIPTION_FIELD));
        addField(expressionField = createExpressionField());
        expressionField.setId(createId(gridId, EXPRESSION_FIELD));
        addField(insertColumnsLink =
                createInsertColumnsLink(viewContext.getMessage(Dict.INSERT_COLUMNS), columnModels));
        insertColumnsLink.setId(createId(gridId, INSERT_COLUMNS_LINK));
        addField(publicField = new CheckBoxField(viewContext.getMessage(Dict.IS_PUBLIC), false));
        publicField.setId(createId(gridId, PUBLIC_FIELD));

        form.setLabelWidth(LABEL_WIDTH);
        form.setFieldWidth(FIELD_WIDTH);
        setWidth(form.getLabelWidth() + form.getFieldWidth() + 50);
    }

    private LabelField createInsertColumnsLink(final String label,
            final List<ColumnDataModel> columnModels)
    {
        LabelField result = new LabelField(LinkRenderer.renderAsLink(label));
        result.sinkEvents(Event.ONCLICK);
        result.addListener(Event.ONCLICK, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    FilterColumnChooserDialog.show(viewContext, columnModels, gridId,
                            asExpressionHolder(expressionField));
                }
            });
        return result;
    }

    public static String createId(String gridId, String suffix)
    {
        return GenericConstants.ID_PREFIX + "filter-edit-register-" + gridId + suffix;
    }

    private MultilineVarcharField createExpressionField()
    {
        MultilineVarcharField field =
                new MultilineVarcharField(viewContext.getMessage(Dict.EXPRESSION), true, 10);
        field.setMaxLength(2000);
        return field;
    }

    protected final void initializeDescription(String description)
    {
        descriptionField.setValue(StringEscapeUtils.unescapeHtml(description));
    }

    protected final void initializeExpression(String expression)
    {
        expressionField.setValue(StringEscapeUtils.unescapeHtml(expression));
    }

    protected final void initializeName(String name)
    {
        nameField.setValue(StringEscapeUtils.unescapeHtml(name));
    }

    protected final void initializePublic(boolean isPublic)
    {
        publicField.setValue(isPublic);
    }

    protected final String extractDescription()
    {
        return descriptionField.getValue();
    }

    protected final String extractExpression()
    {
        return expressionField.getValue();
    }

    protected final String extractName()
    {
        return nameField.getValue();
    }

    protected final boolean extractIsPublic()
    {
        return publicField.getValue();
    }

    private static final IExpressionHolder asExpressionHolder(
            final MultilineVarcharField expressionField)
    {
        return new IExpressionHolder()
            {

                public int getCursorPos()
                {
                    return expressionField.getCursorPos();
                }

                public String getValue()
                {
                    return expressionField.getValue();
                }

                public void setCursorPos(int position)
                {
                    expressionField.setCursorPos(position);
                }

                public void setValue(String value)
                {
                    expressionField.setValue(value);
                }
            };
    }
}
