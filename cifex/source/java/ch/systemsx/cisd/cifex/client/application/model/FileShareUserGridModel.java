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

package ch.systemsx.cisd.cifex.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * The model for the file sharing user grid.
 * 
 * @author Izabela Adamczyk
 */
public final class FileShareUserGridModel extends AbstractUserGridModel
{

    private final String fileName;

    private final String fileId;

    public FileShareUserGridModel(IMessageResources messageResources, User currentUser,
            String fileName, String fileId)
    {
        super(messageResources, currentUser);
        this.fileName = fileName;
        this.fileId = fileId;
    }

    public final List getColumnConfigs()
    {
        final List configs = new ArrayList();
        configs.add(createUserCodeColumnConfig());
        configs.add(createUserEmailColumnConfig());
        configs.add(createFullNameColumnConfig());
        configs.add(createActionColumnConfig());
        configs.add(createFileNameColumnConfig());
        configs.add(createFileIdColumnConfig());
        return configs;
    }

    public final List getData(final Object[] data)
    {
        final List list = new ArrayList();
        for (int i = 0; i < data.length; i++)
        {
            final User user = (User) data[i];
            final String actions = listActionsForUser(user);

            final Object[] objects =
                    new Object[]
                        { user.getUserCode(), user.getEmail(), user.getUserFullName(), actions,
                                fileName, fileId + "" };
            list.add(objects);
        }
        return list;
    }

    public final List getFieldDefs()
    {
        final List fieldDefs = new ArrayList();
        fieldDefs.add(new StringFieldDef(USER_CODE));
        fieldDefs.add(new StringFieldDef(USER_EMAIL));
        fieldDefs.add(new StringFieldDef(FULL_NAME));
        fieldDefs.add(new StringFieldDef(ACTION));
        fieldDefs.add(new StringFieldDef(FILE_NAME));
        fieldDefs.add(new StringFieldDef(FILE_ID));
        return fieldDefs;
    }

    protected String listActionsForUser(final User user)
    {
        String actionLabel =
                DOMUtils.createAnchor(messageResources.getActionStopSharingLabel(),
                        Constants.STOP_SHARING_ID);
        return actionLabel;
    }

    private final ColumnConfig createFileNameColumnConfig()
    {
        return hiddenColumn(FILE_NAME);
    }

    private final ColumnConfig createFileIdColumnConfig()
    {
        return hiddenColumn(FILE_ID);
    }

    private final ColumnConfig hiddenColumn(String name)
    {
        final ColumnConfig column = createSortableColumnConfig(name, "", 0);
        column.setHidden(true);
        return column;
    }

}
