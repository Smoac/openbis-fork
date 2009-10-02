package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter;

import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDataModel;

/**
 * {@link Dialog} displaying {@link FilterColumnChooser}.
 * 
 * @author Izabela Adamczyk
 */
class FilterColumnChooserDialog extends Dialog
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static void show(IViewContext<ICommonClientServiceAsync> viewContext,
            List<ColumnDataModel> columnModels, String gridId, IExpressionHolder expressionField)
    {
        new FilterColumnChooserDialog(viewContext, gridId).show(columnModels, expressionField);
    }

    private FilterColumnChooserDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId)
    {
        this.viewContext = viewContext;
        setHeight(450);
        setWidth(700);
        setLayout(new FitLayout());
        setHeading(viewContext.getMessage(Dict.COLUMNS) + " [" + gridId + "]");
        setModal(true); // without it the dialog goes under filter edit/register dialog
    }

    /**
     * Shows window containing {@link FilterColumnChooser} based on given {@link ColumnModel}.
     */
    private void show(final List<ColumnDataModel> columnModels,
            final IExpressionHolder expressionField)
    {
        assert columnModels != null : "columnModels not specified";
        removeAll();
        final FilterColumnChooser columnChooser =
                new FilterColumnChooser(columnModels, viewContext);
        final Component columnChooserComponent = columnChooser.getComponent();
        add(columnChooserComponent);

        columnChooserComponent.sinkEvents(Event.ONDBLCLICK);
        columnChooserComponent.addListener(Event.ONDBLCLICK, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    insertColumnsIntoExpression(expressionField, columnChooser);
                }

            });
        super.show();
        getButtonBar().getButtonById("ok").addSelectionListener(
                new SelectionListener<ComponentEvent>()
                    {
                        @Override
                        public void componentSelected(ComponentEvent ce)
                        {
                            insertColumnsIntoExpression(expressionField, columnChooser);
                        }
                    });

    }

    private void insertColumnsIntoExpression(IExpressionHolder expressionField,
            FilterColumnChooser columnChooser)
    {
        String expression = expressionField.getValue() != null ? expressionField.getValue() : "";
        int cursor = expressionField.getCursorPos();
        for (String column : columnChooser.getSelectedItems())
        {

            String addressWithSeparator = column + " ";
            expression =
                    expression.substring(0, cursor) + addressWithSeparator
                            + expression.substring(cursor);
            cursor += addressWithSeparator.length();
        }
        expressionField.setValue(expression);
        expressionField.setCursorPos(cursor);
        hide();
    }
}
