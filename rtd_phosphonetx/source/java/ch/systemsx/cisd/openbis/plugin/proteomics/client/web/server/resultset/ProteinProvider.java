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

import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinBrowserColumnIDs.ACCESSION_NUMBER;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinBrowserColumnIDs.COVERAGE;
import static ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinBrowserColumnIDs.PROTEIN_DESCRIPTION;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumn;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.Treatment;

/**
 * Provider of {@link ProteinInfo} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class ProteinProvider extends AbstractTableModelProvider<ProteinInfo>
{
    private static final String ABUNDANCE_PROPERTY_KEY = "ABUNDANCE";

    private final IPhosphoNetXServer server;

    private final String sessionToken;

    private final TechId experimentID;

    private final double falseDiscoveryRate;

    private final AggregateFunction aggregateFunction;

    private final String treatmentTypeCode;

    private final boolean aggregateOnOriginal;

    public ProteinProvider(IPhosphoNetXServer server, String sessionToken, TechId experimentID,
            double falseDiscoveryRate, AggregateFunction function, String treatmentTypeCode,
            boolean aggregateOnOriginal)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.experimentID = experimentID;
        this.falseDiscoveryRate = falseDiscoveryRate;
        this.aggregateFunction = function;
        this.treatmentTypeCode = treatmentTypeCode;
        this.aggregateOnOriginal = aggregateOnOriginal;
    }

    @Override
    protected TypedTableModel<ProteinInfo> createTableModel()
    {
        try
        {

            List<AbundanceColumnDefinition> abundanceColumnDefinitions =
                    server.getAbundanceColumnDefinitionsForProteinByExperiment(sessionToken,
                            experimentID, treatmentTypeCode);
            List<ProteinInfo> proteins =
                    server.listProteinsByExperiment(sessionToken, experimentID, falseDiscoveryRate,
                            aggregateFunction, treatmentTypeCode, aggregateOnOriginal);
            TypedTableModelBuilder<ProteinInfo> builder = new TypedTableModelBuilder<ProteinInfo>();
            builder.addColumn(ACCESSION_NUMBER);
            builder.addColumn(PROTEIN_DESCRIPTION);
            builder.addColumn(COVERAGE).withDefaultWidth(100);
            Map<Long, IColumn> sampleIdToAbundanceColumnMap = new HashMap<Long, IColumn>();
            for (AbundanceColumnDefinition abundanceColumnDefinition : abundanceColumnDefinitions)
            {
                long sampleID = abundanceColumnDefinition.getID();
                String columnID = "abundance-" + Long.toString(sampleID);
                builder.addColumn(columnID).withDefaultWidth(100);
                IColumn column = builder.column(columnID);
                sampleIdToAbundanceColumnMap.put(sampleID, column);
                String header = abundanceColumnDefinition.getSampleCode();
                Map<String, String> properties = new HashMap<String, String>();
                properties.put(ABUNDANCE_PROPERTY_KEY, header);
                List<Treatment> treatments = abundanceColumnDefinition.getTreatments();
                if (treatments.isEmpty() == false)
                {
                    String delim;
                    if (header == null)
                    {
                        header = "";
                        delim = "";
                    } else
                    {
                        delim = ": ";
                    }
                    for (Treatment treatment : treatments)
                    {
                        header += delim + treatment;
                        delim = ", ";
                        column.property(treatment.getTypeCode(), treatment.getValue());
                    }
                }
                column.withTitle(header);
            }
            for (ProteinInfo protein : proteins)
            {
                builder.addRow(protein);
                builder.column(ACCESSION_NUMBER).addString(protein.getAccessionNumber());
                builder.column(PROTEIN_DESCRIPTION).addString(protein.getDescription());
                builder.column(COVERAGE).addDouble(protein.getCoverage());
                Map<Long, Double> abundances = protein.getAbundances();
                Set<Entry<Long, Double>> entrySet = abundances.entrySet();
                for (Entry<Long, Double> entry : entrySet)
                {
                    IColumn column = sampleIdToAbundanceColumnMap.get(entry.getKey());
                    if (column != null)
                    {
                        column.addDouble(entry.getValue());
                    }
                }
            }
            return builder.getModel();
        } catch (Exception e)
        {
            Throwable t = e;
            while (t != null)
            {
                if (t instanceof HighLevelException)
                {
                    throw (HighLevelException) t;
                }
                t = t.getCause();
            }
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }

    }

}
