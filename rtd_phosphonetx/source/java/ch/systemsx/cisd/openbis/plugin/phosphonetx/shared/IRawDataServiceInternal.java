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
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.authorization.validator.RawDataSampleValidator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IRawDataServiceInternal extends IServer
{
    /**
     * Returns all samples of type MS_INJECTION in group MS_DATA which have a parent sample which
     * the specified user is allow to read.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.USER)
    @ReturnValueFilter(validatorClass = RawDataSampleValidator.class)
    public List<Sample> listRawDataSamples(String sessionToken);
    
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.USER)
    public void processRawData(String sessionToken, String dataSetProcessingKey, long[] rawDataSampleIDs);
}