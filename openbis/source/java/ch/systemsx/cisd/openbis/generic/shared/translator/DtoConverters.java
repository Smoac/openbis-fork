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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.BeanUtils.Converter;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A static class which converts <i>business</i> <i>DTOs</i> into <i>GWT</i> ones.
 * 
 * @author Christian Ribeaud
 */
public class DtoConverters
{

    private DtoConverters()
    {
        // Can not be instantiated.
    }

    public static final ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind convertEntityKind(
            final EntityKind entityKind)
    {
        return ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.valueOf(entityKind
                .name());
    }

    public static final EntityKind convertEntityKind(
            final ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind entityKind)
    {
        return EntityKind.valueOf(entityKind.name());
    }

    /**
     * Returns the {@link DataTypePE} converter.
     */
    public final static Converter getDataTypeConverter()
    {
        return DataTypeConverter.INSTANCE;
    }

    /**
     * Returns the {@link VocabularyPE} converter.
     */
    public final static Converter getVocabularyConverter()
    {
        return VocabularyConverter.INSTANCE;
    }

    /**
     * Does not really create an unmodifiable empty list but this works with <i>GWT</i>.
     */
    static final <T> List<T> createUnmodifiableEmptyList()
    {
        return new ArrayList<T>();
    }

    //
    // Helper classes
    //
    /**
     * A {@link BeanUtils.Converter} for converting {@link DataTypePE} into {@link DataType}.
     * 
     * @author Christian Ribeaud
     */
    // Note: the convertToXXX() methods will be used by reflection
    private final static class DataTypeConverter implements BeanUtils.Converter
    {
        static final DataTypeConverter INSTANCE = new DataTypeConverter();

        private DataTypeConverter()
        {
        }

        //
        // BeanUtils.Converter
        //

        @SuppressWarnings("unused")
        public final DataTypeCode convertToCode(final DataTypePE dataType)
        {
            return DataTypeCode.valueOf(dataType.getCode().name());
        }
    }

    /**
     * A {@link BeanUtils.Converter} for converting {@link VocabularyPE} into {@link Vocabulary}.
     * 
     * @author Christian Ribeaud
     */
    // Note: the convertToXXX() methods will be used by reflection
    private final static class VocabularyConverter implements BeanUtils.Converter
    {
        static final VocabularyConverter INSTANCE = new VocabularyConverter();

        private VocabularyConverter()
        {
        }

        //
        // BeanUtils.Converter
        //

        @SuppressWarnings("unused")
        public final String convertToDescription(final VocabularyPE vocabulary)
        {
            return StringEscapeUtils.escapeHtml(vocabulary.getDescription());
        }

        @SuppressWarnings("unused")
        public final List<VocabularyTerm> convertToTerms(final VocabularyPE vocabulary)
        {
            final Set<VocabularyTermPE> terms = vocabulary.getTerms();
            if (HibernateUtils.isInitialized(terms))
            {
                return BeanUtils.createBeanList(VocabularyTerm.class, terms);
            } else
            {
                return createUnmodifiableEmptyList();
            }
        }
    }
}
