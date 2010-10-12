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

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ch.systemsx.cisd.common.maintenance.MaintenanceTaskParameters;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * Configures and starts maintenance tasks.
 * 
 * @author Piotr Buczek
 */
public class MaintenanceTaskStarterRunnable implements Runnable, ApplicationContextAware
{

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        CommonServiceProvider.setApplicationContext(applicationContext);
    }

    public void run()
    {
        MaintenanceTaskParameters[] tasks =
                MaintenanceTaskUtils.createMaintenancePlugins(configurer.getResolvedProps());
        MaintenanceTaskUtils.startupMaintenancePlugins(tasks);
    }

}
