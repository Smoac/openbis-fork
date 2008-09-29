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

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class LoginWidget extends VerticalPanel
{
    private static final String PREFIX = "login_";
    
    private final TextField<String> userField;
    private final TextField<String> passwordField;

    public LoginWidget(final GenericViewContext viewContext)
    {
        add(new Text(viewContext.getMessage(PREFIX + "invitation")));
        
        FormPanel formPanel = new FormPanel();
        formPanel.setBodyBorder(false);
        formPanel.setBorders(false);
        formPanel.setHeaderVisible(false);
        formPanel.setFieldWidth(120);
        formPanel.setWidth(250);
        userField = new TextField<String>();
        userField.setFieldLabel(viewContext.getMessage(PREFIX + "userLabel"));
        userField.setSelectOnFocus(true);
        userField.setAllowBlank(false);
        userField.setValidateOnBlur(true);
        userField.setId("login-user");
        formPanel.add(userField);
        passwordField = new TextField<String>();
        passwordField.setPassword(true);
        passwordField.setAllowBlank(false);
        passwordField.setFieldLabel(viewContext.getMessage(PREFIX + "passwordLabel"));
        passwordField.setId("login-password");
        formPanel.add(passwordField);
        Button button = new Button(viewContext.getMessage(PREFIX + "buttonLabel"));
        button.setId("login-button");
        button.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    login(viewContext);
                }
            });
        formPanel.addButton(button);
        
        add(formPanel);
    }
    
    private void login(final GenericViewContext viewContext)
    {
        System.out.println("LoginWidget.login()");
        viewContext.getService().tryToLogin(userField.getValue(), passwordField.getValue(),
                new AbstractAsyncCallback<SessionContext>(viewContext)
                    {
                        @Override
                        public void process(SessionContext sessionContext)
                        {
                            viewContext.getPageController().reload();
                        }
                    });
        
    }
    
}
