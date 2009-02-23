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

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.TextFieldConfig;

import ch.systemsx.cisd.cifex.client.application.model.FileShareUserGridModel;
import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.application.utils.CifexValidator;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;

/**
 * A dialog to edit file sharing.
 * 
 * @author Izabela Adamczyk
 */

abstract class AbstractFileShareUserDialog extends DefaultLayoutDialog
{
    final static String EMAIL_FIELD_CONFIG = "EMAIL";

    final List existingUsers;

    final List newUsers;

    final ViewContext viewContext;

    final ModelBasedGrid existingUserGrid;

    final ModelBasedGrid newUserGrid;

    public AbstractFileShareUserDialog(final ViewContext context, final UserInfoDTO[] existingUsers,
            final UserInfoDTO[] newUsers, final String name)
    {
        this(context, getArrayList(existingUsers), getArrayList(newUsers), name);
    }

    public AbstractFileShareUserDialog(final ViewContext context, final List existingUsers,
            final List newUsers, final String name)
    {
        super(context.getMessageResources(), context.getMessageResources()
                .getFileSharingTitle(name), 800, 600);
        this.existingUsers = existingUsers;
        this.newUsers = newUsers;
        this.viewContext = context;
        this.existingUserGrid = createUserGrid(this.existingUsers);
        this.newUserGrid = createUserGrid(this.newUsers);
        addContentPanel();

    }

    public AbstractFileShareUserDialog(final ViewContext context, UserInfoDTO[] existingUsers, String name)
    {
        this(context, existingUsers, null, name);
    }

    /**
     * This function is needed to have to possibility to refresh the tables from the outside. The
     * tables are refreshed with the existing data.
     */
    public void refresh()
    {
        newUserGrid.reloadStore(newUsers.toArray());
        existingUserGrid.reloadStore(existingUsers.toArray());
    }

    static ArrayList getArrayList(UserInfoDTO[] users)
    {
        ArrayList list = new ArrayList();
        if (users != null)
        {

            for (int i = 0; i < users.length; i++)
            {
                list.add(users[i]);
            }
        }
        return list;
    }

    /**
     * This method is called, everytime the user selects a checkbox in the usergrid. This is used,
     * that a subclass can specify an action for every change of the checkboxes.
     */
    abstract void checkboxChangeAction();

    /**
     * Method to add a user to the File Share. If you want to add a new user, just specify the email
     * address and leave the rest empty. Existing users have the usercode specified.
     */
    abstract void addUserToFileShare(UserInfoDTO user);

    protected final Widget createContentWidget()
    {
        final VerticalPanel panel = AbstractMainPage.createVerticalPanelPart();
        insertExistingUserGrid(panel);
        insertNewUserGrid(panel);
        insertAddUserForm(panel);
        return panel;

    }

    private final void insertExistingUserGrid(final VerticalPanel verticalPanel)
    {
        final HTML html = new HTML(viewContext.getMessageResources().getExistingUserTableTitle());
        html.setStyleName("cifex-heading");
        verticalPanel.add(html);
        verticalPanel.add(existingUserGrid);
    }

    private final ModelBasedGrid createUserGrid(final List users)
    {
        final IDataGridModel gridModel =
                new FileShareUserGridModel(viewContext.getMessageResources(), viewContext
                        .getModel().getUser());
        UserInfoDTO[] userArray = null;
        if (users != null && users.size() != 0)
        {
            userArray = (UserInfoDTO[]) users.toArray(new UserInfoDTO[users.size()]);
        }
        final ModelBasedGrid userGrid =
                new ModelBasedGrid(viewContext.getMessageResources(), userArray, gridModel);
        userGrid.addGridCellListener(new FileShareUserGridCellListener(this));
        return userGrid;
    }

    private final void insertNewUserGrid(final VerticalPanel verticalPanel)
    {
        final HTML html = new HTML(viewContext.getMessageResources().getNewUserTableTitle());
        html.setStyleName("cifex-heading");
        verticalPanel.add(html);
        verticalPanel.add(newUserGrid);
    }

    private final void insertAddUserForm(final VerticalPanel verticalPanel)
    {
        FormConfig formConfig = new FormConfig();
        formConfig.setButtonAlign(Position.LEFT);
        Form form = new Form(Ext.generateId(), formConfig);

        final HTML html = new HTML(viewContext.getMessageResources().getAddUserFormTitle());
        html.setStyleName("cifex-heading");
        TextFieldConfig fileFieldConfig = new TextFieldConfig();
        fileFieldConfig.setFieldLabel(viewContext.getMessageResources().getEmailFielLabel());
        fileFieldConfig.setValidateOnBlur(true);
        fileFieldConfig.setName(EMAIL_FIELD_CONFIG);
        fileFieldConfig.setValidateOnBlur(true);
        fileFieldConfig.setValidator(CifexValidator.getEmailFieldValidator());
        TextField addUserField = new TextField(fileFieldConfig);

        form.add(addUserField);
        String buttonLabel = viewContext.getMessageResources().getAddUserButtonLabel();
        Button submitButton = form.addButton(buttonLabel);
        submitButton.addButtonListener(getSubmitButtonListener(addUserField));
        form.render();

        verticalPanel.add(html);
        verticalPanel.add(form);
    }

    /**
     * Button Listener to submit the email address to the Cifex service and add it to the UserGrid.
     */
    private ButtonListenerAdapter getSubmitButtonListener(final TextField userTextField)
    {
        return new ButtonListenerAdapter()
            {
                public void onClick(Button button, EventObject e)
                {
                    if (userTextField.validate() == false)
                    {
                        return;
                    }
                    button.disable();
                    final String email = userTextField.getText();
                    viewContext.getCifexService().tryFindUserByEmail(email,
                            new AbstractAsyncCallback(viewContext)
                                {
                                    public void onSuccess(Object result)
                                    {
                                        UserInfoDTO[] users = (UserInfoDTO[]) result;
                                        if (users.length > 0)
                                        {
                                            for (int i = 0; i < users.length; i++)
                                            {
                                                if (existingUsers.contains(users[i]) == false)
                                                {
                                                    existingUsers.add(users[i]);
                                                    addUserToFileShare(users[i]);
                                                }
                                            }
                                            existingUserGrid.reloadStore(existingUsers.toArray());
                                        } else
                                        {

                                            UserInfoDTO user = new UserInfoDTO();
                                            user.setEmail(email);
                                            user.setRegistrator(viewContext.getModel().getUser());
                                            newUsers.add(user);
                                            addUserToFileShare(user);
                                            newUserGrid.reloadStore(newUsers.toArray());
                                        }
                                        // TODO 2008-06-03, Basil Neff: Bug CFX-103: If you add a user to
                                        // the list,
                                        // all checkboxes are back to checked.
                                        // This needs to cleared the removePersonArray
                                        checkboxChangeAction();
                                    }

                                });
                    userTextField.setValue("");
                    button.enable();
                }

            };
    }
}
