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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.cifex.client.application.model.FileShareUserGridModel;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * The dialog for editing file sharing used for the "Edit Sharing" action in the file grid.
 * 
 * @author Basil Neff
 */
public class FileShareUpdateUserDialog extends AbstractFileShareUserDialog
{
    private final List<String> usersToAdd = new ArrayList<String>();

    private final List<UserInfoDTO> initialSharingUsers;

    private final List<String> usersToRemove = new ArrayList<String>();

    private final AsyncCallback<Void> refreshCallback;

    private final long fileId;

    public FileShareUpdateUserDialog(final ViewContext context,
            final List<UserInfoDTO> existingUsers, final List<UserInfoDTO> newUsers,
            final String name, final long fileId, final AsyncCallback<Void> refreshCallback)
    {
        super(context, existingUsers, newUsers, name);
        initialSharingUsers = new ArrayList<UserInfoDTO>(existingUsers);
        this.refreshCallback = refreshCallback;
        createUpdateButton();
        this.fileId = fileId;
    }

    public FileShareUpdateUserDialog(final ViewContext context, List<UserInfoDTO> existingUsers,
            String name, long fileId, final AsyncCallback<Void> refreshCallback)
    {
        this(context, existingUsers, new ArrayList<UserInfoDTO>(), name, fileId, refreshCallback);
    }

    public FileShareUpdateUserDialog(final ViewContext context, final UserInfoDTO[] existingUsers,
            final UserInfoDTO[] newUsers, final String name, final long fileId,
            final AsyncCallback<Void> refreshCallback)
    {
        super(context, existingUsers, newUsers, name);
        initialSharingUsers = getArrayList(existingUsers);
        this.refreshCallback = refreshCallback;
        createUpdateButton();
        this.fileId = fileId;
    }

    // Remembers which users should be removed from the share and which one added.
    @Override
    void checkboxChangeAction()
    {
        for (int i = 0; i < existingUsers.size(); i++)
        {
            // User in the loop
            UserInfoDTO tmpUser = (existingUsers.get(i));
            String userIdentifierWithPrefix = Constants.USER_ID_PREFIX + tmpUser.getUserCode();
            String userIdentifier = (existingUsers.get(i)).getUserCode();
            // Checkbox is unchecked
            if ((Boolean) existingUserGrid.getGrid().getStore().getAt(i).get(
                    FileShareUserGridModel.SHARE_FILE) == false)
            {
                // If user is marked to add to the fileshare, remove him from the list
                if (usersToAdd.contains(userIdentifierWithPrefix) == true)
                {
                    usersToAdd.remove(userIdentifierWithPrefix);
                } else
                // If the user is not marked to be removed from the file share, add him
                if (usersToRemove.contains(userIdentifier) == false)
                {
                    usersToRemove.add(userIdentifier);
                }
            } else
            // If the checkbox is checked
            {
                // If the user is marked to be removed from the file share, remove him from the list
                if (usersToRemove.contains(userIdentifier) == true)
                {
                    usersToRemove.remove(userIdentifier);
                }
                // If the user is not along the users the file was initially sharing with not yet
                // on the list of users to add, add him now
                if (initialSharingUsers.contains(tmpUser) == false
                        && usersToAdd.contains(userIdentifierWithPrefix) == false)
                {
                    usersToAdd.add(userIdentifierWithPrefix);
                }
            }

        }

        // Loop for new generated users
        for (int i = 0; i < newUsers.size(); i++)
        {
            String userIdentifier = (newUsers.get(i)).getEmail();
            // If checkbox of the user is checked
            if ((Boolean) newUserGrid.getGrid().getStore().getAt(i).get(
                    FileShareUserGridModel.SHARE_FILE) == false)
            {
                if (usersToAdd.contains(userIdentifier))
                {
                    usersToAdd.remove(userIdentifier);
                }
            } else
            {
                if (usersToAdd.contains(userIdentifier) == false)
                {
                    usersToAdd.add(userIdentifier);
                }
            }
        }
    }

    @Override
    protected void addUserToFileShare(UserInfoDTO user)
    {
        if (StringUtils.isBlank(user.getUserCode()) == false)
        {
            usersToAdd.add(Constants.USER_ID_PREFIX + user.getUserCode());
        } else if (StringUtils.isBlank(user.getEmail()) == false)
        {
            usersToAdd.add(user.getEmail());
        }
    }

    private final void createUpdateButton()
    {
        final Button button = new Button(msg(SHARE_FILE_SUBMIT_BUTTON_LABEL));
        addButton(button);
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    viewContext.getCifexService().updateSharingLinks(fileId, usersToAdd,
                            usersToRemove, refreshCallback);
                    hide();
                }
            });
    }
}
