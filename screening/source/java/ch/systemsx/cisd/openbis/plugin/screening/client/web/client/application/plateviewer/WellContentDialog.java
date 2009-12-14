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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.plateviewer;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleDatastoreImageRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * A dialog which shows the content of the well.
 * 
 * @author Tomasz Pylak
 */
public class WellContentDialog
{
    private static final int TILE_IMG_WIDTH = 200;

    private static final int TILE_IMG_HEIGHT = 120;

    private final WellData wellData;

    private final PlateContent plateContent;

    private final IViewContext<?> viewContext;

    public static void show(final WellData wellData, final PlateContent plateContent,
            final IViewContext<?> viewContext)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());

        final WellContentDialog viewer = new WellContentDialog(wellData, plateContent, viewContext);
        LayoutContainer descriptionContainer = viewer.createContentDescription();
        container.add(descriptionContainer);

        int dialogWidth;
        int dialogHeight;
        PlateImages images = plateContent.tryGetImages();
        if (images != null)
        {
            PlateImageParameters imageParameters = images.getImageParameters();
            final int tileRowsNum = imageParameters.getTileRowsNum();
            final int tileColsNum = imageParameters.getTileColsNum();
            int channelsNum = imageParameters.getChannelsNum();

            final List<String> channelNames = createChannelsDescriptions(channelsNum);
            ComboBox<SimpleComboValue<String>> channelChooser = createChannelChooser(channelNames);
            descriptionContainer.add(new Text("Channel:"));
            descriptionContainer.add(channelChooser);
            channelChooser
                    .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
                        {
                            @Override
                            public void selectionChanged(
                                    SelectionChangedEvent<SimpleComboValue<String>> se)
                            {
                                String value = se.getSelectedItem().getValue();
                                int channel = channelNames.indexOf(value) + 1;
                                LayoutContainer wellsGrid =
                                        viewer.createWellsGrid(tileRowsNum, tileColsNum, channel);
                                int lastItemIx = container.getItemCount() - 1;
                                container.remove(container.getWidget(lastItemIx));
                                container.insert(wellsGrid, lastItemIx);
                                container.layout();
                            }
                        });
            LayoutContainer wellsGrid = viewer.createWellsGrid(tileRowsNum, tileColsNum, 1);
            container.add(wellsGrid);

            dialogWidth = TILE_IMG_WIDTH * tileColsNum;
            dialogHeight = TILE_IMG_HEIGHT * tileRowsNum + 160;
        } else
        {
            dialogWidth = 500;
            dialogHeight = 130;
        }
        String title = "Well Content: " + wellData.getWellSubcode();
        showWellContentDialog(container, dialogWidth, dialogHeight, title);
    }

    private static ComboBox<SimpleComboValue<String>> createChannelChooser(List<String> channelNames)
    {
        SimpleComboBox<String> combo = new SimpleComboBox<String>();
        combo.setTriggerAction(TriggerAction.ALL);
        combo.add(channelNames);
        combo.setAllowBlank(false);
        combo.setEditable(false);
        combo.setSimpleValue(channelNames.get(0));
        return combo;
    }

    private static List<String> createChannelsDescriptions(int channelsNum)
    {
        assert channelsNum > 0 : "there has to be at least one channel";

        final List<String> channelNames = new ArrayList<String>();
        for (int i = 1; i <= channelsNum; i++)
        {
            channelNames.add(createChannelName(i));
        }
        return channelNames;
    }

    private static String createChannelName(int channel)
    {
        return "Channel " + channel;
    }

    private LayoutContainer createContentDescription()
    {
        LayoutContainer container = new LayoutContainer();
        TableLayout tableLayout = new TableLayout(2);
        tableLayout.setCellPadding(2);
        container.setLayout(tableLayout);
        TableData cellLayout = new TableData();
        cellLayout.setMargin(2);
        WellMetadata metadata = wellData.tryGetMetadata();
        if (metadata != null)
        {
            container.add(new Text("Well: "), cellLayout);
            container.add(createEntityLink(metadata.getWellSample()));

            Material content = metadata.tryGetContent();
            if (content != null)
            {
                container.add(new Text("Content: "), cellLayout);
                container.add(createEntityLink(content));

                Material gene = metadata.tryGetGene();
                if (gene != null)
                {
                    container.add(new Text("Inhibited gene: "), cellLayout);
                    container.add(createEntityLink(gene));

                    container.add(new Text("Gene details: "), cellLayout);
                    container.add(createEntityExternalLink(gene));
                }
            }
        } else
        {
            container.add(new Text("No metadata available."));
        }
        return container;
    }

    private Widget createEntityExternalLink(Material gene)
    {
        String url = viewContext.getMessage(Dict.GENE_LIBRARY_URL, gene.getCode());
        return new Html(LinkRenderer.renderAsLinkWithAnchor("gene database", url, true));
    }

    private Widget createEntityLink(IEntityInformationHolder entity)
    {
        final ClickHandler listener = new OpenEntityDetailsTabClickListener(entity, viewContext);
        return LinkRenderer.getLinkWidget(entity.getIdentifier(), listener);
    }

    private WellContentDialog(WellData wellData, PlateContent plateContent,
            IViewContext<?> viewContext)
    {
        this.wellData = wellData;
        this.plateContent = plateContent;
        this.viewContext = viewContext;
    }

    private LayoutContainer createWellsGrid(int tileRowsNum, int tileColsNum, int channel)
    {
        LayoutContainer container = new LayoutContainer(new TableLayout(tileColsNum));
        for (int i = 1; i <= tileRowsNum * tileColsNum; i++)
        {
            Component tileContent;
            String imagePath = wellData.tryGetImagePath(channel, i);
            if (imagePath != null)
            {
                // if we have a path, then download URL should be also available
                String downloadUrl = plateContent.tryGetImages().getDownloadUrl();
                String sessionId = getSessionId(viewContext);
                String imageURL =
                        SimpleDatastoreImageRenderer.createDatastoreImageUrl(imagePath,
                                TILE_IMG_WIDTH, TILE_IMG_HEIGHT, downloadUrl, sessionId);
                tileContent = new Html(imageURL);
                PlateStyleSetter.setPointerCursor(tileContent);
            } else
            {
                tileContent = new Text("No image.");
                tileContent.setPixelSize(TILE_IMG_WIDTH, TILE_IMG_HEIGHT);
            }
            container.add(tileContent);
        }
        return container;
    }

    private static String getSessionId(IViewContext<?> viewContext)
    {
        return viewContext.getModel().getSessionContext().getSessionID();
    }

    private static void showWellContentDialog(LayoutContainer container, int width, int height,
            String title)
    {
        Dialog dialog = new Dialog();
        dialog.setHeading(title);
        dialog.setLayout(new FitLayout());
        dialog.setScrollMode(Scroll.AUTO);
        dialog.setHideOnButtonClick(true);
        dialog.add(container);
        dialog.setWidth(width);
        dialog.setHeight(height);
        dialog.show();
    }
}
