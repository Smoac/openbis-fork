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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.google.gwt.user.client.ui.HTML;

import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * An <code>AbstractMainPage</code> allowing to change authentication method to external service.
 * 
 * @author Izabela Adamczyk
 */
final class ExternalAuthenticationPage extends AbstractMainPage
{

    ExternalAuthenticationPage(final ViewContext context)
    {
        super(context);
    }

    @Override
    protected final LayoutContainer createMainPanel()
    {
        final LayoutContainer mainPanel = new LayoutContainer();
        final LayoutContainer verticalPanel = createContainer();
        addTitlePart(verticalPanel, context.getMessageResources().getExternalAuthenticationLabel());
        verticalPanel.add(getExplanationWidget(context.getMessageResources()));
        final FormPanel form = new FormPanel();
        form.setLabelAlign(LabelAlign.LEFT);
        form.setButtonAlign(HorizontalAlignment.LEFT);
        form.setLabelWidth(250);
        final TextField<String> passwordField = createPasswordField(context.getMessageResources());
        form.add(passwordField);
        addCancelButton(form, context);
        addOKButton(form, passwordField, context);
        verticalPanel.add(form);
        mainPanel.add(verticalPanel);
        return mainPanel;
    }

    private static final TextField<String> createPasswordField(IMessageResources messageResources)
    {
        final TextField<String> field = new TextField<String>();
        field.setFieldLabel(messageResources.getExternalAuthenticationPasswordLabel());
        field.setPassword(true);
        field.setAllowBlank(false);
        field.setValidateOnBlur(false);
        return field;
    }

    static private final HTML getExplanationWidget(IMessageResources messageResources)
    {
        return new HTML(messageResources.getExternalAuthenticationExplanation());
    }

    static private final void addOKButton(final FormPanel form, final TextField<String> textField,
            final ViewContext context)
    {
        final Button okButton = new Button(context.getMessageResources().getActionOKLabel());
        okButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    if (form.isValid())
                    {
                        okButton.disable();
                        context.getCifexService().trySwitchToExternalAuthentication(
                                context.getModel().getUser().getUserCode(), textField.getValue(),
                                new FinishEditingAssyncCallback(context, okButton));
                    }
                }

            });
        form.addButton(okButton);
    }

    static private final void addCancelButton(final FormPanel form, final ViewContext context)
    {
        final Button cancelButton =
                new Button(context.getMessageResources().getActionCancelLabel());
        cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    finishEditing(context);

                }
            });
        form.addButton(cancelButton);
    }

    //
    // Helper classes
    //

    /** Call <code>finishEditing</code> method after cifexService returned the answer. */
    private static final class FinishEditingAssyncCallback extends
            AbstractAsyncCallback<UserInfoDTO>
    {

        private final ViewContext context;

        private final Button okButton;

        FinishEditingAssyncCallback(final ViewContext context, Button okButton)
        {
            super(context);
            this.context = context;
            this.okButton = okButton;
        }

        private final void updateUserInViewContext(final UserInfoDTO user)
        {
            context.getModel().getUser().setEmail(user.getEmail());
            context.getModel().getUser().setUserFullName(user.getUserFullName());
            context.getModel().getUser().setExternallyAuthenticated(
                    user.isExternallyAuthenticated());
            context.getModel().getUser().setExpirationDate(user.getExpirationDate());
            context.getModel().getUser().setRegistrator(user.getRegistrator());
        }

        public final void onSuccess(final UserInfoDTO result)
        {
            final UserInfoDTO user = result;
            updateUserInViewContext(user);
            MessageBox.alert(context.getMessageResources().getMessageBoxInfoTitle(), context
                    .getMessageResources().getExternalAuthenticationSuccessful(), null);
            finishEditing(context);
        }

        @Override
        public final void onFailure(final Throwable throwable)
        {
            okButton.enable();
            MessageBox.alert(context.getMessageResources().getMessageBoxErrorTitle(), context
                    .getMessageResources().getExternalAuthenticationFail(throwable.getMessage()),
                    null);
        }
    }

    static private final void finishEditing(ViewContext context)
    {
        final IPageController pageController = context.getPageController();
        final IHistoryController historyController = context.getHistoryController();
        final Page previousPage = historyController.getPreviousPage();
        assert previousPage != null : "Undefined previous page.";
        pageController.createPage(previousPage);
    }
}
