/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;

/**
 * Definition of the client-server interface for tracking creation of samples and datasets.
 * 
 * @author Piotr Buczek
 */
public interface ITrackingServer extends IServer
{

    /**
     * Lists samples using given criteria.
     * 
     * @return a sorted list of {@link Sample}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public List<Sample> listSamples(final String sessionToken,
            final TrackingSampleCriteria criteria);

    /**
     * For given sample {@link TechId} returns the corresponding list of {@link ExternalData}.
     * 
     * @return a sorted list of {@link ExternalData}.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public List<ExternalData> listDataSets(final String sessionToken,
            final TrackingDataSetCriteria criteria);

}
