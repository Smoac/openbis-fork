/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import com.extjs.gxt.ui.client.widget.form.CheckBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.DataSetConnectionTypeProvider;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleDataSetsSection extends SingleSectionPanel
{
    final IViewContext<?> viewContext;

    private IDisposableComponent dataSetBrowser;

    public SampleDataSetsSection(final IViewContext<?> viewContext)
    {
        super(viewContext.getMessage(Dict.EXTERNAL_DATA_HEADING));
        this.viewContext = viewContext;
    }

    public void addDataSetGrid(CheckBox showOnlyDirectlyConnectedCheckBox, TechId sampleId,
            SampleType sampleType)
    {
        getHeader().addTool(showOnlyDirectlyConnectedCheckBox);
        dataSetBrowser =
                SampleDataSetBrowser.create(viewContext, sampleId, sampleType,
                        new DataSetConnectionTypeProvider(showOnlyDirectlyConnectedCheckBox),
                        getServerRequestQueue());
        add(dataSetBrowser.getComponent());
    }

    IDisposableComponent getDataSetBrowser()
    {
        return dataSetBrowser;
    }

}
