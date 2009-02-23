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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.TextFieldConfig;
import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;

/**
 * An <code>AbstractMainPage</code> allowing to change authentication method to external service.
 * 
 * @author Izabela Adamczyk
 */
final class ExternalAuthenticationPage extends AbstractMainPage
{

    private Button okButton;

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

    //
    // AbstractMainPage
    //

    protected final ContentPanel createMainPanel()
    {
        final ContentPanel mainPanel = new ContentPanel(Ext.generateId());
        final VerticalPanel verticalPanel = createVerticalPanelPart();
        verticalPanel.add(createPartTitle(context.getMessageResources()
                .getExternalAuthenticationLabel()));
        verticalPanel.add(getExplanationWidget());
        final Form form = new Form(createFormConfig());
        final TextField passwordField = createPasswordField();
        form.add(passwordField);
        addCancelButton(form);
        addOKButton(form, passwordField);
        verticalPanel.add(form);
        mainPanel.add(verticalPanel);
        form.render();
        return mainPanel;
    }

    private final TextField createPasswordField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(context.getMessageResources()
                .getExternalAuthenticationPasswordLabel());
        fieldConfig.setPassword(true);
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        return new TextField(fieldConfig);
    }

    private final static FormConfig createFormConfig()
    {
        final FormConfig formConfig = new FormConfig();
        formConfig.setLabelAlign(Position.LEFT);
        formConfig.setButtonAlign(Position.LEFT);
        formConfig.setLabelWidth(250);
        return formConfig;
    }

    private final HTML getExplanationWidget()
    {
        return new HTML(getExplanation());
    }

    private final String getExplanation()
    {
        return context.getMessageResources().getExternalAuthenticationExplanation();
    }

    private final void addOKButton(final Form form, final TextField textField)
    {
        okButton = form.addButton(context.getMessageResources().getActionOKLabel());
        okButton.addButtonListener(new ButtonListenerAdapter()
            {

                //
                // ButtonListenerAdapter
                //

                public final void onClick(final Button button, final EventObject e)
                {
                    if (form.isValid())
                    {
                        okButton.disable();
                        context.getCifexService().trySwitchToExternalAuthentication(
                                context.getModel().getUser().getUserCode(), textField.getText(),
                                new FinishEditingAssyncCallback(context));
                    }
                }

            });
    }

    private final void addCancelButton(final Form form)
    {
        final Button cancelButton =
                form.addButton(context.getMessageResources().getActionCancelLabel());
        cancelButton.addButtonListener(new ButtonListenerAdapter()
            {

                //
                // ButtonListenerAdapter
                //

                public final void onClick(final Button button, final EventObject e)
                {
                    finishEditing();
                }
            });
    }

    //
    // Helper classes
    //

    /** Call <code>finishEditing</code> method after cifexService returned the answer. */
    private final class FinishEditingAssyncCallback extends AbstractAsyncCallback
    {

        FinishEditingAssyncCallback(final ViewContext context)
        {
            super(context);
        }

        private final void updateUserInViewContext(final UserInfoDTO user)
        {
            context.getModel().getUser().setEmail(user.getEmail());
            context.getModel().getUser().setUserFullName(user.getUserFullName());
            context.getModel().getUser().setExternallyAuthenticated(
                    user.isExternallyAuthenticated());
            context.getModel().getUser().setPermanent(user.isPermanent());
            context.getModel().getUser().setExpirationDate(user.getExpirationDate());
            context.getModel().getUser().setRegistrator(user.getRegistrator());
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            final UserInfoDTO user = (UserInfoDTO) result;
            updateUserInViewContext(user);
            MessageBox.alert(messageResources.getMessageBoxInfoTitle(), messageResources
                    .getExternalAuthenticationSuccessful());
            finishEditing();
        }

        public final void onFailure(final Throwable throwable)
        {
            okButton.enable();
            MessageBox.alert(messageResources.getMessageBoxErrorTitle(), messageResources
                    .getExternalAuthenticationFail(throwable.getMessage()));
        }
    }
}
