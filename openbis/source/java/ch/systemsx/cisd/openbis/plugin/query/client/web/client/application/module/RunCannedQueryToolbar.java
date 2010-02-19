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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * The toolbar of query viewer for running predefined queries.
 * 
 * @author Piotr Buczek
 */
public class RunCannedQueryToolbar extends AbstractCustomQueryToolbar
{

    // 6 parameter fields fit into browser with 1024px width
    private final static int MAX_PARAMETER_COLUMNS = 6;

    private final ContentPanel parameterContainer;

    private final QuerySelectionWidget querySelectionWidget;

    private final Button resetButton;

    private final Map<String, TextField<String>> parameterFields;

    public RunCannedQueryToolbar(final IViewContext<IQueryClientServiceAsync> viewContext)
    {
        super(viewContext);
        add(new LabelToolItem(viewContext.getMessage(Dict.QUERY) + ": "));
        querySelectionWidget = new QuerySelectionWidget(viewContext);
        parameterContainer = new ButtonGroup(MAX_PARAMETER_COLUMNS);
        parameterFields = new HashMap<String, TextField<String>>();
        resetButton = new Button(viewContext.getMessage(Dict.BUTTON_RESET));
        add(querySelectionWidget);
        add(parameterContainer);
        add(executeButton);
        add(resetButton);

        querySelectionWidget.addSelectionChangedListener(new SelectionChangedListener<QueryModel>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<QueryModel> se)
                {
                    updateParameterFields();
                    tryExecuteQuery();
                }

            });
        querySelectionWidget.addPostRefreshCallback(new IDataRefreshCallback()
            {
                public void postRefresh(boolean wasSuccessful)
                {
                    updateParameterFields();
                    // TODO 2009-11-24, Tomasz Pylak: IMPR this apply is usually unnecessary and
                    // causes screen flickering
                    tryExecuteQuery();
                }

            });
        resetButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    resetParameterFields();
                    tryExecuteQuery();
                }
            });
    }

    protected void updateParameterFields()
    {
        // Only show the filter selection widget if there are user choices
        boolean queriesAvailable = querySelectionWidget.getStore().getCount() > 0;
        setEnabled(queriesAvailable);

        executeButton.hide();

        parameterContainer.removeAll();
        QueryExpression queryOrNull = querySelectionWidget.tryGetSelected();
        if (queryOrNull != null)
        {
            createAndAddQueryParameterFields(queryOrNull);
            executeButton.show();
            updateExecuteButtonEnabledState();
        }

        boolean parametersAvailable = parameterContainer.getItemCount() > 0;
        resetButton.setVisible(parametersAvailable);
        parameterContainer.setVisible(parametersAvailable);
    }

    private void createAndAddQueryParameterFields(QueryExpression query)
    {
        parameterContainer.hide();
        for (String parameter : query.getParameters())
        {
            TextField<String> parameterField = createParameterField(parameter);
            parameterFields.put(parameter, parameterField);
            parameterContainer.add(parameterField);
        }
    }

    private static TextField<String> createParameterField(String parameterName)
    {
        TextField<String> result = new VarcharField(parameterName, true);
        result.setEmptyText(parameterName);
        result.setToolTip(parameterName);
        return result;
    }

    private void updateExecuteButtonEnabledState()
    {
        executeButton.setEnabled(isQueryValid());
    }

    private void resetParameterFields()
    {
        for (TextField<String> field : parameterFields.values())
        {
            field.reset();
        }
    }

    @Override
    protected boolean isQueryValid()
    {
        if (querySelectionWidget.isValid() == false)
        {
            return false;
        } else
        {
            boolean valid = true;
            for (TextField<String> field : parameterFields.values())
            {
                valid = field.isValid() && valid;
            }
            return valid;
        }
    }

    //
    // ICustomQueryProvider
    //

    public String tryGetSQLQuery()
    {
        QueryExpression selectedQueryOrNull = querySelectionWidget.tryGetSelected();
        return selectedQueryOrNull == null ? null : selectedQueryOrNull.getExpression();
    }

    public QueryParameterBindings tryGetQueryParameterBindings()
    {
        QueryParameterBindings bindings = new QueryParameterBindings();
        for (Map.Entry<String, TextField<String>> entry : parameterFields.entrySet())
        {
            bindings.addBinding(entry.getKey(), entry.getValue().getValue());
        }
        return bindings;
    }

    // IDatabaseModificationObserver

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return querySelectionWidget.getRelevantModifications();
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        querySelectionWidget.update(observedModifications);
    }

}
