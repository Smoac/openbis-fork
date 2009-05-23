package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;

import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IResultUpdater;

/**
 * {@link Dialog} displaying {@link ColumnSettingsChooser}.
 * 
 * @author Izabela Adamczyk
 */
class ColumnSettingsDialog extends Dialog
{
    private final IMessageProvider messageProvider;

    public static void show(IMessageProvider messageProvider, List<ColumnDataModel> columnModels,
            IResultUpdater<List<ColumnDataModel>> resultUpdater)
    {
        new ColumnSettingsDialog(messageProvider).show(columnModels, resultUpdater);
    }

    private ColumnSettingsDialog(IMessageProvider messageProvider)
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
    private void show(final List<ColumnDataModel> columnModels,
            final IResultUpdater<List<ColumnDataModel>> resultUpdater)
    {
        assert columnModels != null : "columnModels not specified";
        removeAll();
        final ColumnSettingsChooser columnChooser =
                new ColumnSettingsChooser(columnModels, messageProvider);
        add(columnChooser.getComponent());
        super.show();
        getButtonBar().getButtonById("ok").addSelectionListener(
                new SelectionListener<ComponentEvent>()
                    {
                        @Override
                        public void componentSelected(ComponentEvent ce)
                        {
                            resultUpdater.update(columnChooser.getModels());
                            hide();
                        }
                    });
    }
}
