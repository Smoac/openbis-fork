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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * A {@link Window} extension for registering and editing filters.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
abstract public class AbstractFilterEditRegisterDialog extends AbstractRegistrationDialog
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final TextField<String> nameField;

    private final DescriptionField descriptionField;

    private final MultilineVarcharField expressionField;

    private final CheckBoxField publicField;

    protected final String gridId;

    private final List<ColumnDataModel> columnModels;

    public AbstractFilterEditRegisterDialog(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String title,
            final IDelegatedAction postRegistrationCallback, String gridId,
            List<ColumnDataModel> columnModels)
    {
        super(viewContext, title, postRegistrationCallback);
        this.viewContext = viewContext;
        this.gridId = gridId;
        this.columnModels = columnModels;
        addField(nameField = createTextField(viewContext.getMessage(Dict.NAME), true));
        addField(descriptionField = createDescriptionField(viewContext, true));
        addField(expressionField = createExpressionField());
        addField(publicField = new CheckBoxField(viewContext.getMessage(Dict.IS_PUBLIC), false));
        setBottomComponent(new BottomToolbar());
        setWidth(500);
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
        descriptionField.setValue(description);
    }

    protected final void initializeExpression(String expression)
    {
        expressionField.setValue(expression);
    }

    protected final void initializeName(String name)
    {
        nameField.setValue(name);
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

    class BottomToolbar extends ToolBar
    {

        public BottomToolbar()
        {
            add(new FillToolItem());
            Button button = new Button("Available Columns");
            button.addSelectionListener(new SelectionListener<ComponentEvent>()
                {

                    @Override
                    public void componentSelected(ComponentEvent ce)
                    {
                        FilterColumnChooserDialog.show(viewContext, columnModels, gridId);
                    }
                });
            add(new AdapterToolItem(button));
        }
    }

}
