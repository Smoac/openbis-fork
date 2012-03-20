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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SliderEvent;
import com.extjs.gxt.ui.client.widget.Slider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author pkupczyk
 */
public abstract class MovieButtonsWithSlider extends Composite
{

    private MovieButtons buttons;

    private Slider slider;

    public MovieButtonsWithSlider(int numberOfFrames)
    {
        buttons = new MovieButtons(numberOfFrames)
            {
                protected void loadFrame(int frame, AsyncCallback<Void> callback)
                {
                    MovieButtonsWithSlider.this.loadFrame(frame, callback);
                    slider.setValue(frame + 1, true);
                }
            };

        slider = new Slider();
        // we do not want the slider to be long when there are just few points
        slider.setWidth(Math.min(230, Math.max(100, numberOfFrames * 10)));
        slider.setIncrement(1);
        slider.setMinValue(1);
        slider.setMaxValue(numberOfFrames);
        slider.setClickToChange(true);
        slider.setUseTip(false);
        slider.addListener(Events.Change, new Listener<SliderEvent>()
            {
                public void handleEvent(SliderEvent be)
                {
                    buttons.setFrame(be.getNewValue() - 1);
                    MovieButtonsWithSlider.this.loadFrame(be.getNewValue() - 1,
                            new AsyncCallback<Void>()
                                {
                                    public void onSuccess(Void result)
                                    {
                                    }

                                    public void onFailure(Throwable caught)
                                    {
                                    }
                                });
                };
            });

        Panel panel = new VerticalPanel();
        panel.add(slider);
        panel.add(buttons);
        initWidget(panel);
    }

    protected abstract void loadFrame(int frame, AsyncCallback<Void> callback);

}
