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

package ch.systemsx.cisd.datamover.console.client.application.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.datamover.console.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.datamover.console.client.application.IMessageResources;
import ch.systemsx.cisd.datamover.console.client.application.ViewContext;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * Widget for login.
 *
 * @author Franz-Josef Elmer
 */
public class LoginWidget extends Composite
{
    private static final String STYLE_PREFIX = "console-login-";
    
    private final ViewContext viewContext;
    private final TextBox userNameField;
    private final TextBox passwordField;

    public LoginWidget(ViewContext viewContext)
    {
        this.viewContext = viewContext;
        IMessageResources messageResources = viewContext.getMessageResources();
        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(10);
        panel.setStyleName(STYLE_PREFIX + "main");
        panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
        
        Label welcome = new HTML(messageResources.getLoginWelcomeText());
        welcome.setStyleName(STYLE_PREFIX + "welcome");
        panel.add(welcome);
        
        VerticalPanel loginPanel = new VerticalPanel();
        loginPanel.setStyleName(STYLE_PREFIX + "content");
        panel.add(loginPanel);
        FieldSet fieldSet = new FieldSet(messageResources.getLoginLegend());
        loginPanel.add(fieldSet);
        Grid grid = new Grid(2, 2);
        fieldSet.add(grid);
        userNameField = createLabeledTextBoxIn(grid, 0, messageResources.getLoginUserLabel(), false);
        passwordField = createLabeledTextBoxIn(grid, 1, messageResources.getLoginPasswordLabel(), true);
        Button button = new Button(messageResources.getLoginButtonLabel());
        button.addClickListener(new ClickListener()
            {
                public void onClick(Widget widget)
                {
                    authenticate();
                }
            });
        loginPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
        loginPanel.add(button);
        
        initWidget(panel);
    }
    
    private TextBox createLabeledTextBoxIn(Grid grid, int rowIndex, String label, boolean password)
    {
        grid.setText(rowIndex, 0, label + ":");
        TextBox textBox = password ? new PasswordTextBox() : new TextBox();
        grid.setWidget(rowIndex, 1, textBox);
        return textBox;
    }
    
    void authenticate()
    {
        String userName = userNameField.getText();
        String password = passwordField.getText();
        viewContext.getService().tryToLogin(userName, password,
                new AbstractAsyncCallback<User>(viewContext)
                    {
                        public void onSuccess(User user)
                        {
                            if (user == null)
                            {
                                IMessageResources messageResources = viewContext.getMessageResources();
                                Window.alert(messageResources.getLoginFailedMessage());
                            } else
                            {
                                viewContext.getPageController().reload();
                            }
                        }
                    });
    }
    
}
