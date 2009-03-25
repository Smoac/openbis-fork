/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * Dialog to register a new material type.
 * 
 * @author Tomasz Pylak
 */
abstract public class AddEntityTypeDialog extends AbstractRegistrationDialog
{
    /** Registers a new property type and calls the specified callback at the end */
    abstract protected void register(String code, String descriptionOrNull,
            AsyncCallback<Void> registrationCallback);

    private final TextField<String> codeField;

    private final TextField<String> descriptionField;

    public AddEntityTypeDialog(final IViewContext<ICommonClientServiceAsync> viewContext,
            String title, final IDelegatedAction postRegistrationCallback)
    {
        super(viewContext, title, postRegistrationCallback);
        this.codeField = createCodeField();
        addField(codeField);

        this.descriptionField = createDescriptionField();
        addField(descriptionField);
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        register(codeField.getValue(), descriptionField.getValue(), registrationCallback);
    }
}
