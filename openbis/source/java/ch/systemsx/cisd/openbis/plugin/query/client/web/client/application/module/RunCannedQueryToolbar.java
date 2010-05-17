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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonGroup;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ParameterField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.QueryParameterValue;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryDatabase;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * The toolbar of query viewer for running predefined queries.
 * 
 * @author Piotr Buczek
 */
public class RunCannedQueryToolbar extends AbstractQueryProviderToolbar
{

    // 6 parameter fields fit into browser with 1024px width
    private static final int MAX_PARAMETER_COLUMNS = 6;

    private static final String INITIAL_PARAMETER_NAME_PREFIX = "_";

    private final ContentPanel parameterContainer;

    private final QuerySelectionWidget querySelectionWidget;

    private final Button resetButton;

    private final Collection<ParameterField> parameterFields;

    // <name, value> where name starts with additional INITIAL_PARAMETER_NAME_PREFIX
    private final Map<String, QueryParameterValue> initialParameterValues;

    private final Map<String, String> initialFixedParameters;

    public RunCannedQueryToolbar(final IViewContext<IQueryClientServiceAsync> viewContext,
            QueryType queryType)
    {
        this(viewContext, null, new HashMap<String, QueryParameterValue>(0), queryType);
    }

    public RunCannedQueryToolbar(IViewContext<IQueryClientServiceAsync> viewContext,
            String initialQueryNameOrNull, Map<String, QueryParameterValue> initialParameterValues,
            QueryType queryType)
    {
        super(viewContext);
        this.initialParameterValues = initialParameterValues;
        initialFixedParameters = new HashMap<String, String>();
        querySelectionWidget =
                new QuerySelectionWidget(viewContext, initialQueryNameOrNull, queryType);
        parameterContainer = new ButtonGroup(MAX_PARAMETER_COLUMNS);
        parameterFields = new HashSet<ParameterField>();
        resetButton = new Button(viewContext.getMessage(Dict.BUTTON_RESET));
        add(new LabelToolItem(viewContext.getMessage(Dict.QUERY) + ": "));
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

        removeAllParameterFields();
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

        layout();
    }

    private void createAndAddQueryParameterFields(QueryExpression query)
    {
        parameterContainer.hide();
        IDelegatedAction updateExecuteButtonAction = new IDelegatedAction()
            {
                public void execute()
                {
                    updateExecuteButtonEnabledState();
                }
            };
        for (String parameter : query.getParameters())
        {
            final QueryParameterValue initialValueOrNull = tryGetInitialValue(parameter);
            if (initialValueOrNull != null && initialValueOrNull.isFixed())
            {
                addInitialBinding(parameter, initialValueOrNull.getValue());
            } else
            {
                addParameterField(new ParameterField(parameter, updateExecuteButtonAction,
                        initialValueOrNull == null ? null : initialValueOrNull.getValue()));
            }
        }
    }

    private void addInitialBinding(String parameter, String value)
    {

        initialFixedParameters.put(parameter, value);
    }

    private QueryParameterValue tryGetInitialValue(String parameter)
    {
        return initialParameterValues.get(INITIAL_PARAMETER_NAME_PREFIX + parameter);
    }

    private void addParameterField(ParameterField parameterField)
    {
        parameterFields.add(parameterField);
        parameterContainer.add(parameterField);
    }

    private void removeAllParameterFields()
    {
        parameterContainer.removeAll();
        parameterFields.clear();
    }

    private void updateExecuteButtonEnabledState()
    {
        executeButton.setEnabled(isQueryValid());
    }

    private void resetParameterFields()
    {
        for (ParameterField field : parameterFields)
        {
            field.reset();
        }
        updateExecuteButtonEnabledState();
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
            for (ParameterField field : parameterFields)
            {
                valid = field.isValid() && valid;
            }
            return valid;
        }
    }

    //
    // ICustomQueryProvider
    //

    public Long tryGetQueryId()
    {
        QueryExpression selectedQueryOrNull = querySelectionWidget.tryGetSelected();
        return selectedQueryOrNull == null ? null : selectedQueryOrNull.getId();
    }

    public String tryGetSQLQuery()
    {
        QueryExpression selectedQueryOrNull = querySelectionWidget.tryGetSelected();
        return selectedQueryOrNull == null ? null : selectedQueryOrNull.getExpression();
    }

    public QueryDatabase tryGetQueryDatabase()
    {
        QueryExpression selectedQueryOrNull = querySelectionWidget.tryGetSelected();
        return selectedQueryOrNull == null ? null : selectedQueryOrNull.getQueryDatabase();
    }

    public QueryParameterBindings tryGetQueryParameterBindings()
    {
        QueryParameterBindings bindings = new QueryParameterBindings();
        for (String key : initialFixedParameters.keySet())
        {
            bindings.addBinding(key, initialFixedParameters.get(key));
        }
        for (ParameterField field : parameterFields)
        {
            ParameterWithValue parameterWithValue = field.getParameterWithValue();
            bindings.addBinding(parameterWithValue.getParameter(), parameterWithValue.getValue());
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
