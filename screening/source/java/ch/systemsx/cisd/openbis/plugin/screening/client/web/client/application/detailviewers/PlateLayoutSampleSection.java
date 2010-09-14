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

import java.util.Arrays;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetReportGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * A section of plate detail view which shows where the oligo and gene samples are located on the
 * plate and allow to check the content of the well quickly.
 * 
 * @author Tomasz Pylak
 */
public class PlateLayoutSampleSection extends TabContent
{
    public static final String ID_SUFFIX = "PlateLayoutSection";

    private final ScreeningViewContext viewContext;

    private final TechId sampleId;

    public PlateLayoutSampleSection(final ScreeningViewContext viewContext, final TechId sampleId)
    {
        super("Plate Layout", viewContext);
        this.viewContext = viewContext;
        this.sampleId = sampleId;
        setDisplayID(DisplayTypeIDGenerator.CONTAINER_SAMPLES_SECTION, ID_SUFFIX);
    }

    @Override
    protected void showContent()
    {
        add(new Text(viewContext.getMessage(Dict.LOAD_IN_PROGRESS)));
        viewContext.getService().getPlateContent(sampleId, createDisplayPlateCallback(viewContext));
    }

    private AsyncCallback<PlateContent> createDisplayPlateCallback(
            final ScreeningViewContext context)
    {
        return new AbstractAsyncCallback<PlateContent>(context)
            {
                @Override
                protected void process(PlateContent plateContent)
                {
                    removeAll();
                    setLayout(new RowLayout());
                    setScrollMode(Scroll.AUTO);

                    renderPlate(plateContent);
                    addImageAnalysisButton(plateContent);

                    addMetadataTable(plateContent);

                    layout();
                }
            };
    }

    private void addImageAnalysisButton(final PlateContent plateContent)
    {
        Component analysisPanel;
        int datasetsNumber = plateContent.getImageAnalysisDatasetsNumber();
        final DatasetReference dataset = plateContent.tryGetImageAnalysisDataset();
        if (dataset != null)
        {
            assert datasetsNumber == 1 : "only one image analysis dataset expected, but found: "
                    + datasetsNumber;
            Button generateButton =
                    new Button("Show Image Analysis Results", new SelectionListener<ButtonEvent>()
                        {
                            @Override
                            public void componentSelected(ButtonEvent ce)
                            {
                                generateImageAnalysisReport(dataset, plateContent);
                            }
                        });
            analysisPanel = generateButton;
        } else
        {
            if (datasetsNumber == 0)
            {
                analysisPanel = new Text("No image analysis data is available.");
            } else
            {
                analysisPanel =
                        new Text("There are " + datasetsNumber + " analysis datasets, "
                                + "select the one of your interest from the 'Data Sets' section "
                                + "and go to its detail view to see the image analysis results.");
            }
        }
        add(analysisPanel, PlateLayouter.createRowLayoutMarginData());
    }

    private void generateImageAnalysisReport(DatasetReference dataset, PlateContent plateContent)
    {
        DatastoreServiceDescription service = createImageAnalysisReporter(dataset, plateContent);
        DisplayedOrSelectedDatasetCriteria criteria =
                DisplayedOrSelectedDatasetCriteria.createSelectedItems(Arrays.asList(dataset
                        .getCode()));
        DataSetReportGenerator.generate(viewContext.getCommonViewContext(), service, criteria);
    }

    private DatastoreServiceDescription createImageAnalysisReporter(DatasetReference dataset,
            PlateContent plateContent)
    {
        String reportLabel = "Image Analysis of " + plateContent.getPlate().getCode();
        return new DatastoreServiceDescription(ScreeningConstants.PLATE_IMAGE_ANALYSIS_REPORT_KEY,
                reportLabel, new String[] {}, dataset.getDatastoreCode());
    }

    private void renderPlate(PlateContent plateContent)
    {
        LayoutContainer container = new LayoutContainer();
        Widget datasetNumberLegend = tryRenderImageDatasetsNumberLegend(plateContent);
        if (datasetNumberLegend != null)
        {
            container.add(datasetNumberLegend);
        }
        container
                .add(PlateLayouter.createVisualization(plateContent.getPlateImages(), viewContext));

        add(container, PlateLayouter.createRowLayoutMarginData());
    }

    private Widget tryRenderImageDatasetsNumberLegend(PlateContent plateContent)
    {
        int datasetsNumber = plateContent.getImageDatasetsNumber();
        if (datasetsNumber == 0)
        {
            return new Text("No images data is available.");
        } else if (datasetsNumber == 1)
        {
            return null;
        } else
        {
            return new Text("There are " + datasetsNumber + " datasets with images, "
                    + "select the one of your interest from the 'Data Sets' section "
                    + "and go to its detail view to browse acquired images.");
        }
    }

    private void addMetadataTable(final PlateContent plateContent)
    {
        Button generateButton =
                PlateLayouter.createPlateMetadataButton(plateContent.getPlate(), viewContext);
        add(generateButton, PlateLayouter.createRowLayoutMarginData());
    }
}
