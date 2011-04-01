package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search;

import java.util.List;

import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetProcessingMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ReportingPluginSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.report.ReportGeneratedCallback.IOnReportComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * Extension of {@link DetailedSearchWindow} adding widgets for reporting and processing plugins.
 * 
 * @author Piotr Buczek
 */
public class DetailedDataSetSearchToolbar extends DetailedSearchToolbar
{
    private final ReportingPluginSelectionWidget reportSelectionWidget;

    private final IOnReportComponentGeneratedAction reportGeneratedAction;

    public DetailedDataSetSearchToolbar(final IViewContext<?> viewContext,
            final DataSetSearchHitGrid grid, String buttonName,
            final DetailedSearchWindow searchWindow)
    {
        this(viewContext, grid, buttonName, searchWindow, false);
    }

    public DetailedDataSetSearchToolbar(final IViewContext<?> viewContext,
            final DataSetSearchHitGrid grid, String buttonName,
            final DetailedSearchWindow searchWindow, boolean initializeDescriptionFromSearchWindow)
    {
        super(grid, buttonName, searchWindow, initializeDescriptionFromSearchWindow);
        this.reportSelectionWidget = new ReportingPluginSelectionWidget(viewContext, null);
        this.reportGeneratedAction = new IOnReportComponentGeneratedAction()
            {
                public void execute(IDisposableComponent gridComponent)
                {
                    // TODO
                    // replaceContent(gridComponent);
                }
            };
        add(reportSelectionWidget);
        if (viewContext.isSimpleOrEmbeddedMode() == false)
        {
            // processing plugins should be hidden in simple view mode
            viewContext.getCommonService().listDataStoreServices(DataStoreServiceKind.PROCESSING,
                    new LoadProcessingPluginsCallback(viewContext, grid));
        }
    }

    public final class LoadProcessingPluginsCallback extends
            AbstractAsyncCallback<List<DatastoreServiceDescription>>
    {
        private final AbstractExternalDataGrid browser;

        public LoadProcessingPluginsCallback(final IViewContext<?> viewContext,
                AbstractExternalDataGrid browser)
        {
            super(viewContext);
            this.browser = browser;
        }

        @Override
        protected void process(List<DatastoreServiceDescription> result)
        {
            if (result.isEmpty() == false)
            {

                DataSetProcessingMenu menu =
                        new DataSetProcessingMenu(viewContext.getCommonViewContext(),
                                browser.getSelectedAndDisplayedItemsAction(), result);
                add(new SeparatorToolItem());
                add(menu);
            }
        }
    }

}
