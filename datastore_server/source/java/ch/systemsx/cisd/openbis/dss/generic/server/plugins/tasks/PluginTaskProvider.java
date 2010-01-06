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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * Stores plugin tasks factories of one type (the one specified as a generic).
 * 
 * @author Tomasz Pylak
 */
public class PluginTaskProvider<P>
{
    private final TableMap<String, AbstractPluginTaskFactory<P>> factories;

    public PluginTaskProvider(AbstractPluginTaskFactory<P>[] factories)
    {
        this.factories =
                new TableMap<String, AbstractPluginTaskFactory<P>>(Arrays.asList(factories),
                        new IKeyExtractor<String, AbstractPluginTaskFactory<P>>()
                            {
                                public String getKey(AbstractPluginTaskFactory<P> factory)
                                {
                                    return factory.getPluginDescription().getKey();
                                }
                            });
    }

    /** creates an instance of the plugin with the given key */
    public P createPluginInstance(String pluginKey, File storeRoot)
    {
        return factories.tryGet(pluginKey).createPluginInstance(storeRoot);
    }

    public DatastoreServiceDescription getPluginDescription(String pluginKey)
    {
        return factories.tryGet(pluginKey).getPluginDescription();
    }

    public List<DatastoreServiceDescription> getPluginDescriptions()
    {
        List<DatastoreServiceDescription> descriptions =
                new ArrayList<DatastoreServiceDescription>();
        for (AbstractPluginTaskFactory<?> factory : factories.values())
        {
            descriptions.add(factory.getPluginDescription());
        }
        return descriptions;
    }

    /** checks that all factories can produce plugins */
    public void check(boolean checkIfSerializable)
    {
        for (AbstractPluginTaskFactory<P> factory : factories.values())
        {
            factory.check(checkIfSerializable);
        }
    }

    /** Writes information about all plugin factory configurations to the log */
    public void logConfigurations()
    {
        for (AbstractPluginTaskFactory<P> factory : factories.values())
        {
            factory.logConfiguration();
        }
    }

}
