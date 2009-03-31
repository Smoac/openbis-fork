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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;

/**
 * {@link SectionPanel} containing experiment samples.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentSamplesSection extends SectionPanel
{
    private static final String PREFIX = "experiment-samples-section_";

    private IDisposableComponent sampleDisposableGrid;

    public ExperimentSamplesSection(final Experiment experiment, final IViewContext<?> viewContext)
    {
        super("Samples");
        setLayout(new RowLayout());
        String experimentIdentifier = experiment.getIdentifier();
        sampleDisposableGrid =
                SampleBrowserGrid.create(viewContext.getCommonViewContext(), experimentIdentifier,
                        createId(experimentIdentifier));
        add(sampleDisposableGrid.getComponent(), new RowData(-1, 200));
    }

    // @Private
    static String createId(String experimentIdentifier)
    {
        return SampleBrowserGrid.GRID_ID + PREFIX + experimentIdentifier;
    }

    @Override
    protected void onDetach()
    {
        sampleDisposableGrid.dispose();
        super.onDetach();
    }

}
