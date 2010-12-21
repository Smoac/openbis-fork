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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ImagingDatasetGuiUtils.IDatasetImagesReferenceUpdater;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto.LogicalImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * A section of a detail view which shows images for the well or microscopy sample.
 * 
 * @author Tomasz Pylak
 */
public class ImageSampleSection extends TabContent
{

    // --- GUI messages (to be moved to the dictionary)

    private static final String NO_IMAGES_DATASET_LABEL = "No images data has been acquired.";

    private static final String WELL_IMAGE_SECTION_TITLE = "Images";

    // ----

    private final ScreeningViewContext viewContext;

    private final TechId sampleId;

    private final WellLocation wellLocationOrNull;

    public ImageSampleSection(final ScreeningViewContext viewContext, final TechId sampleId,
            WellLocation wellLocationOrNull)
    {
        super(WELL_IMAGE_SECTION_TITLE, viewContext, sampleId);
        this.viewContext = viewContext;
        this.sampleId = sampleId;
        this.wellLocationOrNull = wellLocationOrNull;
        setIds(DisplayTypeIDGenerator.LOGICAL_IMAGE_WELL_SECTION);
    }

    @Override
    protected void showContent()
    {
        add(new Text(viewContext.getMessage(Dict.LOAD_IN_PROGRESS)));
        viewContext.getService().getImageDatasetInfosForSample(sampleId, wellLocationOrNull,
                createDisplayImagesCallback(viewContext));
    }

    private AsyncCallback<ImageSampleContent> createDisplayImagesCallback(
            final ScreeningViewContext context)
    {
        return new AbstractAsyncCallback<ImageSampleContent>(context)
            {
                @Override
                protected void process(ImageSampleContent imageSampleContent)
                {
                    removeAll();
                    setLayout(new RowLayout());
                    setScrollMode(Scroll.AUTO);

                    addVisualisation(imageSampleContent);

                    layout();
                }
            };
    }

    private void addVisualisation(ImageSampleContent imageSampleContent)
    {
        RowData margins = LayoutUtils.createRowLayoutSurroundingData();
        List<LogicalImageInfo> images = imageSampleContent.getLogicalImages();
        if (images.size() == 0)
        {
            add(new Text(NO_IMAGES_DATASET_LABEL), margins);
        } else
        {
            LogicalImageLayouter logicalImageLayouter =
                    new LogicalImageLayouter(viewContext, wellLocationOrNull, images);
            Widget imageDatasetsDetails =
                    new ImagingDatasetGuiUtils(viewContext)
                            .createImageDatasetDetailsRow(
                                    logicalImageLayouter.getDatasetImagesReferences(),
                                    logicalImageLayouter);

            DatasetImagesReference firstImageDataset = images.get(0).getDatasetImagesReference();
            logicalImageLayouter.changeDisplayedImageDataset(firstImageDataset);

            add(imageDatasetsDetails, margins);
            add(logicalImageLayouter, margins);
        }
    }

    private static class LogicalImageLayouter extends LayoutContainer implements
            IDatasetImagesReferenceUpdater
    {
        private final ScreeningViewContext viewContext;

        private final WellLocation wellLocationOrNull;

        private final Map<DatasetImagesReference, LogicalImageInfo> refsMap;

        public LogicalImageLayouter(ScreeningViewContext viewContext,
                WellLocation wellLocationOrNull, List<LogicalImageInfo> images)
        {
            this.viewContext = viewContext;
            this.wellLocationOrNull = wellLocationOrNull;
            this.refsMap = createRefsMap(images);
        }

        public void changeDisplayedImageDataset(DatasetImagesReference dataset)
        {
            LogicalImageInfo imageInfo = refsMap.get(dataset);
            assert imageInfo != null : "cannot find logical image for " + dataset;

            removeAll();
            Widget viewerWidget = createImageViewer(imageInfo);
            add(viewerWidget);
            layout();
        }

        private Widget createImageViewer(LogicalImageInfo imageInfo)
        {
            String experimentPermId = imageInfo.getDatasetReference().getExperimentPermId();
            LogicalImageReference logicalImageReference =
                    new LogicalImageReference(imageInfo.getDatasetImagesReference(),
                            wellLocationOrNull);
            LogicalImageViewer viewer =
                    new LogicalImageViewer(logicalImageReference, viewContext, "", experimentPermId);
            return viewer.getViewerWidget(imageInfo.getChannelStacks());
        }

        public List<DatasetImagesReference> getDatasetImagesReferences()
        {
            return new ArrayList<DatasetImagesReference>(refsMap.keySet());
        }

        private static Map<DatasetImagesReference, LogicalImageInfo> createRefsMap(
                List<LogicalImageInfo> images)
        {
            Map<DatasetImagesReference, LogicalImageInfo> map =
                    new HashMap<DatasetImagesReference, LogicalImageInfo>();
            for (LogicalImageInfo imageInfo : images)
            {
                DatasetImagesReference ref = imageInfo.getDatasetImagesReference();
                map.put(ref, imageInfo);
            }
            return map;
        }
    }

}
