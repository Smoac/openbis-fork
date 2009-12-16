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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> material viewer.
 * 
 * @author Piotr Buczek
 */
public class GenericMaterialViewer extends AbstractViewer<Material>
{
    private static final String PREFIX = "generic-material-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext, final TechId materialId)
    {

        final GenericMaterialViewer viewer = new GenericMaterialViewer(viewContext, materialId);
        MaterialPropertiesComponent propsComp =
                new MaterialPropertiesComponent(viewContext, materialId, -1, -1)
                    {
                        @Override
                        protected void getMaterialInfo(
                                final AsyncCallback<Material> materialInfoCallback)
                        {
                            AbstractAsyncCallback<Material> callback =
                                    new AbstractAsyncCallback<Material>(viewContext)
                                        {
                                            @Override
                                            protected void process(Material result)
                                            {
                                                viewer.updateOriginalData(result);
                                                materialInfoCallback.onSuccess(result);
                                            }
                                        };
                            viewContext.getService().getMaterialInfo(materialId, callback);
                        }
                    };
        viewer.add(propsComp);
        return new DatabaseModificationAwareComponent(viewer, propsComp);
    }

    protected GenericMaterialViewer(final IViewContext<?> viewContext, final TechId materialId)
    {
        super(viewContext, createId(materialId));
        setScrollMode(Scroll.AUTO);
    }

    public static String createId(final TechId materialId)
    {
        return ID_PREFIX + materialId;
    }
}
