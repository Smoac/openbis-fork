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

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.plugin.AbstractSampleServerPlugin;
import ch.systemsx.cisd.openbis.plugin.ISampleServerPlugin;
import ch.systemsx.cisd.openbis.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;

/**
 * The {@link ISampleServerPlugin} implementation for plates of type <code>MASTER_PLATE</code>.
 * <p>
 * This class is annotated with {@link Component} so that it automatically gets registered to
 * {@link SampleServerPluginRegistry} by <i>Spring</i>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
@Component(ResourceNames.MASTER_PLATE_SAMPLE_SERVER_PLUGIN)
public final class MasterPlateSampleServerPlugin extends AbstractSampleServerPlugin
{
    private static final String MASTER_PLATE_TYPE_CODE = "MASTER_PLATE";

    private MasterPlateSampleServerPlugin()
    {
    }

    //
    // ISampleServerPlugin
    //

    public final String getSampleTypeCode()
    {
        return MASTER_PLATE_TYPE_CODE;
    }

    public final ISampleTypeSlaveServerPlugin getSlaveServer()
    {
        return getGenericSampleTypeSlaveServerPlugin();
    }
}
