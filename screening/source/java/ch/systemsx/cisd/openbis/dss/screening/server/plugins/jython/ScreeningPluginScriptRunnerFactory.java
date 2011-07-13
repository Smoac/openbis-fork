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

package ch.systemsx.cisd.openbis.dss.screening.server.plugins.jython;

import org.springframework.beans.factory.BeanFactory;

import ch.systemsx.cisd.common.evaluator.Evaluator;
import ch.systemsx.cisd.openbis.dss.generic.server.openbisauth.OpenBISSessionHolder;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.PluginScriptRunnerFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacadeFactory;

/**
 * A version of the {@link PluginScriptRunnerFactory} with extra support for screening.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ScreeningPluginScriptRunnerFactory extends PluginScriptRunnerFactory
{
    private static final long serialVersionUID = 1L;

    private final static String SCREENING_FACADE_VARIABLE_NAME = "screeningFacade";

    /**
     * Public constructor
     * 
     * @param scriptPath
     */
    public ScreeningPluginScriptRunnerFactory(String scriptPath)
    {
        super(scriptPath);
    }

    @Override
    protected Evaluator createEvaluator(String scriptString, DataSetProcessingContext context)
    {
        Evaluator evaluator = super.createEvaluator(scriptString, context);
        evaluator.set(SCREENING_FACADE_VARIABLE_NAME, createScreeningFacade(context));
        return evaluator;
    }

    private IScreeningOpenbisServiceFacade createScreeningFacade(DataSetProcessingContext context)
    {
        BeanFactory applicationContext = ServiceProvider.getApplicationContext();
        OpenBISSessionHolder sessionContextHolder =
                (OpenBISSessionHolder) applicationContext.getBean("sessionHolder");
        return ScreeningOpenbisServiceFacadeFactory.tryCreate(context.trySessionToken(),
                sessionContextHolder.getServerUrl());
    }
}
