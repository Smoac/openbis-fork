package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search;

import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Contains a button opening {@link DetailedSearchWindow}.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DetailedSearchToolbar extends ToolBar
{
    private LabelToolItem description;

    private final IDetailedSearchHitGrid grid;

    private final DetailedSearchWindow searchWindow;

    public DetailedSearchToolbar(final IDetailedSearchHitGrid grid, String buttonName,
            final DetailedSearchWindow searchWindow)
    {
        this.grid = grid;
        this.searchWindow = searchWindow;
        add(description = new LabelToolItem());
        add(new FillToolItem());
        add(new TextToolItem(buttonName, new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    searchWindow.show();
                }
            }));
    }

    public void updateSearchResults(DetailedSearchCriteria searchCriteria,
            String searchDescription, List<PropertyType> availablePropertyTypes)
    {
        grid.refresh(searchCriteria, availablePropertyTypes);
        description.setLabel(StringUtils.abbreviate(searchDescription, 100));
        description.setToolTip(searchDescription);
    }

    @Override
    protected void onRender(Element target, int index)
    {
        super.onRender(target, index);
        searchWindow.show();
    }
}
