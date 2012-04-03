/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.logicalimage;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Slider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.SliderWithAutoWidth;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.SliderWithMovieButtons;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.SliderWithMovieButtonsValueLoader;

/**
 * @author pkupczyk
 */
public class LogicalImageSeriesTimeAndDepthControls extends LogicalImageSeriesControls
{

    private LogicalImageSeriesControlWithLabel<SliderWithMovieButtons> time;

    private LogicalImageSeriesControlWithLabel<Slider> depth;

    public LogicalImageSeriesTimeAndDepthControls(LogicalImageSeriesDownloader imageDownloader,
            LogicalImageSeriesModel imageModel)
    {
        super(imageDownloader, imageModel);

        time = new LogicalImageSeriesControlWithLabel<SliderWithMovieButtons>();
        time.setControl(new SliderWithMovieButtons(getImageModel().getNumberOfTimepoints()));
        time.getControl().setValue(1);

        Label spacer = new Label();
        spacer.setWidth(80);

        depth = new LogicalImageSeriesControlWithLabel<Slider>();
        depth.setControl(new SliderWithAutoWidth(getImageModel().getNumberOfDepthLevels()));
        depth.getControl().setValue(1);

        Panel sliderContainer = new HorizontalPanel();
        sliderContainer.add(time);
        sliderContainer.add(spacer);
        sliderContainer.add(depth);
        add(sliderContainer);

        time.getControl().setValueLoader(new SliderWithMovieButtonsValueLoader()
            {
                @Override
                public void loadValue(int value, AsyncCallback<Void> callback)
                {
                    refreshValue(callback);
                    refreshLabels();
                }
            });

        depth.getControl().addListener(Events.Change, new Listener<SliderEvent>()
            {
                public void handleEvent(SliderEvent be)
                {
                    refreshValue(null);
                    refreshLabels();
                }
            });

        refreshLabels();
    }

    private void refreshValue(AsyncCallback<Void> callback)
    {
        int timeIndex = time.getControl().getValue();
        int depthIndex = depth.getControl().getValue();

        int frame = (timeIndex - 1) * getImageModel().getNumberOfDepthLevels() + (depthIndex - 1);
        getImageDownloader().frameSelectionChanged(frame, callback);

        layout();
    }

    private void refreshLabels()
    {
        int timeIndex = time.getControl().getValue();
        int depthIndex = depth.getControl().getValue();

        int numberOfTimePoints = getImageModel().getNumberOfTimepoints();
        int numberOfDepthLevels = getImageModel().getNumberOfDepthLevels();

        LogicalImageSeriesPoint point = getImageModel().get(timeIndex - 1, depthIndex - 1);

        time.setLabelText("Time: " + point.getTimePointOrNull() + " sec (" + timeIndex + "/"
                + numberOfTimePoints + ")");
        depth.setLabelText("Depth: " + point.getDepthOrNull() + " (" + depthIndex + "/"
                + numberOfDepthLevels + ")");
    }

}
