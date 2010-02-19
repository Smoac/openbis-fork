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

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.widget.form.TriggerField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;

/**
 * {@link TriggerField} extension for providing values for parameters.
 * 
 * @author Piotr Buczek
 */
public class ParameterField extends TriggerField<ModelData>
{

    private final String parameterName;

    private final IDelegatedAction onValueChangeAction;

    public ParameterField(String parameterName, IDelegatedAction onValueChangeAction)
    {
        this.parameterName = parameterName;
        this.onValueChangeAction = onValueChangeAction;
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
}
