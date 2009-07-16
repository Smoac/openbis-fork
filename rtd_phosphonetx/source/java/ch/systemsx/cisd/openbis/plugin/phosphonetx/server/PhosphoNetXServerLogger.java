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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;

/**
 * @author Franz-Josef Elmer
 */
public class PhosphoNetXServerLogger extends AbstractServerLogger implements IPhosphoNetXServer
{
    PhosphoNetXServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful, final long elapsedTime)
    {
        super(sessionManager, invocationSuccessful, elapsedTime);
    }

    public List<ProteinReference> listProteinsByExperiment(String sessionToken,
            TechId experimentId, double falseDiscoveryRate) throws UserFailureException
    {
        logAccess(sessionToken, "list_proteins_by_experiment", "ID(%s) FDR(%s)", experimentId,
                falseDiscoveryRate);
        return null;
    }

    public ProteinByExperiment getProteinByExperiment(String sessionToken, TechId experimentId,
            TechId proteinReferenceID) throws UserFailureException
    {
        logAccess(sessionToken, "get_protein_by_experiment",
                "EXPERIMENT_ID(%s) PROTEIN_REFERENCE_ID(%s)", experimentId, proteinReferenceID);
        return null;
    }

    public List<ProteinSequence> listProteinSequencesByProteinReference(String sessionToken,
            TechId proteinReferenceID) throws UserFailureException
    {
        logAccess(sessionToken, "list_protein_sequences_by_reference",
                "PROTEIN_REFERENCE_ID(%s)", proteinReferenceID);
        return null;
    }

    public List<DataSetProtein> listProteinsByExperimentAndReference(String sessionToken,
            TechId experimentId, TechId proteinReferenceID) throws UserFailureException
    {
        logAccess(sessionToken, "list_proteins_by_experiment_and_reference",
                "EXPERIMENT_ID(%s) PROTEIN_REFERENCE_ID(%s)", experimentId, proteinReferenceID);
        return null;
    }

    public List<SampleWithPropertiesAndAbundance> listSamplesWithAbundanceByProtein(
            String sessionToken, TechId proteinID) throws UserFailureException
    {
        logAccess(sessionToken, "list_samples_with_abundance_by_protein", "PROTEIN_ID(%s)",
                proteinID);
        return null;
    }

}
