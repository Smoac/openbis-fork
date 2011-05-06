package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.KeyboardEvents;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.KeyCodes;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Shows {@link DetailedSearchCriteriaWidget}, allowing to specify detailed search criteria.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DetailedSearchWindow extends Dialog
{
    public static final String SEARCH_BUTTON_ID = DataSetSearchHitGrid.BROWSER_ID + "search_button";

    private static final Margins MARGINS = new Margins(5, 10, 5, 10);

    private static final int HEIGHT = 400;

    private static final int WIDTH = 550;

    private final DetailedSearchCriteriaWidget criteriaWidget;

    private final List<DetailedSearchSubCriteriaWidget> subCriteriaWidgets =
            new ArrayList<DetailedSearchSubCriteriaWidget>();

    private DetailedSearchToolbar updateListener;

    private final TabPanel tabPanel;

    public DetailedSearchWindow(final IViewContext<ICommonClientServiceAsync> viewContext,
            final EntityKind entityKind)
    {
        setSize(WIDTH, HEIGHT);
        setModal(true);
        setHeading("Sample Search Criteria");
        setLayout(new FillLayout());
        setResizable(false);
        criteriaWidget = new DetailedSearchMainCriteriaWidget(viewContext, entityKind);
        add(createMainCriteriaPanel());
        add(tabPanel = new TabPanel());
        for (AssociatedEntityKind association : getAssociatedEntityKinds(entityKind))
        {
            DetailedSearchSubCriteriaWidget subCriteriaWidget =
                    new DetailedSearchSubCriteriaWidget(viewContext, association);
            subCriteriaWidgets.add(subCriteriaWidget);
            addSearchWidgetTab(subCriteriaWidget);
        }
        addEnterListener();
        final ButtonBar bar = getButtonBar();
        bar.removeAll();
        bar.add(new Button(viewContext.getMessage(Dict.BUTTON_CANCEL),
                new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            hide();
                        }
                    }));
        bar.add(new Button(viewContext.getMessage(Dict.BUTTON_RESET),
                new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            for (DetailedSearchCriteriaWidget widget : getAllWidgets())
                            {
                                widget.reset();
                            }
                        }
                    }));
        final Button searchButton =
                new Button(viewContext.getMessage(Dict.SEARCH_BUTTON),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    onSearch();
                                }
                            });

        searchButton.setId(SEARCH_BUTTON_ID);
        bar.add(searchButton);

        DialogWithOnlineHelpUtils.addHelpButton(viewContext, this,
                createHelpPageIdentifier(entityKind));
    }

    private ContentPanel createMainCriteriaPanel()
    {
        final ContentPanel mainPanel = new ContentPanel();
        mainPanel.setHeaderVisible(false);
        mainPanel.setLayout(new FitLayout());
        mainPanel.setScrollMode(Scroll.AUTOY);
        mainPanel.add(criteriaWidget, new FitData(MARGINS));
        return mainPanel;
    }

    private void addSearchWidgetTab(final DetailedSearchSubCriteriaWidget searchWidget)
    {
        final TabItem tab = new TabItem();
        tab.setClosable(false);
        tab.setLayout(new FitLayout());
        tab.setScrollMode(Scroll.AUTOY);
        tab.setText(searchWidget.getCriteriaLabel());
        tab.add(searchWidget, new FitData(MARGINS));
        tab.setHideMode(HideMode.OFFSETS);
        tabPanel.add(tab);
    }

    /** @return list containing main widget and all sub criteria widgets */
    private List<DetailedSearchCriteriaWidget> getAllWidgets()
    {
        List<DetailedSearchCriteriaWidget> result =
                new ArrayList<DetailedSearchCriteriaWidget>(subCriteriaWidgets);
        result.add(criteriaWidget);
        return result;
    }

    private void addEnterListener()
    {
        for (DetailedSearchCriteriaWidget widget : getAllWidgets())
        {
            widget.addListener(KeyboardEvents.Enter, new Listener<ComponentEvent>()
                {
                    public void handleEvent(ComponentEvent ce)
                    {
                        EventType type = ce.getType();
                        switch (type.getEventCode())
                        {
                            case KeyCodes.KEY_ENTER:
                                onSearch();
                                break;
                            default:
                                break;
                        }

                    }

                });
        }
    }

    @Override
    protected void afterShow()
    {
        super.afterShow();
        criteriaWidget.focus();
    }

    public DetailedSearchCriteria tryGetCriteria()
    {
        final DetailedSearchCriteria mainCriteria = criteriaWidget.extractCriteria();
        for (DetailedSearchSubCriteriaWidget subCriteriaWidget : subCriteriaWidgets)
        {
            if (subCriteriaWidget.isCriteriaFilled())
            {
                final DetailedSearchSubCriteria subCriteria =
                        subCriteriaWidget.extractSubCriteria();
                mainCriteria.addSubCriteria(subCriteria);
            }
        }

        if (mainCriteria.isEmpty())
        {
            return null;
        } else
        {
            return mainCriteria;
        }
    }

    public String getCriteriaDescription()
    {
        StringBuilder sb = new StringBuilder();
        if (criteriaWidget.isCriteriaFilled())
        {
            sb.append(criteriaWidget.getCriteriaDescription());
        }
        for (DetailedSearchCriteriaWidget subCriteriaWidget : subCriteriaWidgets)
        {
            if (subCriteriaWidget.isCriteriaFilled())
            {
                sb.append(", ");
                sb.append(subCriteriaWidget.getCriteriaDescription());
            }
        }
        if (criteriaWidget.isCriteriaFilled() == false)
        {
            sb.delete(0, 2);
        }
        return sb.toString();
    }

    public void setUpdateListener(DetailedSearchToolbar toolbar)
    {
        this.updateListener = toolbar;
    }

    /**
     * Return a help page identifier, possibly using the entity kind as clue to determine it. The
     * default implementation ignores the entity kind and returns an identifier referencing the
     * advanced search domain. Subclasses may override.
     */
    protected HelpPageIdentifier createHelpPageIdentifier(final EntityKind entityKind)
    {
        // Do not use the entity kind -- in general we want all advanced search dialogs to refer to
        // the same help page.
        // return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.valueOf(entityKind
        // .toString()), HelpPageIdentifier.HelpPageAction.ACTION);
        return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.SEARCH,
                HelpPageIdentifier.HelpPageAction.ACTION);
    }

    /**
     * Set the initial search string for this window to the argument. Call this after instantiation,
     * but before the user has provided input, otherwise user input will be overwritten. This method
     * does not notify the listener of any changes -- the caller must keep the window and toolbar in
     * sync.
     */
    public void setInitialSearchCriteria(DetailedSearchCriteria searchCriteria)
    {
        // Set the widget
        criteriaWidget.setInitialSearchCritera(searchCriteria);
    }

    private void onSearch()
    {
        hide();
        List<PropertyType> availablePropertyTypes = criteriaWidget.getAvailablePropertyTypes();
        DetailedSearchCriteria criteria = tryGetCriteria();
        String criteriaDescription = getCriteriaDescription();
        updateListener.updateSearchResults(criteria, criteriaDescription, availablePropertyTypes);
    }

    private static List<AssociatedEntityKind> getAssociatedEntityKinds(final EntityKind sourceEntity)
    {
        // TODO 2011-05-06, Piotr Buczek: after extending data set search use the following
        // return AssociatedEntityKind.getAssociatedEntityKinds(sourceEntity)
        if (sourceEntity == EntityKind.SAMPLE)
        {
            return AssociatedEntityKind.getAssociatedEntityKinds(sourceEntity);
        } else
        {
            return Collections.emptyList();
        }
    }
}
