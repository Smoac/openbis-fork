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
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.DataSetProtein;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSequence;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReference;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IPhosphoNetXServer extends IServer
{
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ProteinReference> listProteinsByExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            TechId experimentId, double falseDiscoveryRate) throws UserFailureException;
    
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public ProteinByExperiment getProteinByExperiment(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            TechId experimentId, TechId proteinReferenceID) throws UserFailureException;
    
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public List<ProteinSequence> listProteinSequencesByProteinReference(String sessionToken,
            TechId proteinReferenceID) throws UserFailureException;
    
    @Transactional
    @RolesAllowed(RoleSet.OBSERVER)
    public List<DataSetProtein> listProteinsByExperimentAndReference(String sessionToken,
            @AuthorizationGuard(guardClass = ExperimentTechIdPredicate.class)
            TechId experimentId, TechId proteinReferenceID) throws UserFailureException;
    
}
