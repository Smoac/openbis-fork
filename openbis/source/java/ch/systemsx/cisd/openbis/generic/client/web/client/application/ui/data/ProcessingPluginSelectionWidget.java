/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Util;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * @author Piotr Buczek
 */
public class ProcessingPluginSelectionWidget extends
        DropDownList<DatastoreServiceDescriptionModel, DatastoreServiceDescription>
{

    private final IViewContext<?> viewContext;

    public ProcessingPluginSelectionWidget(final IViewContext<?> viewContext,
            final IIdHolder ownerId)
    {
        super(viewContext, (ownerId.getId() + "_data-set_processing-plugins"), Dict.BUTTON_PROCESS,
                ModelDataPropertyNames.LABEL, "action", "actions");
        this.viewContext = viewContext;
        addPostRefreshCallback(createHideOnNoServicesAction());
    }

    private IDataRefreshCallback createHideOnNoServicesAction()
    {
        return new IDataRefreshCallback()
            {
                public void postRefresh(boolean wasSuccessful)
                {
                    // hide combo box if there are no services
                    final ListStore<DatastoreServiceDescriptionModel> modelsStore = getStore();
                    if (modelsStore.getCount() > 0)
                    {
                        show();
                    } else
                    {
                        hide();
                    }
                }
            };
    }

    @Override
    protected List<DatastoreServiceDescriptionModel> convertItems(
            List<DatastoreServiceDescription> result)
    {
        List<DatastoreServiceDescriptionModel> models =
                DatastoreServiceDescriptionModel.convert(result, null);
        return models;
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<DatastoreServiceDescription>> callback)
    {
        viewContext.getCommonService().listDataStoreServices(DataStoreServiceKind.PROCESSING,
                callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0]; // don't update
    }

    @Override
    public void setValue(DatastoreServiceDescriptionModel value)
    {
        // fire SelectionChange event on each combo box selection, even if selected item
        // did't change, to refresh viewer
        DatastoreServiceDescriptionModel oldValue = getValue();
        super.setValue(value);
        if (Util.equalWithNull(oldValue, value))
        {
            SelectionChangedEvent<DatastoreServiceDescriptionModel> se =
                    new SelectionChangedEvent<DatastoreServiceDescriptionModel>(this,
                            getSelection());
            fireEvent(Events.SelectionChange, se);
        }
    }

}
