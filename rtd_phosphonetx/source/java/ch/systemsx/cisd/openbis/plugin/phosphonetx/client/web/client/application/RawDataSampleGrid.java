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

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.RawDataSampleGridIDs;

/**
 * @author Franz-Josef Elmer
 */
class RawDataSampleGrid extends TypedTableGrid<Sample>
{
    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + "raw_data_sample_browser";

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        RawDataSampleGrid grid = new RawDataSampleGrid(viewContext);
        DisposableEntityChooser<TableModelRowWithObject<Sample>> disposable =
                grid.asDisposableWithoutToolbar();
        return new DatabaseModificationAwareComponent(disposable.getComponent(), disposable);
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    RawDataSampleGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, true,
                PhosphoNetXDisplayTypeIDGenerator.RAW_DATA_SAMPLE_BROWSER_GRID);
        specificViewContext = viewContext;
        allowMultipleSelection();
        addEntityOperationsLabel();
        RawDataProcessingMenu button =
                new RawDataProcessingMenu(viewContext,
                        new IDelegatedActionWithResult<List<TableModelRowWithObject<Sample>>>()
                            {
                                public List<TableModelRowWithObject<Sample>> execute()
                                {
                                    return getSelectedBaseObjects();
                                }
                            });
        enableButtonOnSelectedItems(button);
        addButton(button);
        linkMSInjectionSample();
        linkBiologicalSample();
    }

    private void linkMSInjectionSample()
    {
        registerListenerAndLinkGenerator(RawDataSampleGridIDs.CODE,
                new ICellListenerAndLinkGenerator<Sample>()
                    {

                        public void handle(TableModelRowWithObject<Sample> rowItem,
                                boolean specialKeyPressed)
                        {
                            showEntityInformationHolderViewer(rowItem.getObjectOrNull(), false,
                                    specialKeyPressed);
                        }

                        public String tryGetLink(Sample entity, ISerializableComparable value)
                        {
                            return LinkExtractor.tryExtract(entity);
                        }

                    });
    }

    private void linkBiologicalSample()
    {
        registerListenerAndLinkGenerator(RawDataSampleGridIDs.PARENT,
                new ICellListenerAndLinkGenerator<Sample>()
                    {

                        public void handle(TableModelRowWithObject<Sample> rowItem,
                                boolean specialKeyPressed)
                        {
                            showEntityInformationHolderViewer(rowItem.getObjectOrNull()
                                    .getGeneratedFrom(), false, specialKeyPressed);
                        }

                        public String tryGetLink(Sample entity,
                                ISerializableComparable comparableValue)
                        {
                            return LinkExtractor.tryExtract(entity.getGeneratedFrom());
                        }
                    });
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<Sample>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<Sample>> callback)
    {
        specificViewContext.getService().listRawDataSamples(resultSetConfig, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<Sample>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportRawDataSamples(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { createOrDelete(ObjectKind.SAMPLE_TYPE), edit(ObjectKind.SAMPLE_TYPE),
                    edit(ObjectKind.SAMPLE), createOrDelete(ObjectKind.SPACE),
                    createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT) };
    }

}
