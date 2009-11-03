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

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DefaultValueValidatorFactory implements IValidatorFactory
{
    private enum ValueType
    {
        UNIQUE
        {
            @Override
            public IValidatorFactory createFactory(Properties properties)
            {
                return new UniqueValidatorFactory(properties);
            }
        },
        ANY
        {
            @Override
            public IValidatorFactory createFactory(Properties properties)
            {
                return AnyValidatorFactory.INSTANCE;
            }
        },
        STRING
        {
            @Override
            public IValidatorFactory createFactory(Properties properties)
            {
                return new StringValidatorFactory(properties);
            }
        },
        NUMERIC
        {
            @Override
            public IValidatorFactory createFactory(Properties properties)
            {
                // TODO Auto-generated method stub
                return null;
            }
        };

        abstract IValidatorFactory createFactory(Properties properties);
    }

    private static final String VALUE_TYPE_KEY = "value-type";
    
    private final IValidatorFactory factory;

    public DefaultValueValidatorFactory(Properties properties)
    {
        String property = properties.getProperty(VALUE_TYPE_KEY, "any");
        ValueType valueType = ValueType.valueOf(property.toUpperCase());
        if (valueType == null)
        {
            throw new ConfigurationFailureException("Invalid value of property '" + VALUE_TYPE_KEY
                    + "': " + property);
        }
        factory = valueType.createFactory(properties);
    }

    public IValidator createValidator()
    {
        return factory.createValidator();
    }

}
