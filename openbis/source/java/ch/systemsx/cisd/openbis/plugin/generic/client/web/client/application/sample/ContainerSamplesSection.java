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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid2;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * {@link TabContent} containing samples with specified container sample.
 * 
 * @author Piotr Buczek
 */
public class ContainerSamplesSection extends DisposableTabContent
{
    private static final String PREFIX = "container-samples-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final Sample container;

    public ContainerSamplesSection(final IViewContext<?> viewContext, final Sample container)
    {
        super(viewContext.getMessage(Dict.PART_OF_HEADING), viewContext, container);
        this.container = container;
        setIds(DisplayTypeIDGenerator.CONTAINER_SAMPLES_SECTION);
    }

    // @Private
    static String createGridId(TechId containerId)
    {
        return SampleBrowserGrid2.createGridId(createBrowserId(containerId));
    }

    private static String createBrowserId(TechId containerId)
    {
        return ID_PREFIX + containerId + "-browser";
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        TechId containerId = TechId.create(container);
        return SampleBrowserGrid2.createGridForContainerSamples(viewContext.getCommonViewContext(),
                containerId, createBrowserId(containerId), container.getSampleType());
    }

}
