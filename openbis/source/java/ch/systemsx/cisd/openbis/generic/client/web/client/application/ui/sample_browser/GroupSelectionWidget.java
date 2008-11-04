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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.GroupModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;

/**
 * {@link ComboBox} containing list of groups loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public class GroupSelectionWidget extends ComboBox<GroupModel>
{
    final class ListGroupsCallback extends AbstractAsyncCallback<List<Group>>
    {
        ListGroupsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final List<Group> result)
        {
            groupStore.removeAll();
            groupStore.add(GroupModel.convert(result));
            if (groupStore.getCount() > 0)
            {
                setEnabled(true);
                setValue(groupStore.getAt(0));
            }
            fireEvent(AppEvents.CALLBACK_FINISHED);
        }
    }

    private static final String PREFIX = "group-select";

    static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final GenericViewContext viewContext;

    private final ListStore<GroupModel> groupStore;

    public GroupSelectionWidget(final GenericViewContext viewContext)
    {

        this.viewContext = viewContext;
        setId(ID);
        setEmptyText("- No groups found -");
        setDisplayField(ModelDataPropertyNames.CODE);
        setEditable(false);
        setEnabled(false);
        setWidth(150);
        setFieldLabel("Group");
        groupStore = new ListStore<GroupModel>();
        setStore(groupStore);
        addListener(Events.OnClick, new Listener<BaseEvent>()
            {
                public void handleEvent(final BaseEvent be)
                {
                    expand();
                }
            });
    }

    public Group tryGetSelected()
    {
        final List<GroupModel> selection = getSelection();
        if (selection.size() > 0)
        {
            return selection.get(0).get(ModelDataPropertyNames.OBJECT);
        } else
        {
            return null;
        }
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    void refresh()
    {
        viewContext.getService().listGroups(null, new ListGroupsCallback(viewContext));
    }

}