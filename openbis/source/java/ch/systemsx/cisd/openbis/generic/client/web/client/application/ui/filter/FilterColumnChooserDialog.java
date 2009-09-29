package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.filter;

import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

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
            List<ColumnDataModel> columnModels, String gridId)
    {
        new FilterColumnChooserDialog(viewContext, gridId).show(columnModels);
    }

    private FilterColumnChooserDialog(IViewContext<ICommonClientServiceAsync> viewContext,
            String gridId)
    {
        this.viewContext = viewContext;
        setHeight(450);
        setWidth(700);
        setLayout(new FitLayout());
        setHeading(viewContext.getMessage(Dict.COLUMNS) + " [" + gridId + "]");
    }

    /**
     * Shows window containing {@link FilterColumnChooser} based on given {@link ColumnModel}.
     */
    private void show(final List<ColumnDataModel> columnModels)
    {
        assert columnModels != null : "columnModels not specified";
        removeAll();
        final FilterColumnChooser columnChooser =
                new FilterColumnChooser(columnModels, viewContext);
        add(columnChooser.getComponent());
        super.show();
        getButtonBar().getButtonById("ok").addSelectionListener(
                new SelectionListener<ComponentEvent>()
                    {
                        @Override
                        public void componentSelected(ComponentEvent ce)
                        {
                            hide();
                        }
                    });
    }

}
