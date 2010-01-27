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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.MaterialPropertiesComponent;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.DefaultChannelState;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ChannelChooser.IChanneledViewerFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.TileImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * @author Tomasz Pylak
 */
public class GeneMaterialViewer extends AbstractViewer<Material>
{
    private static final int IMAGE_WIDTH_PX = 100;

    private static final int IMAGE_HEIGHT_PX = 60;

    private static final String PREFIX = GenericConstants.ID_PREFIX + "ScreeningGeneViewer_";

    /**
     * @param experimentIdentifierOrNull if the experiment is specified, it will be chosen
     *            automatically when the window opens.
     */
    public static DatabaseModificationAwareComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext, IIdentifiable materialId,
            ExperimentIdentifier experimentIdentifierOrNull)
    {
        GeneMaterialViewer viewer =
                new GeneMaterialViewer(viewContext, materialId, experimentIdentifierOrNull);

        return new DatabaseModificationAwareComponent(viewer, viewer.propertiesSection);
    }

    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    private final MaterialPropertiesComponent propertiesSection;

    private final DefaultChannelState channelState;

    private GeneMaterialViewer(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final IIdentifiable materialId, ExperimentIdentifier experimentIdentifierOrNull)
    {
        super(viewContext, createId(materialId));
        TechId materialTechId = TechId.create(materialId);
        this.propertiesSection =
                new MaterialPropertiesComponent(viewContext, materialTechId, -1, 1)
                    {
                        @Override
                        protected void getMaterialInfo(AsyncCallback<Material> materialInfoCallback)
                        {
                            viewContext.getService().getMaterialInfo(materialId,
                                    materialInfoCallback);
                        }
                    };
        this.viewContext = viewContext;
        this.channelState = new DefaultChannelState();
        setLayout(new BorderLayout());
        add(propertiesSection, createLeftBorderLayoutData());
        LayoutContainer locationsPanel =
                createLocationsPanel(materialTechId, experimentIdentifierOrNull);
        add(locationsPanel, createRightBorderLayoutData());

        if (experimentIdentifierOrNull != null)
        {
            loadGeneLocationsPanel(materialTechId, experimentIdentifierOrNull, locationsPanel);
        }
    }

    private LayoutContainer createLocationsPanel(final TechId materialId,
            ExperimentIdentifier experimentIdentifierOrNull)
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
                        ExperimentIdentifier experimentIdentifier =
                                new ExperimentIdentifier(entity.getIdentifier());
                        loadGeneLocationsPanel(materialId, experimentIdentifier, container);
                    }
                }
            });
        chooserField.setEditable(false);
        if (experimentIdentifierOrNull != null)
        {
            chooserField.updateValue(experimentIdentifierOrNull);
        }

        container.add(GuiUtils.withLabel(experimentChooser.getField(), "Experiment:", 10));
        container.add(new Text(
                "Choose an experiment to find wells where this gene has been suppressed."));
        container.setScrollMode(Scroll.AUTO);
        return container;
    }

    private void loadGeneLocationsPanel(TechId materialId,
            ExperimentIdentifier experimentIdentifier, final LayoutContainer container)
    {
        GuiUtils
                .replaceLastItem(container, new Text(viewContext.getMessage(Dict.LOAD_IN_PROGRESS)));
        viewContext.getService().getPlateLocations(materialId, experimentIdentifier,
                new AbstractAsyncCallback<List<WellContent>>(viewContext)
                    {
                        @Override
                        protected void process(List<WellContent> wellLocations)
                        {
                            Widget geneLocationsPanel = createGeneLocationPanel(wellLocations);
                            GuiUtils.replaceLastItem(container, geneLocationsPanel);
                        }
                    });
    }

    private Widget createGeneLocationPanel(final List<WellContent> wellLocations)
    {
        if (wellLocations.size() == 0)
        {
            return new Text(
                    "This gene has not been suppressed in any plate measured in the chosen experiment.");
        }
        int totalChannels = findMaxChannelNumber(wellLocations);
        return ChannelChooser.createViewerWithChannelChooser(new IChanneledViewerFactory()
            {
                public Widget create(int channel)
                {
                    return createGeneLocationPanel(wellLocations, channel);
                }
            }, channelState, totalChannels);
    }

    private static int findMaxChannelNumber(List<WellContent> wells)
    {
        int max = 0;
        for (WellContent well : wells)
        {
            TileImages images = well.tryGetImages();
            if (images != null)
            {
                max = Math.max(max, images.getImageParameters().getChannelsNum());
            }
        }
        return max;
    }

    private Widget createGeneLocationPanel(List<WellContent> wellLocations, int channel)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new TableLayout(3));
        TableData cellLayout = new TableData();
        cellLayout.setPadding(20);
        for (WellContent loc : wellLocations)
        {
            container.add(createLocationDescription(loc, channel), cellLayout);
        }
        return container;
    }

    private LayoutContainer createLocationDescription(WellContent wellContent, int channel)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        int margin = 4;

        Widget plateLink = createEntityLink(wellContent.getPlate());
        container.add(withLabel(plateLink, "Plate: ", margin));

        Widget wellLink = createEntityLink(wellContent.getWell());
        container.add(withLabel(wellLink, "Well: ", margin));

        Widget contentLink = createEntityLink(wellContent.getMaterialContent());
        container.add(withLabel(contentLink, "Content: ", margin));

        TileImages images = wellContent.tryGetImages();
        if (images != null)
        {
            Widget datasetLink = createEntityLink(images.getDatasetReference(), "browse");
            container.add(withLabel(datasetLink, "Dataset: ", margin));

            container.add(createImageViewer(images, wellContent.tryGetLocation(), channel));
        }
        return container;
    }

    private Widget createImageViewer(TileImages images, WellLocation locationOrNull, int channel)
    {
        if (locationOrNull == null)
        {
            return new Text("Incorrect well code.");
        }
        if (channel > images.getImageParameters().getChannelsNum())
        {
            return new Text("No images available for this channel.");
        }
        WellImages wellImages = new WellImages(images, locationOrNull);
        return WellContentDialog.createTilesGrid(wellImages, channel, viewContext, IMAGE_WIDTH_PX,
                IMAGE_HEIGHT_PX);
    }

    private Widget createEntityLink(IEntityInformationHolder entityInformationHolder)
    {
        return createEntityLink(entityInformationHolder, entityInformationHolder.getCode());
    }

    private Widget createEntityLink(IEntityInformationHolder entityInformationHolder, String label)
    {
        final ClickHandler listener =
                new OpenEntityDetailsTabClickListener(entityInformationHolder, viewContext);
        return LinkRenderer.getLinkWidget(label, listener);
    }

    public static final String createId(final IIdentifiable materialId)
    {
        return PREFIX + materialId.getId();
    }

    public static HelpPageIdentifier getHelpPageIdentifier()
    {
        return HelpPageIdentifier.createSpecific("Gene Material Viewer");
    }
}
