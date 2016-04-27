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

import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinSequenceGridColumnIDs.DATABASE_NAME_AND_VERSION;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinSequenceGridColumnIDs.SEQUENCE;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinSequenceGridColumnIDs.SEQUENCE_SHORT_NAME;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSequence;

/**
 * Provider of {@link ProteinSequence} instances.
 *
 * @author Franz-Josef Elmer
 */
public class ProteinSequenceProvider extends AbstractTableModelProvider<ProteinSequence>
{
    private final IPhosphoNetXServer server;

    private final String sessionToken;

    private final TechId experimentID;

    private final TechId proteinReferenceID;

    public ProteinSequenceProvider(IPhosphoNetXServer server, String sessionToken,
            TechId experimentID, TechId proteinReferenceID)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.experimentID = experimentID;
        this.proteinReferenceID = proteinReferenceID;
    }

    @Override
    protected TypedTableModel<ProteinSequence> createTableModel()
    {
        List<ProteinSequence> sequences =
                server.listProteinSequencesByProteinReference(sessionToken, experimentID, proteinReferenceID);
        TypedTableModelBuilder<ProteinSequence> builder =
                new TypedTableModelBuilder<ProteinSequence>();
        builder.addColumn(SEQUENCE_SHORT_NAME).withDefaultWidth(20);
        builder.addColumn(DATABASE_NAME_AND_VERSION);
        builder.addColumn(SEQUENCE).withDefaultWidth(400);
        for (ProteinSequence sequence : sequences)
        {
            builder.addRow(sequence);
            builder.column(SEQUENCE_SHORT_NAME).addString(sequence.getShortName());
            builder.column(DATABASE_NAME_AND_VERSION).addString(
                    sequence.getDatabaseNameAndVersion());
            builder.column(SEQUENCE).addString(sequence.getSequence());
        }
        return builder.getModel();
    }

}
