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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * {@link ComboBox} containing list of sample types loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class SampleTypeSelectionWidget extends ComboBox<SampleTypeModel>
{
    private static final String PREFIX = "sample-type-select_";

    public static final String ID = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final boolean onlyListable;

    public SampleTypeSelectionWidget(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String idSuffix, final boolean onlyListable)
    {
        this.viewContext = viewContext;
        this.onlyListable = onlyListable;
        setId(ID + idSuffix);
        setEnabled(false);
        setDisplayField(ModelDataPropertyNames.CODE);
        setEditable(false);
        setWidth(150);
        setFieldLabel(viewContext.getMessage(Dict.SAMPLE_TYPE));
        setStore(new ListStore<SampleTypeModel>());
    }

    /**
     * Returns the {@link SampleType} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final SampleType tryGetSelectedSampleType()
    {
        return GWTUtils.tryGetSingleSelected(this);
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    void refresh()
    {
        viewContext.getService().listSampleTypes(new ListSampleTypesCallback(viewContext));
    }

    //
    // Helper classes
    //

    public final class ListSampleTypesCallback extends AbstractAsyncCallback<List<SampleType>>
    {

        ListSampleTypesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final List<SampleType> result)
        {
            final ListStore<SampleTypeModel> sampleTypeStore = getStore();
            sampleTypeStore.removeAll();
            sampleTypeStore.add(SampleTypeModel.convert(result, onlyListable));
            if (sampleTypeStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, "sample type"));
                setEnabled(true);
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, "sample types"));
            }
            applyEmptyText();
        }
    }
}