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

/**
 * {@link Dialog} displaying {@link ColumnChooser}.
 * 
 * @author Izabela Adamczyk
 */
class ColumnChooserDialog extends Dialog
{
    public ColumnChooserDialog()
    {
        setHeight(400);
        setLayout(new FitLayout());
        setHeading("Configure visibility and order of the columns");
    }

    /**
     * Shows window containing {@link ColumnChooser} based on given {@link ColumnModel}.
     */
    public <M extends ModelData> void show(final Grid<M> grid)
    {
        assert grid != null : "Grid must be loaded";
        final MoveableColumnModel cm = (MoveableColumnModel) grid.getColumnModel();
        removeAll();
        final ColumnChooser columnChooser = new ColumnChooser(createModels(cm));
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
                                cm.setHidden(oldIndex, m.isChecked() == false);
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
            list.add(new ColumnDataModel(cm.getColumnHeader(i), cm.isHidden(i) == false, cm
                    .getColumnId(i)));
        }
        return list;
    }
}
