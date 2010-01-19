package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractDialogWithOnlineHelp;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Shows {@link DetailedSearchCriteriaWidget}, allowing to specify detailed search criteria.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DetailedSearchWindow extends AbstractDialogWithOnlineHelp
{
    public static final String SEARCH_BUTTON_ID = DataSetSearchHitGrid.BROWSER_ID + "search_button";

    private static final int MARGIN = 5;

    private static final int HEIGHT = 400;

    private static final int WIDTH = 550;

    private DetailedSearchCriteriaWidget criteriaWidget;

    private DetailedSearchToolbar updateListener;

    public DetailedSearchWindow(final IViewContext<ICommonClientServiceAsync> viewContext,
            final EntityKind entityKind)
    {
        setSize(WIDTH, HEIGHT);
        setModal(true);
        setScrollMode(Scroll.AUTOY);
        setLayout(new FitLayout());
        setResizable(false);
        add(criteriaWidget = new DetailedSearchCriteriaWidget(viewContext, entityKind),
                new FitData(MARGIN));
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
                            criteriaWidget.reset();
                        }
                    }));
        final Button searchButton =
                new Button(viewContext.getMessage(Dict.SEARCH_BUTTON),
                        new SelectionListener<ButtonEvent>()
                            {
                                @Override
                                public void componentSelected(ButtonEvent ce)
                                {
                                    List<PropertyType> availablePropertyTypes =
                                            criteriaWidget.getAvailablePropertyTypes();
                                    DetailedSearchCriteria criteria =
                                            criteriaWidget.tryGetCriteria();
                                    String criteriaDescription =
                                            criteriaWidget.getCriteriaDescription();
                                    updateListener.updateSearchResults(criteria,
                                            criteriaDescription, availablePropertyTypes);
                                    hide();
                                }
                            });

        searchButton.setId(SEARCH_BUTTON_ID);
        bar.add(searchButton);

        addHelpButton(viewContext, getHelpPageIdentifier(entityKind));
    }

    public DetailedSearchCriteria tryGetCriteria()
    {
        return criteriaWidget.tryGetCriteria();
    }

    public String getCriteriaDescription()
    {
        return criteriaWidget.getCriteriaDescription();
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
    protected HelpPageIdentifier getHelpPageIdentifier(final EntityKind entityKind)
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
    public void setInitialSearchCriterion(IAttributeSearchFieldKind initialKind, String searchString)
    {
        // Set the widget
        criteriaWidget.setInitialSearchCriterion(initialKind, searchString);
    }
}
