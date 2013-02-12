/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.io.File;

import ch.ethz.cisd.hotdeploy.PluginContainer;
import ch.ethz.cisd.hotdeploy.PluginMapHolder;

/**
 * @author Pawel Glyzewski
 */
public class HotDeploymentController implements IHotDeploymentController
{
    private final PluginContainer pluginContainer;

    public HotDeploymentController()
    {
        PluginContainer.initHotDeployment();
        pluginContainer = PluginContainer.tryGetInstance();
    }

    @Override
    public void addPluginDirectory(File pluginDirectory)
    {
        pluginContainer.addPluginDirectory(pluginDirectory);
    }

    @Override
    public <T> PluginMapHolder<T> getPluginMap(Class<T> pluginClass)
    {
        return new PluginMapHolder<T>(pluginContainer, pluginClass);
    }
}
