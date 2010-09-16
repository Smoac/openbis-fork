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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Header;
import com.google.gwt.user.client.Window;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * A default {@link ITabItem} implementation with various factory methods.
 * 
 * @author Christian Ribeaud
 */
public class DefaultTabItem implements ITabItem
{
    private final TabTitleUpdater titleUpdater;

    private final Component component;

    private final IDelegatedAction disposerActionOrNull;

    private final boolean isCloseConfirmationNeeded;

    private final LastModificationStateUpdater lastModificationStateUpdaterOrNull;

    /**
     * Creates a tab with the specified {@link Component}. The tab is unaware of database
     * modifications and will not be automatically refreshed if changes occur.
     */
    public static ITabItem createUnaware(final String title, final Component component,
            boolean isCloseConfirmationNeeded)
    {
        return new DefaultTabItem(title, component, null, null, isCloseConfirmationNeeded);
    }

    /**
     * Creates a tab with the specified {@link ContentPanel}. The tab is unaware of database
     * modifications and will not be automatically refreshed if changes occur.
     */
    public static ITabItem createUnaware(final ContentPanel component,
            boolean isCloseConfirmationNeeded)
    {
        String title = getTabTitle(component);
        return new DefaultTabItem(title, component, null, null, isCloseConfirmationNeeded);
    }

    private static String getTabTitle(ContentPanel contentPanel)
    {
        final Header header = contentPanel.getHeader();
        return header != null ? header.getText() : contentPanel.getId();
    }

    /**
     * Creates a tab with the specified component. The tab is aware of database modifications and
     * will be automatically refreshed if relevant changes take place.
     */
    public static ITabItem create(final String title,
            final DatabaseModificationAwareComponent component, IViewContext<?> viewContext,
            boolean isCloseConfirmationNeeded)
    {
        return create(title, component.get(), viewContext, component, null,
                isCloseConfirmationNeeded);
    }

    public static ITabItem create(final String title, final IDisposableComponent component,
            IViewContext<?> viewContext)
    {
        boolean isCloseConfirmationNeeded = false;
        IDelegatedAction disposer = createDisposer(component);
        return create(title, component.getComponent(), viewContext, component, disposer,
                isCloseConfirmationNeeded);
    }

    private static DefaultTabItem create(final String title, final Component component,
            IViewContext<?> viewContext, IDatabaseModificationObserver modificationObserver,
            IDelegatedAction disposerActionOrNull, boolean isCloseConfirmationNeeded)
    {
        LastModificationStateUpdater updater =
                new LastModificationStateUpdater(viewContext, modificationObserver);
        return new DefaultTabItem(title, component, updater, disposerActionOrNull,
                isCloseConfirmationNeeded);
    }

    private static IDelegatedAction createDisposer(final IDisposableComponent disposableComponent)
    {
        return new IDelegatedAction()
            {
                public void execute()
                {
                    disposableComponent.dispose();
                }
            };
    }

    private DefaultTabItem(final String initialTitle, final Component component,
            LastModificationStateUpdater lastModificationStateUpdaterOrNull,
            IDelegatedAction disposerActionOrNull, boolean isCloseConfirmationNeeded)
    {
        assert initialTitle != null : "Unspecified title.";
        assert component != null : "Unspecified component.";
        this.titleUpdater = new TabTitleUpdater(initialTitle);
        this.component = component;
        this.lastModificationStateUpdaterOrNull = lastModificationStateUpdaterOrNull;
        this.disposerActionOrNull = disposerActionOrNull;
        // TODO 2009-05-08, Tomasz Pylak: uncomment this when confirmation will be asked only in
        // relevant moments.
        // this.isCloseConfirmationNeeded = isCloseConfirmationNeeded;
        this.isCloseConfirmationNeeded = false;
    }

    //
    // ITabItem
    //
    public final Component getComponent()
    {
        return component;
    }

    public final TabTitleUpdater getTabTitleUpdater()
    {
        return titleUpdater;
    }

    public boolean isCloseConfirmationNeeded()
    {
        return isCloseConfirmationNeeded;
    }

    public void onActivate()
    {
        if (lastModificationStateUpdaterOrNull != null)
        {
            lastModificationStateUpdaterOrNull.update();
        }
        Window.setTitle(getTabTitleUpdater().getCurrentTitle() + " - "
                + CommonViewContext.ClientStaticState.getPageTitleSuffix());
    }

    public void onClose()
    {
        if (disposerActionOrNull != null)
        {
            disposerActionOrNull.execute();
        }
    }
}
