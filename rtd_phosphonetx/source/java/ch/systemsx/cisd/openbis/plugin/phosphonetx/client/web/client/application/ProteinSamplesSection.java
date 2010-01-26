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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * {@link SingleSectionPanel} containing specified protein samples.
 * 
 * @author Piotr Buczek
 */
public class ProteinSamplesSection extends SingleSectionPanel
{
    private static final String PREFIX = "protein-samples-section_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private IDisposableComponent sampleDisposableGrid;

    public ProteinSamplesSection(final IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            final TechId proteinReferenceID, IIdentifiable experimentOrNull)
    {
        super("Samples");
        Long experimentID = experimentOrNull == null ? null : experimentOrNull.getId();
        sampleDisposableGrid =
                SampleAbundanceBrowserGrid.createGridForProteinSamples(viewContext,
                        proteinReferenceID, experimentID, createGridId(proteinReferenceID));
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
