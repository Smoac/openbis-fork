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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * The login widget.
 * 
 * @author Franz-Josef Elmer
 */
public class LoginWidget extends VerticalPanel
{

    private static final String PREFIX = "login_";

    private static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    static final String USER_FIELD_ID = ID_PREFIX + "user";

    static final String PASSWORD_FIELD_ID = ID_PREFIX + "password";

    static final String BUTTON_ID = ID_PREFIX + "button";

    private final TextField<String> userField;

    private final TextField<String> passwordField;

    private final Button button;

    private final FormPanel formPanel;

    public LoginWidget(final GenericViewContext viewContext)
    {
        setSpacing(10);
        add(new Text(viewContext.getMessage(PREFIX + "invitation")));
        formPanel = createFormPanel();
        userField = createUserField(viewContext);
        formPanel.add(userField);
        passwordField = createPasswordField(viewContext);
        formPanel.add(passwordField);
        button = createButton(viewContext);
        formPanel.addButton(button);
        add(formPanel);
    }

    private final static FormPanel createFormPanel()
    {
        final FormPanel formPanel = new FormPanel();
        formPanel.setStyleName("login-widget");
        formPanel.setBodyBorder(false);
        formPanel.setHeaderVisible(false);
        formPanel.setFieldWidth(130);
        formPanel.setWidth(250);
        formPanel.setButtonAlign(HorizontalAlignment.RIGHT);
        return formPanel;
    }

    private final TextField<String> createUserField(final GenericViewContext viewContext)
    {
        final TextField<String> field = new TextField<String>();
        field.setFieldLabel(viewContext.getMessage(PREFIX + "userLabel"));
        field.setSelectOnFocus(true);
        field.setAllowBlank(false);
        field.setValidateOnBlur(true);
        field.setId(USER_FIELD_ID);
        field.setValidateOnBlur(true);
        addEnterKeyListener(field, viewContext);
        return field;
    }

    private final void addEnterKeyListener(final Component component,
            final GenericViewContext viewContext)
    {
        component.addListener(Events.KeyPress, new Listener<BaseEvent>()
            {
                //
                // Listener
                //

                public final void handleEvent(final BaseEvent be)
                {
                    final int keyCode = ((FieldEvent) be).getKeyCode();
                    if (keyCode == 13)
                    {
                        doLogin(viewContext);
                    }
                }
            });
    }

    private final TextField<String> createPasswordField(final GenericViewContext viewContext)
    {
        final TextField<String> field = new TextField<String>();
        field.setPassword(true);
        field.setAllowBlank(false);
        field.setFieldLabel(viewContext.getMessage(PREFIX + "passwordLabel"));
        field.setId(PASSWORD_FIELD_ID);
        field.setValidateOnBlur(true);
        addEnterKeyListener(field, viewContext);
        return field;
    }

    private final Button createButton(final GenericViewContext viewContext)
    {
        final Button b = new Button(viewContext.getMessage(PREFIX + "buttonLabel"));
        b.setId(BUTTON_ID);
        b.addSelectionListener(new SelectionListener<ComponentEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ComponentEvent ce)
                {
                    doLogin(viewContext);
                }
            });
        return b;
    }

    private final void doLogin(final GenericViewContext viewContext)
    {
        if (formPanel.isValid())
        {
            button.disable();
            final String user = userField.getValue();
            final String password = passwordField.getValue();
            viewContext.getService().tryToLogin(user, password, new LoginCallback(viewContext));
        }
    }

    //
    // Helper classes
    //

    public static final class LoginCallback extends AbstractAsyncCallback<SessionContext>
    {
        private LoginCallback(final GenericViewContext viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        public final void process(final SessionContext sessionContext)
        {
            viewContext.getPageController().reload();
        }
    }
}
