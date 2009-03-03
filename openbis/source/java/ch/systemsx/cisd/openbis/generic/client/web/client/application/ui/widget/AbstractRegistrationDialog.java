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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelagatedAction;

/**
 * Abstract {@link Window} with Save and Cancel buttons, useful when simple registration operation
 * is needed.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractRegistrationDialog extends Window
{
    protected abstract void register(AsyncCallback<Void> registrationCallback);

    public final class RegisterDialogCallback extends AbstractAsyncCallback<Void>
    {
        private RegisterDialogCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public final void process(final Void result)
        {
            hide();
            postRegistrationCallback.execute();
        }
    }

    public static final String SAVE_BUTTON_ID = GenericConstants.ID_PREFIX + "dialog-save-button";

    public static final String CODE_FIELD_ID = GenericConstants.ID_PREFIX + "dialog-code-field";

    private final IDelagatedAction postRegistrationCallback;

    private final IViewContext<?> viewContext;

    private final FormPanel form;

    public AbstractRegistrationDialog(final IViewContext<?> viewContext, String title,
            final IDelagatedAction postRegistrationCallback)
    {
        this.postRegistrationCallback = postRegistrationCallback;
        this.viewContext = viewContext;
        setHeading(title);
        setModal(true);
        setWidth(400);
        this.form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        add(form);

        addButton(createSaveButton());
        addButton(createCancelButton());
    }

    protected final void addField(Widget widget)
    {
        form.add(widget);
    }

    private Button createCancelButton()
    {
        final Button button =
                new Button(viewContext.getMessage(Dict.BUTTON_CANCEL),
                        new SelectionListener<ComponentEvent>()
                            {
                                @Override
                                public final void componentSelected(ComponentEvent ce)
                                {
                                    hide();
                                }
                            });
        return button;
    }

    private Button createSaveButton()
    {
        final Button button =
                new Button(viewContext.getMessage(Dict.BUTTON_SAVE),
                        new SelectionListener<ComponentEvent>()
                            {
                                @Override
                                public final void componentSelected(final ComponentEvent ce)
                                {
                                    register(new RegisterDialogCallback(viewContext));
                                }
                            });
        button.setId(SAVE_BUTTON_ID);
        return button;
    }

    public static TextField<String> createDescriptionField()
    {
        TextField<String> field = new TextField<String>();
        field.setFieldLabel("Description");
        return field;
    }

    public static TextField<String> createCodeField()
    {
        final TextField<String> codeField = new TextField<String>();
        codeField.setWidth(100);
        codeField.setFieldLabel("Code");
        codeField.setAllowBlank(false);
        codeField.setValidateOnBlur(true);
        codeField.setId(CODE_FIELD_ID);
        return codeField;
    }
}
