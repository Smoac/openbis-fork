package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;

/**
 * Shows {@link CriteriaWidget}, allowing to specify search criteria.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetSearchWindow extends Dialog
{
    public static final String SEARCH_BUTTON_ID = DataSetSearchHitGrid.BROWSER_ID + "search_button";

    private static final int MARGIN = 5;

    private static final int HEIGHT = 400;

    private static final int WIDTH = 550;

    private CriteriaWidget criteriaWidget;

    private DataSetSearchToolbar updateListener;

    public DataSetSearchWindow(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        setSize(WIDTH, HEIGHT);
        setModal(true);
        setScrollMode(Scroll.AUTOY);
        setLayout(new FitLayout());
        setResizable(false);
        add(criteriaWidget = new CriteriaWidget(viewContext), new FitData(MARGIN));
        final ButtonBar bar = new ButtonBar();
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
                                    DataSetSearchCriteria criteria = criteriaWidget.tryGetCriteria();
                                    String criteriaDescription =
                                            criteriaWidget.getCriteriaDescription();
                                    updateListener.updateSearchResults(criteria,
                                            criteriaDescription, availablePropertyTypes);
                                    hide();
                                }
                            });

        searchButton.setId(SEARCH_BUTTON_ID);
        bar.add(searchButton);
        setButtonBar(bar);
        setButtons("");
    }

    public void setUpdateListener(DataSetSearchToolbar toolbar)
    {
        this.updateListener = toolbar;
    }
}