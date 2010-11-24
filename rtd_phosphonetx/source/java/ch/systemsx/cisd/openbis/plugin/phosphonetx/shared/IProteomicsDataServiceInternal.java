/*
 * Copyright 2010 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DataSetCodeCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.ExperimentValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.authorization.validator.RawDataSampleValidator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IProteomicsDataServiceInternal extends IServer
{
    /**
     * Returns all samples of type MS_INJECTION in group MS_DATA which have a parent sample which
     * the specified user is allow to read.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @ReturnValueFilter(validatorClass = RawDataSampleValidator.class)
    public List<MsInjectionSample> listRawDataSamples(String sessionToken);
    
    @Deprecated
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void processRawData(String sessionToken, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType);
    
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void processDataSets(
            String sessionToken,
            String dataSetProcessingKey,
            @AuthorizationGuard(guardClass = DataSetCodeCollectionPredicate.class) List<String> dataSetCodes);
    
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    @ReturnValueFilter(validatorClass = ExperimentValidator.class)
    public List<Experiment> listExperiments(String sessionToken, String experimentTypeCode);
    
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_USER)
    public void processProteinResultDataSets(String sessionToken, String dataSetProcessingKey,
            String experimentTypeCode, long[] searchExperimentIDs);
    
}