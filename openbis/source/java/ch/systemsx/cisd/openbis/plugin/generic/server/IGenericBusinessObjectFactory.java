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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * A <i>generic</i> plugin specific business object factory.
 * 
 * @author Christian Ribeaud
 */
public interface IGenericBusinessObjectFactory
{

    /**
     * Creates a {@link ISampleBO} <i>Business Object</i>.
     */
    public ISampleBO createSampleBO(final Session session);

    /**
     * Creates a {@link IExperimentBO} <i>Business Object</i>.
     */
    public IExperimentBO createExperimentBO(final Session session);

    /**
     * Creates a {@link ISampleTable} <i>Business Object</i>.
     */
    public ISampleTable createSampleTable(final Session session);
}
