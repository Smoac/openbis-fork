/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;

/**
 * Shows details about one material (across all plates in one or all experiments). <br>
 * Can be used as a tab in {@link ImagingMaterialViewer} or independently.
 * 
 * @author Tomasz Pylak
 */
// TODO 2011-03-23, Tomasz Pylak: this class will replace the Plate Locations tab on the material
// detail view in future.
// For now it is a prototype accessible only in embedded mode.
public class MaterialDetailsComponent extends ContentPanel
{
    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final ExperimentSearchCriteria experimentCriteriaOrNull;

    private final TechId materialId;

    public MaterialDetailsComponent(IViewContext<IScreeningClientServiceAsync> viewContext,
            TechId materialId, ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        this.screeningViewContext = viewContext;
        this.materialId = materialId;
        this.experimentCriteriaOrNull = experimentCriteriaOrNull;
    }

    private IDisposableComponent createWellSearchGrid()
    {
        return WellSearchGrid.create(screeningViewContext, experimentCriteriaOrNull, materialId);
    }

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext, TechId materialId,
            ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        MaterialDetailsComponent materialDetailsComponent =
                new MaterialDetailsComponent(viewContext, materialId, experimentCriteriaOrNull);
        ContentPanel view = new ContentPanel();
        view.setLayout(new RowLayout());
        view.setHeading("Gene ID: " + materialId);
        IDisposableComponent wellSearchGrid = materialDetailsComponent.createWellSearchGrid();
        view.add(wellSearchGrid.getComponent());
        return new DisposableComponentWrapper(view, wellSearchGrid);
    }

    private static class DisposableComponentWrapper implements IDisposableComponent
    {
        private final Component component;

        private final IDisposableComponent delegator;

        public DisposableComponentWrapper(Component component, IDisposableComponent delegator)
        {
            this.component = component;
            this.delegator = delegator;
        }

        public Component getComponent()
        {
            return component;
        }

        // --- delegate

        public void update(Set<DatabaseModificationKind> observedModifications)
        {
            delegator.update(observedModifications);
        }

        public DatabaseModificationKind[] getRelevantModifications()
        {
            return delegator.getRelevantModifications();
        }

        public void dispose()
        {
            delegator.dispose();
        }

    }
}
