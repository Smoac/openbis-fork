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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinSequenceCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSequence;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinSequenceGrid extends TypedTableGrid<ProteinSequence>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "protein-sequence-browser";

    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + TypedTableGrid.GRID_POSTFIX;

    static IDisposableComponent create(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            Experiment experimentOrNull, TechId proteinReferenceID)
    {
        return new ProteinSequenceGrid(viewContext, experimentOrNull, proteinReferenceID)
                .asDisposableWithoutToolbar();
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    private ListProteinSequenceCriteria criteria;

    private ProteinSequenceGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            Experiment experimentOrNull, TechId proteinReferenceID)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID + proteinReferenceID, true,
                PhosphoNetXDisplayTypeIDGenerator.PROTEIN_SEQUENCE_BROWSER_GRID);
        specificViewContext = viewContext;
        criteria = new ListProteinSequenceCriteria();
        if (experimentOrNull != null)
        {
            criteria.setExperimentID(new TechId(experimentOrNull.getId()));
        }
        criteria.setProteinReferenceID(proteinReferenceID);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList();
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<ProteinSequence>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<ProteinSequence>> callback)
    {
        criteria.copyPagingConfig(resultSetConfig);
        specificViewContext.getService().listSequencesByProteinReference(criteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<ProteinSequence>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportProteinSequences(exportCriteria, callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

}
