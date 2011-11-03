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

import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.DataSetProteinGridColumnIDs.DATA_SET_PERM_ID;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.DataSetProteinGridColumnIDs.FDR;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.DataSetProteinGridColumnIDs.PEPTIDE_COUNT;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.DataSetProteinGridColumnIDs.SEQUENCE_NAME;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.DataSetProtein;

/**
 * Provider of {@link DataSetProtein} instances.
 *
 * @author Franz-Josef Elmer
 */
public class DataSetProteinProvider extends AbstractTableModelProvider<DataSetProtein>
{
    private final IPhosphoNetXServer server;

    private final String sessionToken;

    private final TechId experimentID;

    private final TechId proteinReferenceID;

    public DataSetProteinProvider(IPhosphoNetXServer server, String sessionToken,
            TechId experimentID, TechId proteinReferenceID)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.experimentID = experimentID;
        this.proteinReferenceID = proteinReferenceID;
    }

    @Override
    protected TypedTableModel<DataSetProtein> createTableModel()
    {
        List<DataSetProtein> proteins =
                server.listProteinsByExperimentAndReference(sessionToken, experimentID,
                        proteinReferenceID);
        TypedTableModelBuilder<DataSetProtein> builder =
                new TypedTableModelBuilder<DataSetProtein>();
        builder.addColumn(DATA_SET_PERM_ID).withDefaultWidth(200);
        builder.addColumn(SEQUENCE_NAME).withDefaultWidth(80);
        builder.addColumn(PEPTIDE_COUNT).withDataType(DataTypeCode.INTEGER).withDefaultWidth(80);
        builder.addColumn(FDR).withDefaultWidth(80);
        for (DataSetProtein protein : proteins)
        {
            builder.addRow(protein);
            builder.column(DATA_SET_PERM_ID).addString(protein.getDataSetPermID());
            builder.column(SEQUENCE_NAME).addString(protein.getSequenceName());
            builder.column(PEPTIDE_COUNT).addInteger((long) protein.getPeptideCount());
            int perMille = (int) (1000 * protein.getFalseDiscoveryRate() + 0.5);
            builder.column(FDR).addString((perMille / 10) + "." + (perMille % 10) + " %");
        }
        return builder.getModel();
    }

}
