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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

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
        TechId experimentId = TechId.create(experiment);
        sampleDisposableGrid =
                SampleBrowserGrid.createGridForExperimentSamples(
                        viewContext.getCommonViewContext(), experimentId,
                        createGridId(experimentId), experiment.getExperimentType());
        add(sampleDisposableGrid.getComponent());
    }

    public IDatabaseModificationObserver getDatabaseModificationObserver()
    {
        return sampleDisposableGrid;
    }

    // @Private
    static String createGridId(TechId experimentId)
    {
        return SampleBrowserGrid.GRID_ID + PREFIX + experimentId;
    }

    @Override
    protected void onDetach()
    {
        sampleDisposableGrid.dispose();
        super.onDetach();
    }

}
