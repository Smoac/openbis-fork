package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ToolBarEvent;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteria;

/**
 * Contains a button opening {@link DataSetSearchWindow}.
 * 
 * @author Izabela Adamczyk
 */
class DataSetSearchToolbar extends ToolBar
{
    private LabelToolItem description;

    private final DataSetSearchHitGrid grid;

    private final DataSetSearchWindow searchWindow;

    public DataSetSearchToolbar(final DataSetSearchHitGrid grid, String buttonName,
            final DataSetSearchWindow searchWindow)
    {
        this.grid = grid;
        this.searchWindow = searchWindow;
        add(description = new LabelToolItem());
        add(new FillToolItem());
        add(new TextToolItem(buttonName, new SelectionListener<ToolBarEvent>()
            {
                @Override
                public void componentSelected(ToolBarEvent ce)
                {
                    searchWindow.show();
                }
            }));
    }

    public void updateSearchResults(SearchCriteria searchCriteria, String searchDescription,
            List<PropertyType> availablePropertyTypes)
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