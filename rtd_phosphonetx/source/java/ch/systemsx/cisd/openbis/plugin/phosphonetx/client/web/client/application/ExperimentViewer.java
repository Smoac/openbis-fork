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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentViewer extends GenericExperimentViewer
{
    public static DatabaseModificationAwareComponent create(
            IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            BasicEntityType experimentType, IIdentifiable experimentId)
    {
        ExperimentViewer viewer =
                new ExperimentViewer(new GenericViewContext(viewContext.getCommonViewContext()),
                        viewContext, experimentType, experimentId);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    protected ExperimentViewer(IViewContext<IGenericClientServiceAsync> viewContext,
            IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext,
            BasicEntityType experimentType, IIdentifiable experimentId)
    {
        super(viewContext, experimentType, experimentId);
        this.specificViewContext = specificViewContext;
    }

    @Override
    protected List<DisposableSectionPanel> createAdditionalBrowserSectionPanels(
            String displyIdSuffix)
    {
        DisposableSectionPanel section =
                new DisposableSectionPanel(specificViewContext.getMessage(Dict.PROTEINS_SECTION),
                        specificViewContext)
                    {
                        @Override
                        protected IDisposableComponent createDisposableContent()
                        {
                            return ProteinByExperimentBrowserGrid.create(specificViewContext,
                                    experimentType, experimentId);
                        }
                    };
        section.setDisplayID(DisplayTypeIDGenerator.PROTEIN_SECTION, displyIdSuffix);
        return Collections.<DisposableSectionPanel> singletonList(section);
    }

}
