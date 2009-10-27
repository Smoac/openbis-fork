package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisplayTypeIDProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.TextColumnFilterWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnDistinctValues;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridColumnFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomFilter;

/**
 * Toolbar with filters.
 * 
 * @author Izabela Adamczyk
 */
public class FilterToolbar<T> extends ToolBar implements IDatabaseModificationObserver
{
    static final String APPLY_ID = "apply_button";

    static final String RESET_ID = "reset_button";

    private final IMessageProvider messageProvider;

    // stores the state of column filters, even if custom filter is currently used
    private final List<IColumnFilterWidget<T>> columnFilters;

    private final LayoutContainer filterContainer;

    private final FilterSelectionWidget filterSelectionWidget;

    private final IDelegatedAction applyFiltersAction;

    private final TextToolItem applyTool;

    private final TextToolItem resetTool;

    public FilterToolbar(IViewContext<ICommonClientServiceAsync> viewContext, String gridId,
            IDisplayTypeIDProvider displayTypeIDProvider, IDelegatedAction applyFiltersAction)
    {
        this.messageProvider = viewContext;
        this.columnFilters = new ArrayList<IColumnFilterWidget<T>>();
        this.applyFiltersAction = applyFiltersAction;
        add(new LabelToolItem(messageProvider.getMessage(Dict.FILTER) + ": "));
        filterSelectionWidget =
                new FilterSelectionWidget(viewContext, gridId, displayTypeIDProvider);
        filterContainer = new LayoutContainer(new FillLayout(Orientation.HORIZONTAL));
        filterContainer.setLayoutOnChange(true); // fixes jumping filter fields in firefox
        AdapterToolItem filterTool = new AdapterToolItem(filterSelectionWidget);
        add(filterTool);
        add(new AdapterToolItem(filterContainer));
        applyTool = new TextToolItem(messageProvider.getMessage(Dict.APPLY_FILTER));
        applyTool.setId(createId(APPLY_ID, gridId));
        applyTool.setEnabled(false);
        applyTool.hide();
        add(applyTool);
        resetTool = new TextToolItem(messageProvider.getMessage(Dict.RESET_FILTER));
        resetTool.setId(createId(RESET_ID, gridId));
        add(resetTool);

        filterSelectionWidget.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<ModelData> se)
                {
                    updateFilterFields();
                    apply();
                }

            });
        filterSelectionWidget.addPostRefreshCallback(new IDataRefreshCallback()
            {
                public void postRefresh(boolean wasSuccessful)
                {
                    updateFilterFields();
                    apply();
                }
            });
        applyTool.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    apply();
                }
            });
        resetTool.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    resetFilterFields();
                    apply();
                }
            });

    }

    public static String createId(String prefix, String gridId)
    {
        return GenericConstants.ID_PREFIX + prefix + gridId;
    }

    private boolean isColumnFilterSelected()
    {
        return isCustomFilterSelected() == false;
    }

    private boolean isCustomFilterSelected()
    {
        return getCustomFilterSelectedState(false);
    }

    // requiredState - true for column filter, false for custom filters
    private boolean getCustomFilterSelectedState(boolean requiredState)
    {
        GridCustomFilter selected = filterSelectionWidget.tryGetSelected();
        return (selected != null)
                && (selected.getName().equals(GridCustomFilter.COLUMN_FILTER) == requiredState);
    }

    private CustomFilterInfo<T> tryGetCustomFilter()
    {
        if (isCustomFilterSelected() && isValid())
        {
            GridCustomFilter selected = filterSelectionWidget.tryGetSelected();
            CustomFilterInfo<T> info = new CustomFilterInfo<T>();
            info.setExpression(selected.getExpression());
            Set<ParameterWithValue> parameters = new HashSet<ParameterWithValue>();
            for (Component field : filterContainer.getItems())
            {
                parameters.add(((CustomFilterParameterWidget) field).getParameterWithValue());
            }
            info.setParameters(parameters);
            return info;
        }
        return null;
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return filterSelectionWidget.getRelevantModifications();
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        filterSelectionWidget.update(observedModifications);
    }

    private void updateFilterFields()
    {
        filterContainer.removeAll();
        if (isColumnFilterSelected())
        {
            updateFilterContainer(getColumnFilterWidgets(columnFilters));
            applyTool.hide();
        } else
        {
            GridCustomFilter filter = filterSelectionWidget.tryGetSelected();
            if (filter != null)
            {
                updateFilterContainer(createCustomFilterWidgets(filter));
                applyTool.show();
                updateApplyToolEnabledState();
            }
        }
        // don't show reset button if there are no fields to reset
        resetTool.setVisible(filterContainer.getItemCount() > 0);
    }

    private static <T> List<Widget> getColumnFilterWidgets(
            List<IColumnFilterWidget<T>> columnFilters)
    {
        List<Widget> filterWidgets = new ArrayList<Widget>();
        for (IColumnFilterWidget<T> filterWidget : columnFilters)
        {
            filterWidgets.add(filterWidget.getWidget());
        }
        return filterWidgets;
    }

    private List<Widget> createCustomFilterWidgets(GridCustomFilter filter)
    {
        List<Widget> filterWidgets = new ArrayList<Widget>();
        for (String parameter : filter.getParameters())
        {
            filterWidgets.add(new CustomFilterParameterWidget(parameter));
        }
        return filterWidgets;
    }

    // Replaces current filter widgets with the new ones, but tries to make the minimal amount of
    // changes to avoid screen flickering.
    // Does not work great when a filter is added or removed (especially at the beginning), but
    // copes well with cases when one filter widget changes into another.
    private void updateFilterContainer(List<Widget> filterWidgets)
    {

        for (int i = 0; i < filterWidgets.size(); i++)
        {
            Widget filterWidget = filterWidgets.get(i);
            if (i < filterContainer.getItemCount())
            {
                Widget previousItem = filterContainer.getItem(i);
                if (previousItem.equals(filterWidget) == false)
                {
                    filterContainer.remove(previousItem);
                    filterContainer.insert(filterWidget, i);
                }
            } else
            {
                filterContainer.add(filterWidget);
            }
        }
        for (int i = filterWidgets.size(); i < filterContainer.getItemCount(); i++)
        {
            filterContainer.remove(filterContainer.getItem(i));
        }
    }

    @SuppressWarnings("unchecked")
    private void resetFilterFields()
    {
        for (Component field : filterContainer.getItems())
        {
            if (field instanceof Field)
            {
                ((Field) field).reset();
            }
        }
    }

    private void apply()
    {
        // if filter is invalid the action only refreshes the grid without applying any filters
        applyFiltersAction.execute();
    }

    private void updateApplyToolEnabledState()
    {
        applyTool.setEnabled(isValid());
    }

    private boolean isValid()
    {
        if (isCustomFilterSelected())
        {
            boolean valid = true;
            for (Component field : filterContainer.getItems())
            {
                CustomFilterParameterWidget f = (CustomFilterParameterWidget) field;
                valid = f.isValid() && valid;
            }
            return valid;
        } else
        {
            return true; // column filters are always valid
        }
    }

    private class CustomFilterParameterWidget extends TriggerField<ModelData>
    {

        private final String label;

        public CustomFilterParameterWidget(String label)
        {
            this.label = label;
            setEmptyText(label);
            setTriggerStyle("x-form-clear-trigger");
            setWidth(100);
            setAllowBlank(false);
            setAutoValidate(true);
            setValidateOnBlur(true);
        }

        @Override
        protected void onKeyUp(FieldEvent fe)
        {
            super.onKeyUp(fe);
            updateApplyToolEnabledState();
        }

        public ParameterWithValue getParameterWithValue()
        {
            return new ParameterWithValue(label, getRawValue());
        }

        @Override
        protected void onTriggerClick(ComponentEvent ce)
        {
            super.onTriggerClick(ce);
            setValue(null);
            updateApplyToolEnabledState();
        }
    }

    public void refresh()
    {
        filterSelectionWidget.refreshStore();
    }

    // ------------------------------------

    /** @return filters specified on this filter toolbar to be applied to the rows of the grid. */
    public GridFilters<T> getFilters()
    {
        if (isColumnFilterSelected())
        {
            return GridFilters.createColumnFilter(getColumnFiltersInfo());
        }
        CustomFilterInfo<T> customFilter = tryGetCustomFilter();
        if (customFilter != null)
        {
            return GridFilters.createCustomFilter(customFilter);
        }
        return GridFilters.createEmptyFilter();
    }

    // returns filters which user wants to apply to the data
    private List<GridColumnFilterInfo<T>> getColumnFiltersInfo()
    {
        List<GridColumnFilterInfo<T>> filters = new ArrayList<GridColumnFilterInfo<T>>();

        for (IColumnFilterWidget<T> filterWidget : columnFilters)
        {
            filters.add(filterWidget.getFilter());
        }
        return filters;
    }

    public void refreshColumnFiltersDistinctValues(List<ColumnDistinctValues> columnDistinctValues)
    {
        List<IColumnFilterWidget<T>> newColumnFilters = new ArrayList<IColumnFilterWidget<T>>();
        boolean refreshNeeded = false;
        for (IColumnFilterWidget<T> columnFilter : columnFilters)
        {
            ColumnDistinctValues distinctValues =
                    tryFindForColumn(columnFilter.getFilteredColumnId(), columnDistinctValues);
            List<String> valuesOrNull =
                    distinctValues == null ? null : distinctValues.getDistinctValues();
            IColumnFilterWidget<T> newFilter = columnFilter.createOrRefresh(valuesOrNull);
            refreshNeeded = refreshNeeded || newFilter != columnFilter;
            newColumnFilters.add(newFilter);
        }
        if (refreshNeeded)
        {
            updateColumnFilters(newColumnFilters);
        }
    }

    private static ColumnDistinctValues tryFindForColumn(String columnId,
            List<ColumnDistinctValues> columnDistinctValues)
    {
        for (ColumnDistinctValues columnValues : columnDistinctValues)
        {
            if (columnValues.getColumnIdentifier().equals(columnId))
            {
                return columnValues;
            }
        }
        return null;
    }

    /**
     * @return true if the toolbar with filters has just disappeared or appeared
     */
    public boolean rebuildColumnFilters(List<IColumnDefinition<T>> filteredColumns)
    {
        if (hasFilteredColumnsChanged(filteredColumns, this.columnFilters))
        {
            List<IColumnFilterWidget<T>> newColumnFilters =
                    createColumnFilterWidgets(filteredColumns, this.columnFilters,
                            applyFiltersAction);
            updateColumnFilters(newColumnFilters);
            return true;
        } else
        {
            return false;
        }
    }

    private void updateColumnFilters(List<IColumnFilterWidget<T>> newColumnFilters)
    {
        this.columnFilters.clear();
        this.columnFilters.addAll(newColumnFilters);
        updateFilterFields();
    }

    private boolean hasFilteredColumnsChanged(List<IColumnDefinition<T>> filters1,
            List<IColumnFilterWidget<T>> filters2)
    {
        if (filters1.size() != filters2.size())
        {
            return true;
        }
        for (int i = 0; i < filters1.size(); i++)
        {
            String colId1 = filters1.get(i).getIdentifier();
            String colId2 = filters2.get(i).getFilteredColumnId();
            if (colId1.equals(colId2) == false)
            {
                return true;
            }
        }
        return false;
    }

    private static <T> List<IColumnFilterWidget<T>> createColumnFilterWidgets(
            List<IColumnDefinition<T>> availableFilters,
            List<IColumnFilterWidget<T>> previousColumnFilters, IDelegatedAction onFilterAction)
    {
        List<IColumnFilterWidget<T>> filterWidgets = new ArrayList<IColumnFilterWidget<T>>();
        for (IColumnDefinition<T> columnDefinition : availableFilters)
        {
            IColumnFilterWidget<T> filterWidget =
                    tryFindColumnFilter(columnDefinition.getIdentifier(), previousColumnFilters);
            if (filterWidget == null)
            {
                // we do not have distinct values in the columns at this moment, so plain filter
                // widget is always created
                filterWidget = new TextColumnFilterWidget<T>(columnDefinition, onFilterAction);
            }
            filterWidgets.add(filterWidget);
        }
        return filterWidgets;
    }

    private static <T> IColumnFilterWidget<T> tryFindColumnFilter(String columnId,
            List<IColumnFilterWidget<T>> columnFilters)
    {
        for (IColumnFilterWidget<T> columnFilter : columnFilters)
        {
            if (columnFilter.getFilteredColumnId().equals(columnId))
            {
                return columnFilter;
            }
        }
        return null;
    }

    /** @return ids of columns which have filtering switched on at the column filters toolbar */
    public List<String> extractFilteredColumnIds()
    {
        return extractColumnIds(columnFilters);
    }

    private static <T> List<String> extractColumnIds(List<IColumnFilterWidget<T>> columns)
    {
        List<String> filteredColumnsIds = new ArrayList<String>();
        for (IColumnFilterWidget<T> column : columns)
        {
            filteredColumnsIds.add(column.getFilteredColumnId());
        }
        return filteredColumnsIds;
    }
}
