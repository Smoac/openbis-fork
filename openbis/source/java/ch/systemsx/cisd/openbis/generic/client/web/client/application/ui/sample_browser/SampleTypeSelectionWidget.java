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
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * {@link ComboBox} containing list of sample types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public class SampleTypeSelectionWidget extends ComboBox<SampleTypeModel>
{

    final class ListSampleTypesCallback extends AbstractAsyncCallback<List<SampleType>>
    {
        private final boolean allowEmptyCall;

        ListSampleTypesCallback(final IViewContext<?> viewContext, final boolean allowEmpty)
        {
            super(viewContext);
            allowEmptyCall = allowEmpty;
        }

        @Override
        protected void process(final List<SampleType> result)
        {
            sampleTypeStore.removeAll();
            sampleTypeStore.add(convert(result));
            if (sampleTypeStore.getCount() > 0)
            {
                setEnabled(true);
                if (allowEmptyCall == false)
                {
                    setValue(sampleTypeStore.getAt(0));
                }
            }
        }

        List<SampleTypeModel> convert(final List<SampleType> sampleTypes)
        {
            final List<SampleTypeModel> result = new ArrayList<SampleTypeModel>();
            for (final SampleType st : sampleTypes)
            {
                result.add(new SampleTypeModel(st));
            }
            return result;
        }
    }

    private static final String PREFIX = "sample-select";

    static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final ListStore<SampleTypeModel> sampleTypeStore;

    private final boolean allowEmpty;

    public SampleTypeSelectionWidget(final IViewContext<IGenericClientServiceAsync> viewContext)
    {
        this(viewContext, false);
    }

    public SampleTypeSelectionWidget(final IViewContext<IGenericClientServiceAsync> viewContext,
            final boolean allowEmpty)
    {
        this.viewContext = viewContext;
        this.allowEmpty = allowEmpty;
        setId(ID);
        setEmptyText(allowEmpty ? "Choose sample type..." : "- No sample types found -");
        setEnabled(false);
        setDisplayField(ModelDataPropertyNames.CODE);
        setEditable(false);
        setWidth(150);
        setFieldLabel("Sample type");
        sampleTypeStore = new ListStore<SampleTypeModel>();
        setStore(sampleTypeStore);
        addListener(Events.OnClick, new Listener<BaseEvent>()
            {

                public void handleEvent(final BaseEvent be)
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
        viewContext.getService().listSampleTypes(
                new ListSampleTypesCallback(viewContext, allowEmpty));
    }
}