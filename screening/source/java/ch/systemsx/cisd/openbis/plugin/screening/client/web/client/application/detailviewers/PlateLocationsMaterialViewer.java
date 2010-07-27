/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * A viewer for a material which can be a content of the well.
 * 
 * @author Tomasz Pylak
 */
public class PlateLocationsMaterialViewer extends GenericMaterialViewer
{

    /**
     * @param experimentIdentifierOrNull if the experiment is specified, it will be chosen
     *            automatically when the window opens.
     */
    public static DatabaseModificationAwareComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext, TechId materialId,
            ExperimentIdentifier experimentIdentifierOrNull)
    {
        PlateLocationsMaterialViewer viewer =
                new PlateLocationsMaterialViewer(viewContext, materialId, experimentIdentifierOrNull);
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final ExperimentIdentifier experimentIdentifierOrNull;

    private PlateLocationsMaterialViewer(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final TechId materialTechId, ExperimentIdentifier experimentIdentifierOrNull)
    {
        super(viewContext, materialTechId);
        this.screeningViewContext = viewContext;
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

    @Override
    protected void loadMaterialInfo(TechId materialTechId, AsyncCallback<Material> callback)
    {
        new GenericViewContext(screeningViewContext.getCommonViewContext()).getService()
                .getMaterialInfo(materialTechId, callback);
    }

    @Override
    protected List<SingleSectionPanel> createAdditionalSectionPanels()
    {

        List<SingleSectionPanel> sections = new ArrayList<SingleSectionPanel>();
        sections.add(new PlateLocationsMaterialSection(screeningViewContext, materialId,
                experimentIdentifierOrNull));
        return sections;
    }

    public static HelpPageIdentifier getHelpPageIdentifier()
    {
        return HelpPageIdentifier.createSpecific("Well Content Material Viewer");
    }

}
