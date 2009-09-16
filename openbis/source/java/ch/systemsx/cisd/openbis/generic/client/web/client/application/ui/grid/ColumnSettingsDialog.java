package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter.FilterGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IResultUpdater;

/**
 * {@link Dialog} displaying {@link ColumnSettingsChooser}.
 * 
 * @author Izabela Adamczyk
 */
class ColumnSettingsDialog extends Dialog
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final String gridId;

    public static void show(IViewContext<ICommonClientServiceAsync> viewContext,
            List<ColumnDataModel> columnModels,
            IResultUpdater<List<ColumnDataModel>> resultUpdater, String gridId)
    {
        new ColumnSettingsDialog(viewContext, gridId).show(columnModels, resultUpdater);
    }

    private ColumnSettingsDialog(IViewContext<ICommonClientServiceAsync> viewContext, String gridId)
    {
        this.viewContext = viewContext;
        this.gridId = gridId;
        setHeight(450);
        setWidth(700);
        setLayout(new FitLayout());
        setHeading(viewContext.getMessage(Dict.GRID_SETTINGS_TITLE));
    }

    /**
     * Shows window containing {@link ColumnSettingsChooser} based on given {@link ColumnModel}.
     */
    private void show(final List<ColumnDataModel> columnModels,
            final IResultUpdater<List<ColumnDataModel>> resultUpdater)
    {
        assert columnModels != null : "columnModels not specified";
        removeAll();
        final ColumnSettingsChooser columnChooser =
                new ColumnSettingsChooser(columnModels, viewContext);
        final IDisposableComponent filters = FilterGrid.create(viewContext, gridId, columnModels);
        TabPanel panel = new TabPanel();
        TabItem columnsTab = new TabItem(viewContext.getMessage(Dict.COLUMNS));
        columnsTab.setLayout(new FitLayout());
        columnsTab.add(columnChooser.getComponent());
        panel.add(columnsTab);
        // AI dont show if user is not at least power user
        TabItem filtersTab = new TabItem(viewContext.getMessage(Dict.CUSTOM_FILTERS));
        filtersTab.setLayout(new FitLayout());
        filtersTab.add(filters.getComponent());
        panel.add(filtersTab);
        add(panel);
        super.show();
        getButtonBar().getButtonById("ok").addSelectionListener(
                new SelectionListener<ComponentEvent>()
                    {
                        @Override
                        public void componentSelected(ComponentEvent ce)
                        {
                            resultUpdater.update(columnChooser.getModels());
                            filters.dispose();
                            hide();
                        }
                    });
    }

}
