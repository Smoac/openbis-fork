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

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DescriptionField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewFilter;

/**
 * {@link Window} containing filter registration form.
 * 
 * @author Izabela Adamczyk
 */
public class AddFilterDialog extends AbstractRegistrationDialog
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final TextField<String> nameFiled;

    private final DescriptionField descriptionField;

    private final MultilineVarcharField expressionField;

    private final CheckBoxField publicField;

    private final String gridId;

    public AddFilterDialog(final IViewContext<ICommonClientServiceAsync> viewContext,
            final IDelegatedAction postRegistrationCallback, String gridId)
    {
        super(viewContext, "Add a new filter", postRegistrationCallback);
        this.viewContext = viewContext;
        this.gridId = gridId;
        addField(nameFiled = createTextField("Name", true));
        addField(descriptionField = createDescriptionField(viewContext, true));
        addField(expressionField = createExpressionField());
        addField(publicField = new CheckBoxField("Public", false));
    }

    private MultilineVarcharField createExpressionField()
    {
        MultilineVarcharField field = new MultilineVarcharField("Expression", true);
        field.setMaxLength(2000);
        return field;
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        NewFilter filter = new NewFilter();
        filter.setDescription(descriptionField.getValue());
        filter.setExpression(expressionField.getValue());
        filter.setGridId(gridId);
        filter.setName(nameFiled.getValue());
        filter.setPublic(publicField.getValue().booleanValue());
        viewContext.getService().registerFilter(filter, registrationCallback);
    }

}
