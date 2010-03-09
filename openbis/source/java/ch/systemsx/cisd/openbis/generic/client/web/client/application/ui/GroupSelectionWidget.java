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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.GroupModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of groups loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public class GroupSelectionWidget extends DropDownList<GroupModel, Space>
{

    public static final String SUFFIX = "group-select";

    private final IViewContext<?> viewContext;

    private String initialGroupOrNull;

    public static final boolean isSharedGroup(Space g)
    {
        return SHARED_SPACE_CODE.equals(g.getCode());
    }

    public static final String tryToGetGroupCode(Space space)
    {
        String code = space.getCode();
        return code.equals(ALL_SPACES_CODE) ? null : code;
    }

    public static final String SHARED_SPACE_CODE = "(Shared)";

    public static final String ALL_SPACES_CODE = "(all)";

    private final boolean addShared;

    public boolean dataLoaded = false;

    private final boolean addAll;

    public GroupSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            boolean addShared, boolean addAll)
    {
        this(viewContext, idSuffix, addShared, addAll, null);
    }

    public GroupSelectionWidget(final IViewContext<?> viewContext, final String idSuffix,
            boolean addShared, boolean addAll, final String initialGroupCodeOrNull)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.GROUP, ModelDataPropertyNames.CODE, viewContext
                .getMessage(Dict.GROUP), viewContext.getMessage(Dict.GROUP));
        this.viewContext = viewContext;
        this.addShared = addShared;
        this.addAll = addAll;
        this.initialGroupOrNull = initialGroupCodeOrNull;
    }

    /**
     * Returns the {@link Space} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final Space tryGetSelectedGroup()
    {
        return super.tryGetSelected();
    }

    private Space createSharedSpace()
    {
        final Space space = new Space();
        space.setCode(SHARED_SPACE_CODE);
        space.setIdentifier("/");
        return space;
    }

    private Space createAllSpaces()
    {
        Space space = new Space();
        space.setCode(ALL_SPACES_CODE);
        return space;
    }

    private final class ListGroupsCallback extends AbstractAsyncCallback<ResultSet<Space>>
    {
        ListGroupsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final ResultSet<Space> result)
        {
            final ListStore<GroupModel> groupStore = getStore();
            groupStore.removeAll();
            if (addShared)
            {
                groupStore.add(new GroupModel(createSharedSpace()));
            }
            if (addAll)
            {
                groupStore.add(new GroupModel(createAllSpaces()));
            }
            groupStore.add(convertItems(result.getList().extractOriginalObjects()));
            dataLoaded = true;
            if (groupStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, viewContext
                        .getMessage(Dict.GROUP)));
                setReadOnly(false);
                if (initialGroupOrNull != null)
                {
                    selectGroupAndUpdateOriginal(initialGroupOrNull);
                } else
                {
                    final int homeGroupIndex = getHomeGroupIndex(groupStore);
                    if (homeGroupIndex > -1)
                    {
                        setValue(groupStore.getAt(homeGroupIndex));
                        setOriginalValue(getValue());
                    }
                }
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, viewContext
                        .getMessage(Dict.GROUPS)));
                setReadOnly(true);
            }
        }

        int getHomeGroupIndex(ListStore<GroupModel> groupStore)
        {
            final SessionContext sessionContext = viewContext.getModel().getSessionContext();
            final User user = sessionContext.getUser();
            final String homeGroup = user.getHomeGroupCode();
            if (homeGroup != null)
            {
                for (int i = 0; i < groupStore.getCount(); i++)
                {
                    if (groupStore.getAt(i).get(ModelDataPropertyNames.CODE).equals(homeGroup))
                    {
                        return i;
                    }
                }
            }
            return -1;
        }
    }

    public void selectGroupAndUpdateOriginal(String group)
    {
        initialGroupOrNull = group;
        if (dataLoaded && initialGroupOrNull != null)
        {
            try
            {
                GWTUtils.setSelectedItem(GroupSelectionWidget.this, ModelDataPropertyNames.CODE,
                        initialGroupOrNull);
            } catch (IllegalArgumentException ex)
            {
                MessageBox.alert("Error", "Space '" + group + "' doesn't exist.", null);
            }
            updateOriginalValue();
        }
    }

    @Override
    protected List<GroupModel> convertItems(List<Space> result)
    {
        return GroupModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<Space>> callback)
    {
        DefaultResultSetConfig<String, Space> config = DefaultResultSetConfig.createFetchAll();
        viewContext.getCommonService().listGroups(config, new ListGroupsCallback(viewContext));
        callback.ignore();
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.SPACE);
    }
}
