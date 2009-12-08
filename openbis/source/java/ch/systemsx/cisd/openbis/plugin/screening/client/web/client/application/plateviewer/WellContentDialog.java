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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.renderers.SimpleDatastoreImageRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;

/**
 * A dialog which shows the content of the well.
 * 
 * @author Tomasz Pylak
 */
public class WellContentDialog
{
    private final WellData wellData;

    private final PlateContent plateContent;

    private final IViewContext<?> viewContext;

    public static void show(final WellData wellData, final PlateContent plateContent,
            final IViewContext<?> viewContext)
    {
        int imgWidth = 200;
        int imgHeight = 120;
        int tileRowsNum = plateContent.getTileRowsNum();
        int tileColsNum = plateContent.getTileColsNum();

        LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());

        WellContentDialog viewer = new WellContentDialog(wellData, plateContent, viewContext);
        container.add(viewer.createContentDescription());
        LayoutContainer wellsGrid =
                viewer.createWellsGrid(imgWidth, imgHeight, tileRowsNum, tileColsNum);
        container.add(wellsGrid);

        int dialogWidth = imgWidth * tileColsNum;
        int dialogHeight = imgHeight * tileRowsNum + 160;
        String title = "Well Content: " + wellData.getWellSubcode();
        showWellContentDialog(container, dialogWidth, dialogHeight, title);
    }

    private Widget createContentDescription()
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new TableLayout(2));
        WellMetadata metadata = wellData.tryGetMetadata();
        if (metadata != null)
        {
            container.add(new Text("Well: "));
            container.add(createEntityLink(metadata.getWellSample()));

            Material content = metadata.tryGetContent();
            if (content != null)
            {
                container.add(new Text("Content: "));
                container.add(createEntityLink(content));

                Material gene = metadata.tryGetGene();
                if (gene != null)
                {
                    container.add(new Text("Inhibited gene: "));
                    container.add(createEntityLink(gene));
                }
            }
        } else
        {
            container.add(new Text("No metadata available."));
        }
        return container;
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

    private LayoutContainer createWellsGrid(int imgWidth, int imgHeight, int tileRowsNum,
            int tileColsNum)
    {
        LayoutContainer container = new LayoutContainer(new TableLayout(tileColsNum));
        for (int i = 1; i <= tileRowsNum * tileColsNum; i++)
        {
            Widget tileContent;
            // TODO 2009-12-08, Tomasz Pylak: allow to choose a channel
            int channel = 1;
            String imagePath = wellData.tryGetImagePath(channel, i);
            if (imagePath != null)
            {
                // if we have a path, then download URL should be also available
                String downloadUrl = plateContent.tryGetImages().getDownloadUrl();
                String sessionId = getSessionId(viewContext);
                String imageURL =
                        SimpleDatastoreImageRenderer.createDatastoreImageUrl(imagePath, imgWidth,
                                imgHeight, downloadUrl, sessionId);
                tileContent = new Html(imageURL);
            } else
            {
                tileContent = new Text("No image.");
                tileContent.setWidth("" + imgWidth);
                tileContent.setHeight("" + imgHeight);
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
