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

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.ui.EditUserWidget;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.application.ui.UserWidget;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;

/**
 * A <code>AbstractLayoutDialog</code> to edit an user.
 * <p>
 * This dialog window comes when the user is a <i>regular</i> one or an <i>administrator</i>.
 * </p>
 * 
 * @author Basil Neff
 */
public final class EditUserDialog extends DefaultLayoutDialog
{
    private final EditUserWidget editUserWidget;

    public EditUserDialog(final ViewContext context, final UserInfoDTO user, final ModelBasedGrid userGrid)
    {
        super(context.getMessageResources(), context.getMessageResources().getEditUserDialogTitle(
                user.getUserCode()), UserWidget.TOTAL_WIDTH + 30, 220);
        editUserWidget =
                new EditUserWidget(context, context.getModel().getUser().isAdmin(), user, false)
                    {

                        //
                        // EditUserWidget
                        //

                        protected final void finishEditing()
                        {
                            new UserGridRefresherCallback(context, userGrid).onSuccess(null);
                        }
                    };
        createUpdateButton();
        addContentPanel();
    }

    private final void createUpdateButton()
    {
        final Button button = addButton(editUserWidget.getSubmitButtonLabel());
        button.addButtonListener(new ButtonListenerAdapter()
            {

                //
                // ButtonListenerAdapter
                //

                public void onClick(final Button b, final EventObject e)
                {
                    editUserWidget.submitForm();
                    hide();
                }
            });
    }

    //
    // AbstractLayoutDialog
    //

    protected final String getCloseButtonLabel()
    {
        return messageResources.getActionCancelLabel();
    }

    protected final Widget createContentWidget()
    {
        final VerticalPanel panel = AbstractMainPage.createVerticalPanelPart();
        panel.add(editUserWidget);
        return panel;
    }
}
