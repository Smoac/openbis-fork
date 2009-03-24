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
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
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
    
    private static abstract class AbstractConfirmationDialog extends Dialog
    {
        protected final IViewContext<?> viewContext;

        protected final List<String> dataSetCodes;

        protected final IBrowserGridActionInvoker invoker;

        AbstractConfirmationDialog(IViewContext<?> viewContext, List<ExternalData> dataSets,
                IBrowserGridActionInvoker invoker, String titleKey)
        {
            this.viewContext = viewContext;
            this.invoker = invoker;
            dataSetCodes = new ArrayList<String>();
            for (ExternalData externalData : dataSets)
            {
                dataSetCodes.add(externalData.getCode());
            }
            setHeading(viewContext.getMessage(titleKey));
            setButtons(Dialog.OKCANCEL);
            setHideOnButtonClick(true);
            setModal(true);
        }
    }
    
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
    
    private static final class DeletionConfirmationDialog extends AbstractConfirmationDialog
    {
        private final TextField<String> reason;

        public DeletionConfirmationDialog(IViewContext<?> viewContext, List<ExternalData> dataSets,
                IBrowserGridActionInvoker invoker)
        {
            super(viewContext, dataSets, invoker, Dict.CONFIRM_DATASET_DELETION_TITLE);
            addText(viewContext.getMessage(Dict.CONFIRM_DATASET_DELETION_MSG, dataSets.size()));
            reason = new TextField<String>();
            reason.setSelectOnFocus(true);
            reason.setHideLabel(true);
            reason.setWidth("100%");
            reason.setMaxLength(250);
            reason.addKeyListener(new KeyListener()
                {
                    @Override
                    public void handleEvent(ComponentEvent ce)
                    {
                        okBtn.setEnabled(reason.isValid());
                    }
                });
            add(reason);
        }
        
        @Override
        protected void onButtonPressed(Button button)
        {
            super.onButtonPressed(button);
            if (button.getItemId().equals(Dialog.OK) && reason.isValid())
            {
                viewContext.getCommonService().deleteDataSets(dataSetCodes, reason.getValue(),
                        new DeletionCallback(viewContext, invoker));
            }
        }
    }
    
    static final class UploadCallback extends AbstractAsyncCallback<Void>
    {
        private UploadCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }
        
        @Override
        protected void process(Void result)
        {
        }
    }
    
    private static final class UploadConfirmationDialog extends AbstractConfirmationDialog
    {
        private final String cifexURL;
        private final TextField<String> password;

        public UploadConfirmationDialog(IViewContext<?> viewContext, List<ExternalData> dataSets,
                IBrowserGridActionInvoker invoker)
        {
            super(viewContext, dataSets, invoker, Dict.CONFIRM_DATASET_UPLOAD_TITLE);
            cifexURL = viewContext.getModel().getApplicationInfo().getCIFEXURL();
            addText(viewContext.getMessage(Dict.CONFIRM_DATASET_UPLOAD_MSG, dataSets.size(), cifexURL));
            password = new TextField<String>();
            password.setPassword(true);
            password.setSelectOnFocus(true);
            password.setHideLabel(true);
            password.setWidth("100%");
            password.setMaxLength(50);
            add(password);
        }
        
        @Override
        protected void onButtonPressed(Button button)
        {
            super.onButtonPressed(button);
            if (button.getItemId().equals(Dialog.OK))
            {
                viewContext.getCommonService().uploadDataSets(dataSetCodes, cifexURL, password.getValue(),
                        new UploadCallback(viewContext));
            }
        }
    }
    
    private abstract class AbstractDataSetAction extends SelectionListener<ButtonEvent>
    {
        @Override
        public void componentSelected(ButtonEvent ce)
        {
            List<BaseEntityModel<ExternalData>> items = getSelectedItems();
            if (items.isEmpty() == false)
            {
                List<ExternalData> dataSets = new ArrayList<ExternalData>();
                for (BaseEntityModel<ExternalData> item : items)
                {
                    dataSets.add(item.getBaseObject());
                }
                IBrowserGridActionInvoker invoker = asActionInvoker();
                createDialog(dataSets, invoker).show();
            }
        }

        protected abstract Dialog createDialog(List<ExternalData> dataSets,
                IBrowserGridActionInvoker invoker);
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
        addButton(Dict.BUTTON_DELETE_DATASETS, new AbstractDataSetAction()
            {
                @Override
                protected Dialog createDialog(List<ExternalData> dataSets,
                        IBrowserGridActionInvoker invoker)
                {
                    return new DeletionConfirmationDialog(viewContext, dataSets, invoker);
                }
            });
        addButton(Dict.BUTTON_UPLOAD_DATASETS, new AbstractDataSetAction()
            {
                @Override
                protected Dialog createDialog(List<ExternalData> dataSets,
                        IBrowserGridActionInvoker invoker)
                {
                    return new UploadConfirmationDialog(viewContext, dataSets, invoker);
                }
            });
        allowMultipleSelection();
    }
    
    private void addButton(String labelKey, SelectionListener<ButtonEvent> action)
    {
        Button button = new Button(viewContext.getMessage(labelKey));
        button.addSelectionListener(action);
        pagingToolbar.add(new AdapterToolItem(button));
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
