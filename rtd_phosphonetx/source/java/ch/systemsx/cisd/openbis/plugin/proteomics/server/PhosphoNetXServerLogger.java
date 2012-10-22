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

package ch.systemsx.cisd.openbis.plugin.proteomics.server;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinRelatedSample;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinSummary;

/**
 * @author Franz-Josef Elmer
 */
public class PhosphoNetXServerLogger extends AbstractServerLogger implements IPhosphoNetXServer
{
    PhosphoNetXServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    @Override
    public Vocabulary getTreatmentTypeVocabulary(String sessionToken) throws UserFailureException
    {
        logAccess(sessionToken, "get_treatment_type_vocabulary");
        return null;
    }

    @Override
    public List<AbundanceColumnDefinition> getAbundanceColumnDefinitionsForProteinByExperiment(
            String sessionToken, TechId experimentID, String treatmentTypeOrNull)
            throws UserFailureException
    {
        logAccess(sessionToken, "get_abundance_colum_definitions",
                "EXPERIMENT_ID(%s) TREATMENT_TYPE(%s)", experimentID,
                treatmentTypeOrNull);
        return null;
    }

    @Override
    public List<ProteinInfo> listProteinsByExperiment(String sessionToken, TechId experimentId,
            double falseDiscoveryRate, AggregateFunction function, String treatmentTypeCode,
            boolean aggregateOnOriginal) throws UserFailureException
    {
        logAccess(sessionToken, "list_proteins_by_experiment",
                "ID(%s) FDR(%s) AGGREGATE_FUNCTION(%s) TREATMENT_TYPE(%s) "
                        + "AGGREGATE_ON_ORIGINAL(%s)", experimentId, falseDiscoveryRate, function
                        .getLabel(), treatmentTypeCode, aggregateOnOriginal);
        return null;
    }
    
    @Override
    public List<ProteinSummary> listProteinSummariesByExperiment(String sessionToken,
            TechId experimentId) throws UserFailureException
            {
        logAccess(sessionToken, "list_protein_summaries_by_experiment", "EXPERIMENT_ID(%s)",
                experimentId);
        return null;
            }

    @Override
    public ProteinByExperiment getProteinByExperiment(String sessionToken, TechId experimentId,
            TechId proteinReferenceID) throws UserFailureException
    {
        logAccess(sessionToken, "get_protein_by_experiment",
                "EXPERIMENT_ID(%s) PROTEIN_REFERENCE_ID(%s)", experimentId, proteinReferenceID);
        return null;
    }

    @Override
    public List<ProteinSequence> listProteinSequencesByProteinReference(String sessionToken,
            TechId experimentID, TechId proteinReferenceID) throws UserFailureException
    {
        logAccess(sessionToken, "list_protein_sequences_by_reference",
                "EXPERIMENT_ID(%s) PROTEIN_REFERENCE_ID(%s)", experimentID, proteinReferenceID);
        return null;
    }

    @Override
    public List<DataSetProtein> listProteinsByExperimentAndReference(String sessionToken,
            TechId experimentId, TechId proteinReferenceID) throws UserFailureException
    {
        logAccess(sessionToken, "list_proteins_by_experiment_and_reference",
                "EXPERIMENT_ID(%s) PROTEIN_REFERENCE_ID(%s)", experimentId, proteinReferenceID);
        return null;
    }

    @Override
    public List<ProteinRelatedSample> listProteinRelatedSamplesByProtein(String sessionToken,
            TechId experimentID, TechId proteinReferenceID) throws UserFailureException
    {
        logAccess(sessionToken, "list_protein_related_samples_by_protein",
                "EXPERIMENT_ID(%s) PROTEIN_REFERENCE_ID(%s)", experimentID, proteinReferenceID);
        return null;
    }

}
