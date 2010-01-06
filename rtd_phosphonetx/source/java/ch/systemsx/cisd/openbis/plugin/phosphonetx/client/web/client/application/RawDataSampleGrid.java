/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.createOrDelete;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.edit;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.Dict.COPY_DATA_SETS_BUTTON_LABEL;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.Dict.COPY_DATA_SETS_MESSAGE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.Dict.COPY_DATA_SETS_TITLE;

import java.util.List;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IBrowserGridActionInvoker;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns.RawDataSampleColDefKind;

/**
 * @author Franz-Josef Elmer
 */
class RawDataSampleGrid extends AbstractSimpleBrowserGrid<Sample>
{
    private static final class CopyConfirmationDialog extends
            AbstractDataConfirmationDialog<List<Sample>>
    {
        private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;
        private final List<Sample> samples;

        private CopyConfirmationDialog(IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext,
                List<Sample> samples, String title)
        {
            super(specificViewContext, samples, title);
            this.specificViewContext = specificViewContext;
            this.samples = samples;
        }

        @Override
        protected String createMessage()
        {
            String list = "[";
            String delim = "";
            for (Sample sample : samples)
            {
                list += delim + sample.getCode();
                delim = ", ";
            }
            list += "]";
            return specificViewContext.getMessage(COPY_DATA_SETS_MESSAGE, list);
        }

        @Override
        protected void executeConfirmedAction()
        {
            long[] rawDataSampleIDs = new long[samples.size()];
            for (int i = 0; i < samples.size(); i++)
            {
                rawDataSampleIDs[i] = samples.get(i).getId();
            }
            specificViewContext.getService().copyRawData(rawDataSampleIDs,
                    new VoidAsyncCallback<Void>(specificViewContext));
        }

        @Override
        protected void extendForm()
        {
        }
    }

    public static final String BROWSER_ID =
            GenericConstants.ID_PREFIX + "raw-data-sample-browser";

    public static final String GRID_ID = BROWSER_ID + "-grid";

    public static IDisposableComponent create(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        RawDataSampleGrid grid = new RawDataSampleGrid(viewContext);
        return grid.asDisposableWithoutToolbar();
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;
    
    RawDataSampleGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID, true,
                PhosphoNetXDisplayTypeIDGenerator.RAW_DATA_SAMPLE_BROWSER_GRID);
        specificViewContext = viewContext;
        allowMultipleSelection();
        registerLinkClickListenerFor(RawDataSampleColDefKind.CODE.id(), new ICellListener<Sample>()
            {
                public void handle(Sample rowItem)
                {
                    showEntityViewer(rowItem, false);
                }
            });
        addEntityOperationsLabel();
        Button uploadButton =
                new Button(viewContext.getMessage(COPY_DATA_SETS_BUTTON_LABEL),
                        new AbstractCreateDialogListener()
                            {
                                @Override
                                protected Dialog createDialog(List<Sample> samples,
                                        IBrowserGridActionInvoker invoker)
                                {
                                    return new CopyConfirmationDialog(specificViewContext, samples,
                                            specificViewContext.getMessage(COPY_DATA_SETS_TITLE));
                                }
                            });
        addButton(uploadButton);
    }

    @Override
    protected IColumnDefinitionKind<Sample>[] getStaticColumnsDefinition()
    {
        return RawDataSampleColDefKind.values();
    }
    
    @Override
    protected ColumnDefsAndConfigs<Sample> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<Sample> schema = super.createColumnsDefinition();
        GridCellRenderer<BaseEntityModel<?>> linkCellRenderer = createInternalLinkCellRenderer();
        schema.setGridCellRendererFor(RawDataSampleColDefKind.CODE.id(), linkCellRenderer);
        return schema;
    }
    
    @Override
    protected List<IColumnDefinition<Sample>> getInitialFilters()
    {
        return asColumnFilters(new RawDataSampleColDefKind[] {RawDataSampleColDefKind.CODE});
    }
    
    @Override
    protected void listEntities(DefaultResultSetConfig<String, Sample> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Sample>> callback)
    {
        specificViewContext.getService().listRawDataSamples(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Sample> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportRawDataSamples(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { createOrDelete(ObjectKind.SAMPLE_TYPE), edit(ObjectKind.SAMPLE_TYPE),
                    createOrDelete(ObjectKind.GROUP),
                    createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

    @Override
    protected void showEntityViewer(Sample entity, boolean editMode)
    {
        showEntityInformationHolderViewer(entity, editMode);
    }
}
