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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BorderLayoutEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.LayoutEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;

/**
 * {@link AbstractViewer} extension with additional support for vertical layout with left panel
 * state (expanded size / collapsed) saved in display settings.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractViewerWithVerticalSplit<D extends IEntityInformationHolder> extends
        AbstractViewer<D>
{
    private final static String LEFT_PANEL_PREFIX = "left_panel_";

    private final static int INITIAL_LEFT_PANEL_SIZE = 300;

    protected AbstractViewerWithVerticalSplit(IViewContext<?> viewContext, String id)
    {
        super(viewContext, id);
    }

    protected AbstractViewerWithVerticalSplit(final IViewContext<?> viewContext, String title,
            String id, boolean withToolBar)
    {
        super(viewContext, title, id, withToolBar);
    }

    protected final static BorderLayoutData createRightBorderLayoutData()
    {
        return createBorderLayoutData(LayoutRegion.CENTER);
    }

    /**
     * Creates {@link BorderLayoutData} for the left panel extracting initial size from display
     * settings.
     */
    protected final BorderLayoutData createLeftBorderLayoutData()
    {
        final String panelId = getLeftPanelId();
        float initialSize = getLeftPanelInitialSize();
        viewContext.log(panelId + " initial size: " + initialSize);

        BorderLayoutData layoutData =
                BorderLayoutDataFactory.create(LayoutRegion.WEST, initialSize);
        layoutData.setCollapsible(true);
        return layoutData;
    }

    /**
     * Adds listeners and sets up the initial left panel state.
     * <p>
     * Id for display settings needs to be initialized before this method is called.
     */
    protected void configureLeftPanel(final Component panel)
    {
        // displayIdSuffix must be initialized first
        if (isLeftPanelInitiallyCollapsed())
        {
            viewContext.log(displayIdSuffix + " Initially Collapsed");
            ((BorderLayout) getLayout()).collapse(LayoutRegion.WEST);
        }

        // Add the listeners after configuring the panel, so as not to cause confusion
        addLeftPanelCollapseExpandListeners(panel);
    }

    private void addLeftPanelCollapseExpandListeners(final Component panel)
    {
        final String panelId = getLeftPanelId();
        getLayout().addListener(Events.Collapse, new Listener<BorderLayoutEvent>()
            {
                public void handleEvent(BorderLayoutEvent be)
                {
                    viewContext.log(panelId + " Collapsed");
                    viewContext.getDisplaySettingsManager().updatePanelCollapsedSetting(panelId,
                            Boolean.TRUE);
                }

            });
        getLayout().addListener(Events.Expand, new Listener<BorderLayoutEvent>()
            {
                public void handleEvent(BorderLayoutEvent be)
                {
                    viewContext.log(panelId + " Expand");
                    viewContext.getDisplaySettingsManager().updatePanelCollapsedSetting(panelId,
                            Boolean.FALSE);
                }

            });
        getLayout().addListener(Events.AfterLayout, new Listener<LayoutEvent>()
            {
                public void handleEvent(LayoutEvent le)
                {
                    final Integer size = panel.getOffsetWidth();
                    viewContext.log(panelId + " AfterLayout, size: " + size);
                    // Left panel minimal size can't be less than 20 unless collapse button is used.
                    // We want to save size only if user dragged the splitter so that after restore
                    // from collapsed state the size before collapsing it will be used.
                    if (size > 0)
                    {
                        viewContext.getDisplaySettingsManager().updatePanelSizeSetting(panelId,
                                size);
                    }
                }
            });
    }

    private String getLeftPanelId()
    {
        return LEFT_PANEL_PREFIX + displayIdSuffix;
    }

    private boolean isLeftPanelInitiallyCollapsed()
    {
        Boolean collapsedOrNull =
                viewContext.getDisplaySettingsManager().tryGetPanelCollapsedSetting(
                        getLeftPanelId());
        return collapsedOrNull == null ? false : collapsedOrNull.booleanValue();
    }

    private int getLeftPanelInitialSize()
    {
        Integer sizeOrNull =
                viewContext.getDisplaySettingsManager().tryGetPanelSizeSetting(getLeftPanelId());
        return sizeOrNull == null ? INITIAL_LEFT_PANEL_SIZE : sizeOrNull.intValue();
    }

}
