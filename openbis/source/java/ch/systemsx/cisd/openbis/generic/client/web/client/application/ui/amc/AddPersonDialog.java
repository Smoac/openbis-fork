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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.PersonsView;

/**
 * {@link Window} containing person registration form.
 * 
 * @author Izabela Adamczyk
 */
public class AddPersonDialog extends Window
{
    public final class RegisterPersonCallback extends AbstractAsyncCallback<Void>
    {
        private RegisterPersonCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public final void process(final Void result)
        {
            hide();
            personList.refresh();
        }
    }

    private static final String PREFIX = "add-person_";

    static final String CODE_FIELD_ID = GenericConstants.ID_PREFIX + PREFIX + "code-field";

    static final String SAVE_BUTTON_ID = GenericConstants.ID_PREFIX + PREFIX + "save-button";

    private final PersonsView personList;

    public AddPersonDialog(final CommonViewContext viewContext, final PersonsView p)
    {
        this.personList = p;
        setHeading("Add a new person");
        setModal(true);
        setWidth(400);
        final FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        add(form);
        final TextField<String> codeField = new TextField<String>();
        codeField.setWidth(100);
        codeField.setFieldLabel("Code");
        codeField.setAllowBlank(false);
        codeField.setValidateOnBlur(true);
        codeField.setId(CODE_FIELD_ID);
        form.add(codeField);

        addButton(createSaveButton(viewContext, codeField));
        addButton(createCancelButton());
    }

    private Button createCancelButton()
    {
        final Button button = new Button("Cancel", new SelectionListener<ComponentEvent>()
            {
                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(ComponentEvent ce)
                {
                    hide();
                }
            });
        return button;
    }

    private Button createSaveButton(final CommonViewContext viewContext,
            final TextField<String> codeField)
    {
        final Button button = new Button("Save", new SelectionListener<ComponentEvent>()
            {
                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ComponentEvent ce)
                {
                    viewContext.getService().registerPerson(codeField.getValue(),
                            new RegisterPersonCallback(viewContext));
                }
            });
        button.setId(SAVE_BUTTON_ID);
        return button;
    }

}
