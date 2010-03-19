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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;

/**
 * {@link SingleSectionPanel} containing experiment samples.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentSamplesSection extends DisposableSectionPanel
{
    private static final String PREFIX = "experiment-samples-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final ExperimentType experimentType;

    private final TechId experimentId;

    public ExperimentSamplesSection(IViewContext<?> viewContext, ExperimentType experimentType,
            IIdentifiable experimentId)
    {
        super("Samples", viewContext);
        this.experimentType = experimentType;
        this.experimentId = new TechId(experimentId.getId());
    }

    // @Private
    static String createGridId(TechId experimentId)
    {
        return ID_PREFIX + experimentId + "-grid";
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        return SampleBrowserGrid.createGridForExperimentSamples(viewContext.getCommonViewContext(),
                experimentId, createGridId(experimentId), experimentType);
    }

}
