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

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ui.UserRenderer;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * The model for the file sharing user grid.
 * 
 * @author Izabela Adamczyk
 */
public final class FileShareUserGridModel extends AbstractUserGridModel
{
    private static final long serialVersionUID = Constants.VERSION;

    public static final String SHARE_FILE = "shareFile";

    public FileShareUserGridModel(final IMessageResources messageResources,
            final UserInfoDTO currentUser, UserInfoDTO user)
    {
        super(messageResources, currentUser);
        boolean checkedUser = true;
        String registratorAnchor = null;
        if (user.getRegistrator() != null)
        {
            registratorAnchor = UserRenderer.createUserAnchor(user.getRegistrator());
        }
        set(SHARE_FILE, new Boolean(checkedUser));// Boolean
        set(USER_CODE, user.getUserCode());// String
        set(USER_EMAIL, user.getEmail());// String
        set(FULL_NAME, user.getUserFullName());// String
        set(REGISTRATOR, registratorAnchor);// String
        set(STATUS, getUserRoleDescription(user));// String
        set(ACTIVE, new Boolean(user.isActive()));// Boolean
    }

    public final static List<FileShareUserGridModel> convert(IMessageResources messageResources,
            final UserInfoDTO currentUser, final List<UserInfoDTO> users)
    {
        if (users == null)
        {
            return null;
        }
        final List<FileShareUserGridModel> result = new ArrayList<FileShareUserGridModel>();
        for (final UserInfoDTO filter : users)
        {
            result.add(new FileShareUserGridModel(messageResources, currentUser, filter));
        }
        return result;
    }

    static public final List<ColumnConfig> getColumnConfigs(IMessageResources messageResources)
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(createShareFileCheckboxColumnConfig(messageResources));
        configs.add(createUserCodeColumnConfig(messageResources));
        configs.add(createUserEmailColumnConfig(messageResources));
        configs.add(createFullNameColumnConfig(messageResources));
        configs.add(createRegistratorColumnConfig(messageResources));
        configs.add(createSortableColumnConfig(STATUS, messageResources.getStatusLabel(), 200));
        return configs;
    }

    private final static ColumnConfig createShareFileCheckboxColumnConfig(
            IMessageResources messageResources)
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(SHARE_FILE, messageResources.getShareLabel(), 45);
        columnConfig.setFixed(true);
        columnConfig.setRenderer(new GridCellRenderer<FileShareUserGridModel>()
            {
                public Object render(FileShareUserGridModel model, String property,
                        ColumnData config, int rowIndex, int colIndex,
                        ListStore<FileShareUserGridModel> store, Grid<FileShareUserGridModel> grid)
                {
                    Object propertyValue = model.get(property);
                    boolean checked = (Boolean) propertyValue;
                    return "<img class=\"checkbox\" src=\"js/ext/resources/images/default/menu/"
                            + (checked ? "checked.gif" : "unchecked.gif") + "\"/>";
                }
            });
        return columnConfig;
    }
}
