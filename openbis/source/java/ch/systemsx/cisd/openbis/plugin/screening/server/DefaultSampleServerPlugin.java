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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import ch.systemsx.cisd.openbis.plugin.ISampleServerPlugin;
import ch.systemsx.cisd.openbis.plugin.ISampleTypeSlaveServerPlugin;

/**
 * The default {@link ISampleServerPlugin} implementation for the <i>screening</i> technology.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultSampleServerPlugin implements ISampleServerPlugin
{
    private static final DefaultSlaveServerPlugin GENERIC_SLAVE_SERVER = new DefaultSlaveServerPlugin();

    //
    // ISampleServerPlugin
    //

    public final String getSampleTypeCode()
    {
        throw new UnsupportedOperationException("No sample type code is associated to "
                + "the generic implementation.");
    }

    public final ISampleTypeSlaveServerPlugin getSlaveServer()
    {
        return GENERIC_SLAVE_SERVER;
    }
}
