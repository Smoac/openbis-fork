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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * The model for the file sharing user grid.
 * 
 * @author Izabela Adamczyk
 */
public final class FileShareUserGridModel extends AbstractUserGridModel
{
    private static final String ID_PREFIX = "id:";

    private static final long serialVersionUID = Constants.VERSION;

    public static final String SHARE_FILE = "shareFile";

    public FileShareUserGridModel(final UserInfoDTO currentUser, final UserInfoDTO user,
            final boolean checkedUser)
    {
        super(currentUser);
        set(ID, user.getID());// long
        set(SHARE_FILE, new Boolean(checkedUser));// Boolean
        set(USER_CODE, user.getUserCode());// String
        set(USER_EMAIL, user.getEmail());// String
        set(FULL_NAME, user.getUserFullName());// String
        set(REGISTRATOR, user.getRegistrator());// UserInfoDTO
        set(STATUS, getUserRoleDescription(user));// String
        set(ACTIVE, new Boolean(user.isActive()));// Boolean
    }

    public final static List<FileShareUserGridModel> convert(final UserInfoDTO currentUser,
            final List<UserInfoDTO> users, final ListStore<FileShareUserGridModel> storeOrNull)
    {
        if (users == null)
        {
            return null;
        }
        final Map<String, Boolean> checkStatusMap = new HashMap<String, Boolean>();
        if (storeOrNull != null)
        {
            for (int i = 0; i < storeOrNull.getCount(); ++i)
            {
                final FileShareUserGridModel m = storeOrNull.getAt(i);
                final Boolean checkedUser = m.get(FileShareUserGridModel.SHARE_FILE);
                final String userCode = m.get(FileShareUserGridModel.USER_CODE);
                if (userCode != null)
                {
                    checkStatusMap.put(ID_PREFIX + userCode, checkedUser);
                }
                checkStatusMap.put((String) m.get(FileShareUserGridModel.USER_EMAIL), checkedUser);
            }
        }

        final List<FileShareUserGridModel> result = new ArrayList<FileShareUserGridModel>();
        for (final UserInfoDTO user : users)
        {
            final String userCodeOrNull = user.getUserCode();
            Boolean checkedUserValue =
                    StringUtils.isBlank(userCodeOrNull) ? null : checkStatusMap.get(ID_PREFIX
                            + userCodeOrNull);
            if (checkedUserValue == null)
            {
                checkedUserValue = checkStatusMap.get(user.getEmail());
            }
            final boolean checkedUser = (Boolean.FALSE.equals(checkedUserValue) == false);
            result.add(new FileShareUserGridModel(currentUser, user, checkedUser));
        }
        return result;
    }

    public long getID()
    {
        return get(ID);
    }

    static public final List<ColumnConfig> getColumnConfigs()
    {
        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(createIdColumnConfig());
        configs.add(createShareFileCheckboxColumnConfig());
        configs.add(createUserCodeColumnConfig());
        configs.add(createUserEmailColumnConfig());
        configs.add(createFullNameColumnConfig());
        configs.add(createRegistratorColumnConfig());
        configs.add(createSortableColumnConfig(STATUS, msg(LIST_USERS_STATUS_COLUMN_HEADER), 195));
        return configs;
    }

    private final static ColumnConfig createShareFileCheckboxColumnConfig()
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(SHARE_FILE,
                        msg(LIST_USERS_FILESHARING_SHAREFLAG_COLUMN_HEADER), 45);
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
