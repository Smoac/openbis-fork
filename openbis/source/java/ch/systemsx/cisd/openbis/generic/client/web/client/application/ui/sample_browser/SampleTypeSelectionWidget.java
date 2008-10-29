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

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataModelPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * {@link ComboBox} containing list of sample types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
class SampleTypeSelectionWidget extends ExtendedComboBox<SampleTypeModel>
{

    final class ListSampleTypesCallback extends AbstractAsyncCallback<List<SampleType>>
    {
        ListSampleTypesCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(List<SampleType> result)
        {
            sampleTypeStore.add(convert(result));
            if (sampleTypeStore.getCount() > 0)
            {
                setEnabled(true);
                setValue(sampleTypeStore.getAt(0));
            }
        }

        List<SampleTypeModel> convert(List<SampleType> sampleTypes)
        {
            List<SampleTypeModel> result = new ArrayList<SampleTypeModel>();
            for (SampleType st : sampleTypes)
            {
                result.add(new SampleTypeModel(st));
            }
            return result;
        }
    }

    private static final String PREFIX = "sample-select";

    static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final GenericViewContext viewContext;

    private ListStore<SampleTypeModel> sampleTypeStore;

    public SampleTypeSelectionWidget(GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        setId(ID);
        setEmptyText("- No sample types found -");
        setEnabled(false);
        setDisplayField(DataModelPropertyNames.CODE);
        setEditable(false);
        setWidth(150);
        sampleTypeStore = new ListStore<SampleTypeModel>();
        setStore(sampleTypeStore);
        addListener(Events.OnClick, new Listener<BaseEvent>()
            {

                public void handleEvent(BaseEvent be)
                {
                    expand();
                }
            });
    }

    public SampleType tryGetSelected()
    {

        final List<SampleTypeModel> selection = getSelection();
        if (selection.size() > 0)
        {
            return selection.get(0).get(DataModelPropertyNames.OBJECT);
        } else
        {
            return null;
        }
    }

    void refresh()
    {
        viewContext.getService().listSampleTypes(new ListSampleTypesCallback(viewContext));
    }
}