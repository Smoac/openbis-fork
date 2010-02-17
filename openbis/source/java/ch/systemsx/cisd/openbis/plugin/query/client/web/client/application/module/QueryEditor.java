/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExpression;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Constants;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class QueryEditor extends AbstractRegistrationDialog
{
    public static final String ID = Constants.QUERY_ID_PREFIX + "_query_editor";

    private final IViewContext<IQueryClientServiceAsync> viewContext;
    private final TextField<String> nameField;
    private final TextField<String> descriptionField;
    private final MultilineVarcharField statementField;
    private final CheckBoxField isPublicField;
    
    public QueryEditor(IViewContext<IQueryClientServiceAsync> viewContext, IDelegatedAction refreshAction)
    {
        super(viewContext, "edit", refreshAction);
        this.viewContext = viewContext;
        setLayout(new FitLayout());
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        nameField = AbstractRegistrationDialog.createTextField(viewContext.getMessage(Dict.NAME), true);
        nameField.setMaxLength(200);
        form.add(nameField, new FormData("100%"));
        descriptionField = AbstractRegistrationDialog.createTextField(viewContext.getMessage(Dict.DESCRIPTION), false);
        descriptionField.setMaxLength(GenericConstants.DESCRIPTION_2000);
        form.add(descriptionField, new FormData("100%"));
        statementField = createStatementField();
        form.add(statementField, new FormData("100%"));
        isPublicField = new CheckBoxField(viewContext.getMessage(Dict.IS_PUBLIC), false);
        form.add(isPublicField);
        setWidth("100%");
        setHeight(500);
        Button testButton = new Button(viewContext.getMessage(Dict.BUTTON_TEST_QUERY));
        testButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    System.out.println("test");
                }
            });
        addButton(testButton);
    }
    
    private MultilineVarcharField createStatementField()
    {
        MultilineVarcharField field =
                new MultilineVarcharField(viewContext.getMessage(Dict.SQL_STATEMENT), true, 15);
        field.setMaxLength(2000);
        return field;
    }
    
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        NewExpression query = new NewExpression();
        query.setName(nameField.getValue());
        query.setDescription(descriptionField.getValue());
        query.setExpression(statementField.getValue());
        query.setPublic(isPublicField.isValid());
        viewContext.getService().registerQuery(query, registrationCallback);
    }

}
