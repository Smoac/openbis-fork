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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.EnumMap;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.DateFormatThreadLocal;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;

/**
 * The default {@link IPropertyValueValidator} implementation.
 * 
 * @author Christian Ribeaud
 */
// TODO 2008-12-03, Christian Ribeaud: Write a test for this class.
public final class PropertyValidator implements IPropertyValueValidator
{
    public static final ThreadLocal<SimpleDateFormat> CANONICAL_DATE_FORMAT =
            new DateFormatThreadLocal("yyyy-MM-dd HH:mm:ss Z");

    private final static Map<EntityDataType, IDataTypeValidator> dataTypeValidators = createMap();

    private final static Map<EntityDataType, IDataTypeValidator> createMap()
    {
        final Map<EntityDataType, IDataTypeValidator> map =
                new EnumMap<EntityDataType, IDataTypeValidator>(EntityDataType.class);
        map.put(EntityDataType.BOOLEAN, new BooleanValidator());
        map.put(EntityDataType.VARCHAR, new VarcharValidator());
        map.put(EntityDataType.TIMESTAMP, new TimestampValidator());
        map.put(EntityDataType.INTEGER, new IntegerValidator());
        map.put(EntityDataType.REAL, new RealValidator());
        map.put(EntityDataType.CONTROLLEDVOCABULARY, new ControlledVocabularyValidator());
        return map;
    }

    //
    // IPropertyValidator
    //

    public final void validatePropertyValue(final PropertyTypePE propertyType, final String value)
            throws UserFailureException
    {
        assert propertyType != null : "Unspecified property type.";
        assert value != null : "Unspecified value.";
        final EntityDataType entityDataType = propertyType.getType().getCode();
        final IDataTypeValidator dataTypeValidator = dataTypeValidators.get(entityDataType);
        assert dataTypeValidator != null : String.format("No IDataTypeValidator implementation "
                + "specified for '%s'.", entityDataType);
        if (entityDataType == EntityDataType.CONTROLLEDVOCABULARY)
        {
            ((ControlledVocabularyValidator) dataTypeValidator).setVocabulary(propertyType
                    .getVocabulary());
        }
        if (dataTypeValidator.isValid(value))
        {
            return;
        }
        throw UserFailureException.fromTemplate(
                "Value '%s' has improper format. It should be of type '%s'.", value, entityDataType
                        .getNiceRepresentation());
    }

    //
    // Helper classes
    //

    private static interface IDataTypeValidator
    {
        /**
         * Whether given <var>value</var> is valid.
         */
        public boolean isValid(final String value);
    }

    private final static class BooleanValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final boolean isValid(final String value)
        {
            assert value != null : "Unspecified value.";
            return PropertyUtils.Boolean.getBoolean(value) != null;
        }
    }

    private final static class VarcharValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final boolean isValid(final String value)
        {
            assert value != null : "Unspecified value.";
            return true;
        }
    }

    private final static class TimestampValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final boolean isValid(final String value)
        {
            assert value != null : "Unspecified value.";
            try
            {
                CANONICAL_DATE_FORMAT.get().parse(value);
                return true;
            } catch (final ParseException ex)
            {
                return false;
            }
        }
    }

    private final static class IntegerValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final boolean isValid(final String value)
        {
            assert value != null : "Unspecified value.";
            try
            {
                Integer.parseInt(value);
                return true;
            } catch (final NumberFormatException ex)
            {
                return false;
            }
        }
    }

    private final static class RealValidator implements IDataTypeValidator
    {

        //
        // IDataTypeValidator
        //

        public final boolean isValid(final String value)
        {
            assert value != null : "Unspecified value.";
            try
            {
                Double.parseDouble(value);
                return true;
            } catch (final NumberFormatException ex)
            {
                return false;
            }
        }
    }

    private final static class ControlledVocabularyValidator implements IDataTypeValidator
    {

        private VocabularyPE vocabulary;

        final void setVocabulary(final VocabularyPE vocabulary)
        {
            this.vocabulary = vocabulary;
        }

        //
        // IDataTypeValidator
        //

        public final boolean isValid(final String value)
        {
            assert value != null : "Unspecified value.";
            assert vocabulary != null : "Unspecified vocabulary.";
            for (final VocabularyTermPE term : vocabulary.getTerms())
            {
                if (term.getCode().equals(value))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
