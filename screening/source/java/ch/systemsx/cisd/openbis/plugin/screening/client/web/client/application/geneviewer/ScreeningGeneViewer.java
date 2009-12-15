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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.geneviewer;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * @author Tomasz Pylak
 */
public class ScreeningGeneViewer extends GenericMaterialViewer
{
    public static DatabaseModificationAwareComponent create(
            final IViewContext<IScreeningClientServiceAsync> viewContext, final TechId materialId)
    {
        ScreeningGeneViewer viewer = new ScreeningGeneViewer(viewContext, materialId);
        viewer.reloadData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    protected ScreeningGeneViewer(IViewContext<IScreeningClientServiceAsync> viewContext,
            TechId materialId)
    {
        super(viewContext, materialId);
        this.viewContext = viewContext;
    }

    @Override
    protected void getMaterialInfo(AsyncCallback<Material> materialInfoCallback)
    {
        viewContext.getService().getMaterialInfo(materialId, materialInfoCallback);
    }

}
