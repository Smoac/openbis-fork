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
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.GroupModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;

/**
 * {@link ComboBox} containing list of groups loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class GroupSelectionWidget extends ComboBox<GroupModel>
{
    private static final String PREFIX = "group-select";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<?> viewContext;

    public static final boolean isSharedGroup(Group g)
    {
        return SHARED_GROUP_CODE.equals(g.getCode());
    }

    public static final String SHARED_GROUP_CODE = "(Shared)";

    public GroupSelectionWidget(final IViewContext<?> viewContext, final String idSuffix)
    {
        this.viewContext = viewContext;
        setId(ID + idSuffix);
        setDisplayField(ModelDataPropertyNames.CODE);
        setEditable(false);
        setEnabled(false);
        setWidth(150);
        setFieldLabel(viewContext.getMessage(Dict.GROUP));
        setStore(new ListStore<GroupModel>());
    }

    /**
     * Returns the {@link Group} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final Group tryGetSelectedGroup()
    {
        return GWTUtils.tryGetSingleSelected(this);
    }

    void refresh()
    {
        viewContext.getCommonService().listGroups(null, new ListGroupsCallback(viewContext));
    }

    //
    // ComboBox
    //

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    //
    // Helper classes
    //

    private Group createSharedGroup()
    {
        final Group group = new Group();
        group.setCode(SHARED_GROUP_CODE);
        return group;
    }

    public final class ListGroupsCallback extends AbstractAsyncCallback<List<Group>>
    {
        ListGroupsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final List<Group> result)
        {
            final ListStore<GroupModel> groupStore = getStore();
            groupStore.removeAll();
            groupStore.add(new GroupModel(createSharedGroup()));
            groupStore.add(GroupModel.convert(result));
            if (groupStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, "group"));
                applyEmptyText();
                setEnabled(true);
                final int homeGroupIndex = getHomeGroupIndex(groupStore);
                if (homeGroupIndex > -1)
                {
                    setValue(groupStore.getAt(homeGroupIndex));
                }
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, "groups"));
                applyEmptyText();
            }
            fireEvent(AppEvents.CALLBACK_FINISHED);
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
}