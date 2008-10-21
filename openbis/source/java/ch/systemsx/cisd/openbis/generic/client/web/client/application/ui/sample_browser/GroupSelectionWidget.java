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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;

/**
 * {@link ComboBox} containing list of groups loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
class GroupSelectionWidget extends ComboBox<GroupModel>
{

    private final GenericViewContext viewContext;

    private ListStore<GroupModel> groupStore;

    public GroupSelectionWidget(GenericViewContext viewContext)
    {

        this.viewContext = viewContext;
        setEmptyText("Select a group...");
        setDisplayField(GroupModel.CODE);
        setAllowBlank(false);
        setEditable(false);
        groupStore = new ListStore<GroupModel>();
        setStore(groupStore);
    }

    public Group tryGetSelected()
    {

        final List<GroupModel> selection = getSelection();
        if (selection.size() > 0)
        {
            return selection.get(0).get(GroupModel.OBJECT);
        } else
        {
            return null;
        }
    }

    void refresh()
    {
        viewContext.getService().listGroups(null,
                new AbstractAsyncCallback<List<Group>>(viewContext)
                    {
                        @Override
                        protected void process(List<Group> result)
                        {
                            groupStore.add(convert(result));
                            if (groupStore.getCount() > 0)
                            {
                                setValue(groupStore.getAt(0));
                            }
                        }
                    });
    }

    List<GroupModel> convert(List<Group> groups)
    {
        List<GroupModel> result = new ArrayList<GroupModel>();
        for (Group g : groups)
        {
            result.add(new GroupModel(g));
        }
        return result;
    }

}