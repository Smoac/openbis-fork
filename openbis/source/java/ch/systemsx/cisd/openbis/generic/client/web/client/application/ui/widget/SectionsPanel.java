package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToggleToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;

/**
 * Content panel which allows to choose which contained panels should be visible and uses the whole
 * space available to show them.
 * 
 * @author Izabela Adamczyk
 */
public class SectionsPanel extends ContentPanel
{
    List<SectionElement> elements = new ArrayList<SectionElement>();

    private ToolBar toolbar;

    private final boolean withShowHide;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public SectionsPanel(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this(true, viewContext);
    }

    public SectionsPanel(boolean withShowHide, IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.withShowHide = withShowHide;
        this.viewContext = viewContext;
        setLayout(new FillLayout());
        toolbar = new ToolBar();
        setHeaderVisible(false);
        setTopComponent(toolbar);

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
                        lastRefreshCheckTime = System.currentTimeMillis();
                        updateSettings();
                        refreshLayout();
                    }
                    lastRefreshCheckTime = System.currentTimeMillis();
                }

                /** checks if update of section settings and refresh of layout is needed */
                private boolean isRefreshNeeded()
                {
                    boolean result = false;
                    for (SectionElement sectionElement : elements)
                    {
                        final String sectionID = sectionElement.getPanel().getDisplayID();
                        final Long lastModificationTimeOrNull =
                                viewContext.getDisplaySettingsManager()
                                        .tryGetSectionSettingsModificationTime(sectionID);
                        if (lastRefreshCheckTime == null)
                        {
                            // No need to refresh when sections are displayed for the first time.
                            return false;
                        } else if (lastModificationTimeOrNull != null
                                && lastModificationTimeOrNull > lastRefreshCheckTime)
                        {
                            // Section settings have been modified in another browser of the
                            // same type. Refresh of section settings is needed.
                            return true;
                        }
                    }
                    return result;
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
                        sectionElement.getButton().toggle(newSettings);
                    }
                }

            });
    }

    public void addPanel(final SingleSectionPanel panel)
    {
        addPanel(panel, true);
    }

    public void addPanel(final SingleSectionPanel panel, boolean pressByDeafult)
    {
        final SectionElement element =
                new SectionElement(panel, withShowHide, viewContext, pressByDeafult);
        element.getButton().addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    refreshLayout();
                }
            });
        elements.add(element);
        addToToolbar(element.getButton());
        if (element.getButton().isPressed())
            internalAdd(element);
    }

    /** removes all sections and adds them once again with with refreshed state */
    private void refreshLayout()
    {
        removeAll();
        for (SectionElement el : elements)
        {
            if (el.getButton().isPressed())
            {
                internalAdd(el);
            }
        }
        layout();
    }

    private void addToToolbar(ToggleToolItem bb)
    {
        toolbar.add(bb);
    }

    public void removePanel(final SingleSectionPanel panel)
    {
        int index = elements.indexOf(panel);
        if (index > -1)
        {
            internalRemove(panel);
            elements.remove(index);
            toolbar.remove(toolbar.getItem(index));
        }
    }

    private void internalAdd(final SectionElement element)
    {
        super.add(element.getPanel());
    }

    private void internalRemove(final ContentPanel panel)
    {
        super.remove(panel);
    }

    /**
     * Use {@link #removePanel(SingleSectionPanel)}
     */
    @Deprecated
    @Override
    protected boolean remove(Component item)
    {
        return super.remove(item);
    }

    /**
     * Use {@link #addPanel(SingleSectionPanel)}
     */
    @Deprecated
    @Override
    protected boolean add(Component item)
    {
        return super.add(item);
    }

    private static class SectionElement
    {

        private ToggleToolItem button;

        private SingleSectionPanel panel;

        public SectionElement(SingleSectionPanel panel, boolean withShowHide,
                IViewContext<ICommonClientServiceAsync> viewContext, boolean defaultPressedValue)
        {
            panel.setCollapsible(false);
            this.setPanel(panel);
            String heading = panel.getHeading();
            Boolean sectionSettings =
                    viewContext.getDisplaySettingsManager()
                            .getSectionSettings(panel.getDisplayID());
            boolean pressed = sectionSettings != null ? sectionSettings : defaultPressedValue;
            button =
                    createButton(heading, withShowHide, pressed, panel.getDisplayID(), viewContext);
        }

        public ToggleToolItem getButton()
        {
            return button;
        }

        void setPanel(SingleSectionPanel panel)
        {
            this.panel = panel;
        }

        SingleSectionPanel getPanel()
        {
            return panel;
        }

        private static String getHeading(String heading, boolean withShowHide, boolean pressed)
        {
            final String showHeading = withShowHide ? ("Show " + heading) : heading;
            final String hideHeading = withShowHide ? ("Hide " + heading) : heading;
            return pressed ? hideHeading : showHeading;
        }

        private static ToggleToolItem createButton(final String heading,
                final boolean withShowHide, boolean pressed, final String displayId,
                final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            final ToggleToolItem result =
                    new ToggleToolItem(getHeading(heading, withShowHide, pressed));
            initializePressedState(result, pressed);

            // when user clicks toggle button we store changed settings
            result.addSelectionListener(new SelectionListener<ComponentEvent>()
                {
                    @Override
                    public void componentSelected(ComponentEvent ce)
                    {
                        viewContext.getDisplaySettingsManager().storeSectionSettings(displayId,
                                result.isPressed());
                    }
                });
            // heading needs to be updated also when we refresh settings using toggle
            result.addListener(Events.Toggle, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        result.setText(getHeading(heading, withShowHide, result.isPressed()));
                    }
                });

            return result;
        }

        private static void initializePressedState(ToggleToolItem result, boolean pressed)
        {
            // because of strange ToggleToolItem implementation need to initialize both:
            // - 'pressed' value
            result.pressed = pressed;
            // - internal button pressed state using 'toggle(boolean)'
            result.toggle(pressed);
        }
    }

}