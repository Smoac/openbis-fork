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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

class SampleTypeSelectionWidget extends ComboBox<SampleTypeModel>
{

    private final GenericViewContext viewContext;

    private ListStore<SampleTypeModel> sampleTypeStore;

    public SampleTypeSelectionWidget(GenericViewContext viewContext)
    {

        this.viewContext = viewContext;
        setEmptyText("Select a sample type...");
        setDisplayField(SampleTypeModel.CODE);
        setAllowBlank(false);
        setEditable(false);
        sampleTypeStore = new ListStore<SampleTypeModel>();
        setStore(sampleTypeStore);
    }

    public SampleType tryGetSelected()
    {

        final List<SampleTypeModel> selection = getSelection();
        if (selection.size() > 0)
        {
            return selection.get(0).get(SampleTypeModel.OBJECT);
        } else
        {
            return null;
        }
    }

    void refresh()
    {
        viewContext.getService().listSampleTypes(
                new AbstractAsyncCallback<List<SampleType>>(viewContext)
                    {
                        @Override
                        protected void process(List<SampleType> result)
                        {
                            sampleTypeStore.add(convert(result));
                        }

                    });

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