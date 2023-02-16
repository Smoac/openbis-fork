/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;

/**
 * The default {@link ISampleServerPlugin} implementation for the <i>generic</i> technology.
 * 
 * @author Christian Ribeaud
 */
@Component(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.GENERIC_SAMPLE_SERVER_PLUGIN)
public final class GenericSampleServerPlugin extends AbstractGenericServerPlugin implements ISampleServerPlugin
{
    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.GENERIC_SAMPLE_TYPE_SLAVE_SERVER_PLUGIN)
    private GenericSampleTypeSlaveServerPlugin genericSampleTypeSlaveServerPlugin;

    public GenericSampleServerPlugin()
    {
    }

    //
    // ISampleServerPlugin
    //

    @Override
    public final ISampleTypeSlaveServerPlugin getSlaveServer()
    {
        return genericSampleTypeSlaveServerPlugin;
    }
}
