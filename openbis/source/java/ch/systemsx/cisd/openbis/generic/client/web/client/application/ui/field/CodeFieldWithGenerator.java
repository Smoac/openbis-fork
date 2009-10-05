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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.event.ComponentEvent;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * The {@link CodeField} extension "auto generate" option.
 * 
 * @author Izabela Adamczyk
 */
public class CodeFieldWithGenerator extends CodeField
{
    private IViewContext<?> viewContext;

    private String codePrefix;

    public CodeFieldWithGenerator(final IViewContext<?> viewContext, final String label,
            String codePrefix)
    {
        super(viewContext, label);
        this.viewContext = viewContext;
        this.codePrefix = codePrefix;
        setTriggerStyle("x-form-trigger-generate");
        setHideTrigger(false);
        setTitle("Click the button to automatically generate the code");
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce)
    {
        super.onTriggerClick(ce);
        setEnabled(false);
        viewContext.getCommonService().generateCode(codePrefix,
                new GenerateCodeCallback(viewContext));
    }

    public final class GenerateCodeCallback extends AbstractAsyncCallback<String>
    {

        GenerateCodeCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final String result)
        {
            setValue(result);
            setEnabled(true);
        }

        @Override
        public final void finishOnFailure(final Throwable caught)
        {
            setEnabled(true);
        }

    }

}
