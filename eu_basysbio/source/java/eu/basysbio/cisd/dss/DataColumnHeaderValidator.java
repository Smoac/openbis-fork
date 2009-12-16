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
 * Special {@link IColumnHeaderValidator} for Data Columns in time series data sets.
 *
 * @author Franz-Josef Elmer
 */
public class DataColumnHeaderValidator implements IColumnHeaderValidator
{
    static final String ELEMENTS_KEY = "elements";
    static final String TYPE_KEY = "type";
    static final String TYPE_VOCABULARY = "vocabulary";
    static final String TERMS_KEY = "terms";
    static final String TYPE_INTEGER = "integer";
    static final String TYPE_STRING = "string";
    static final String PATTERN_KEY = "pattern";
    
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


        public String getType()
        {
            return TYPE_VOCABULARY;
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
                        return "It is not a term from the following vocabulary: " + set;
                    }
                };
        }
    }
    
    private static final class IntegerValidatorFactory implements IElementValidatorFactory
    {

        public String getType()
        {
            return TYPE_INTEGER;
        }
        
        public IElementValidator createValidator(Properties properties)
        {
            return new IElementValidator()
                {
                    public String validate(String element)
                    {
                        try
                        {
                            Util.parseIntegerWithPlusSign(element);
                            return null;
                        } catch (NumberFormatException ex)
                        {
                            return "It is not an integer number.";
                        }
                    }
                };
        }
    }
    
    private static final class StringValidatorFactory implements IElementValidatorFactory
    {
        

        public String getType()
        {
            return TYPE_STRING;
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
                        return "It does not match the following regular expression: " + pattern;
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
        String[] elements = header.split(DataColumnHeader.SEPARATOR);
        if (elements.length < elementValidators.size())
        {
            return Result.failure(elementValidators.size() + " elements separated by '" + DataColumnHeader.SEPARATOR
                    + "' expected instead of only " + elements.length + ".");
        }
        for (int i = 0, n = Math.min(elements.length, elementValidators.size()); i < n; i++)
        {
            String element = elements[i];
            String result = elementValidators.get(i).validate(element);
            if (result != null)
            {
                return Result.failure("Element '" + element + "' is invalid: " + result);
            }
        }
        return Result.OK;
    }

}
