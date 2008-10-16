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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.plugin.generic.server.GenericSampleServerPlugin;

/**
 * A sample server registry for plug-ins.
 * 
 * @author Christian Ribeaud
 */
public final class SampleServerPluginRegistry
{
    private final static String PACKAGE_START = "ch.systemsx.cisd.openbis.plugin.";

    private final static ISampleServerPlugin GENERIC_SAMPLE_SERVER_PLUGIN =
            new GenericSampleServerPlugin();

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SampleServerPluginRegistry.class);

    private final static Map<TechnologySampleType, ISampleServerPlugin> plugins =
            new HashMap<TechnologySampleType, ISampleServerPlugin>();

    private SampleServerPluginRegistry()
    {
        // Can not be instantiated.
    }

    /**
     * Register given {@link ISampleServerPlugin}.
     */
    public final static synchronized void registerPlugin(final ISampleServerPlugin plugin)
    {
        assert plugin != null : "Unspecified plugin.";
        final String sampleTypeCode = plugin.getSampleTypeCode();
        assert sampleTypeCode != null : "Unspecified sample type code.";
        final Technology technology = getTechnology(plugin);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Plugin '%s' registered for technology '%s' and sample type code '%s'.", plugin
                            .getClass().getName(), technology, sampleTypeCode));
        }
        final TechnologySampleType technologySampleType =
                new TechnologySampleType(technology, sampleTypeCode);
        final ISampleServerPlugin serverPlugin = plugins.get(technologySampleType);
        if (serverPlugin != null)
        {
            throw new IllegalArgumentException(String.format(
                    "There is already a plugin '%s' registered for '%s'.", serverPlugin.getClass()
                            .getName(), technologySampleType));
        }
        plugins.put(technologySampleType, plugin);
    }

    private final static Technology getTechnology(final ISampleServerPlugin plugin)
    {
        final String packageName = plugin.getClass().getPackage().getName();
        assert packageName.startsWith(PACKAGE_START) : String.format(
                "Package name '%s' does not start as expected '%s'.", packageName, PACKAGE_START);
        final int len = PACKAGE_START.length();
        final String name = packageName.substring(len, packageName.indexOf('.', len));
        return new Technology(name.toUpperCase());
    }

    /**
     * Returns the appropriate plug-in for given sample type and given technology.
     * 
     * @return never <code>null</code> but could return the generic implementation if none has
     *         been found for given technology and given sample type.
     */
    public final static synchronized ISampleServerPlugin getPlugin(final Technology technology,
            final SampleTypePE sampleType)
    {
        assert technology != null : "Unspecified technology.";
        assert sampleType != null : "Unspecified sample type.";
        final ISampleServerPlugin sampleServerPlugin =
                plugins.get(new TechnologySampleType(technology, sampleType.getCode()));
        if (sampleServerPlugin == null)
        {
            return GENERIC_SAMPLE_SERVER_PLUGIN;
        }
        return sampleServerPlugin;
    }

    //
    // Helper classes
    //

    private final static class TechnologySampleType extends AbstractHashable
    {
        final Technology technology;

        final String sampleTypeCode;

        TechnologySampleType(final Technology technology, final String sampleTypeCode)
        {
            this.technology = technology;
            this.sampleTypeCode = sampleTypeCode;
        }
    }
}
