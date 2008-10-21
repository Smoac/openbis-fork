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

package ch.systemsx.cisd.openbis.plugin;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * The slave server plug-in for a <i>Sample Type</i>.
 * <p>
 * Each implementation should be stateless. Additionally, each method specified here should start
 * with {@link IDAOFactory} resp. {@link Session} parameter, in case where the implementing method
 * needs more business information to do its job.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface ISampleTypeSlaveServerPlugin
{
    /**
     * For given {@link SamplePE} returns the {@link SampleGenerationDTO}.
     */
    SampleGenerationDTO getSampleInfo(final IDAOFactory factory, final Session session,
            final SamplePE sample);

}
