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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.grid.AbstractFilterField;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractUserGridModel;
import ch.systemsx.cisd.cifex.client.application.model.FileShareUserGridModel;
import ch.systemsx.cisd.cifex.client.application.page.AbstractMainPageTabController;
import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.utils.CifexValidator;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * A dialog to edit file sharing.
 * 
 * @author Izabela Adamczyk
 */

abstract class AbstractFileShareUserDialog extends DefaultLayoutDialog
{
    private static final int DIALOG_HEIGHT = 690;

    private static final int DIALOG_WIDTH = 980;

    private final static String EMAIL_FIELD_CONFIG = "EMAIL";

    protected final List<UserInfoDTO> existingUsers;

    protected final List<UserInfoDTO> newUsers;

    protected final ViewContext viewContext;

    protected final GridWidget<FileShareUserGridModel> existingUserGrid;

    protected final GridWidget<FileShareUserGridModel> newUserGrid;

    public AbstractFileShareUserDialog(final ViewContext context,
            final UserInfoDTO[] existingUsers, final UserInfoDTO[] newUsers, final String name)
    {
        this(context, getArrayList(existingUsers), getArrayList(newUsers), name);
    }

    public AbstractFileShareUserDialog(final ViewContext context,
            final List<UserInfoDTO> oldSharingUsers, final List<UserInfoDTO> newUsers,
            final String name)
    {
        super(msg(SHARE_FILE_DIALOG_TITLE, name), DIALOG_WIDTH, DIALOG_HEIGHT);
        this.existingUsers = oldSharingUsers;
        this.newUsers = newUsers;
        this.viewContext = context;
        this.existingUserGrid = createUserGrid(this.existingUsers);
        this.newUserGrid = createUserGrid(this.newUsers);
        add(createContentWidget());
    }

    public AbstractFileShareUserDialog(final ViewContext context, UserInfoDTO[] existingUsers,
            String name)
    {
        this(context, existingUsers, null, name);
    }

    /**
     * This function is needed to have to possibility to refresh the tables from the outside. The
     * tables are refreshed with the existing data.
     */
    public void refresh()
    {
        newUserGrid.setDataAndRefresh(FileShareUserGridModel.convert(viewContext.getModel()
                .getUser(), newUsers, null));
        existingUserGrid.setDataAndRefresh(FileShareUserGridModel.convert(viewContext.getModel()
                .getUser(), existingUsers, null));
    }

    static ArrayList<UserInfoDTO> getArrayList(UserInfoDTO[] users)
    {
        ArrayList<UserInfoDTO> list = new ArrayList<UserInfoDTO>();
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
     * This method is called whenever the user selects a checkbox in the usergrid. This is so that a
     * subclass can specify an action for every change of the checkboxes.
     */
    abstract void checkboxChangeAction();

    /**
     * Method to add a user to the File Share. If you want to add a new user, just specify the email
     * address and leave the rest empty. Existing users have the usercode specified.
     */
    abstract void addUserToFileShare(UserInfoDTO user);

    protected final Widget createContentWidget()
    {
        final LayoutContainer panel = AbstractMainPageTabController.createContainer();
        insertExistingUserGrid(panel);
        insertNewUserGrid(panel);
        insertAddUserForm(panel);
        return panel;

    }

    private final void insertExistingUserGrid(final LayoutContainer verticalPanel)
    {
        final HTML html = new HTML(msg(LIST_USERS_FILESHARING_EXISTING_GRID_TITLE));
        html.setStyleName("cifex-heading");
        verticalPanel.add(html);
        verticalPanel.add(existingUserGrid.getWidget());
    }

    private final GridWidget<FileShareUserGridModel> createUserGrid(final List<UserInfoDTO> users)
    {
        List<FileShareUserGridModel> data =
                FileShareUserGridModel.convert(viewContext.getModel().getUser(), users, null);
        List<ColumnConfig> columnConfigs = FileShareUserGridModel.getColumnConfigs();
        List<AbstractFilterField<FileShareUserGridModel>> filterItems =
                AbstractUserGridModel.createFilterItems(columnConfigs);

        GridWidget<FileShareUserGridModel> gridWidget =
                GridWidget.create(columnConfigs, data, filterItems);

        Grid<FileShareUserGridModel> grid = gridWidget.getGrid();
        grid.addListener(Events.CellClick, new FileShareUserGridCellListener(this));
        return gridWidget;
    }

    private final void insertNewUserGrid(final LayoutContainer verticalPanel)
    {
        final HTML html = new HTML(msg(LIST_USERS_FILESHARING_NEW_GRID_TITLE));
        html.setStyleName("cifex-heading");
        verticalPanel.add(html);
        verticalPanel.add(newUserGrid.getWidget());
    }

    private final void insertAddUserForm(final LayoutContainer verticalPanel)
    {

        FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setBodyBorder(false);
        form.setBorders(false);
        form.setButtonAlign(HorizontalAlignment.LEFT);

        final HTML html = new HTML(msg(SHARE_FILE_ADDUSER_PANEL_TITEL));
        html.setStyleName("cifex-heading");
        TextField<String> addUserField = new TextField<String>();
        addUserField.setFieldLabel(msg(SHARE_FILE_ADDUSER_EMAIL_LABEL));
        addUserField.setValidateOnBlur(true);
        addUserField.setName(EMAIL_FIELD_CONFIG);
        addUserField.setValidateOnBlur(true);
        addUserField.setValidator(CifexValidator.getUserFieldValidator());

        form.add(addUserField);
        String buttonLabel = msg(SHARE_FILE_ADDUSER_BUTTON_LABEL);
        Button submitButton = new Button(buttonLabel);
        submitButton.addSelectionListener(getSubmitButtonListener(addUserField));
        form.addButton(submitButton);

        verticalPanel.add(html);
        verticalPanel.add(form);
    }

    /**
     * Button Listener to submit the email address to the Cifex service and add it to the UserGrid.
     */
    private SelectionListener<ButtonEvent> getSubmitButtonListener(
            final TextField<String> userTextField)
    {
        return new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    if (userTextField.validate() == false)
                    {
                        return;
                    }
                    Button button = ce.getButton();
                    button.disable();
                    final String emailOrCode = userTextField.getValue();
                    if (emailOrCode.startsWith(Constants.USER_ID_PREFIX))
                    {
                        final String userCode =
                                emailOrCode.substring(Constants.USER_ID_PREFIX.length());
                        viewContext.getCifexService().tryFindUserByUserCodeOrCreate(userCode,
                                new AbstractAsyncCallback<UserInfoDTO>(viewContext)
                                    {
                                        public void onSuccess(UserInfoDTO result)
                                        {
                                            UserInfoDTO user = result;
                                            if (user != null)
                                            {
                                                if (existingUsers.contains(user) == false)
                                                {
                                                    existingUsers.add(user);
                                                    addUserToFileShare(user);
                                                }
                                                existingUserGrid
                                                        .setDataAndRefresh(FileShareUserGridModel
                                                                .convert(viewContext.getModel()
                                                                        .getUser(), existingUsers,
                                                                        existingUserGrid.getGrid()
                                                                                .getStore()));
                                                checkboxChangeAction();
                                            } else
                                            {
                                                MessageBox.alert(msg(MESSAGE_BOX_ERROR_TITLE), msg(
                                                        UNKNOWN_USER_MSG, userCode), null);
                                            }
                                        }

                                    });
                    } else
                    {
                        viewContext.getCifexService().findUserByEmail(emailOrCode,
                                new AbstractAsyncCallback<List<UserInfoDTO>>(viewContext)
                                    {
                                        public void onSuccess(List<UserInfoDTO> users)
                                        {
                                            final UserInfoDTO requestUser =
                                                    viewContext.getModel().getUser();
                                            UserUtils.removeUnsuitableUsersForSharing(requestUser,
                                                    users);
                                            if (users.size() > 0)
                                            {
                                                for (int i = 0; i < users.size(); i++)
                                                {
                                                    UserInfoDTO user = users.get(i);
                                                    if (existingUsers.contains(user) == false)
                                                    {
                                                        existingUsers.add(user);
                                                        addUserToFileShare(user);
                                                    }
                                                }
                                                existingUserGrid
                                                        .setDataAndRefresh(FileShareUserGridModel
                                                                .convert(requestUser, existingUsers,
                                                                        existingUserGrid.getGrid()
                                                                                .getStore()));
                                            } else
                                            {

                                                UserInfoDTO user = new UserInfoDTO();
                                                user.setEmail(emailOrCode);
                                                user.setRegistrator(viewContext.getModel()
                                                        .getUser());
                                                newUsers.add(user);
                                                addUserToFileShare(user);
                                                newUserGrid
                                                        .setDataAndRefresh(FileShareUserGridModel
                                                                .convert(requestUser, newUsers,
                                                                        newUserGrid.getGrid()
                                                                                .getStore()));
                                            }
                                            checkboxChangeAction();
                                        }

                                    });
                    }
                    userTextField.setValue("");
                    button.enable();
                }

            };
    }
}
