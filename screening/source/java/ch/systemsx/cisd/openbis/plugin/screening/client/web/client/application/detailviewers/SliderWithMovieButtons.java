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
public class SliderWithMovieButtons extends Composite
{

    private Slider slider;

    private MovieButtons buttons;

    private Loading loading;

    private int loadingCounter;

    private SliderWithMovieButtonsValueLoader valueLoader;

    public SliderWithMovieButtons(int numberOfValues)
    {
        valueLoader = SliderWithMovieButtonsValueLoader.NULL_LOADER;

        loading = new Loading();

        buttons = new MovieButtons(numberOfValues);
        buttons.setFrameLoader(new MovieButtonsFrameLoader()
            {
                public void loadFrame(final int frame, final AsyncCallback<Void> callback)
                {
                    showLoading();
                    slider.setValue(frame + 1, true);
                    valueLoader.loadValue(frame + 1, new AsyncCallback<Void>()
                        {
                            public void onSuccess(Void result)
                            {
                                hideLoading();
                                callback.onSuccess(result);
                            }

                            public void onFailure(Throwable caught)
                            {
                                hideLoading();
                                callback.onFailure(caught);
                            }
                        });
                }
            });

        slider = new SliderWithAutoWidth(numberOfValues);
        slider.addListener(Events.Change, new Listener<SliderEvent>()
            {
                public void handleEvent(SliderEvent be)
                {
                    showLoading();
                    buttons.setFrame(be.getNewValue() - 1);
                    valueLoader.loadValue(be.getNewValue(), new AsyncCallback<Void>()
                        {
                            public void onSuccess(Void result)
                            {
                                hideLoading();
                            }

                            public void onFailure(Throwable caught)
                            {
                                hideLoading();
                            }
                        });
                }
            });

        Panel panel = new VerticalPanel();
        panel.add(slider);
        panel.add(buttons);
        panel.add(loading);
        initWidget(panel);
    }

    public int getValue()
    {
        return slider.getValue();
    }

    public void setValue(int value)
    {
        slider.setValue(value);
    }

    public SliderWithMovieButtonsValueLoader getValueLoader()
    {
        return valueLoader;
    }

    public void setValueLoader(SliderWithMovieButtonsValueLoader valueLoader)
    {
        if (valueLoader == null)
        {
            throw new IllegalArgumentException("Value loader was null");
        }
        this.valueLoader = valueLoader;
    }

    private void showLoading()
    {
        loadingCounter++;
        if (loadingCounter >= 0)
        {
            loading.showLoading();
        }
    }

    private void hideLoading()
    {
        loadingCounter--;
        if (loadingCounter <= 0)
        {
            loading.hideLoading();
        }
    }

}
