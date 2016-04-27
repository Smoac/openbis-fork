/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinSummaryGridColumnIDs.DECOY_PEPTIDE_COUNT;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinSummaryGridColumnIDs.DECOY_PROTEIN_COUNT;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinSummaryGridColumnIDs.FDR;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinSummaryGridColumnIDs.PEPTIDE_COUNT;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinSummaryGridColumnIDs.PROTEIN_COUNT;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSummary;

/**
 * Provider of {@link ProteinSummary} instances.
 *
 * @author Franz-Josef Elmer
 */
public class ProteinSummaryProvider extends AbstractTableModelProvider<ProteinSummary>
{
    private final IPhosphoNetXServer server;

    private final String sessionToken;

    private final TechId experimentID;

    public ProteinSummaryProvider(IPhosphoNetXServer server, String sessionToken,
            TechId experimentID)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.experimentID = experimentID;
    }

    @Override
    protected TypedTableModel<ProteinSummary> createTableModel()
    {
        List<ProteinSummary> sumaries = server.listProteinSummariesByExperiment(sessionToken, experimentID);
        TypedTableModelBuilder<ProteinSummary> builder = new TypedTableModelBuilder<ProteinSummary>();
        builder.addColumn(FDR);
        builder.addColumn(PROTEIN_COUNT).withDefaultWidth(100);
        builder.addColumn(PEPTIDE_COUNT).withDefaultWidth(100);
        builder.addColumn(DECOY_PROTEIN_COUNT).withDefaultWidth(100);
        builder.addColumn(DECOY_PEPTIDE_COUNT).withDefaultWidth(100);
        for (ProteinSummary summary : sumaries)
        {
            builder.addRow(summary);
            builder.column(FDR).addDouble(summary.getFDR());
            builder.column(PROTEIN_COUNT).addInteger((long) summary.getProteinCount());
            builder.column(PEPTIDE_COUNT).addInteger((long) summary.getPeptideCount());
            builder.column(DECOY_PROTEIN_COUNT).addInteger((long) summary.getDecoyProteinCount());
            builder.column(DECOY_PEPTIDE_COUNT).addInteger((long) summary.getDecoyPeptideCount());
        }
        return builder.getModel();
    }

}
