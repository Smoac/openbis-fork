package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.expressions.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisplayTypeIDProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.PagingColumnFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
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

    private final List<PagingColumnFilter<T>> columnFilters;

    private final LayoutContainer filterContainer;

    private final FilterSelectionWidget filterSelectionWidget;

    private final IDelegatedAction applyFiltersAction;

    private final TextToolItem applyTool;

    private final TextToolItem resetTool;

    public FilterToolbar(IViewContext<ICommonClientServiceAsync> viewContext, String gridId,
            IDisplayTypeIDProvider displayTypeIDProvider, IDelegatedAction applyFiltersAction)
    {
        this.messageProvider = viewContext;
        this.columnFilters = new ArrayList<PagingColumnFilter<T>>();
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
        return getCustomFilterSelectedState(true);
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
        GridCustomFilter filter = filterSelectionWidget.tryGetSelected();
        if (filter != null)
        {
            filterContainer.removeAll();
            if (isColumnFilterSelected())
            {
                for (PagingColumnFilter<T> filterWidget : columnFilters)
                {
                    filterContainer.add(filterWidget);
                }
                applyTool.hide();
            } else
            {
                for (String parameter : filter.getParameters())
                {
                    filterContainer.add(new CustomFilterParameterWidget(parameter));
                }
                applyTool.show();
                updateApplyToolEnabledState();
            }
            // don't show reset button if there are no fields to reset
            resetTool.setVisible(filterContainer.getItemCount() > 0);
        }
    }

    @SuppressWarnings("unchecked")
    private void resetFilterFields()
    {
        for (Component field : filterContainer.getItems())
        {
            if (field instanceof Field)
            {
                Field f = (Field) field;
                // Simple 'f.reset()' causes automatic filter application,
                // but we want to reload data only once after all filters are cleared.
                f.setRawValue(f.getEmptyText());
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

        for (PagingColumnFilter<T> filterWidget : columnFilters)
        {
            filters.add(filterWidget.getFilter());
        }
        return filters;
    }

    // true if the toolbar with filters has just disappeared or appeared
    public void rebuildColumnFilters(List<IColumnDefinition<T>> filteredColumns)
    {
        List<PagingColumnFilter<T>> newFilterWidgets =
                createColumnFilterWidgets(filteredColumns, applyFiltersAction);
        rebuildColumnFilterWidgets(newFilterWidgets, this.columnFilters, messageProvider);

        this.columnFilters.clear();
        this.columnFilters.addAll(newFilterWidgets);
        updateFilterFields();
    }

    private static <T> List<PagingColumnFilter<T>> createColumnFilterWidgets(
            List<IColumnDefinition<T>> availableFilters, IDelegatedAction onFilterAction)
    {
        List<PagingColumnFilter<T>> filterWidgets = new ArrayList<PagingColumnFilter<T>>();
        for (IColumnDefinition<T> columnDefinition : availableFilters)
        {
            PagingColumnFilter<T> filterWidget =
                    new PagingColumnFilter<T>(columnDefinition, onFilterAction);
            filterWidgets.add(filterWidget);
        }
        return filterWidgets;
    }

    // Sets the value of filter widgets using the previous filter widgets
    private static <T> void rebuildColumnFilterWidgets(List<PagingColumnFilter<T>> filterWidgets,
            List<PagingColumnFilter<T>> previousFilterWidgetsOrNull,
            IMessageProvider messageProvider)
    {
        if (filterWidgets.size() == 0)
        {
            return;
        }

        Map<String, PagingColumnFilter<T>> previousFiltersByColumnId =
                new HashMap<String, PagingColumnFilter<T>>();
        if (previousFilterWidgetsOrNull != null)
        {
            for (PagingColumnFilter<T> filter : previousFilterWidgetsOrNull)
            {
                previousFiltersByColumnId.put(filter.getFilteredColumnId(), filter);
            }
        }

        for (PagingColumnFilter<T> filterWidget : filterWidgets)
        {
            // restore previous value if set
            PagingColumnFilter<T> previousFilterWidgetOrNull =
                    previousFiltersByColumnId.get(filterWidget.getFilteredColumnId());
            if (previousFilterWidgetOrNull != null)
            {
                String previousValue = previousFilterWidgetOrNull.getRawValue();
                if (previousValue != "")
                {
                    // simply setting value with setValue does not work
                    filterWidget.setRawValue(previousFilterWidgetOrNull.getRawValue());
                    filterWidget.validate(); // a hack to show previous value as black text
                }
            }
        }
    }

    /** @return ids of columns which have filtering switched on at the column filters toolbar */
    public List<String> extractFilteredColumnIds()
    {
        List<String> filteredColumnsIds = new ArrayList<String>();
        for (PagingColumnFilter<T> filter : columnFilters)
        {
            filteredColumnsIds.add(filter.getFilteredColumnId());
        }
        return filteredColumnsIds;
    }
}
