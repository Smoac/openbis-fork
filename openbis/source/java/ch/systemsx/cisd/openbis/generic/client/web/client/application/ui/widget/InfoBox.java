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

import com.extjs.gxt.ui.client.util.TextMetrics;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;

/**
 * A {@link Html} extension that should be used as information panel.
 * <p>
 * It nicely informs the user about a certain action result. It should be used instead of a
 * {@link MessageBox} in some cases.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class InfoBox extends Html
{

    public InfoBox()
    {
        setStyleAttribute("textAlign", "center");
        setStyleAttribute("borderStyle", "solid");
        setStyleAttribute("borderWidth", "1px");
        setHeight(computeHeight());
        reset();
    }

    private final int computeHeight()
    {
        final TextMetrics textMetrics = TextMetrics.get();
        textMetrics.bind(getElement());
        return textMetrics.getHeight("X");
    }

    /**
     * Display given <var>text</var> as <i>error</i> text.
     */
    public final void displayError(final String text)
    {
        display(text, Type.ERROR);
    }

    /**
     * Display given <var>text</var> as <i>info</i> text.
     */
    public final void displayInfo(final String text)
    {
        display(text, Type.INFO);
    }

    /**
     * Displays given <var>text</var> of given <var>type</var>.
     */
    public final void display(final String text, final Type type)
    {
        if (StringUtils.isBlank(text) == false)
        {
            setStyleAttribute("backgroundColor", type.backgroundColor);
            setStyleAttribute("borderColor", type.borderColor);
            setHtml(text);
        }
    }

    /**
     * Resets the info box.
     * <p>
     * Background resp. border color are reset to <i>white</i>. And <i>HTML</i> text is reset to
     * empty string.
     * </p>
     */
    public final void reset()
    {
        setStyleAttribute("backgroundColor", "#ffffff");
        setStyleAttribute("borderColor", "#ffffff");
        setHtml("");
    }

    //
    // Helper classes
    //

    private static enum Type
    {

        ERROR("#f6cece", "#f5a9a9"), INFO("#cef6ce", "#a9f5a9");

        private final String backgroundColor;

        private final String borderColor;

        private Type(final String backgroundColor, final String borderColor)
        {

            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
        }
    }
}