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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * {@link PagingToolBar} extension with overwritten behavior of the <i>Refresh</i> button and
 * additional <i>Export</i> button.
 * 
 * @author Tomasz Pylak
 */
public final class BrowserGridPagingToolBar extends PagingToolBar
{
    // @Private
    public static final String REFRESH_BUTTON_ID =
            GenericConstants.ID_PREFIX + "paged-grid-refresh-button";

    public static final String CONFIG_BUTTON_ID =
            GenericConstants.ID_PREFIX + "paged-grid-config-button";

    private final IMessageProvider messageProvider;

    private final Button exportButton;

    private final Button refreshButton;

    private final Button configButton;

    private int nextTableButtonIndex;

    public BrowserGridPagingToolBar(IBrowserGridActionInvoker invoker,
            IMessageProvider messageProvider, int pageSize, String gridId)
    {
        super(pageSize);
        nextTableButtonIndex = indexOf(refresh);
        remove(refresh);

        this.messageProvider = messageProvider;

        insertTableButton(createTableOperationsLabel());

        this.configButton = createConfigButton(messageProvider, invoker, gridId);
        insertTableButton(configButton);
        updateDefaultConfigButton(false);

        this.refreshButton = createRefreshButton(invoker);
        insertTableButton(refreshButton);
        updateDefaultRefreshButton(false);
        this.refreshButton.setId(REFRESH_BUTTON_ID);

        this.exportButton = createExportButton(messageProvider, invoker);
        disableExportButton();
        insertTableButton(exportButton);
    }

    /** Total number of items on all pages */
    public int getTotalCount()
    {
        return totalLength;
    }

    /**
     * Adding table specific buttons right after 'original refresh' button.
     */
    private void insertTableButton(Component item)
    {
        insert(item, nextTableButtonIndex);
        nextTableButtonIndex++;
    }

    public final void addEntityOperationsLabel()
    {
        add(new FillToolItem());
        add(new LabelToolItem(messageProvider.getMessage(Dict.ENTITY_OPERATIONS)));
    }

    public boolean isDefaultRefreshButtonEnabled()
    {
        return refreshButton.isEnabled();
    }

    protected final void updateDefaultRefreshButton(boolean isEnabled)
    {
        updateRefreshButton(refreshButton, isEnabled, messageProvider);
    }

    protected final void updateDefaultConfigButton(boolean isEnabled)
    {
        updateConfigButton(configButton, isEnabled, messageProvider);
    }

    /**
     * Refreshes the 'configure' button state.
     */
    public static final void updateConfigButton(Button button, boolean isEnabled,
            IMessageProvider messageProvider)
    {
        button.setEnabled(isEnabled);
        if (isEnabled)
        {
            button.setTitle(messageProvider.getMessage(Dict.TOOLTIP_CONFIG_ENABLED));
        } else
        {
            button.setTitle(messageProvider.getMessage(Dict.TOOLTIP_CONFIG_DISABLED));
        }
    }

    /**
     * Refreshes the refresh button state.
     */
    public static final void updateRefreshButton(Button refreshButton, boolean isEnabled,
            IMessageProvider messageProvider)
    {
        refreshButton.setEnabled(isEnabled);
        if (isEnabled)
        {
            refreshButton.setTitle(messageProvider.getMessage(Dict.TOOLTIP_REFRESH_ENABLED));
        } else
        {
            refreshButton.setTitle(messageProvider.getMessage(Dict.TOOLTIP_REFRESH_DISABLED));
        }
    }

    public final void enableExportButton()
    {
        exportButton.setEnabled(true);
        String title = messageProvider.getMessage(Dict.TOOLTIP_EXPORT_ENABLED);
        exportButton.setTitle(title);
    }

    public final void disableExportButton()
    {
        exportButton.setEnabled(false);
        String title = messageProvider.getMessage(Dict.TOOLTIP_EXPORT_DISABLED);
        exportButton.setTitle(title);
    }

    private Button createRefreshButton(final IBrowserGridActionInvoker invoker)
    {
        return createRefreshButton(messageProvider.getMessage(Dict.BUTTON_REFRESH), invoker);
    }

    /** creates a new refresh button, the caller has to add it to a parent container */
    public static Button createRefreshButton(String title, final IBrowserGridActionInvoker invoker)
    {
        final Button button = new Button(title, new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    if (ce.getButton().isEnabled())
                    {
                        invoker.refresh();
                    }
                }
            });
        return button;
    }

    /** creates a new export button, the caller has to add it to a parent container */
    public static Button createExportButton(IMessageProvider messageProvider,
            final IBrowserGridActionInvoker invoker)
    {
        final Button button =
                new Button(messageProvider.getMessage(Dict.BUTTON_EXPORT_TABLE),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    invoker.export();
                                }
                            });
        return button;
    }

    /**
     * creates a grid configuration button, the caller has to add it to a parent container
     * 
     * @param gridId
     */
    private static Button createConfigButton(IMessageProvider messageProvider,
            final IBrowserGridActionInvoker invoker, String gridId)
    {
        final Button button =
                new Button(messageProvider.getMessage(Dict.BUTTON_CONFIGURE),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    invoker.configure();
                                }
                            });
        button.setId(CONFIG_BUTTON_ID + gridId);
        return button;
    }

    private Component createTableOperationsLabel()
    {
        return new LabelToolItem(messageProvider.getMessage(Dict.TABLE_OPERATIONS));
    }

    // HACK
    //
    // In GXT 1.2 ToolBar items were subclasses of ToolItems and we added widgets
    // to BrowserGridPagingToolbar wrapped in AdapterToolItems. Now components are kept directly as
    // items of ToolBar that extends a Container.
    //
    // Implementation of Container enable() and disable() methods enables and disables all items.
    // Before it didn't change state of widgets - AdapterToolItem didn't invoke enable/disable on
    // them. With new implementation we lost disabled state set to e.g. buttons when grid data were
    // loaded and toolbar enabled everything.
    //
    // Solution chosen here is to use Component enable and disable implementation that does nothing
    // with items as we know that we don't want to change their state.
    //
    // Other solution would be to wrap every component added to BrowserGridPagingToolbar in sth that
    // delegates everything except enable and disable method invocation but there are lots of
    // methods in Component to delegate.

    @Override
    public void enable()
    {
        if (rendered)
        {
            onEnable();
        }
        disabled = false;
        fireEvent(Events.Enable);
    }

    @Override
    public void disable()
    {
        if (rendered)
        {
            onDisable();
        }
        disabled = true;
        fireEvent(Events.Disable);
    }
}
