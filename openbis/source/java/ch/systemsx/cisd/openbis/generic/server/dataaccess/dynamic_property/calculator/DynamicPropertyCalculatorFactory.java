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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyCalculatorFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDynamicPropertyCalculator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDynamicPropertyCalculatorHotDeployPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.AbstractCommonPropertyBasedHotDeployPluginFactory;

/**
 * The class is responsible for getting a dynamic property calculator.
 * 
 * @author Pawel Glyzewski
 */
public class DynamicPropertyCalculatorFactory
        extends
        AbstractCommonPropertyBasedHotDeployPluginFactory<IDynamicPropertyCalculatorHotDeployPlugin>
        implements IDynamicPropertyCalculatorFactory
{
    public DynamicPropertyCalculatorFactory(String pluginDirectoryPath)
    {
        super(pluginDirectoryPath);
    }

    @Override
    /** Returns a calculator for given script (creates a new one if nothing is found in cache). */
    public IDynamicPropertyCalculator getCalculator(EntityTypePropertyTypePE etpt)
    {
        switch (etpt.getScript().getPluginType())
        {
            case JYTHON:
                return JythonDynamicPropertyCalculator.create(etpt.getScript().getScript());
            case PREDEPLOYED:
                IDynamicPropertyCalculator dynamicPropertyCalculator =
                        tryGetPredeployedPluginByName(etpt.getScript().getName());
                if (dynamicPropertyCalculator == null)
                {
                    throw new UserFailureException("Couldn't find plugin named '"
                            + etpt.getScript().getName() + "'.");
                }

                return dynamicPropertyCalculator;
        }

        return null;
    }

    @Override
    protected String getPluginDescription()
    {
        return "dynamic property";
    }

    @Override
    protected Class<IDynamicPropertyCalculatorHotDeployPlugin> getPluginClass()
    {
        return IDynamicPropertyCalculatorHotDeployPlugin.class;
    }

    @Override
    protected ScriptType getScriptType()
    {
        return ScriptType.DYNAMIC_PROPERTY;
    }

    @Override
    protected String getDefaultPluginSubDirName()
    {
        return "dynamic-properties";
    }
}
