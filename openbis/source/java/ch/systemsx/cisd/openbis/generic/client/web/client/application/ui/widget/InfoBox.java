/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Information panel that nicely informs a user about a certain action result. It should be used
 * instead of a {@link MessageBox} in some cases.
 * 
 * @author Christian Ribeaud
 */
public final class InfoBox extends Composite implements IInfoHandler
{
    private static final int TRUNCATE_THRESHOLD = 200;

    private static final String TRUNCATE_SUFFIX = "...";

    private static final String PLACEHOLDER_TEXT = "X";

    private static final String WHITE = "#ffffff";

    private Panel mainPanel;

    private String fullMessage;

    private InfoType messageType;

    private Html truncatedMessageHtml;

    private PopupDialogBasedInfoHandler fullMessageDialog;

    private Anchor showFullMessageLink;

    private Timer progressTimer;

    /**
     * Default constructor with {@link HasHorizontalAlignment#ALIGN_CENTER}.
     */
    public InfoBox(final IMessageProvider messageProvider)
    {
        this(messageProvider, HasHorizontalAlignment.ALIGN_DEFAULT);
    }

    public InfoBox(final IMessageProvider messageProvider,
            final HorizontalAlignmentConstant alignment)
    {
        truncatedMessageHtml = new Html();
        showFullMessageLink =
                new Anchor(messageProvider.getMessage(Dict.INFO_BOX_SHOW_FULL_MESSAGE));
        showFullMessageLink.addClickHandler(new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    if (fullMessageDialog != null)
                    {
                        fullMessageDialog.hide();
                    }
                    fullMessageDialog = new PopupDialogBasedInfoHandler(messageProvider);
                    fullMessageDialog.display(messageType, fullMessage);
                }
            });

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlignment(alignment);
        verticalPanel.add(truncatedMessageHtml);
        verticalPanel.add(showFullMessageLink);

        mainPanel = new SimplePanel();
        Style mainPanelStyle = mainPanel.getElement().getStyle();
        mainPanelStyle.setBorderStyle(BorderStyle.SOLID);
        mainPanelStyle.setBorderWidth(1, Unit.PX);
        mainPanelStyle.setPadding(3, Unit.PX);
        mainPanel.add(verticalPanel);

        initWidget(mainPanel);
        reset();
    }

    /**
     * Display given <var>text</var> as <i>error</i> text.
     */
    @Override
    public final void displayError(final String text)
    {
        display(text, InfoType.ERROR);
    }

    /**
     * Display given <var>text</var> as <i>info</i> text.
     */
    @Override
    public final void displayInfo(final String text)
    {
        display(text, InfoType.INFO);
    }

    /**
     * Display given <var>text</var> as <i>progress</i> text.
     */
    @Override
    public final void displayProgress(final String text)
    {
        display(text, InfoType.PROGRESS);

        progressTimer = new Timer()
            {
                String dots;

                @Override
                public void run()
                {
                    if (dots != null && dots.length() < 6)
                    {
                        dots += ".";
                    } else
                    {
                        dots = "...";
                    }

                    truncatedMessageHtml.setHtml(truncate(text) + dots);
                }
            };
        progressTimer.run();
        progressTimer.scheduleRepeating(500);
    }

    /**
     * Displays given <var>text</var> of given <var>type</var>.
     */
    public final void display(final String text, final InfoType type)
    {
        if (progressTimer != null)
        {
            progressTimer.cancel();
            progressTimer = null;
        }
        if (StringUtils.isBlank(text) == false)
        {
            Style mainPanelStyle = mainPanel.getElement().getStyle();
            mainPanelStyle.setColor("#000000");
            mainPanelStyle.setBackgroundColor(type.getBackgroundColor());
            mainPanelStyle.setBorderColor(type.getBorderColor());

            fullMessage = text;
            messageType = type;

            if (shouldTruncate(text))
            {
                truncatedMessageHtml.setHtml(truncate(text));
                showFullMessageLink.setVisible(true);
            } else
            {
                truncatedMessageHtml.setHtml(text);
                showFullMessageLink.setVisible(false);
            }

            if (fullMessageDialog != null)
            {
                fullMessageDialog.hide();
                fullMessageDialog = null;
            }

            getElement().scrollIntoView();
        }
    }

    /**
     * Resets the info box.
     * <p>
     * Background resp. border color are reset to <i>white</i>. And <i>HTML</i> text is reset to a
     * placeholder default text.
     * </p>
     */
    public final void reset()
    {
        if (progressTimer != null)
        {
            progressTimer.cancel();
            progressTimer = null;
        }

        // Make placeholder invisible.
        Style mainPanelStyle = mainPanel.getElement().getStyle();
        mainPanelStyle.setBackgroundColor(WHITE);
        mainPanelStyle.setBorderColor(WHITE);
        mainPanelStyle.setColor(WHITE);

        fullMessage = null;
        truncatedMessageHtml.setHtml(PLACEHOLDER_TEXT);
        showFullMessageLink.setVisible(false);

        if (fullMessageDialog != null)
        {
            fullMessageDialog.hide();
            fullMessageDialog = null;
        }
    }

    private boolean shouldTruncate(String text)
    {
        return text != null && text.length() > TRUNCATE_THRESHOLD;
    }

    private String truncate(String text)
    {
        if (shouldTruncate(text))
        {
            return text.substring(0, TRUNCATE_THRESHOLD) + TRUNCATE_SUFFIX;
        } else
        {
            return text;
        }
    }

}
