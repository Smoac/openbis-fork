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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material;

import java.util.Set;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * @author Tomasz Pylak
 */
abstract public class MaterialPropertiesComponent extends LayoutContainer implements
        IDatabaseModificationObserver
{
    abstract protected void getMaterialInfo(final AsyncCallback<Material> materialInfoCallback);

    private final IViewContext<?> viewContext;

    protected final TechId materialId;

    private final int width;

    private final int height;

    protected MaterialPropertiesComponent(final IViewContext<?> viewContext,
            final TechId materialId, int width, int height)
    {
        this.materialId = materialId;
        this.viewContext = viewContext;
        this.width = width;
        this.height = height;
        reloadData();
    }

    private void addSection(final LayoutContainer lc, final Widget w)
    {
        lc.add(w, new RowData(width, height, new Margins(5)));
    }

    /**
     * Load the material information.
     */
    protected void reloadData()
    {
        getMaterialInfo(new MaterialInfoCallback(viewContext, this));
    }

    private final class MaterialInfoCallback extends AbstractAsyncCallback<Material>
    {
        private final MaterialPropertiesComponent viewer;

        private MaterialInfoCallback(final IViewContext<?> viewContext,
                final MaterialPropertiesComponent viewer)
        {
            super(viewContext);
            this.viewer = viewer;
        }

        /**
         * Sets the {@link Material} for this <var>generic</var> material viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final Material result)
        {
            viewer.removeAll();
            MaterialPropertiesSection props = new MaterialPropertiesSection(result, viewContext);
            props.setContentVisible(true);
            addSection(viewer, props);
            viewer.layout();
        }

    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.edit(ObjectKind.MATERIAL),
                    DatabaseModificationKind.createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM),
                    DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM) };
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadData(); // reloads everything
    }
}
