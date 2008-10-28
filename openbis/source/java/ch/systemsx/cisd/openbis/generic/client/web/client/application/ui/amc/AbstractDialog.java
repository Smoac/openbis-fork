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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.ui.Widget;

/**
 * An <i>abstract</i> {@link Dialog} extension.
 * <p>
 * Subclasses MUST call {@link #addWidget()} method.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractDialog extends Dialog
{
    public static final int DEFAULT_WIDTH = 500;

    public static final int DEFAULT_HEIGHT = 300;

    public AbstractDialog(final String heading, final int width, final int height)
    {
        setHeading(heading);
        setButtons(OK);
        setScrollMode(Scroll.AUTO);
        setWidth(width);
        setHeight(height);
        setBodyStyle("backgroundColor: #ffffff;");
        setHideOnButtonClick(true);
    }

    protected final void addWidget()
    {
        add(getWidget());
    }

    public AbstractDialog(final String heading)
    {
        this(heading, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Returns the {@link Widget} that should be added as main component.
     */
    public abstract Widget getWidget();
}
