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

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public SectionsPanel(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FillLayout());
        toolbar = new TabPanel();
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

                /** checks if update of section settings and refresh of layout is needed */
                private boolean isRefreshNeeded()
                {
                    boolean result = false;
                    for (SectionElement sectionElement : elements)
                    {
                        if (lastRefreshCheckTime == null)
                        {
                            // No need to refresh when sections are displayed for the first time.
                            return false;
                        } else if (isModificationDoneInAnotherViewSinceLastRefresh(sectionElement))
                        {
                            // Section settings have been modified in another view of the
                            // same type. Refresh of section settings is needed.
                            return true;
                        }
                        // do nothing - other sections may have been modified
                    }
                    return result;
                }

                private boolean isModificationDoneInAnotherViewSinceLastRefresh(
                        SectionElement element)
                {
                    final String sectionID = element.getPanel().getDisplayID();
                    final Modification lastModificationOrNull =
                            viewContext.getDisplaySettingsManager()
                                    .tryGetLastSectionSettingsModification(sectionID);
                    return lastModificationOrNull != null
                            && lastModificationOrNull.getModifier().equals(SectionsPanel.this) == false
                            && lastModificationOrNull.getTime() > lastRefreshCheckTime;
                }

                /** updates all section settings */
                private void updateSettings()
                {
                    for (SectionElement sectionElement : elements)
                    {
                        final String sectionID = sectionElement.getPanel().getDisplayID();
                        Boolean newSettings =
                                viewContext.getDisplaySettingsManager().getSectionSettings(
                                        sectionID);
                        if (newSettings != null)
                        {
                            // sectionElement.getButton().toggle(newSettings);
                        }
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
        // panel.setContentVisible(true);
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
                IViewContext<ICommonClientServiceAsync> viewContext)
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

}
