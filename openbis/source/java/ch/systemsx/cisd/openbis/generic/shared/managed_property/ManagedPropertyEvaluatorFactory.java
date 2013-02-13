/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.managed_property;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.ethz.cisd.hotdeploy.PluginMapHolder;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.server.IHotDeploymentController;
import ch.systemsx.cisd.openbis.generic.server.JythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * Factory for creating managed property evaluators. (Could do some caching or other cleverness.)
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Jakub Straszewski
 * @author Pawel Glyzewski
 */
public class ManagedPropertyEvaluatorFactory implements IManagedPropertyEvaluatorFactory
{
    private PluginMapHolder<IManagedPropertyEvaluator> predeployedPlugins;

    public ManagedPropertyEvaluatorFactory(IHotDeploymentController hotDeploymentController,
            String pluginDirectoryPath)
    {
        if (false == StringUtils.isBlank(pluginDirectoryPath))
        {
            this.predeployedPlugins =
                    hotDeploymentController.getPluginMap(IManagedPropertyEvaluator.class);
            hotDeploymentController.addPluginDirectory(new File(pluginDirectoryPath));
        } else
        {
            this.predeployedPlugins = null;
        }
    }

    @Override
    public IManagedPropertyEvaluator createManagedPropertyEvaluator(
            EntityTypePropertyTypePE entityTypePropertyTypePE)
    {
        final ScriptPE scriptPE = entityTypePropertyTypePE.getScript();
        assert scriptPE != null && scriptPE.getScriptType() == ScriptType.MANAGED_PROPERTY;

        return getManagedPropertyEvaluator(scriptPE.getPluginType(), scriptPE.getName(),
                scriptPE.getScript());
    }

    @Override
    public IManagedPropertyEvaluator createManagedPropertyEvaluator(
            EntityTypePropertyType<?> entityTypePropertyType)
    {
        Script script = entityTypePropertyType.getScript();

        return getManagedPropertyEvaluator(script.getPluginType(), script.getName(),
                script.getScript());
    }

    private IManagedPropertyEvaluator getManagedPropertyEvaluator(PluginType pluginType,
            String scriptName, String scriptBody)
    {
        switch (pluginType)
        {
            case JYTHON:
                return createJythonManagedPropertyEvaluator(scriptBody);
            case PREDEPLOYED:
                if (predeployedPlugins == null)
                {
                    throw new UserFailureException(
                            "Predeployed managed property evaluator plugins are not configured properly.");
                }
                IManagedPropertyEvaluator managedPropertyEvaluator =
                        predeployedPlugins.tryGet(scriptName);
                if (managedPropertyEvaluator == null)
                {
                    throw new UserFailureException("Couldn't find plugin named '" + scriptName
                            + "'.");
                }

                return managedPropertyEvaluator;

        }

        return null;
    }

    private static JythonManagedPropertyEvaluator createJythonManagedPropertyEvaluator(String script)
    {
        if (JythonEvaluatorPool.INSTANCE != null)
        {
            return new JythonManagedPropertyEvaluator(
                    JythonEvaluatorPool.INSTANCE.getManagedPropertiesRunner(script));
        } else
        {
            return new JythonManagedPropertyEvaluator(script);
        }
    }

    @Override
    public List<String> listPredeployedPlugins()
    {
        if (predeployedPlugins == null)
        {
            return Collections.emptyList();
        }

        return new ArrayList<String>(predeployedPlugins.getPluginNames());
    }
}
