/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExternalDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.CommonExternalDataColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractExternalDataGrid extends AbstractSimpleBrowserGrid<ExternalData>
{
    public static final String GRID_POSTFIX = "-grid";
    
    static final class DeletionCallback extends AbstractAsyncCallback<Void>
    {
        private final IBrowserGridActionInvoker invoker;

        private DeletionCallback(IViewContext<?> viewContext, IBrowserGridActionInvoker invoker)
        {
            super(viewContext);
            this.invoker = invoker;
        }
        
        @Override
        protected void process(Void result)
        {
            invoker.refresh();
        }
    }
    
    private static final class DeletionConfirmationDialog extends ConfirmationDialog
    {

        private static String render(List<ExternalData> dataSets)
        {
            StringBuilder builder = new StringBuilder();
            for (ExternalData externalData : dataSets)
            {
                builder.append("\n").append(externalData.getCode());
            }
            return builder.toString();
        }
        
        private final IViewContext<?> viewContext;

        private final List<String> dataSetCodes;

        private final IBrowserGridActionInvoker invoker;

        public DeletionConfirmationDialog(IViewContext<?> viewContext, List<ExternalData> dataSets,
                IBrowserGridActionInvoker invoker)
        {
            super(viewContext.getMessage(Dict.CONFIRM_DATASET_DELETION_TITLE), viewContext
                    .getMessage(Dict.CONFIRM_DATASET_DELETION_MSG, render(dataSets)));
            this.viewContext = viewContext;
            this.invoker = invoker;
            dataSetCodes = new ArrayList<String>();
            for (ExternalData externalData : dataSets)
            {
                dataSetCodes.add(externalData.getCode());
            }
            setSize(400, 300);
        }

        @Override
        protected void onYes()
        {
            viewContext.getCommonService().deleteDataSets(dataSetCodes,
                    new DeletionCallback(viewContext, invoker));
        }
    }
    
    protected AbstractExternalDataGrid(final IViewContext<ICommonClientServiceAsync> viewContext,
            String browserId)
    {
        super(viewContext, browserId, browserId + GRID_POSTFIX);
        registerCellClickListenerFor(CommonExternalDataColDefKind.CODE.id(),
                new ICellListener<ExternalData>()
                    {
                        public void handle(ExternalData rowItem)
                        {
                            DataSetUtils.showDataSet(rowItem, viewContext.getModel());
                        }
                    });
        Button deleteButton = new Button(viewContext.getMessage(Dict.BUTTON_DELETE_DATASETS));
        deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    BaseEntityModel<ExternalData> item = tryGetSelectedItem();
                    if (item != null)
                    {
                        ExternalData dataSet = item.getBaseObject();
                        List<ExternalData> dataSets = Arrays.asList(dataSet);
                        IBrowserGridActionInvoker invoker = asActionInvoker();
                        new DeletionConfirmationDialog(viewContext, dataSets, invoker).show();
                    }
                    
                }
            });
        pagingToolbar.add(new AdapterToolItem(deleteButton));
    }

    @Override
    protected IColumnDefinitionKind<ExternalData>[] getStaticColumnsDefinition()
    {
        return CommonExternalDataColDefKind.values();
    }

    @Override
    protected BaseEntityModel<ExternalData> createModel(ExternalData entity)
    {
        return new ExternalDataModel(entity);
    }
    
    @Override
    protected List<IColumnDefinition<ExternalData>> getAvailableFilters()
    {
        return asColumnFilters(new CommonExternalDataColDefKind[]
            { CommonExternalDataColDefKind.CODE, CommonExternalDataColDefKind.LOCATION,
                    CommonExternalDataColDefKind.FILE_FORMAT_TYPE });
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<ExternalData> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportDataSetSearchHits(exportCriteria, callback);
    }

}
