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
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils.GuiUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * A dialog which shows the content of the well.
 * 
 * @author Tomasz Pylak
 */
public class WellContentDialog
{
    private final WellMetadata metadataOrNull;

    private final IViewContext<?> viewContext;

    public static void showContentDialog(final WellData wellData, final IViewContext<?> viewContext)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());

        final WellContentDialog contentDialog =
                new WellContentDialog(wellData.tryGetMetadata(), viewContext);
        LayoutContainer descriptionContainer = contentDialog.createContentDescription();
        container.add(descriptionContainer);

        int dialogWidth;
        int dialogHeight;

        final WellImages images = wellData.tryGetImages();
        if (images != null)
        {
            int imgW = 200;
            int imgH = 120;
            LayoutContainer imageViewer = createImageViewer(images, viewContext, imgW, imgH);
            container.add(imageViewer);

            dialogWidth = imgW * images.getTileColsNum();
            dialogHeight = imgH * images.getTileRowsNum() + 200;
        } else
        {
            dialogWidth = 250;
            dialogHeight = 160;
        }
        String title = "Well Content: " + wellData.getWellContentDescription();
        showWellContentDialog(container, dialogWidth, dialogHeight, title);
    }

    public static LayoutContainer createImageViewer(final WellImages images,
            final IViewContext<?> viewContext, final int imageWidth, final int imageHeight)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());

        int channelsNum = images.getChannelsNum();

        final List<String> channelNames = createChannelsDescriptions(channelsNum);
        if (channelsNum > 1)
        {
            ComboBox<SimpleComboValue<String>> channelChooser = createChannelChooser(channelNames);
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
                                        createWellsGrid(images, channel, viewContext, imageWidth,
                                                imageHeight);
                                GuiUtils.replaceLastItem(container, wellsGrid);
                            }
                        });
            container.add(GuiUtils.withLabel(channelChooser, "Channel:"));
        }
        LayoutContainer wellsGrid =
                createWellsGrid(images, 1, viewContext, imageWidth, imageHeight);
        container.add(wellsGrid);
        return container;
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
        if (metadataOrNull != null)
        {
            container.add(new Text("Well: "), cellLayout);
            container.add(createEntityLink(metadataOrNull.getWellSample()));

            Material content = metadataOrNull.tryGetContent();
            if (content != null)
            {
                Material gene = metadataOrNull.tryGetGene();
                if (gene != null)
                {
                    container.add(new Text("Inhibited gene: "), cellLayout);
                    container.add(createEntityLink(gene));

                    container.add(new Text("Gene details: "), cellLayout);
                    container.add(createEntityExternalLink(gene));
                }

                container.add(new Text("Content: "), cellLayout);
                container.add(createEntityLink(content));
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
        return LinkRenderer.getLinkWidget(entity.getCode(), listener);
    }

    private WellContentDialog(WellMetadata metadataOrNull, IViewContext<?> viewContext)
    {
        this.metadataOrNull = metadataOrNull;
        this.viewContext = viewContext;
    }

    private static LayoutContainer createWellsGrid(WellImages images, int channel,
            IViewContext<?> viewContext, int imageWidth, int imageHeight)
    {
        LayoutContainer container = new LayoutContainer(new TableLayout(images.getTileColsNum()));
        for (int row = 1; row <= images.getTileRowsNum(); row++)
        {
            for (int col = 1; col <= images.getTileColsNum(); col++)
            {
                Component tileContent;
                String imagePath = images.getImagePath(channel, row, col);
                String sessionId = getSessionId(viewContext);
                String imageURL =
                        SimpleDatastoreImageRenderer.createDatastoreImageUrl(imagePath, imageWidth,
                                imageHeight, images.getDownloadUrl(), sessionId);
                tileContent = new Html(imageURL);
                tileContent.setHeight("" + imageHeight);
                PlateStyleSetter.setPointerCursor(tileContent);
                container.add(tileContent);
            }
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
