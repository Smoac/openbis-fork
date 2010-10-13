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

import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLListEncoder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;

/**
 * {@link TriggerField} extension for providing values for parameters.
 * 
 * @author Piotr Buczek
 */
public class ParameterField extends TriggerField<ModelData> implements IParameterField
{

    private static String PARAMETER_NAME_SEPARATOR = "::";

    private static String ENUM_LIST_EXPRESSION_PREFIX = "list=";

    private static String QUERY_LIST_EXPRESSION_PREFIX = "query=";

    private final String parameterName;

    private final IDelegatedAction onValueChangeAction;

    private final String initialValueOrNull;

    public static IParameterField create(String parameterName,
            IDelegatedAction onValueChangeAction, String initialValueOrNull)
    {
        String[] split = parameterName.split(PARAMETER_NAME_SEPARATOR);
        if (split.length == 2)
        {
            String name = split[0];
            String listExpression = split[1];
            if (listExpression.startsWith(ENUM_LIST_EXPRESSION_PREFIX))
            {
                String itemList = listExpression.substring(ENUM_LIST_EXPRESSION_PREFIX.length());
                String[] values = URLListEncoder.decodeItemList(itemList);
                return createSelectionField(name, Arrays.asList(values), initialValueOrNull,
                        onValueChangeAction);
            } else if (name.startsWith(QUERY_LIST_EXPRESSION_PREFIX))
            {
                // TODO 2010-10-13, Piotr Buczek: deal with this case
            } else
            {
                // TODO 2010-10-13, Piotr Buczek: deal with this case
            }
        }

        return new ParameterField(parameterName, onValueChangeAction, initialValueOrNull);
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
}
