package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * {@link Dialog} displaying {@link ColumnSettingsChooser}.
 * 
 * @author Izabela Adamczyk
 */
class ColumnSettingsDialog extends Dialog
{
    private final IMessageProvider messageProvider;

    public ColumnSettingsDialog(IMessageProvider messageProvider)
    {
        this.messageProvider = messageProvider;
        setHeight(400);
        setWidth(450);
        setLayout(new FitLayout());
        setHeading(messageProvider.getMessage(Dict.GRID_COLUMN_CHOOSER_TITLE));
    }

    /**
     * Shows window containing {@link ColumnSettingsChooser} based on given {@link ColumnModel}.
     */
    public <M extends ModelData> void show(final Grid<M> grid)
    {
        assert grid != null : "Grid must be loaded";
        final MoveableColumnModel cm = (MoveableColumnModel) grid.getColumnModel();
        removeAll();
        final ColumnSettingsChooser columnChooser =
                new ColumnSettingsChooser(createModels(cm), messageProvider);
        add(columnChooser.getComponent());
        super.show();
        getButtonBar().getButtonById("ok").addSelectionListener(
                new SelectionListener<ComponentEvent>()
                    {
                        @Override
                        public void componentSelected(ComponentEvent ce)
                        {
                            int newIndex = 0;
                            for (ColumnDataModel m : columnChooser.getModels())
                            {
                                int oldIndex = cm.getIndexById(m.getColumnID());
                                cm.setHidden(oldIndex, m.isVisible() == false);
                                cm.move(oldIndex, newIndex++);
                            }
                            grid.setLoadMask(false);
                            grid.reconfigure(grid.getStore(), grid.getColumnModel());
                            grid.setLoadMask(true);
                            hide();
                        }
                    });
    }

    private List<ColumnDataModel> createModels(ColumnModel cm)
    {
        int cols = cm.getColumnCount();
        List<ColumnDataModel> list = new ArrayList<ColumnDataModel>();
        for (int i = 0; i < cols; i++)
        {
            if (cm.getColumnHeader(i) == null || cm.getColumnHeader(i).equals("") || cm.isFixed(i))
            {
                continue;
            }
            boolean isVisible = cm.isHidden(i) == false;
            boolean hasFilter = false; // TODO 2009-05-15, Tomasz Pylak: implement this
            list.add(new ColumnDataModel(cm.getColumnHeader(i), isVisible, hasFilter, cm
                    .getColumnId(i)));
        }
        return list;
    }
}
