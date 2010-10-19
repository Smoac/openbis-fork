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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.IDynamicPropertyEvaluationScheduler;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * {@link IMaintenanceTask} for reevaluation of all dynamic properties.
 * 
 * @author Piotr Buczek
 */
public class DynamicPropertyEvaluationMaintenanceTask implements IMaintenanceTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DynamicPropertyEvaluationMaintenanceTask.class);

    private static final List<Class<? extends IEntityInformationWithPropertiesHolder>> entityClasses =
            new ArrayList<Class<? extends IEntityInformationWithPropertiesHolder>>();

    static
    {
        entityClasses.add(ExternalDataPE.class);
        entityClasses.add(ExperimentPE.class);
        entityClasses.add(MaterialPE.class);
        entityClasses.add(SamplePE.class);
    }

    public void execute()
    {
        operationLog.info("execution started");
        IDynamicPropertyEvaluationScheduler scheduler =
                CommonServiceProvider.getDAOFactory().getDynamicPropertyEvaluationScheduler();

        // all entities will be scheduled for update so previous schedule can be cleared
        scheduler.clear();
        for (Class<? extends IEntityInformationWithPropertiesHolder> entityClass : entityClasses)
        {
            DynamicPropertyEvaluationOperation operation =
                    DynamicPropertyEvaluationOperation.evaluateAll(entityClass);
            scheduler.scheduleUpdate(operation);
        }
        operationLog.info("task executed");
    }

    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Task " + pluginName + " initialized.");
    }

}
