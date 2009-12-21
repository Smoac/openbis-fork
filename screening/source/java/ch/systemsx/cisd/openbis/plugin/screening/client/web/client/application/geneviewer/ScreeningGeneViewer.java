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

import static ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils.withLabel;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.MaterialPropertiesComponent;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.plateviewer.WellContentDialog;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.plateviewer.WellImages;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * @author Tomasz Pylak
 */
public class ScreeningGeneViewer extends AbstractViewer<Material>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "ScreeningGeneViewer_";

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IScreeningClientServiceAsync> viewContext, final TechId materialId)
    {
        ScreeningGeneViewer viewer = new ScreeningGeneViewer(viewContext, materialId);

        return new DatabaseModificationAwareComponent(viewer, viewer.propertiesSection);
    }

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final MaterialPropertiesComponent propertiesSection;

    protected ScreeningGeneViewer(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final TechId materialId)
    {
        super(viewContext, createId(materialId));
        this.propertiesSection = new MaterialPropertiesComponent(viewContext, materialId, -1, 1)
            {
                @Override
                protected void getMaterialInfo(AsyncCallback<Material> materialInfoCallback)
                {
                    viewContext.getService().getMaterialInfo(materialId, materialInfoCallback);
                }
            };
        this.viewContext = viewContext;
        setLayout(new BorderLayout());
        add(propertiesSection, createLeftBorderLayoutData());
        add(createLocationsPanel(materialId), createRightBorderLayoutData());
    }

    private Widget createLocationsPanel(final TechId materialId)
    {
        final LayoutContainer container = new LayoutContainer();

        ExperimentChooserFieldAdaptor experimentChooser =
                ExperimentChooserField.create("", true, null, viewContext.getCommonViewContext());
        ExperimentChooserField chooserField = experimentChooser.getChooserField();
        chooserField.addChosenEntityListener(new IChosenEntityListener<Experiment>()
            {
                public void entityChosen(Experiment entity)
                {
                    if (entity != null)
                    {
                        loadGeneLocationsPanel(materialId, entity, container);
                    }
                }
            });
        chooserField.setEditable(false);
        container.add(GuiUtils.withLabel(experimentChooser.getField(), "Experiment:"));
        container.add(new Text(
                "Choose an experiment to find wells where this gene has been suppressed."));
        container.setScrollMode(Scroll.AUTO);
        return container;
    }

    private void loadGeneLocationsPanel(TechId materialId, Experiment entity,
            final LayoutContainer container)
    {
        GuiUtils
                .replaceLastItem(container, new Text(viewContext.getMessage(Dict.LOAD_IN_PROGRESS)));
        viewContext.getService().getPlateLocations(materialId,
                new ExperimentIdentifier(entity.getIdentifier()),
                new AbstractAsyncCallback<List<WellLocation>>(viewContext)
                    {
                        @Override
                        protected void process(List<WellLocation> wellLocations)
                        {
                            Widget geneLocationsPanel = createGeneLocationPanel(wellLocations);
                            GuiUtils.replaceLastItem(container, geneLocationsPanel);
                        }
                    });
    }

    private Widget createGeneLocationPanel(List<WellLocation> wellLocations)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new TableLayout(3));
        TableData cellLayout = new TableData();
        cellLayout.setPadding(20);
        for (WellLocation loc : wellLocations)
        {
            container.add(createLocationDescription(loc), cellLayout);
        }
        return container;
    }

    private LayoutContainer createLocationDescription(WellLocation loc)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        int margin = 4;

        Widget plateLink = createEntityLink(loc.getPlate());
        container.add(withLabel(plateLink, "Plate: ", margin));

        Widget wellLink = createEntityLink(loc.getWell());
        container.add(withLabel(wellLink, "Well: ", margin));

        Widget contentLink = createEntityLink(loc.getMaterialContent());
        container.add(withLabel(contentLink, "Content: ", margin));

        if (loc.tryGetImages() != null)
        {
            container.add(createImageViewer(loc.tryGetImages()));
        }
        return container;
    }

    private Widget createImageViewer(TileImages images)
    {
        WellImages wellImages = new WellImages(images);
        return WellContentDialog.createImageViewer(wellImages, viewContext, 100, 60);
    }

    private Widget createEntityLink(EntityReference entity)
    {
        final ClickHandler listener = new OpenEntityDetailsTabClickListener(entity, viewContext);
        return LinkRenderer.getLinkWidget(entity.getCode(), listener);
    }

    public static final String createId(final TechId materialId)
    {
        return PREFIX + materialId;
    }
}
