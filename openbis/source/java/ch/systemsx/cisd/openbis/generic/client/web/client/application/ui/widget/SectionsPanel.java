package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager.Modification;

/**
 * Content panel which allows to choose which contained panels should be visible and uses the whole
 * space available to show them.
 * 
 * @author Izabela Adamczyk
 */
public class SectionsPanel extends ContentPanel
{
    public static final String POSTFIX_BUTTON_ID = "_button";

    private List<SectionElement> elements = new ArrayList<SectionElement>();

    private final ToolBar toolbar;

    private final boolean withShowHide;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public SectionsPanel(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this(true, viewContext);
    }

    private SectionsPanel(boolean withShowHide, IViewContext<ICommonClientServiceAsync> viewContext)
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
                        // need to set time here otherwise invoking layout will cause an infinite
                        // loop
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
                            sectionElement.getButton().toggle(newSettings);
                        }
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
        element.getButton().addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    refreshLayout();
                }
            });
        elements.add(element);
        addToToolbar(element.getButton());
        updateElementVisibility(element);
    }

    /** removes all sections and adds them once again with with refreshed state */
    private void refreshLayout()
    {
        // NOTE: changing visibility of only those sections that changed state doesn't improve
        // performance but makes code more complex
        removeAll();
        for (SectionElement el : elements)
        {
            updateElementVisibility(el);
        }
        layout();
    }

    private void updateElementVisibility(SectionElement element)
    {
        boolean visible = element.getButton().isPressed();
        element.getPanel().setContentVisible(visible);
        if (visible)
        {
            internalAdd(element);
        }
    }

    private void addToToolbar(ToggleButton bb)
    {
        toolbar.add(bb);
    }

    private void internalAdd(final SectionElement element)
    {
        super.add(element.getPanel());
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

    private class SectionElement
    {

        private final ToggleButton button;

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
            button = createButton(heading, pressed, panel.getDisplayID());
        }

        public ToggleButton getButton()
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

        private String getHeading(String heading, boolean pressed)
        {
            final String showHeading = withShowHide ? ("Show " + heading) : heading;
            final String hideHeading = withShowHide ? ("Hide " + heading) : heading;
            return pressed ? hideHeading : showHeading;
        }

        private ToggleButton createButton(final String heading, boolean pressed,
                final String displayId)
        {
            final ToggleButton result = new ToggleButton(getHeading(heading, pressed));
            result.setId(GenericConstants.ID_PREFIX + displayId + POSTFIX_BUTTON_ID);
            initializePressedState(result, pressed);

            // when user clicks toggle button we store changed settings
            result.addSelectionListener(new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce)
                    {
                        viewContext.getDisplaySettingsManager().storeSectionSettings(displayId,
                                result.isPressed(), SectionsPanel.this);
                    }
                });
            // heading needs to be updated also when we refresh settings using toggle
            result.addListener(Events.Toggle, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        result.setText(getHeading(heading, result.isPressed()));
                    }
                });

            return result;
        }

        private void initializePressedState(ToggleButton result, boolean pressed)
        {
            result.toggle(pressed);
        }
    }

}
