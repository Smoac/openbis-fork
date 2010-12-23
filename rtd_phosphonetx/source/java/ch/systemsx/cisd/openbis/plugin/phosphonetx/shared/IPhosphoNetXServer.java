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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.SampleWithPropertiesAndAbundance;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IPhosphoNetXServer extends IServer
{
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public Vocabulary getTreatmentTypeVocabulary(String sessionToken) throws UserFailureException;

    
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @CacheData
    public List<AbundanceColumnDefinition> getAbundanceColumnDefinitionsForProteinByExperiment(
            String sessionToken, @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            TechId experimentID, String treatmentTypeOrNull) throws UserFailureException; 
    
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    @CacheData
    public List<ProteinInfo> listProteinsByExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            TechId experimentId, double falseDiscoveryRate, AggregateFunction function,
            String treatmentTypeCode, boolean aggregateOnOriginal) throws UserFailureException;
    
    
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<ProteinSummary> listProteinSummariesByExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class) TechId experimentId)
            throws UserFailureException;
    
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public ProteinByExperiment getProteinByExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            TechId experimentId, TechId proteinReferenceID) throws UserFailureException;
    
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<ProteinSequence> listProteinSequencesByProteinReference(String sessionToken,
            TechId proteinReferenceID) throws UserFailureException;
    
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<DataSetProtein> listProteinsByExperimentAndReference(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            TechId experimentId, TechId proteinReferenceID) throws UserFailureException;
    
    @Transactional
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public List<SampleWithPropertiesAndAbundance> listSamplesWithAbundanceByProtein(
            String sessionToken, TechId experimentID, TechId proteinReferenceID)
            throws UserFailureException;

}
