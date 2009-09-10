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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns.ProteinSequenceColDefKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinSequenceCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinSequenceGrid extends AbstractSimpleBrowserGrid<ProteinSequence>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "protein-sequence-browser";

    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + "_grid";

    static IDisposableComponent create(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            TechId proteinReferenceID)
    {
        return new ProteinSequenceGrid(viewContext, proteinReferenceID)
                .asDisposableWithoutToolbar();
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    private ListProteinSequenceCriteria criteria;

    private ProteinSequenceGrid(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            TechId proteinReferenceID)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID + proteinReferenceID, GRID_ID
                + proteinReferenceID, true,
                PhosphoNetXDisplayTypeIDGenerator.PROTEIN_SEQUENCE_BROWSER_GRID);
        specificViewContext = viewContext;
        criteria = new ListProteinSequenceCriteria();
        criteria.setProteinReferenceID(proteinReferenceID);
    }

    @Override
    protected IColumnDefinitionKind<ProteinSequence>[] getStaticColumnsDefinition()
    {
        return ProteinSequenceColDefKind.values();
    }

    @Override
    protected List<IColumnDefinition<ProteinSequence>> getInitialFilters()
    {
        return asColumnFilters(new ProteinSequenceColDefKind[] {});
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, ProteinSequence> resultSetConfig,
            AbstractAsyncCallback<ResultSet<ProteinSequence>> callback)
    {
        criteria.copyPagingConfig(resultSetConfig);
        specificViewContext.getService().listSequencesByProteinReference(criteria, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<ProteinSequence> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportProteinSequences(exportCriteria, callback);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

}
