/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * DataSetInfoExtractor which delegates all the tasks to the instance specified in configuration
 * with {@link #DELEGATOR_CLASS_PROPERTY} property.
 * <p>
 * This class is supposed to be extended to add specific functionality which has to be performed
 * besides the basic operations.
 * </p>
 * 
 * @author Tomasz Pylak
 */
// TODO 2009-07-13, Tomasz Pylak: move to datastore_server project at the end of the sprint
abstract public class AbstractDelegatingDataSetInfoExtractor implements IDataSetInfoExtractor
{
    /**
     * Property name which is used to specify the class of the default data set info extractor, to
     * which all calls are delegated.
     */
    public final static String DELEGATOR_CLASS_PROPERTY = "delegator";

    final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION,
                    AbstractDelegatingDataSetInfoExtractor.class);

    private final IDataSetInfoExtractor delegator;

    protected AbstractDelegatingDataSetInfoExtractor(Properties properties)
    {
        this(createDelegator(properties));
    }

    protected AbstractDelegatingDataSetInfoExtractor(IDataSetInfoExtractor delegator)
    {
        this.delegator = delegator;
    }

    @Private
    static IDataSetInfoExtractor createDelegator(Properties properties)
    {
        String delegateClass =
                PropertyUtils.getMandatoryProperty(properties, DELEGATOR_CLASS_PROPERTY);
        Properties p =
                ExtendedProperties.getSubset(properties, DELEGATOR_CLASS_PROPERTY + ".", true);
        return createClass(IDataSetInfoExtractor.class, delegateClass, p);
    }

    private final static <T> T createClass(final Class<T> superClazz, String className,
            Object... argumentsOrNull)
    {
        try
        {
            return ClassUtils.create(superClazz, className, argumentsOrNull);
        } catch (IllegalArgumentException ex)
        {
            throw new ConfigurationFailureException(ex.getMessage());
        }
    }

    //
    // delegation
    //

    public DataSetInformation getDataSetInformation(File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        return delegator.getDataSetInformation(incomingDataSetPath, openbisService);
    }
}
