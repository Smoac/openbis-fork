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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog.createTextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.ExpressionUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.IReportInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewQuery;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Constants;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
public class QueryEditor extends Dialog
{
    public static final String ID = Constants.QUERY_ID_PREFIX + "_query_editor";

    private static final FormData FORM_DATA = new FormData("100%");

    private static Button createCancelButton(IViewContext<?> viewContext, final Window window)
    {
        return new Button(viewContext.getMessage(Dict.BUTTON_CANCEL),
                new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public final void componentSelected(ButtonEvent ce)
                        {
                            window.hide();
                        }
                    });
    }

    private static interface QueryExecutor
    {
        public void execute(QueryParameterBindings parameterBindings);
    }

    private static final class BindingsDialog extends Dialog
    {
        private final Map<String, TextField<String>> parameterFields;

        private final QueryExecutor queryExecutor;

        public BindingsDialog(IViewContext<IQueryClientServiceAsync> viewContext,
                List<String> parameters, QueryExecutor queryExecutor)
        {
            this.queryExecutor = queryExecutor;
            setHeading(viewContext.getMessage(Dict.QUERY_PARAMETERS_BINDINGS_DIALOG_TITLE));
            setModal(true);
            setScrollMode(Scroll.AUTO);
            setHideOnButtonClick(true);
            setButtons("");
            final FormPanel form = new FormPanel();
            form.setHeaderVisible(false);
            form.setBorders(false);
            form.setBodyBorder(false);
            form.setLabelWidth(150);
            form.setFieldWidth(250);

            parameterFields = new HashMap<String, TextField<String>>();
            for (String parameter : parameters)
            {
                TextField<String> field = createTextField(parameter, true);
                parameterFields.put(parameter, field);
                form.add(field);
            }
            add(form, new BorderLayoutData(LayoutRegion.CENTER));
            addButton(new Button(viewContext.getMessage(Dict.BUTTON_SUBMIT),
                    new SelectionListener<ButtonEvent>()
                        {
                            @Override
                            public final void componentSelected(ButtonEvent ce)
                            {
                                if (form.isValid())
                                {
                                    prepareBindingsAndExecuteQuery();
                                    hide();
                                }
                            }
                        }));
            addButton(createCancelButton(viewContext, this));
            setWidth(500);
        }

        private void prepareBindingsAndExecuteQuery()
        {
            QueryParameterBindings bindings = new QueryParameterBindings();
            for (Map.Entry<String, TextField<String>> entry : parameterFields.entrySet())
            {
                bindings.addBinding(entry.getKey(), entry.getValue().getValue());
            }
            queryExecutor.execute(bindings);
        }
    }

    private final IViewContext<IQueryClientServiceAsync> viewContext;

    private final TextField<String> nameField;

    private final TextField<String> descriptionField;

    private final MultilineVarcharField statementField;

    private final CheckBoxField isPublicField;

    private final QueryExpression queryOrNull;

    private final int parentHeight;

    private final SimpleComboBox<QueryType> queryTypeField;

    public QueryEditor(final IViewContext<IQueryClientServiceAsync> viewContext,
            QueryExpression queryOrNull, IDelegatedAction refreshAction, int parentWidth,
            int parentHeight)
    {
        this.viewContext = viewContext;
        this.queryOrNull = queryOrNull;
        this.parentHeight = parentHeight;
        setHeading(viewContext.getMessage(queryOrNull == null ? Dict.QUERY_CREATE_TITLE
                : Dict.QUERY_EDIT_TITLE));
        setModal(true);
        setLayout(new FitLayout());
        setButtons("");
        FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBorders(true);
        form.setBodyBorder(false);
        nameField =
                AbstractRegistrationDialog.createTextField(viewContext.getMessage(Dict.NAME), true);
        nameField.setMaxLength(200);
        descriptionField =
                AbstractRegistrationDialog.createTextField(
                        viewContext.getMessage(Dict.DESCRIPTION), false);
        descriptionField.setMaxLength(GenericConstants.DESCRIPTION_2000);
        statementField = createStatementField();
        isPublicField = new CheckBoxField(viewContext.getMessage(Dict.IS_PUBLIC), false);
        queryTypeField = new QueryTypeComboBox(viewContext);
        if (queryOrNull != null)
        {
            nameField.setValue(queryOrNull.getName());
            descriptionField.setValue(StringEscapeUtils.unescapeHtml(queryOrNull.getDescription()));
            statementField.setValue(StringEscapeUtils.unescapeHtml(queryOrNull.getExpression()));
            isPublicField.setValue(queryOrNull.isPublic());
            queryTypeField.setSimpleValue(queryOrNull.getQueryType());
        }
        form.add(nameField, FORM_DATA);
        form.add(queryTypeField, FORM_DATA);
        form.add(descriptionField, FORM_DATA);
        form.add(statementField, FORM_DATA);
        form.add(isPublicField);
        form.setPadding(20);
        setTopComponent(form);

        addButton(createSaveButton(form, refreshAction));
        addButton(createTestButton(form));
        addButton(createCancelButton(viewContext, this));

        setPosition(5, 70);
        setWidth(parentWidth);
    }

    static class QueryTypeComboBox extends SimpleComboBox<QueryType>
    {
        public QueryTypeComboBox(IMessageProvider messages)
        {
            setAllowBlank(false);
            setEditable(false);
            setTriggerAction(TriggerAction.ALL);
            setFieldLabel(messages.getMessage(Dict.QUERY_TYPE));
            for (QueryType qt : QueryType.values())
            {
                add(qt);
            }
            setSimpleValue(QueryType.GENERIC);
        }
    }

    private MultilineVarcharField createStatementField()
    {
        MultilineVarcharField field = new SQLQueryField(viewContext, true, 10);
        field.setMaxLength(2000);
        return field;
    }

    private Button createSaveButton(final FormPanel form, final IDelegatedAction refreshAction)
    {
        final Button button =
                new Button(viewContext.getMessage(Dict.BUTTON_SAVE),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public final void componentSelected(final ButtonEvent ce)
                                {
                                    if (form.isValid())
                                    {
                                        register(new AbstractAsyncCallback<Void>(viewContext)
                                            {

                                                @Override
                                                protected void process(Void result)
                                                {
                                                    hide();
                                                    refreshAction.execute();
                                                }
                                            });
                                        hide();
                                    }
                                }
                            });
        return button;
    }

    private Button createTestButton(final FormPanel form)
    {
        Button testButton = new Button(viewContext.getMessage(Dict.BUTTON_TEST_QUERY));
        testButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    if (form.isValid())
                    {
                        List<String> parameters =
                                ExpressionUtil.extractParameters(statementField.getValue());
                        parameters = ExpressionUtil.createDistinctParametersList(parameters);
                        if (parameters.size() > 0)
                        {
                            new BindingsDialog(viewContext, parameters, new QueryExecutor()
                                {
                                    public void execute(QueryParameterBindings parameterBindings)
                                    {
                                        runQuery(parameterBindings);
                                    }
                                }).show();
                        } else
                        {
                            runQuery(new QueryParameterBindings());
                        }
                    }
                }

            });
        return testButton;
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
    }

    protected void register(AsyncCallback<Void> registrationCallback)
    {
        if (queryOrNull == null)
        {
            NewQuery query = new NewQuery();
            query.setName(nameField.getValue());
            query.setDescription(descriptionField.getValue());
            query.setExpression(statementField.getValue());
            query.setPublic(isPublicField.getValue());
            query.setQueryType(queryTypeField.getSimpleValue());
            viewContext.getService().registerQuery(query, registrationCallback);
        } else
        {
            queryOrNull.setName(nameField.getValue());
            queryOrNull.setDescription(descriptionField.getValue());
            queryOrNull.setExpression(statementField.getValue());
            queryOrNull.setPublic(isPublicField.getValue());
            queryOrNull.setQueryType(queryTypeField.getSimpleValue());
            viewContext.getService().updateQuery(queryOrNull, registrationCallback);
        }
    }

    private void runQuery(QueryParameterBindings parameterBindings)
    {
        String sqlStatement = statementField.getValue();
        if (sqlStatement != null && sqlStatement.length() > 0)
        {
            viewContext.getService().createQueryResultsReport(
                    sqlStatement,
                    parameterBindings,
                    ReportGeneratedCallback.create(viewContext.getCommonViewContext(),
                            createReportInformationProvider(sqlStatement),
                            createDisplayQueryResultsAction()));
        }
    }

    private IReportInformationProvider createReportInformationProvider(final String sqlQuery)
    {
        return new IReportInformationProvider()
            {
                public String getDownloadURL()
                {
                    return null;
                }

                public String getKey()
                {
                    return Integer.toString(sqlQuery.hashCode());
                }
            };
    }

    private IOnReportComponentGeneratedAction createDisplayQueryResultsAction()
    {
        return new IOnReportComponentGeneratedAction()
            {
                public void execute(final IDisposableComponent reportComponent)
                {
                    removeAll();
                    add(reportComponent.getComponent());
                    if (getHeight() < parentHeight)
                    {
                        setHeight(parentHeight);
                    }
                    layout();
                }
            };
    }
}
