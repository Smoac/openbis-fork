package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
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
    }

    public void addPanel(final SingleSectionPanel panel)
    {
        final SectionElement element = new SectionElement(panel, withShowHide, viewContext);
        element.getButton().addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    removeAll();
                    for (SectionElement el : elements)
                    {
                        if (el.getButton().pressed)
                        {
                            internalAdd(el);
                        }
                    }
                    layout();
                }
            });
        elements.add(element);
        addToToolbar(element.getButton());
        if (element.getButton().pressed)
            internalAdd(element);
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
                IViewContext<ICommonClientServiceAsync> viewContext)
        {
            panel.setCollapsible(false);
            this.setPanel(panel);
            String heading = panel.getHeading();
            Boolean sectionSetting =
                    viewContext.getModel().getSessionContext().getDisplaySettings()
                            .getSectionSettings().get(panel.getDisplayID());
            boolean pressed = sectionSetting != null ? sectionSetting : true;
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

        ContentPanel getPanel()
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
            result.pressed = pressed;
            result.addSelectionListener(new SelectionListener<ComponentEvent>()
                {
                    @Override
                    public void componentSelected(ComponentEvent ce)
                    {
                        result.pressed = (result.pressed == false);
                        result.setText(getHeading(heading, withShowHide, result.pressed));
                        viewContext.getDisplaySettingsManager().storeSectionSettings(displayId,
                                result.pressed);
                    }
                });
            return result;
        }
    }

}