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

package ch.systemsx.cisd.etlserver.plugins;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.IAutoArchiverPolicy;
import ch.systemsx.cisd.etlserver.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * {@link IMaintenanceTask} performing automatic archivization of data sets.
 * 
 * @author Piotr Buczek
 */
public class AutoArchiverTask implements IMaintenanceTask
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AutoArchiverTask.class);

    private static final String POLICY_SECTION_NAME = "policy";

    private static final String CLASS_PROPERTY_NAME = "class";

    private static final String DATA_SET_TYPE_PROPERTY_NAME = "data-set-type";

    private static final String OLDER_THAN_PROPERTY_NAME = "older-than";

    private IEncapsulatedOpenBISService openBISService;

    private IAutoArchiverPolicy policyOrNull;

    private ArchiverDataSetCriteria criteria;

    public void execute()
    {
        operationLog.info("start");
        List<ExternalData> dataSets = openBISService.listActiveDataSets(criteria);
        if (policyOrNull != null)
        {
            policyOrNull.filter(dataSets);
        }
        if (dataSets.isEmpty())
        {
            operationLog.info("nothing to archive");
        } else
        {
            operationLog.info("archiving: "
                    + CollectionUtils.abbreviate(Code.extractCodes(dataSets), 10));
            openBISService.archiveDataSets(Code.extractCodes(dataSets));
        }
        operationLog.info("end");
    }

    public void setUp(String pluginName, Properties properties)
    {
        openBISService = ServiceProvider.getOpenBISService();
        criteria = createCriteria(properties);
        SectionProperties policySectionProperties =
                PropertyParametersUtil.extractSingleSectionProperties(properties,
                        POLICY_SECTION_NAME, false);
        policyOrNull = tryCreatePolicyInstance(policySectionProperties);
        operationLog.info("Plugin " + pluginName + " initialized");
    }

    private ArchiverDataSetCriteria createCriteria(Properties properties)
    {
        String dataSetTypeCodeOrNull = properties.getProperty(DATA_SET_TYPE_PROPERTY_NAME);
        int olderThan = PropertyUtils.getInt(properties, OLDER_THAN_PROPERTY_NAME, 0);
        return new ArchiverDataSetCriteria(olderThan, dataSetTypeCodeOrNull);
    }

    private IAutoArchiverPolicy tryCreatePolicyInstance(SectionProperties policySectionProperties)
    {
        String className = policySectionProperties.getProperties().getProperty(CLASS_PROPERTY_NAME);
        if (className == null)
        {
            return null;
        }
        try
        {
            return ClassUtils.create(IAutoArchiverPolicy.class, className, policySectionProperties
                    .getProperties());
        } catch (ConfigurationFailureException ex)
        {
            throw ex; // rethrow the exception without changing the message
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find the policy class '" + className
                    + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

}
