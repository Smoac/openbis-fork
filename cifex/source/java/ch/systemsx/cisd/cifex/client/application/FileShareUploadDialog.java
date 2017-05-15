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

import ch.systemsx.cisd.cifex.client.application.model.FileShareUserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.UserTextArea;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * The dialog for editing file sharing used for the "File upload" widget.
 * 
 * @author Basil Neff
 */
public class FileShareUploadDialog extends AbstractFileShareUserDialog

{

    private final UserTextArea userTextArea;

    public FileShareUploadDialog(final ViewContext context, final List<UserInfoDTO> existingUsers,
            final List<UserInfoDTO> newUsers, final String name, final UserTextArea userTextArea)
    {
        super(context, existingUsers, newUsers, name);
        this.userTextArea = userTextArea;
    }

    @Override
    protected void checkboxChangeAction()
    {
        // Changes the values in the userTextArea, that only the ones are specified, which are
        // checked.
        final List<String> userEntries = new ArrayList<String>();
        for (int i = 0; i < existingUsers.size(); i++)
        {
            if ((Boolean) existingUserGrid.getGrid().getStore().getAt(i)
                    .get(FileShareUserGridModel.SHARE_FILE))
            {
                userEntries.add(Constants.USER_ID_PREFIX + (existingUsers.get(i)).getUserCode());
            }
        }
        for (int i = 0; i < newUsers.size(); i++)
        {
            if ((Boolean) newUserGrid.getGrid().getStore().getAt(i).get(FileShareUserGridModel.SHARE_FILE))
            {
                userEntries.add((newUsers.get(i)).getEmail());
            }
        }
        userTextArea.setUserEntries(userEntries.toArray(new String[userEntries.size()]));
    }

    @Override
    protected void addUserToFileShare(UserInfoDTO user)
    {
        userTextArea.addUser(user);
    }

}
