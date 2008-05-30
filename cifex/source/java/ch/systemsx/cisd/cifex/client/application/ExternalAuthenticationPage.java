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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * An <code>AbstractMainPage</code> allowing to change authentication method to external service.
 */
final class ExternalAuthenticationPage extends AbstractMainPage
{

    private static final String WIDTH = "700";

    private static final int SPACING = 10;

    ExternalAuthenticationPage(final ViewContext context)
    {
        super(context);
    }

    protected final void finishEditing()
    {
        final IPageController pageController = context.getPageController();
        final IHistoryController historyController = context.getHistoryController();
        final Page previousPage = historyController.getPreviousPage();
        assert previousPage != null : "Undefined previous page.";
        pageController.createPage(previousPage);
    }

    protected ContentPanel createMainPanel()
    {
        final ContentPanel mainPanel = new ContentPanel(Ext.generateId());
        final VerticalPanel externalAuthenticationPanel = createVerticalPanelPart();

        externalAuthenticationPanel.setSpacing(SPACING);
        externalAuthenticationPanel.setWidth(WIDTH);
        externalAuthenticationPanel.add(getExplanationWidget());
        final HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(SPACING);
        buttonPanel.add(getCancelButton());
        final HorizontalPanel passwordPanel = new HorizontalPanel();
        passwordPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        passwordPanel.setSpacing(SPACING);
        if (context.getModel().getUser().isExternallyAuthenticated() == false)
        {
            final PasswordTextBox externalPasswordField = new PasswordTextBox();
            externalPasswordField.setTitle("External password");
            passwordPanel.add(new HTML("Password in external authentication service:"));
            passwordPanel.add(externalPasswordField);
            externalAuthenticationPanel.add(passwordPanel);
            buttonPanel.add(getOkButton(externalPasswordField));
        }

        externalAuthenticationPanel.add(buttonPanel);
        mainPanel.add(createPartTitle("External authentication"));
        mainPanel.add(externalAuthenticationPanel);
        return mainPanel;
    }

    private HTML getExplanationWidget()
    {
        final HTML widget = new HTML(getExplanation());
        return widget;
    }

    private String getExplanation()
    {
        String explanation;
        if (context.getModel().getUser().isExternallyAuthenticated())
        {
            explanation =
                    "Your account is already synchronized with external authentication service.";
        } else
        {
            explanation =

                    "<ul>"
                            + "<li>If you want to login to CIFEX using external authentication service put your external password into the field below.</li>"
                            + "<li><b>Note:</b> You will not be able to revert this operation.</li>"
                            + "</ul>";
        }
        return explanation;
    }

    private Button getOkButton(final PasswordTextBox passwordBox)
    {
        final Button okButton = new Button("OK");
        okButton.addButtonListener(new ButtonListenerAdapter()
            {
                public void onClick(final Button button, final EventObject e)
                {
                    context.getCifexService().trySwitchToExternalAuthentication(
                            context.getModel().getUser().getUserCode(), passwordBox.getText(),
                            new FinishEditingAssyncCallback(context));
                    return;

                }

            });
        return okButton;
    }

    /** Call <code>finishEditing</code> method after cifexService returned the answer. */
    class FinishEditingAssyncCallback extends AbstractAsyncCallback
    {
        public FinishEditingAssyncCallback(final ViewContext context)
        {
            super(context);
        }

        public void onSuccess(final Object result)
        {
            final User user = (User) result;
            updateUserInViewContext(user);
            MessageBox.alert(messageResources.getMessageBoxInfoTitle(),
                    "Switching to external authentication completed.<br/>"
                            + "You can now use your external password to login to CIFEX.");
            finishEditing();
        }

        private void updateUserInViewContext(final User user)
        {
            context.getModel().getUser().setEmail(user.getEmail());
            context.getModel().getUser().setUserFullName(user.getUserFullName());
            context.getModel().getUser().setExternallyAuthenticated(
                    user.isExternallyAuthenticated());
            context.getModel().getUser().setPermanent(user.isPermanent());
            context.getModel().getUser().setExpirationDate(user.getExpirationDate());
            context.getModel().getUser().setRegistrator(user.getRegistrator());
        }

        public void onFailure(final Throwable throwable)
        {
            MessageBox.alert(messageResources.getMessageBoxErrorTitle(),
                    "Switching to external authentication failed.<br/> " + throwable.getMessage());
        }

    }

    private Button getCancelButton()
    {
        final Button cancelButton = new Button("Cancel");
        cancelButton.addButtonListener(new ButtonListenerAdapter()
            {

                public void onClick(final Button button, final EventObject e)
                {
                    finishEditing();
                }
            });
        return cancelButton;
    }

}
