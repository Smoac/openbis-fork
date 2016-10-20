/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation.config.IOperationExecutionConfig;
import ch.ethz.sis.openbis.generic.server.asapi.v3.task.OperationExecutionMarkTimeOutPendingMaintenanceTask;
import ch.ethz.sis.openbis.generic.server.asapi.v3.task.OperationExecutionMarkTimedOutOrDeletedMaintenanceTask;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.MaintenancePlugin;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskParameters;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * Configures and starts maintenance tasks.
 * 
 * @author Piotr Buczek
 */
public class MaintenanceTaskStarter implements ApplicationContextAware, InitializingBean,
        DisposableBean
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MaintenanceTaskStarter.class);

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Autowired
    private IOperationExecutionConfig operationExecutionConfig;

    private List<MaintenancePlugin> plugins;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        CommonServiceProvider.setApplicationContext(applicationContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        MaintenanceTaskParameters[] tasks =
                MaintenanceTaskUtils.createMaintenancePlugins(configurer.getResolvedProps());

        if (false == isTaskConfigured(tasks, OperationExecutionMarkTimeOutPendingMaintenanceTask.class))
        {
            tasks = addTask(tasks, OperationExecutionMarkTimeOutPendingMaintenanceTask.class,
                    operationExecutionConfig.getMarkTimeOutPendingTaskName(),
                    operationExecutionConfig.getMarkTimeOutPendingTaskInterval());
        }

        if (false == isTaskConfigured(tasks, OperationExecutionMarkTimedOutOrDeletedMaintenanceTask.class))
        {
            tasks = addTask(tasks, OperationExecutionMarkTimedOutOrDeletedMaintenanceTask.class,
                    operationExecutionConfig.getMarkTimedOutOrDeletedTaskName(),
                    operationExecutionConfig.getMarkTimedOutOrDeletedTaskInterval());
        }

        plugins = MaintenanceTaskUtils.startupMaintenancePlugins(tasks);
    }

    private boolean isTaskConfigured(MaintenanceTaskParameters[] tasks, Class<?> clazz)
    {
        for (MaintenanceTaskParameters task : tasks)
        {
            if (clazz.getName().equals(task.getClassName()))
            {
                return true;
            }
        }
        return false;
    }

    private MaintenanceTaskParameters[] addTask(MaintenanceTaskParameters[] tasks, Class<?> clazz, String name, int interval)
    {
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, clazz.getName());
        properties.setProperty(MaintenanceTaskParameters.INTERVAL_KEY, String.valueOf(interval));

        operationLog.info("Automatically adding maintenance task '" + name + "' (class: " + clazz.getName() + ", interval: " + interval + ")");

        List<MaintenanceTaskParameters> tasksList = new ArrayList<MaintenanceTaskParameters>(Arrays.asList(tasks));
        tasksList.add(new MaintenanceTaskParameters(properties, name));
        return tasksList.toArray(new MaintenanceTaskParameters[] {});
    }

    @Override
    public void destroy() throws Exception
    {
        MaintenanceTaskUtils.shutdownMaintenancePlugins(plugins);
    }

    public List<MaintenancePlugin> getPlugins()
    {
        return Collections.unmodifiableList(plugins);
    }

}
