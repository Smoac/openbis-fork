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

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.PlateLayouter;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * A section of a well detail view which shows images for the well.
 * 
 * @author Tomasz Pylak
 */
public class WellImageSampleSection extends TabContent
{

    // --- GUI messages (to be moved to the dictionary)

    private static final String TO_MANY_IMAGE_DATASETS_LABEL =
            "More than one image dataset exists, go to a plate view and click on the well with a particular image dataset chosen.";

    private static final String NO_IMAGES_DATASET_LABEL = "No images data has been acquired.";

    private static final String WELL_IMAGE_SECTION_TITLE = "Images";

    // ----

    private final ScreeningViewContext viewContext;

    private final TechId sampleId;

    private final WellLocation wellLocationOrNull;

    public WellImageSampleSection(final ScreeningViewContext viewContext, final TechId sampleId,
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
        List<LogicalImageInfo> images = imageSampleContent.getLogicalImages();
        if (images.size() == 0)
        {
            add(new Text(NO_IMAGES_DATASET_LABEL));
        } else if (images.size() > 1)
        {
            add(new Text(TO_MANY_IMAGE_DATASETS_LABEL));
        } else
        {
            LogicalImageInfo imageInfo = images.get(0);
            String experimentPermId = imageInfo.getDatasetReference().getExperimentPermId();
            LogicalImageReference logicalImageReference =
                    new LogicalImageReference(imageInfo.getDatasetImagesReference(),
                            wellLocationOrNull);
            LogicalImageViewer viewer =
                    new LogicalImageViewer(logicalImageReference,
                            WellImageSampleSection.this.viewContext, "", experimentPermId);
            Widget viewerWidget = viewer.getViewerWidget(imageInfo.getChannelStacks());
            add(viewerWidget, PlateLayouter.createRowLayoutSurroundingData());
        }
    }
}
