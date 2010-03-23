/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.IToStringConverter;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.PropertyUtils.Boolean;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.ValidationUtilities.HyperlinkValidationHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * The default {@link IPropertyValueValidator} implementation.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyValidator implements IPropertyValueValidator
{
    public enum SupportedDatePattern
    {
        DAYS_DATE_PATTERN("yyyy-MM-dd"),

        MINUTES_DATE_PATTERN("yyyy-MM-dd HH:mm"),

        SECONDS_DATE_PATTERN("yyyy-MM-dd HH:mm:ss"),

        US_DATE_PATTERN("M/d/yy"),

        US_DATE_TIME_PATTERN("M/d/yy h:mm a"),

        US_DATE_TIME_24_PATTERN("M/d/yy HH:mm"),

        CANONICAL_DATE_PATTERN(BasicConstant.CANONICAL_DATE_FORMAT_PATTERN),

        RENDERED_CANONICAL_DATE_PATTERN(BasicConstant.RENDERED_CANONICAL_DATE_FORMAT_PATTERN);

        private final String pattern;

        SupportedDatePattern(String pattern)
        {
            this.pattern = pattern;
        }

        public String getPattern()
        {
            return pattern;
        }
    }

    private final static String[] DATE_PATTERNS = createDatePatterns();

    private final static Map<DataTypeCode, IDataTypeValidator> dataTypeValidators =
            createDataTypeValidators();

    private final static Map<DataTypeCode, IDataTypeValidator> createDataTypeValidators()
    {
        final Map<DataTypeCode, IDataTypeValidator> map =
                new EnumMap<DataTypeCode, IDataTypeValidator>(DataTypeCode.class);
        map.put(DataTypeCode.BOOLEAN, new BooleanValidator());
        map.put(DataTypeCode.VARCHAR, new VarcharValidator());
        map.put(DataTypeCode.TIMESTAMP, new TimestampValidator());
        map.put(DataTypeCode.INTEGER, new IntegerValidator());
        map.put(DataTypeCode.REAL, new RealValidator());
        map.put(DataTypeCode.CONTROLLEDVOCABULARY, new ControlledVocabularyValidator());
        map.put(DataTypeCode.MATERIAL, new MaterialValidator());
        map.put(DataTypeCode.HYPERLINK, new HyperlinkValidator());
        map.put(DataTypeCode.MULTILINE_VARCHAR, new VarcharValidator());
        return map;
    }

    private final static String[] createDatePatterns()
    {
        final List<String> datePatterns = new ArrayList<String>();
        // Order does not matter due to DateUtils implementation used.
        for (SupportedDatePattern supportedPattern : SupportedDatePattern.values())
        {
            datePatterns.add(supportedPattern.getPattern());
        }
        return datePatterns.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public PropertyValidator(IDAOFactory daoFactory)
    {
        assert daoFactory != null : "Unspecified DAO factory.";

        final IDataTypeValidator dataTypeValidator =
                dataTypeValidators.get(DataTypeCode.CONTROLLEDVOCABULARY);
        ((ControlledVocabularyValidator) dataTypeValidator).setDaoFactory(daoFactory);
    }

    //
    // IPropertyValidator
    //

    public final String validatePropertyValue(final PropertyTypePE propertyType, final String value)
            throws UserFailureException
    {
        assert propertyType != null : "Unspecified property type.";
        assert value != null : "Unspecified value.";
        final DataTypeCode entityDataType = propertyType.getType().getCode();
        final IDataTypeValidator dataTypeValidator = dataTypeValidators.get(entityDataType);
        assert dataTypeValidator != null : String.format("No IDataTypeValidator implementation "
                + "specified for '%s'.", entityDataType);
        if (entityDataType == DataTypeCode.CONTROLLEDVOCABULARY)
        {
            ((ControlledVocabularyValidator) dataTypeValidator).setVocabulary(propertyType
                    .getVocabulary());
        }
        return dataTypeValidator.validate(value);
    }

    //
    // Helper classes
    //

    private static interface IDataTypeValidator
    {
        /**
         * Validates given <var>value</var> according to this data type.
         * 
         * @return the validated value. Note that it can differ from the given one.
         * @throws UserFailureException if given <var>value</var> is not valid.
         */
        public String validate(final String value) throws UserFailureException;
    }

    private final static class BooleanValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            final Boolean bool = PropertyUtils.Boolean.getBoolean(value);
            if (bool == null)
            {
                throw UserFailureException.fromTemplate("Boolean value '%s' has improper format. "
                        + "It should be either 'true' or 'false'.", value);
            }
            return java.lang.Boolean.toString(bool.toBoolean());
        }
    }

    private final static class VarcharValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            return value;
        }
    }

    public final static class TimestampValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            try
            {
                Date date = DateUtils.parseDate(value, DATE_PATTERNS);
                // we store date in CANONICAL_DATE_PATTERN
                return DateFormatUtils.format(date, SupportedDatePattern.CANONICAL_DATE_PATTERN
                        .getPattern());
            } catch (final ParseException ex)
            {
                throw UserFailureException.fromTemplate(
                        "Date value '%s' has improper format. It must be one of '%s'.", value,
                        Arrays.toString(DATE_PATTERNS));
            }
        }
    }

    private final static class IntegerValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            try
            {
                Integer.parseInt(value);
                return value;
            } catch (final NumberFormatException ex)
            {
                throw UserFailureException.fromTemplate("Integer value '%s' has improper format.",
                        value);
            }
        }
    }

    private final static class RealValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            try
            {
                Double.parseDouble(value);
                return value;
            } catch (final NumberFormatException ex)
            {
                throw UserFailureException.fromTemplate("Double value '%s' has improper format.",
                        value);
            }
        }
    }

    private final static class ControlledVocabularyValidator implements IDataTypeValidator
    {

        private VocabularyPE vocabulary;

        private IDAOFactory daoFactory;

        final void setVocabulary(final VocabularyPE vocabulary)
        {
            this.vocabulary = vocabulary;
        }

        public void setDaoFactory(final IDAOFactory daoFactory)
        {
            this.daoFactory = daoFactory;
        }

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            assert vocabulary != null : "Unspecified vocabulary.";

            final String upperCaseValue = value.toUpperCase();
            VocabularyTermPE termOrNull =
                    daoFactory.getVocabularyDAO().tryFindVocabularyTermByCode(vocabulary,
                            upperCaseValue);
            if (termOrNull != null)
            {
                return upperCaseValue;
            }
            throw UserFailureException.fromTemplate("Vocabulary value '%s' is not valid. "
                    + "It must exist in '%s' controlled vocabulary %s", upperCaseValue, vocabulary
                    .getCode(), getVocabularyDetails());
        }

        /**
         * @return Details about vocabulary dependent on {@link VocabularyPE#isChosenFromList()}
         *         value:
         *         <ul>
         *         <li>for <var>true</var> - returns a list of first few vocabulary terms from it.
         *         <li>for <var>false</var> - returns a vocabulary description
         *         </ul>
         */
        private final String getVocabularyDetails()
        {
            if (vocabulary.isChosenFromList())
            {
                return CollectionUtils.abbreviate(vocabulary.getTerms(), 10,
                        new IToStringConverter<VocabularyTermPE>()
                            {

                                //
                                // IToStringConverter
                                //

                                public final String toString(final VocabularyTermPE term)
                                {
                                    return term.getCode();
                                }
                            });
            } else
            {
                String descriptionOrNull = vocabulary.getDescription();
                return descriptionOrNull == null ? "" : " - " + descriptionOrNull;
            }
        }

    }

    private final static class MaterialValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";
            if (StringUtils.isBlank(value))
            {
                return null;
            }
            if (MaterialIdentifier.tryParseIdentifier(value) == null)
            {
                throw UserFailureException.fromTemplate(
                        "Material specification '%s' has improper format.", value);
            }
            return value;
        }
    }

    private final static class HyperlinkValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final String validate(final String value) throws UserFailureException
        {
            assert value != null : "Unspecified value.";

            // validate protocols and format
            if (HyperlinkValidationHelper.isProtocolValid(value) == false)
            {
                throw UserFailureException.fromTemplate(
                        "Hyperlink '%s' should start with one of the following protocols: '%s'",
                        value, HyperlinkValidationHelper.getValidProtocolsAsString());
            }
            if (HyperlinkValidationHelper.isFormatValid(value) == false)
            {
                throw UserFailureException.fromTemplate(
                        "Hyperlink value '%s' has improper format.", value);
            }

            // validated value is valid
            return value;
        }
    }
}
