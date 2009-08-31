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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * {@link SingleSectionPanel} containing samples with specified container sample.
 * 
 * @author Piotr Buczek
 */
public class ContainerSamplesSection extends SingleSectionPanel
{
    private static final String PREFIX = "container-samples-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<?> viewContext;

    private IDisposableComponent sampleDisposableGrid;

    public ContainerSamplesSection(final IViewContext<?> viewContext)
    {
        super(viewContext.getMessage(Dict.PART_OF_HEADING));
        this.viewContext = viewContext;
    }

    public void addSamplesGrid(final Sample container)
    {
        TechId containerId = TechId.create(container);
        sampleDisposableGrid =
                SampleBrowserGrid.createGridForContainerSamples(viewContext.getCommonViewContext(),
                        containerId, createGridId(containerId), container.getSampleType());
        add(sampleDisposableGrid.getComponent());
    }

    public IDatabaseModificationObserver getDatabaseModificationObserver()
    {
        return sampleDisposableGrid;
    }

    // @Private
    static String createGridId(TechId containerId)
    {
        return ID_PREFIX + containerId + "-grid";
    }

    @Override
    protected void onDetach()
    {
        sampleDisposableGrid.dispose();
        super.onDetach();
    }

}
