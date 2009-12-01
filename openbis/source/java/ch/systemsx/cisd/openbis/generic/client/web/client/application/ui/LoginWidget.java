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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
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

    public LoginWidget(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        setSpacing(10);

        add(new Text(viewContext.getMessage(Dict.LOGIN_INVITATION)));
        formPanel = createFormPanel();
        userField = createUserField(viewContext);
        formPanel.add(userField);
        passwordField = createPasswordField(viewContext);
        formPanel.add(passwordField);
        button = createButton(viewContext);
        formPanel.addButton(button);

        // NOTE: it would be better to invoke it on reset but it somehow doesn't have any effect
        focusOnFirstField();

        add(formPanel);
    }

    private void focusOnFirstField()
    {
        formPanel.getItem(0).focus();
    }

    private final static FormPanel createFormPanel()
    {
        final FormPanel formPanel = new FormPanel();
        formPanel.setStyleName("login-widget");
        formPanel.setBodyBorder(false);
        formPanel.setBorders(false);
        formPanel.setHeaderVisible(false);
        formPanel.setFieldWidth(130);
        formPanel.setWidth(250);
        formPanel.setButtonAlign(HorizontalAlignment.RIGHT);
        return formPanel;
    }

    private final TextField<String> createUserField(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final TextField<String> field = new TextField<String>();
        field.setFieldLabel(viewContext.getMessage(Dict.LOGIN_USER_LABEL));
        field.setId(USER_FIELD_ID);
        field.setAllowBlank(false);
        field.setValidateOnBlur(true);
        addEnterKeyListener(field, viewContext);
        return field;
    }

    private final void addEnterKeyListener(final Field<String> field,
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        field.addKeyListener(new EnterKeyListener()
            {

                @Override
                protected final void onEnterKey()
                {
                    doLogin(viewContext);
                }
            });
    }

    private final TextField<String> createPasswordField(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final TextField<String> field = new TextField<String>();
        field.setPassword(true);
        field.setFieldLabel(viewContext.getMessage(Dict.LOGIN_PASSWORD_LABEL));
        field.setId(PASSWORD_FIELD_ID);
        field.setAllowBlank(false);
        field.setValidateOnBlur(true);
        addEnterKeyListener(field, viewContext);
        return field;
    }

    private final Button createButton(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final Button b = new Button(viewContext.getMessage(Dict.LOGIN_BUTTON_LABEL));
        b.setId(BUTTON_ID);
        b.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    doLogin(viewContext);
                }
            });
        return b;
    }

    private final void doLogin(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        if (GWTUtils.isTesting() || formPanel.isValid())
        {
            button.disable();
            final String user = userField.getValue();
            final String password = passwordField.getValue();
            viewContext.getService().tryToLogin(user, password, new LoginCallback(viewContext));
        }
    }

    public final void resetFields()
    {
        userField.reset();
        passwordField.reset();
        button.enable();
    }

    //
    // VerticalPanel
    //

    @Override
    protected final void onLoad()
    {
        super.onLoad();
        resetFields();
    }

    //
    // Helper classes
    //

    // public only for tests
    public final class LoginCallback extends AbstractAsyncCallback<SessionContext>
    {
        private static final int TIMER_PERIOD = 30 * 60 * 1000; // 30min

        private LoginCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        public final void finishOnFailure(final Throwable caught)
        {
            resetFields();
        }

        @Override
        public final void process(final SessionContext sessionContext)
        {
            if (sessionContext == null)
            {
                MessageBox.alert(viewContext.getMessage(Dict.MESSAGEBOX_WARNING), viewContext
                        .getMessage(Dict.LOGIN_FAILED), new Listener<MessageBoxEvent>()
                    {

                        //
                        // Listener
                        //

                        public void handleEvent(final MessageBoxEvent be)
                        {
                            viewContext.getPageController().reload(false);
                        }
                    });
            } else
            {
                viewContext.getService().setBaseURL(GWTUtils.getBaseIndexURL(),
                        new AbstractAsyncCallback<SessionContext>(viewContext)
                            {
                                @Override
                                protected void process(SessionContext result)
                                {
                                }
                            });
                viewContext.getPageController().reload(false);
                keepSessionAlive();
            }
        }

        /** tries to keep session alive until user logs out or closes browser */
        private void keepSessionAlive()
        {
            Timer t = new Timer()
                {
                    @Override
                    public void run()
                    {
                        // callback will cancel keeping session alive if something went wrong
                        // or user logged out
                        AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>()
                            {

                                public void onSuccess(Boolean result)
                                {
                                    if (result == false)
                                    {
                                        cancel();
                                    }
                                }

                                public void onFailure(Throwable caught)
                                {
                                    cancel();
                                }
                            };
                        viewContext.getCommonService().keepSessionAlive(callback);
                    }
                };
            t.scheduleRepeating(TIMER_PERIOD);
        }
    }
}
