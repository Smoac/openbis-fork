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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ParameterValueModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ParameterValue;

/**
 * {@link TriggerField} extension for providing values for parameters.
 * 
 * @author Piotr Buczek
 */
public class ParameterField extends TriggerField<ModelData> implements IParameterField
{

    private static String PARAMETER_NAME_SEPARATOR = "::";

    private static String ENUM_LIST_EXPRESSION_PREFIX = "list=";

    private static String ENUM_LIST_SEPARATOR = ",";

    private static String QUERY_LIST_EXPRESSION_PREFIX = "query=";

    private final String parameterName;

    private final IDelegatedAction onValueChangeAction;

    private final String initialValueOrNull;

    public static IParameterField create(IViewContext<?> viewContextOrNull, String parameterName,
            IDelegatedAction onValueChangeAction, String initialValueOrNull,
            IParameterValuesLoader loaderOrNull)
    {
        String[] split = parameterName.split(PARAMETER_NAME_SEPARATOR);
        if (split.length == 2)
        {
            String namePart = split[0];
            String expressionPart = split[1];
            if (expressionPart.startsWith(ENUM_LIST_EXPRESSION_PREFIX))
            {
                String itemList = expressionPart.substring(ENUM_LIST_EXPRESSION_PREFIX.length());
                String[] values = itemList.split(ENUM_LIST_SEPARATOR);
                return createSelectionField(namePart, Arrays.asList(values), initialValueOrNull,
                        onValueChangeAction);
            } else if (expressionPart.startsWith(QUERY_LIST_EXPRESSION_PREFIX))
            {
                String queryExpression =
                        expressionPart.substring(QUERY_LIST_EXPRESSION_PREFIX.length());
                return createSelectionField(viewContextOrNull, namePart, queryExpression,
                        loaderOrNull, initialValueOrNull, onValueChangeAction);
            } else
            {
                MessageBox.alert("Error", "Filter parameter '" + namePart
                        + "' is not defined properly.", null);
                return new ParameterField(namePart, onValueChangeAction, initialValueOrNull);
            }
        }

        return new ParameterField(parameterName, onValueChangeAction, initialValueOrNull);
    }

    private static IParameterField createSelectionField(IViewContext<?> viewContextOrNull,
            String parameterName, String queryExpression, IParameterValuesLoader loaderOrNull,
            String initialValueOrNull, IDelegatedAction onValueChangeAction)
    {
        final String idSuffix = parameterName.replaceAll(" ", "_");
        return ParameterSelectionDropDownList.create(parameterName, queryExpression, idSuffix,
                parameterName, viewContextOrNull, loaderOrNull, true, initialValueOrNull,
                onValueChangeAction);
    }

    private ParameterField(String parameterName, IDelegatedAction onValueChangeAction,
            String initialValueOrNull)
    {
        this.parameterName = parameterName;
        this.onValueChangeAction = onValueChangeAction;
        this.initialValueOrNull = initialValueOrNull;
        setEmptyText(parameterName);
        setToolTip(parameterName);
        setTriggerStyle("x-form-clear-trigger");
        setWidth(100);
        setAllowBlank(false);
        setAutoValidate(true);
        setValidateOnBlur(true);
    }

    public ParameterWithValue getParameterWithValue()
    {
        return new ParameterWithValue(parameterName, getRawValue());
    }

    public Field<?> asWidget()
    {
        return this;
    }

    @Override
    protected void onRender(Element target, int index)
    {
        super.onRender(target, index);
        setRawValue(initialValueOrNull);
    }

    @Override
    protected void onKeyUp(FieldEvent fe)
    {
        super.onKeyUp(fe);
        onValueChangeAction.execute();
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce)
    {
        super.onTriggerClick(ce);
        setValue(null);
        onValueChangeAction.execute();
    }

    private static ParameterSelectionField createSelectionField(String parameterName,
            List<String> parameterValues, String initialValueOrNull,
            IDelegatedAction onValueChangeAction)
    {
        return new ParameterSelectionField(parameterName, parameterValues, initialValueOrNull,
                onValueChangeAction);
    }

    // TODO 2010-10-13, Piotr Buczek: extract common code with ParameterField
    private static class ParameterSelectionField extends SimpleComboBox<String> implements
            IParameterField
    {

        private final String parameterName;

        private final String initialValueOrNull;

        private final IDelegatedAction onValueChangeAction;

        ParameterSelectionField(final String parameterName, final List<String> values,
                String initialValueOrNull, final IDelegatedAction onValueChangeAction)

        {
            this.parameterName = parameterName;
            this.initialValueOrNull = initialValueOrNull;
            this.onValueChangeAction = onValueChangeAction;
            GWTUtils.setToolTip(this, parameterName);
            GWTUtils.setupAutoWidth(this);
            setEmptyText(emptyText);
            setAllowBlank(false);
            setAutoValidate(true);
            setValidateOnBlur(true);
            setWidth(100);
            add(values);

            addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
                {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se)
                    {
                        onValueChangeAction.execute();
                    }
                });
        }

        @Override
        public String getSimpleValue()
        {
            return StringUtils.trimToNull(super.getSimpleValue());
        }

        public Field<?> asWidget()
        {
            return this;
        }

        public ParameterWithValue getParameterWithValue()
        {
            return new ParameterWithValue(parameterName, getRawValue());
        }

        @Override
        protected void onRender(Element target, int index)
        {
            super.onRender(target, index);
            setRawValue(initialValueOrNull);
        }

        @Override
        protected void onKeyUp(FieldEvent fe)
        {
            super.onKeyUp(fe);
            onValueChangeAction.execute();
        }

    }

    private static class ParameterSelectionDropDownList extends
            DropDownList<ParameterValueModel, ParameterValue> implements IParameterField
    {

        private static final String CHOOSE_MSG = "Choose...";

        private static final String VALUE_NOT_IN_LIST_MSG = "Value not in the list";

        private static final String EMPTY_MSG = "- No values found -";

        private final IViewContext<?> viewContextOrNull;

        private final IParameterValuesLoader loaderOrNull;

        private final String parameterName;

        private String initialValueOrNull;

        private final String queryExpressionOrNull;

        private final IDelegatedAction onValueChangeAction;

        /**
         * Allows to choose one of the specified values, is able to refresh the available values by
         * calling the server.
         * 
         * @param onValueChangeAction
         */
        public static IParameterField create(final String parameterName, String queryExpression,
                String idSuffix, String label, IViewContext<?> viewContextOrNull,
                IParameterValuesLoader loader, final boolean mandatory, String initialValueOrNull,
                IDelegatedAction onValueChangeAction)
        {
            return new ParameterSelectionDropDownList(parameterName, idSuffix, label, mandatory,
                    loader, queryExpression, viewContextOrNull, null, initialValueOrNull,
                    onValueChangeAction);
        }

        /**
         * Allows to choose one of the specified values.
         */
        @SuppressWarnings("unused")
        public ParameterSelectionDropDownList(final String parameterName, String idSuffix,
                String label, final boolean mandatory, List<ParameterValue> initialValuesOrNull,
                String initialValueOrNull)
        {
            this(parameterName, idSuffix, label, mandatory, null, null, null, initialValuesOrNull,
                    initialValueOrNull, null);
        }

        protected ParameterSelectionDropDownList(final String parameterName, String idSuffix,
                String label, boolean mandatory, IParameterValuesLoader loaderOrNull,
                String queryExpressionOrNull, IViewContext<?> viewContextOrNull,
                List<ParameterValue> valuesOrNull, String initialValueOrNull,
                final IDelegatedAction onValueChangeAction)
        {
            super(idSuffix, ModelDataPropertyNames.CODE, label, CHOOSE_MSG, EMPTY_MSG,
                    VALUE_NOT_IN_LIST_MSG, mandatory, viewContextOrNull, valuesOrNull == null);
            this.parameterName = parameterName;
            this.queryExpressionOrNull = queryExpressionOrNull;
            this.viewContextOrNull = viewContextOrNull;
            this.loaderOrNull = loaderOrNull;
            this.initialValueOrNull = initialValueOrNull;
            this.onValueChangeAction = onValueChangeAction;
            FieldUtil.setMandatoryFlag(this, mandatory);
            setAllowBlank(mandatory == false);
            if (valuesOrNull != null)
            {
                setValues(valuesOrNull);
            }
            setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.CODE,
                    ModelDataPropertyNames.TOOLTIP));
            setWidth(100);
            setAllowValueNotFromList(true);

            addSelectionChangedListener(new SelectionChangedListener<ParameterValueModel>()
                {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<ParameterValueModel> se)
                    {
                        onValueChangeAction.execute();
                    }
                });
        }

        private void setValues(List<ParameterValue> values)
        {
            final List<ParameterValueModel> models = new ArrayList<ParameterValueModel>();
            models.addAll(convertItems(values));
            updateStore(models);
            getPropertyEditor().setList(store.getModels());
            selectInitialValue();
        }

        @Override
        protected List<ParameterValueModel> convertItems(List<ParameterValue> result)
        {
            return ParameterValueModel.convert(result);
        }

        @Override
        protected void loadData(AbstractAsyncCallback<List<ParameterValue>> callback)
        {
            if (viewContextOrNull != null && loaderOrNull != null && queryExpressionOrNull != null)
            {
                loaderOrNull.loadData(queryExpressionOrNull, new ListParameterValuesCallback(
                        viewContextOrNull));
            }
            callback.ignore();
        }

        public DatabaseModificationKind[] getRelevantModifications()
        {
            return new DatabaseModificationKind[0];
        }

        public void selectInitialValue()
        {
            if (initialValueOrNull != null)
            {
                trySelectByValue(initialValueOrNull);
                updateOriginalValue();
            }
        }

        public void trySelectByValue(String parameterValue)
        {
            GWTUtils.setSelectedItem(this, ModelDataPropertyNames.CODE, parameterValue);
        }

        private class ListParameterValuesCallback extends
                ParameterSelectionDropDownList.ListItemsCallback
        {

            protected ListParameterValuesCallback(IViewContext<?> viewContext)
            {
                super(viewContext);
            }

            @Override
            public void process(List<ParameterValue> result)
            {
                super.process(result);
                selectInitialValue();
            }
        }

        public ParameterWithValue getParameterWithValue()
        {
            return new ParameterWithValue(parameterName, getRawValue());
        }

        public Field<?> asWidget()
        {
            return this;
        }

        @Override
        protected void onKeyUp(FieldEvent fe)
        {
            super.onKeyUp(fe);
            onValueChangeAction.execute();
        }
    }
}
