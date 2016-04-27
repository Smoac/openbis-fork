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

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode.INTEGER;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode.REAL;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinRelatedSampleGridColumnIDs.ABUNDANCE;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinRelatedSampleGridColumnIDs.MODIFICATION_FRACTION;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinRelatedSampleGridColumnIDs.MODIFICATION_MASS;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinRelatedSampleGridColumnIDs.MODIFICATION_POSITION;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinRelatedSampleGridColumnIDs.MODIFIED_AMINO_ACID;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinRelatedSampleGridColumnIDs.SAMPLE_IDENTIFIER;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinRelatedSampleGridColumnIDs.SAMPLE_TYPE;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AminoAcid;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinRelatedSample;

/**
 * Provider of {@link ProteinRelatedSample}.
 *
 * @author Franz-Josef Elmer
 */
public class ProteinRelatedSampleProvider extends AbstractTableModelProvider<ProteinRelatedSample>
{
    private static final String PROPERTIES_GROUP = "property-";

    private final IPhosphoNetXServer server;

    private final String sessionToken;

    private final TechId experimentID;

    private final TechId proteinReferenceID;

    public ProteinRelatedSampleProvider(IPhosphoNetXServer server, String sessionToken,
            TechId experimentID, TechId proteinReferenceID)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.experimentID = experimentID;
        this.proteinReferenceID = proteinReferenceID;
    }

    @Override
    protected TypedTableModel<ProteinRelatedSample> createTableModel()
    {
        List<ProteinRelatedSample> samples =
                server.listProteinRelatedSamplesByProtein(sessionToken, experimentID,
                        proteinReferenceID);
        TypedTableModelBuilder<ProteinRelatedSample> builder =
                new TypedTableModelBuilder<ProteinRelatedSample>();
        builder.column(SAMPLE_IDENTIFIER);
        builder.column(SAMPLE_TYPE);
        builder.column(ABUNDANCE).withDataType(REAL).withDefaultWidth(100);
        builder.column(MODIFIED_AMINO_ACID);
        builder.column(MODIFICATION_POSITION).withDataType(INTEGER).withDefaultWidth(100);
        builder.column(MODIFICATION_MASS).withDataType(REAL).withDefaultWidth(100);
        builder.column(MODIFICATION_FRACTION).withDataType(REAL).withDefaultWidth(100);
        for (ProteinRelatedSample sample : samples)
        {
            builder.addRow(sample);
            builder.column(SAMPLE_IDENTIFIER).addString(sample.getIdentifier());
            builder.column(SAMPLE_TYPE).addString(sample.getEntityType().getCode());
            builder.column(ABUNDANCE).addDouble(sample.getAbundance());
            builder.column(MODIFIED_AMINO_ACID).addString(getAminoAcidName(sample));
            builder.column(MODIFICATION_POSITION).addInteger(sample.getModificationPosition());
            builder.column(MODIFICATION_MASS).addDouble(sample.getModificationMass());
            builder.column(MODIFICATION_FRACTION).addDouble(sample.getModificationFraction());
            builder.columnGroup(PROPERTIES_GROUP).addProperties(sample.getProperties());
        }
        return builder.getModel();
    }

    private String getAminoAcidName(ProteinRelatedSample sample)
    {
        char modifiedAminoAcid = sample.getModifiedAminoAcid();
        if (modifiedAminoAcid == 0)
        {
            return null;
        }
        try
        {
            char aminoAcidSymbol = Character.toUpperCase(modifiedAminoAcid);
            return AminoAcid.valueOf(Character.toString(aminoAcidSymbol)).getName();
        } catch (IllegalArgumentException ex)
        {
            return "?";
        }
    }

}
