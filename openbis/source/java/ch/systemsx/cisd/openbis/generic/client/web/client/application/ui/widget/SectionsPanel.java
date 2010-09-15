package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager.Modification;

/**
 * {@link LayoutContainer} which allows to choose which contained panels should be visible and uses
 * the whole space available to show them.
 * 
 * @author Izabela Adamczyk
 */
public class SectionsPanel extends LayoutContainer
{
    public static final String POSTFIX_SECTION_TAB_ID = "_sections_tab";

    private final List<SectionElement> elements = new ArrayList<SectionElement>();

    private final TabPanel toolbar;

    private String displayId;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public SectionsPanel(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FillLayout());
        toolbar = new TabPanel();
        toolbar.setAutoSelect(false);
        super.add(toolbar);
        addRefreshDisplaySettingsListener();
    }

    private void addRefreshDisplaySettingsListener()
    {
        // all sections are refreshed in one go
        addListener(Events.AfterLayout, new Listener<BaseEvent>()
            {

                private Long lastRefreshCheckTime;

                public void handleEvent(BaseEvent be)
                {
                    if (isRefreshNeeded())
                    {
                        // need to set time here otherwise invoking layout will cause an infinite
                        // loop
                        lastRefreshCheckTime = System.currentTimeMillis();
                        updateSettings();
                    }
                    lastRefreshCheckTime = System.currentTimeMillis();
                }

                /** checks if update of tab settings and refresh of layout is needed */
                private boolean isRefreshNeeded()
                {
                    if (lastRefreshCheckTime == null
                            || isModificationDoneInAnotherViewSinceLastRefresh())
                    {
                        // Refresh when panel is displayed for the first time or if
                        // tab settings have been modified in another view of the
                        // same type.
                        return true;
                    }
                    return false;
                }

                private boolean isModificationDoneInAnotherViewSinceLastRefresh()
                {
                    final Modification lastModificationOrNull =
                            viewContext.getDisplaySettingsManager()
                                    .tryGetLastTabSettingsModification(getDisplayID());
                    return lastModificationOrNull != null
                            && lastModificationOrNull.getModifier().equals(SectionsPanel.this) == false
                            && lastModificationOrNull.getTime() > lastRefreshCheckTime;
                }

                /** updates all section settings */
                private void updateSettings()
                {
                    for (SectionElement sectionElement : elements)
                    {
                        final String thisTabID = sectionElement.getPanel().getDisplayID();
                        String tabToActivateID =
                                viewContext.getDisplaySettingsManager().getTabSettings(
                                        getDisplayID());
                        if (tabToActivateID != null && tabToActivateID.equals(thisTabID))
                        {
                            toolbar.setSelection(sectionElement);
                            return;
                        }
                    }
                    if (elements.size() > 0)
                    {
                        toolbar.setSelection(elements.get(0));
                    }
                }

            });
    }

    public void addPanel(final TabContent panel)
    {

        final SectionElement element = new SectionElement(panel, viewContext);
        // sections will be disposed when section panel is removed, not when they are hidden
        // (see onDetach())
        panel.disableAutoDisposeComponents();
        elements.add(element);
        addToToolbar(element);
    }

    @Override
    protected void onDetach()
    {
        for (SectionElement el : elements)
        {
            el.getPanel().disposeComponents();
        }
        super.onDetach();
    }

    private void addToToolbar(SectionElement element)
    {
        toolbar.add(element);
    }

    /**
     * Use {@link #addPanel(TabContent)}
     */
    @Deprecated
    @Override
    protected boolean add(Component item)
    {
        return super.add(item);
    }

    private class SectionElement extends TabItem
    {

        private TabContent panel;

        public SectionElement(final TabContent panel,
                final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            setClosable(false);
            setLayout(new FitLayout());
            this.setPanel(panel);
            setText(panel.getHeading());
            panel.setHeaderVisible(false);
            add(panel);

            addListener(Events.Select, new Listener<TabPanelEvent>()
                {
                    public void handleEvent(TabPanelEvent be)
                    {
                        panel.setContentVisible(true);
                        layout();
                        viewContext.getDisplaySettingsManager().storeTabSettings(getDisplayID(),
                                panel.getDisplayID(), SectionsPanel.this);
                    }
                });
        }

        void setPanel(TabContent panel)
        {
            this.panel = panel;
        }

        TabContent getPanel()
        {
            return panel;
        }
    }

    public String getDisplayID()
    {
        if (displayId == null)
        {
            throw new IllegalStateException("Undefined display ID");
        } else
        {
            return displayId;
        }
    }

    public void setDisplayID(IDisplayTypeIDGenerator generator, String suffix)
    {
        if (suffix != null)
        {
            this.displayId = generator.createID(suffix);
        } else
        {
            this.displayId = generator.createID();
        }
    }

}
