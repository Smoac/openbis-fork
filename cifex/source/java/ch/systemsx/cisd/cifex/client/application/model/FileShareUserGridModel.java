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

import com.gwtext.client.data.BooleanFieldDef;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.StringFieldDef;
import com.gwtext.client.widgets.grid.CellMetadata;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.Renderer;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ui.UserRenderer;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;

/**
 * The model for the file sharing user grid.
 * 
 * @author Izabela Adamczyk
 */
public final class FileShareUserGridModel extends AbstractUserGridModel
{

    public static final String SHARE_FILE = "shareFile";

    public FileShareUserGridModel(final IMessageResources messageResources, final UserInfoDTO currentUser)
    {
        super(messageResources, currentUser);
    }

    public final List getColumnConfigs()
    {
        final List configs = new ArrayList();
        configs.add(createShareFileCheckboxColumnConfig());
        configs.add(createUserCodeColumnConfig());
        configs.add(createUserEmailColumnConfig());
        configs.add(createFullNameColumnConfig());
        configs.add(createRegistratorColumnConfig());
        configs.add(createSortableColumnConfig(STATUS, messageResources.getStatusLabel(), 200));
        return configs;
    }

    public final List getData(final Object[] data)
    {
        final List list = new ArrayList();
        if (data != null)
        {
            for (int i = 0; i < data.length; i++)
            {
                final UserInfoDTO user = (UserInfoDTO) data[i];
                boolean checkedUser = true;
                String registratorAnchor = null;
                if (user.getRegistrator() != null)
                {
                    registratorAnchor = UserRenderer.createUserAnchor(user.getRegistrator());
                }
                final Object[] objects =
                        new Object[]
                            { new Boolean(checkedUser), user.getUserCode(), user.getEmail(),
                                    user.getUserFullName(), registratorAnchor,
                                    getUserRoleDescription(user), new Boolean(user.isActive()) };
                list.add(objects);
            }
        }
        return list;
    }

    public final List getFieldDefs()
    {
        final List fieldDefs = new ArrayList();
        fieldDefs.add(new BooleanFieldDef(SHARE_FILE));
        fieldDefs.add(new StringFieldDef(USER_CODE));
        fieldDefs.add(new StringFieldDef(USER_EMAIL));
        fieldDefs.add(new StringFieldDef(FULL_NAME));
        fieldDefs.add(new StringFieldDef(REGISTRATOR));
        fieldDefs.add(new StringFieldDef(STATUS));
        fieldDefs.add(new BooleanFieldDef(ACTIVE));
        return fieldDefs;
    }

    private final ColumnConfig createShareFileCheckboxColumnConfig()
    {
        final ColumnConfig columnConfig =
                createSortableColumnConfig(SHARE_FILE, messageResources.getShareLabel(), 15);
        columnConfig.setFixed(true);
        columnConfig.setRenderer(new Renderer()
            {

                public String render(Object value, CellMetadata cellMetadata, Record record,
                        int rowIndex, int colNum, Store store)
                {
                    boolean checked = ((Boolean) value).booleanValue();
                    return "<img class=\"checkbox\" src=\"js/ext/resources/images/default/menu/"
                            + (checked ? "checked.gif" : "unchecked.gif") + "\"/>";
                }
            });
        return columnConfig;
    }
}
