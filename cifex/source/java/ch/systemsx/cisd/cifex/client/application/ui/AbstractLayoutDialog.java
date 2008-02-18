/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.LayoutDialog;
import com.gwtext.client.widgets.LayoutDialogConfig;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.ContentPanel;
import com.gwtext.client.widgets.layout.LayoutRegionConfig;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;

/**
 * An abstract <code>LayoutDialog</code> which incorporates a button in the south region.
 * <p>
 * To finalize the construction of this <code>LayoutDialog</code> you must call {@link #addContentPanel()} in your
 * extension.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractLayoutDialog extends LayoutDialog
{

    private static final String DEFAULT_CLOSE_BUTTON_LABEL = "Close";

    private static final int MINIMUM_SIZE = 300;

    protected static final int DEFAULT_WIDTH = 500;

    protected static final int DEFAULT_HEIGHT = 300;

    protected final ViewContext viewContext;

    protected AbstractLayoutDialog(final ViewContext viewContext, final String title)
    {
        this(viewContext, title, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    protected AbstractLayoutDialog(final ViewContext viewContext, final String title, final int width, final int height)
    {
        this(viewContext, title, width, height, false, true);
    }

    protected AbstractLayoutDialog(final ViewContext viewContext, final String title, final int width,
            final int height, final boolean modal, final boolean closable)
    {
        super(createLayoutDialogConfig(title, width, height, modal, closable), createLayoutRegionConfig());
        this.viewContext = viewContext;
        if (closable)
        {
            createCloseButton();
        }
    }

    /** Calls this method in your extension to finalize the construction of this dialog. */
    protected final void addContentPanel()
    {
        final ContentPanel contentPanel = new ContentPanel();
        contentPanel.add(createContentWidget());
        getLayout().add(contentPanel);
    }

    private final static LayoutRegionConfig createLayoutRegionConfig()
    {
        final LayoutRegionConfig config = new LayoutRegionConfig();
        return config;
    }

    protected final static LayoutDialogConfig createLayoutDialogConfig(final String title, final int width,
            final int height, final boolean modal, final boolean closable)
    {
        final LayoutDialogConfig config = new LayoutDialogConfig();
        config.setTitle(title);
        config.setModal(modal);
        config.setWidth(width);
        config.setHeight(height);
        config.setClosable(closable);
        config.setShadow(true);
        config.setFixedCenter(true);
        config.setMinHeight(MINIMUM_SIZE);
        config.setMinWidth(MINIMUM_SIZE);
        return config;
    }

    private final void createCloseButton()
    {
        final IMessageResources messageResources = viewContext.getMessageResources();
        final String buttonLabel =
                messageResources == null ? DEFAULT_CLOSE_BUTTON_LABEL : messageResources.getDialogCloseButtonLabel();
        final Button button = addButton(buttonLabel);
        button.addButtonListener(new ButtonListenerAdapter()
            {

                //
                // ButtonListenerAdapter
                //

                public final void onClick(final Button but, final EventObject e)
                {
                    hide();
                }
            });
    }

    /** Creates the <code>Widget</code> that is going to be added in the center region. */
    protected abstract Widget createContentWidget();
}