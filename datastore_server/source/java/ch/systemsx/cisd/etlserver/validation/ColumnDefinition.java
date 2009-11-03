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

package ch.systemsx.cisd.etlserver.validation;

import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ColumnDefinition
{
    private static final String MANDATORY_KEY = "mandatory";
    private static final String ORDER_KEY = "order";
    private static final String HEADER_VALIDATOR_KEY = "header-validator";
    private static final String HEADER_PATTERN_KEY = "header-pattern";
    private static final String VALUE_VALIDATOR_KEY = "value-validator";
    
    private final String name;
    private final IColumnHeaderValidator headerValidator;
    private final IValidatorFactory valueValidatorFactory;
    private final boolean mandatory;
    private final Integer orderOrNull;
    
    static ColumnDefinition create(String name, Properties properties)
    {
        boolean mandatory = PropertyUtils.getBoolean(properties, MANDATORY_KEY, false);
        Integer order = null;
        if (properties.getProperty(ORDER_KEY) != null)
        {
            order = PropertyUtils.getInt(properties, ORDER_KEY, 0);
            if (order < 1)
            {
                throw new ConfigurationFailureException("Order value has to be positive: " + order);
            }
        }
        String headerValidatorName = properties.getProperty(HEADER_VALIDATOR_KEY);
        IColumnHeaderValidator headerValidator;
        if (headerValidatorName == null)
        {
            headerValidator =
                    new RegExBasedValidator(properties.getProperty(HEADER_PATTERN_KEY, ".*"));
        } else
        {
            headerValidator =
                    ClassUtils
                            .create(IColumnHeaderValidator.class, headerValidatorName, properties);
        }
        String validatorFactoryName =
                properties.getProperty(VALUE_VALIDATOR_KEY, DefaultValueValidatorFactory.class
                        .getName());
        IValidatorFactory factory =
                ClassUtils.create(IValidatorFactory.class, validatorFactoryName, properties);
        return new ColumnDefinition(name, headerValidator, factory, mandatory, order);
    }

    ColumnDefinition(String name, IColumnHeaderValidator headerValidator,
            IValidatorFactory valueValidatorFactory, boolean mandatory, Integer orderOrNull)
    {
        this.name = name;
        this.headerValidator = headerValidator;
        this.valueValidatorFactory = valueValidatorFactory;
        this.mandatory = mandatory;
        this.orderOrNull = orderOrNull;
    }

    boolean isMandatory()
    {
        return mandatory;
    }

    Integer getOrderOrNull()
    {
        return orderOrNull;
    }

    String getName()
    {
        return name;
    }

    void assertValidHeader(String header)
    {
        if (isValidHeader(header) == false)
        {
            throw new UserFailureException("According to column definition '" + name
                    + "' the following header is invalid: " + header);
        }
    }

    boolean isValidHeader(String header)
    {
        return headerValidator.isValidHeader(header);
    }

    IValidator createValidator()
    {
        return valueValidatorFactory.createValidator();
    }
}
