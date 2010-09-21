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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;

/**
 * A section of dataset detail view for datasets containing HCS images. Shows where the oligo and
 * gene samples are located on the plate and allow to check the content of the well quickly.
 * 
 * @author Tomasz Pylak
 */
public class PlateLayoutDatasetSection extends TabContent
{
    public static final String ID_SUFFIX = "PlateLayoutDatasetSection";

    private final ScreeningViewContext viewContext;

    private final TechId datasetId;

    public PlateLayoutDatasetSection(final ScreeningViewContext viewContext, final TechId datasetId)
    {
        super("Plate Layout", viewContext, datasetId);
        this.viewContext = viewContext;
        this.datasetId = datasetId;
        setIds(DisplayTypeIDGenerator.PLATE_LAYOUT_DATASET_SECTION);
    }

    @Override
    protected void showContent()
    {
        add(new Text(viewContext.getMessage(Dict.LOAD_IN_PROGRESS)));

        viewContext.getService().getPlateContentForDataset(datasetId,
                createDisplayPlateCallback(viewContext));
    }

    private AsyncCallback<PlateImages> createDisplayPlateCallback(final ScreeningViewContext context)
    {
        return new AbstractAsyncCallback<PlateImages>(context)
            {
                @Override
                protected void process(PlateImages plateContent)
                {
                    removeAll();
                    setLayout(new RowLayout());
                    setScrollMode(Scroll.AUTO);

                    renderPlate(plateContent);

                    layout();
                }
            };
    }

    private void renderPlate(PlateImages plateImages)
    {
        LayoutContainer container = new LayoutContainer();
        container.add(PlateLayouter.createVisualization(plateImages, viewContext));
        add(container, PlateLayouter.createRowLayoutMarginData());
    }
}
