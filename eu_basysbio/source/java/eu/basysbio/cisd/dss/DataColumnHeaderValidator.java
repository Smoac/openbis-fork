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

package eu.basysbio.cisd.dss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.validation.IColumnHeaderValidator;
import ch.systemsx.cisd.etlserver.validation.Result;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil.SectionProperties;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DataColumnHeaderValidator implements IColumnHeaderValidator
{
    private static final String SEPARATOR = "::";
    static final String ELEMENTS_KEY = "elements";
    static final String TYPE_KEY = "type";
    
    private static interface IElementValidator
    {
        public String validate(String element);
    }
    
    private static interface IElementValidatorFactory
    {
        public String getType();
        
        public IElementValidator createValidator(Properties properties);
    }
    
    private static final class VocabularyValidatorFactory implements IElementValidatorFactory
    {
        private static final String TERMS_KEY = "terms";

        public String getType()
        {
            return "vocabulary";
        }
        
        public IElementValidator createValidator(Properties properties)
        {
            String termsSequence = PropertyUtils.getMandatoryProperty(properties, TERMS_KEY);
            final HashSet<String> set = new HashSet<String>();
            String[] terms = PropertyParametersUtil.parseItemisedProperty(termsSequence, TERMS_KEY);
            set.addAll(Arrays.asList(terms));
            return new IElementValidator()
                {
                    public String validate(String element)
                    {
                        if (set.contains(element))
                        {
                            return null;
                        }
                        return "Is not a term from the following vocabulary: " + set;
                    }
                };
        }
    }
    
    private static final class IntegerValidatorFactory implements IElementValidatorFactory
    {
        public String getType()
        {
            return "integer";
        }
        
        public IElementValidator createValidator(Properties properties)
        {
            return new IElementValidator()
                {
                    public String validate(String element)
                    {
                        try
                        {
                            Integer.parseInt(element);
                            return null;
                        } catch (NumberFormatException ex)
                        {
                            return "Is not an integer number";
                        }
                    }
                };
        }
    }
    
    private static final class StringValidatorFactory implements IElementValidatorFactory
    {
        private static final String PATTERN_KEY = "pattern";
        
        public String getType()
        {
            return "string";
        }

        public IElementValidator createValidator(Properties properties)
        {
            String regex = PropertyUtils.getMandatoryProperty(properties, PATTERN_KEY);
            final Pattern pattern;
            try
            {
                pattern = Pattern.compile(regex);
            } catch (PatternSyntaxException ex)
            {
                throw new ConfigurationFailureException("Invalid regular expression: " + regex);
            }
            return new IElementValidator()
                {
                
                    public String validate(String element)
                    {
                        if (pattern.matcher(element).matches())
                        {
                            return null;
                        }
                        return "Does not match the following regular expression: " + pattern;
                    }
                };
        }
        
    }
    
    private final List<IElementValidator> elementValidators;

    public DataColumnHeaderValidator(Properties properties)
    {
        Map<String, IElementValidatorFactory> factories = createValidatorFactories();
        SectionProperties[] sections = PropertyParametersUtil.extractSectionProperties(properties, ELEMENTS_KEY, false);
        elementValidators = new ArrayList<IElementValidator>(sections.length);
        for (SectionProperties sectionProperties : sections)
        {
            String key = sectionProperties.getKey();
            Properties validatorProperties = sectionProperties.getProperties();
            String type = validatorProperties.getProperty(TYPE_KEY);
            if (type == null)
            {
                throw new ConfigurationFailureException("Missing property '" + TYPE_KEY
                        + "' for element '" + key + "' of data column header validator.");
            }
            IElementValidatorFactory factory = factories.get(type);
            if (factory == null)
            {
                throw new ConfigurationFailureException("Unknown validator type '" + type
                        + "' for element '" + key + "' of data column header validator.");
            }
            try
            {
                elementValidators.add(factory.createValidator(validatorProperties));
            } catch (ConfigurationFailureException e)
            {
                throw new ConfigurationFailureException(
                        "Error in validator definition for element '" + key
                                + "' of data column header validator: " + e.getMessage());
            }
        }
    }
    
    private Map<String, IElementValidatorFactory> createValidatorFactories()
    {
        Map<String, IElementValidatorFactory> map = new HashMap<String, IElementValidatorFactory>();
        register(map, new VocabularyValidatorFactory());
        register(map, new IntegerValidatorFactory());
        register(map, new StringValidatorFactory());
        return map;
    }
    
    private void register(Map<String, IElementValidatorFactory> map, IElementValidatorFactory factory)
    {
        map.put(factory.getType(), factory);
    }
    
    public Result validateHeader(String header)
    {
        String[] elements = header.split(SEPARATOR);
        if (elements.length < elementValidators.size())
        {
            return Result.failure(elementValidators.size() + " elements separated by '" + SEPARATOR
                    + "' expected instead of only " + elements.length + ".");
        }
        for (int i = 0; i < elements.length; i++)
        {
            String element = elements[i];
            String result = elementValidators.get(i).validate(element);
            if (result != null)
            {
                return Result.failure("Element '" + element + "' is invalid: " + result);
            }
        }
        System.out.println(Arrays.asList(elements));
        return Result.OK;
    }

}
