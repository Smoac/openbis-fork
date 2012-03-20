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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;

/**
 * @author pkupczyk
 */
public abstract class MovieButtons extends Composite
{

    private int currentFrame;

    private int numberOfFrames;

    private MovieButtonsView view;

    private MovieButtonsState state;

    public MovieButtons(int numberOfFrames)
    {
        this.currentFrame = 0;
        this.numberOfFrames = numberOfFrames;
        initView();
        initState();
    }

    private void initView()
    {
        view = new MovieButtonsView();

        view.addPlayListener(new SelectionListener<ButtonEvent>()
            {
                public void componentSelected(ButtonEvent event)
                {
                    handlePlay();
                }
            });
        view.addStopListener(new SelectionListener<ButtonEvent>()
            {
                public void componentSelected(ButtonEvent event)
                {
                    handleStop();
                }
            });
        view.addPreviousListener(new SelectionListener<ButtonEvent>()
            {
                public void componentSelected(ButtonEvent event)
                {
                    handlePrevious();
                }
            });
        view.addNextListener(new SelectionListener<ButtonEvent>()
            {
                public void componentSelected(ButtonEvent event)
                {
                    handleNext();
                }
            });

        initWidget(view);
    }

    private void initState()
    {
        setState(new MovieButtonsStoppedState());
    }

    private MovieButtonsState getState()
    {
        return state;
    }

    private void setState(MovieButtonsState newState)
    {
        state = newState;
        state.init();
    }

    public int getFrame()
    {
        return state.handleGetFrame();
    }

    public void setFrame(int frame)
    {
        state.handleSetFrame(frame);
    }

    private boolean isFirstFrame()
    {
        return getFrame() == 0;
    }

    private boolean isLastFrame()
    {
        return getFrame() == (numberOfFrames - 1);
    }

    protected abstract void loadFrame(int frame, AsyncCallback<Void> callback);

    private void loadFrame(int frame)
    {
        loadFrame(frame, new AsyncCallback<Void>()
            {
                public void onSuccess(Void result)
                {
                }

                public void onFailure(Throwable caught)
                {
                }
            });
    }

    private void handlePlay()
    {
        state.handlePlay();
    }

    private void handleStop()
    {
        state.handleStop();
    }

    private void handlePrevious()
    {
        state.handlePrevious();
    }

    private void handleNext()
    {
        state.handleNext();
    }

    private class MovieButtonsView extends Composite
    {
        private Button playButton;

        private Button stopButton;

        private Button previousButton;

        private Button nextButton;

        public MovieButtonsView()
        {
            playButton = new Button("Play");
            stopButton = new Button("Stop");
            previousButton = new Button("<<");
            nextButton = new Button(">>");

            Panel panel = new HorizontalPanel();
            panel.setStyleName("movieButtons");
            panel.add(playButton);
            panel.add(stopButton);
            panel.add(previousButton);
            panel.add(nextButton);

            initWidget(panel);
        }

        public void addPlayListener(SelectionListener<ButtonEvent> listener)
        {
            playButton.addSelectionListener(listener);
        }

        public void addStopListener(SelectionListener<ButtonEvent> listener)
        {
            stopButton.addSelectionListener(listener);
        }

        public void addPreviousListener(SelectionListener<ButtonEvent> listener)
        {
            previousButton.addSelectionListener(listener);
        }

        public void addNextListener(SelectionListener<ButtonEvent> listener)
        {
            nextButton.addSelectionListener(listener);
        }

        public void setPlayEnabled(boolean enabled)
        {
            playButton.setEnabled(enabled);
        }

        public void setStopEnabled(boolean enabled)
        {
            stopButton.setEnabled(enabled);
        }

        public void setPreviousEnabled(boolean enabled)
        {
            previousButton.setEnabled(enabled);
        }

        public void setNextEnabled(boolean enabled)
        {
            nextButton.setEnabled(enabled);
        }

    }

    private interface MovieButtonsState
    {
        public void init();

        public void handlePlay();

        public void handleStop();

        public void handlePrevious();

        public void handleNext();

        public int handleGetFrame();

        public void handleSetFrame(int frame);
    }

    private class MovieButtonsStoppedState implements MovieButtonsState
    {

        public void init()
        {
            view.setPlayEnabled(true);
            view.setStopEnabled(false);
            view.setPreviousEnabled(!isFirstFrame());
            view.setNextEnabled(!isLastFrame());
        }

        public void handlePlay()
        {
            if (isLastFrame())
            {
                setFrame(0);
            }
            setState(new MovieButtonsPlayingState());
        }

        public void handleStop()
        {
            // do nothing
        }

        public void handlePrevious()
        {
            if (!isFirstFrame())
            {
                setFrame(getFrame() - 1);
            }
        }

        public void handleNext()
        {
            if (!isLastFrame())
            {
                setFrame(getFrame() + 1);
            }
        }

        public int handleGetFrame()
        {
            return currentFrame;
        }

        public void handleSetFrame(int frame)
        {
            currentFrame = frame;
            view.setPreviousEnabled(!isFirstFrame());
            view.setNextEnabled(!isLastFrame());
            loadFrame(frame);
        }
    }

    private class MovieButtonsPlayingState implements MovieButtonsState
    {

        private static final int PREFFERED_DELAY_BETWEEN_FRAMES_IN_MILLIS = 100;

        public void init()
        {
            view.setPlayEnabled(false);
            view.setStopEnabled(true);
            view.setPreviousEnabled(true);
            view.setNextEnabled(true);
            loadNextFrame(1);
        }

        public void handlePlay()
        {
            // do nothing
        }

        public void handleStop()
        {
            setState(new MovieButtonsStoppedState());
        }

        public void handlePrevious()
        {
            handleStop();
        }

        public void handleNext()
        {
            handleStop();
        }

        public int handleGetFrame()
        {
            return currentFrame;
        }

        public void handleSetFrame(int frame)
        {
            currentFrame = frame;
        }

        private void loadNextFrame(int delay)
        {
            Timer timer = new Timer()
                {
                    public void run()
                    {
                        final long startTime = System.currentTimeMillis();

                        if (isLastFrame())
                        {
                            handleStop();
                            setFrame(0);
                        } else
                        {
                            setFrame(getFrame() + 1);

                            loadFrame(getFrame(), new AsyncCallback<Void>()
                                {

                                    public void onSuccess(Void result)
                                    {
                                        if (MovieButtonsPlayingState.this == getState())
                                        {
                                            int currentDelay =
                                                    (int) (System.currentTimeMillis() - startTime);

                                            if (currentDelay < PREFFERED_DELAY_BETWEEN_FRAMES_IN_MILLIS)
                                            {
                                                loadNextFrame(PREFFERED_DELAY_BETWEEN_FRAMES_IN_MILLIS
                                                        - currentDelay);
                                            } else
                                            {
                                                loadNextFrame(1);
                                            }
                                        }
                                    }

                                    public void onFailure(Throwable caught)
                                    {
                                        onSuccess(null);
                                    }

                                });
                        }
                    }
                };
            timer.schedule(delay);
        }
    }

}
