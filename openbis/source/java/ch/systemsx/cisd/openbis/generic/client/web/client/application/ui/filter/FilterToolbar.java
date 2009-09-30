package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter;

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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParameterWithValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Filter;

/**
 * Toolbar with filters.
 * 
 * @author Izabela Adamczyk
 */
public class FilterToolbar<T> extends ToolBar implements IDatabaseModificationObserver,
        IDelegatedAction
{
    static final String APPLY_ID = "apply_button";

    private final List<PagingColumnFilter<T>> columnFilters;

    private final LayoutContainer filterContainer;

    private final FilterSelectionWidget filterSelectionWidget;

    private final IDelegatedAction delegatedAction;

    private final TextToolItem applyTool;

    public FilterToolbar(IViewContext<ICommonClientServiceAsync> viewContext, String gridId,
            IDisplayTypeIDProvider displayTypeIDProvider,
            final List<PagingColumnFilter<T>> filterWidgets, IDelegatedAction delegatedAction)
    {
        this.columnFilters = filterWidgets;
        this.delegatedAction = delegatedAction;
        add(new LabelToolItem(viewContext.getMessage(Dict.FILTER) + ": "));
        filterSelectionWidget =
                new FilterSelectionWidget(viewContext, gridId, displayTypeIDProvider);
        filterContainer = new LayoutContainer(new FillLayout(Orientation.HORIZONTAL));
        filterContainer.setLayoutOnChange(true); // fixes jumping filter fields in firefox
		AdapterToolItem filterTool = new AdapterToolItem(filterSelectionWidget);
        add(filterTool);
        add(new AdapterToolItem(filterContainer));
        applyTool = new TextToolItem(viewContext.getMessage(Dict.APPLY_FILTER));
        applyTool.setId(createId(APPLY_ID, gridId));
        applyTool.setEnabled(false);
        add(applyTool);

        filterSelectionWidget.addSelectionChangedListener(new SelectionChangedListener<ModelData>()
            {
                @Override
                public void selectionChanged(SelectionChangedEvent<ModelData> se)
                {
                    updateFilterContainer();
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

    }

    public static String createId(String prefix, String gridId)
    {
        return GenericConstants.ID_PREFIX + prefix + gridId;
    }

    public void updateColumnFilter(final List<PagingColumnFilter<T>> newFilters)
    {
        this.columnFilters.clear();
        this.columnFilters.addAll(newFilters);
        updateFilterContainer();
    }

    public CustomFilterInfo<T> tryGetCustomFilters()
    {
        Filter selected = filterSelectionWidget.tryGetSelected();
        if (selected != null)
        {
            if (selected.getName().equals(Filter.COLUMN_FILTER))
            {
                return null;
            } else
            {
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

    private void updateFilterContainer()
    {
        Filter filter = filterSelectionWidget.tryGetSelected();
        if (filter != null)
        {
            filterContainer.removeAll();
            if (filter.getName().equals(Filter.COLUMN_FILTER))
            {
                for (PagingColumnFilter<T> filterWidget : FilterToolbar.this.columnFilters)
                {
                    filterContainer.add(filterWidget);
                }
            } else
            {
                for (String parameter : filter.getParameters())
                {
                    filterContainer.add(new CustomFilterParameterWidget(parameter));
                }
            }
        }
    }

    private void apply()
    {
        boolean valid = isValid();
        if (valid)
        {
            FilterToolbar.this.delegatedAction.execute();
        }
        updateApplyTool();
    }

    private void updateApplyTool()
    {
        applyTool.setEnabled(isValid());
    }

    private boolean isValid()
    {
        Filter filter = filterSelectionWidget.tryGetSelected();
        if (filter == null)
        {
            return true;
        } else if (filter.getName().equals(Filter.COLUMN_FILTER))
        {
            return true;
        } else if (filter.getParameters().size() == 0)
        {
            return true;
        } else
        {
            boolean valid = true;
            for (Component field : filterContainer.getItems())
            {

                CustomFilterParameterWidget f = (CustomFilterParameterWidget) field;
                valid = f.isValid() && valid;
            }
            return valid;
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
            updateApplyTool();
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
            updateApplyTool();
        }
    }

    public void execute()
    {
        filterSelectionWidget.execute();
    }

}
