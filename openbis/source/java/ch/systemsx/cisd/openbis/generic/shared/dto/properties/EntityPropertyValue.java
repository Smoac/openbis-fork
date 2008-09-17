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

package ch.systemsx.cisd.openbis.generic.shared.dto.properties;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.common.utilities.DateFormatThreadLocal;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleEntityProperty;

/**
 * Storage of entity property value. Use casting methods with tryAs prefix to extract a value. Note
 * that if a value of a certain type is stored (e.g. integer), than using a casting method different
 * than tryAsInteger is a programming error! Use {@code getType()} method to figure out the type of
 * value, which is stored.
 * 
 * @author Tomasz Pylak
 */
// TODO 2007-11-22, Christian Ribeaud: typed methods 'tryAsDouble', 'tryAsInteger' and 'tryAsString'
// should be removed as they are only used inside the property type framework.
public class EntityPropertyValue extends AbstractHashable
{
    private static final String TRUE_VALUE = "true";

    private static final String FALSE_VALUE = "false";

    public static final ThreadLocal<SimpleDateFormat> CANONICAL_DATE_FORMAT =
            new DateFormatThreadLocal("yyyy-MM-dd HH:mm:ss Z");

    private final EntityDataType type;

    private final Object valueOrNull;

    private EntityPropertyValue(final Object valueOrNull, final EntityDataType type)
    {
        this.valueOrNull = valueOrNull;
        this.type = type;
    }

    /** @return the type of value, which is stored */
    public EntityDataType getType()
    {
        return type;
    }

    /** @return true if the stored value is null (type independent) */
    public boolean isNull()
    {
        return valueOrNull == null;
    }

    /** throws exception when value cannot be properly casted */
    public static EntityPropertyValue createFromUntyped(final String untypedValueOrNull,
            final EntityDataType type) throws UserFailureException
    {
        if (untypedValueOrNull == null)
        {
            return new EntityPropertyValue(null, type);
        }
        try
        {
            return internalCreateFromUntyped(untypedValueOrNull, type);
        } catch (final NumberFormatException e)
        {
            throw UserFailureException.fromTemplate(
                    "Value '%s' has improper format. It should be of type '%s'",
                    untypedValueOrNull, type.getNiceRepresentation());
        }
    }

    private static EntityPropertyValue internalCreateFromUntyped(final String untypedValue,
            final EntityDataType type)
    {
        if (type.equals(EntityDataType.INTEGER))
        {
            return createInteger(Integer.parseInt(untypedValue));
        } else if (type.equals(EntityDataType.REAL))
        {
            return createDouble(Double.parseDouble(untypedValue));
        } else if (type.equals(EntityDataType.VARCHAR))
        {
            return createString(untypedValue);
        } else if (type.equals(EntityDataType.BOOLEAN))
        {
            return createBoolean(parseBoolean(untypedValue));
        } else if (type.equals(EntityDataType.TIMESTAMP))
        {
            return createDate(parseDate(untypedValue));
        } else if (type.equals(EntityDataType.CONTROLLEDVOCABULARY))
        {
            return createVocabularyTerm(untypedValue);
        } else
        {
            throw InternalErr.error();
        }
    }

    private static Date parseDate(final String untypedValue)
    {
        try
        {
            return CANONICAL_DATE_FORMAT.get().parse(untypedValue);
        } catch (final ParseException ex)
        {
            throw UserFailureException.fromTemplate(
                    "Date value '%s' has improper format. The proper format is '%s'.",
                    untypedValue, CANONICAL_DATE_FORMAT.get().toPattern());
        }
    }

    private static Boolean parseBoolean(final String untypedValue)
    {
        if (untypedValue.equalsIgnoreCase(TRUE_VALUE))
        {
            return Boolean.TRUE;
        } else if (untypedValue.equalsIgnoreCase(FALSE_VALUE))
        {
            return Boolean.FALSE;
        } else
        {
            throw UserFailureException.fromTemplate(
                    "Boolean value '%s' has improper format. It should be either '%s' or '%s',",
                    untypedValue, TRUE_VALUE, FALSE_VALUE);
        }
    }

    public static EntityPropertyValue createInteger(final Integer valOrNull)
    {
        return new EntityPropertyValue(valOrNull, EntityDataType.INTEGER);
    }

    public static EntityPropertyValue createDouble(final Double valOrNull)
    {
        return new EntityPropertyValue(valOrNull, EntityDataType.REAL);
    }

    public static EntityPropertyValue createString(final String valOrNull)
    {
        return new EntityPropertyValue(valOrNull, EntityDataType.VARCHAR);
    }

    public static EntityPropertyValue createVocabularyTerm(final String valOrNull)
    {
        return new EntityPropertyValue(valOrNull, EntityDataType.CONTROLLEDVOCABULARY);
    }

    public static EntityPropertyValue createBoolean(final Boolean valOrNull)
    {
        return new EntityPropertyValue(valOrNull, EntityDataType.BOOLEAN);
    }

    public static EntityPropertyValue createDate(final Date valOrNull)
    {
        return new EntityPropertyValue(valOrNull, EntityDataType.TIMESTAMP);
    }

    // --- casting methods, fail if type does not match

    public String tryAsString()
    {
        ensure(type.equals(EntityDataType.VARCHAR));
        return (String) valueOrNull;
    }

    public Integer tryAsInteger()
    {
        ensure(type.equals(EntityDataType.INTEGER));
        return (Integer) valueOrNull;
    }

    public Double tryAsDouble()
    {
        ensure(type.equals(EntityDataType.REAL));
        return (Double) valueOrNull;
    }

    public Boolean tryAsBoolean()
    {
        ensure(type.equals(EntityDataType.BOOLEAN));
        return (Boolean) valueOrNull;
    }

    public Date tryAsDate()
    {
        ensure(type.equals(EntityDataType.TIMESTAMP));
        return (Date) valueOrNull;
    }

    /** returns canonical representation of the value */
    public String tryGetUntypedValue()
    {
        if (valueOrNull == null)
        {
            return null;
        }
        if (type.equals(EntityDataType.INTEGER) || type.equals(EntityDataType.REAL)
                || type.equals(EntityDataType.VARCHAR)
                || type.equals(EntityDataType.CONTROLLEDVOCABULARY))
        {
            return valueOrNull.toString();
        } else if (type.equals(EntityDataType.BOOLEAN))
        {
            return tryAsBoolean() ? TRUE_VALUE : FALSE_VALUE;
        } else if (type.equals(EntityDataType.TIMESTAMP))
        {
            return CANONICAL_DATE_FORMAT.get().format(tryAsDate());
        } else
        {
            throw InternalErr.error();
        }
    }

    private void ensure(final boolean value)
    {
        assert value : "Type does not match";
    }

    public SimpleEntityProperty createSimple(final String code, final String label)
    {
        return new SimpleEntityProperty(code, label, type, (Serializable) this.valueOrNull);
    }

    public static EntityPropertyValue createFromSimple(SimpleEntityProperty property)
    {
        return new EntityPropertyValue(property.getValue(), property.getDataType());
    }
}
