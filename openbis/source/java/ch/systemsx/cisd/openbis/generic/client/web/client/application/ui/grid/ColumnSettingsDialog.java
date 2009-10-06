package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter.GridCustomColumnGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter.GridCustomFilterGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IResultUpdater;

/**
 * {@link Dialog} displaying {@link ColumnSettingsChooser}.
 * 
 * @author Izabela Adamczyk
 */
public class ColumnSettingsDialog extends Dialog
{
    public static final String TAB_PANEL_ID_PREFIX = GenericConstants.ID_PREFIX + "tab-panel";

    public static final String FILTERS_TAB = GenericConstants.ID_PREFIX + "filters-tab";

    public static final String COLUMNS_TAB = GenericConstants.ID_PREFIX + "columns-tab";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final String gridDisplayId;

    public static void show(IViewContext<ICommonClientServiceAsync> viewContext,
            List<ColumnDataModel> columnModels,
            IResultUpdater<List<ColumnDataModel>> resultUpdater, String gridDisplayId)
    {
        new ColumnSettingsDialog(viewContext, gridDisplayId).show(columnModels, resultUpdater);
    }

    private ColumnSettingsDialog(IViewContext<ICommonClientServiceAsync> viewContext, String gridId)
    {
        this.viewContext = viewContext;
        this.gridDisplayId = gridId;
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
        TabPanel panel = new TabPanel();
        panel.setId(TAB_PANEL_ID_PREFIX + gridDisplayId);

        final ColumnSettingsChooser columnChooser =
                new ColumnSettingsChooser(columnModels, viewContext);
        TabItem columnsTab = createTabItem(columnChooser.getComponent(), Dict.COLUMNS, "");
        panel.add(columnsTab);

        final IDisposableComponent filters =
                GridCustomFilterGrid.create(viewContext, gridDisplayId, columnModels);
        TabItem customFiltersTab =
                createTabItem(filters.getComponent(), Dict.GRID_CUSTOM_FILTERS, FILTERS_TAB);
        panel.add(customFiltersTab);

        final IDisposableComponent columns =
                GridCustomColumnGrid.create(viewContext, gridDisplayId, columnModels);
        TabItem customColumnsTab =
                createTabItem(columns.getComponent(), Dict.GRID_CUSTOM_COLUMNS, COLUMNS_TAB);
        panel.add(customColumnsTab);

        add(panel);
        super.show();
        Button okButton = getButtonBar().getButtonById("ok");
        okButton.setId(OK + gridDisplayId);
        okButton.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    resultUpdater.update(columnChooser.getModels());
                    filters.dispose();
                    columns.dispose();
                    hide();
                }
            });
    }

    private TabItem createTabItem(final Component component, String titleDictKey, String tabIdPrefix)
    {
        TabItem customColumnsTab = new TabItem(viewContext.getMessage(titleDictKey));
        customColumnsTab.setId(tabIdPrefix + gridDisplayId);
        customColumnsTab.setLayout(new FitLayout());
        customColumnsTab.add(component);
        return customColumnsTab;
    }
}
