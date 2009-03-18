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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.common.collections.UnmodifiableSetDecorator;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.BeanUtils.Converter;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IMatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
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

    /**
     * Returns the {@link SamplePE} converter.
     */
    public final static BeanUtils.Converter getSampleConverter()
    {
        return SampleConverter.INSTANCE;
    }

    /**
     * Returns the {@link IMatchingEntity} converter.
     */
    public final static BeanUtils.Converter getMatchingEntityConverter()
    {
        return MatchingEntityConverter.INSTANCE;
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
     * A {@link BeanUtils.Converter} for converting {@link SamplePE} into {@link Sample}.
     * 
     * @author Christian Ribeaud
     */
    private final static class SampleConverter implements BeanUtils.Converter
    {
        static final SampleConverter INSTANCE = new SampleConverter();

        private final EntityPropertiesConverter<SamplePropertyPE, SampleType, SampleTypePropertyType, SampleProperty> propertiesConverter;

        private SampleConverter()
        {
            propertiesConverter =
                    new EntityPropertiesConverter<SamplePropertyPE, SampleType, SampleTypePropertyType, SampleProperty>()
                        {

                            //
                            // EntityPropertiesConverter
                            //

                            @Override
                            protected final SampleProperty convert(final SamplePropertyPE property)
                            {
                                return SamplePropertyTranslator.translate(property);
                            }
                        };
        }

        private final Sample convertToSample(final SamplePE sample)
        {
            if (HibernateUtils.isInitialized(sample) == false)
            {
                return null;
            }
            return BeanUtils.createBean(Sample.class, sample, this);
        }

        //
        // BeanUtils.Converter
        //

        public final String convertToSampleIdentifier(final SamplePE samplePE)
        {
            return StringEscapeUtils.escapeHtml(samplePE.getSampleIdentifier().toString());
        }

        public final Sample convertToGeneratedFrom(final SamplePE samplePE)
        {
            return convertToSample(samplePE.getGeneratedFrom());
        }

        public final Sample convertToContainer(final SamplePE samplePE)
        {
            return convertToSample(samplePE.getContainer());
        }

        public final List<SampleProperty> convertToProperties(
                final IEntityPropertiesHolder<SamplePropertyPE> entity)
        {
            return propertiesConverter.convertToProperties(entity);
        }

        public final List<SampleTypePropertyType> convertToSampleTypePropertyTypes(
                final SampleTypePE sampleTypePE)
        {
            return SampleTypeConverter.INSTANCE.convertToSampleTypePropertyTypes(sampleTypePE);
        }

    }

    /**
     * A {@link BeanUtils.Converter} for converting {@link EntityPropertyPE} into
     * {@link EntityProperty}.
     * 
     * @author Christian Ribeaud
     */
    private final static class SampleTypeConverter implements BeanUtils.Converter
    {

        static final SampleTypeConverter INSTANCE = new SampleTypeConverter();

        private SampleTypeConverter()
        {
        }

        //
        // BeanUtils.Converter
        //

        public final List<SampleTypePropertyType> convertToSampleTypePropertyTypes(
                final SampleTypePE sampleTypePE)
        {
            final Set<SampleTypePropertyTypePE> sampleTypePropertyTypes =
                    sampleTypePE.getSampleTypePropertyTypes();
            if (HibernateUtils.isInitialized(sampleTypePropertyTypes) == false)
            {
                return createUnmodifiableEmptyList();
            }
            return BeanUtils.createBeanList(SampleTypePropertyType.class, sampleTypePropertyTypes);
        }

    }

    /**
     * A {@link BeanUtils.Converter} for converting {@link EntityPropertyPE} into
     * {@link EntityProperty}.
     * 
     * @author Christian Ribeaud
     */
    private static abstract class EntityPropertiesConverter<T extends EntityPropertyPE, R extends EntityType, S extends EntityTypePropertyType<R>, P extends EntityProperty<R, S>>
            implements BeanUtils.Converter
    {

        EntityPropertiesConverter()
        {
        }

        /**
         * Converts given {@link EntityPropertyPE} into a {@link EntityProperty}.
         * <p>
         * Must be implemented by subclasses.
         * </p>
         */
        protected abstract P convert(final T property);

        //
        // Converter
        //

        public final List<P> convertToProperties(final IEntityPropertiesHolder<T> entity)
        {
            Set<T> properties = entity.getProperties();
            if (properties instanceof UnmodifiableSetDecorator)
            {
                properties = ((UnmodifiableSetDecorator<T>) properties).getDecorated();
            }
            if (HibernateUtils.isInitialized(properties) == false)
            {
                return createUnmodifiableEmptyList();
            }
            final List<P> entityProperties = new ArrayList<P>();
            for (final T property : properties)
            {
                entityProperties.add(convert(property));
            }
            return entityProperties;
        }
    }

    /**
     * A {@link BeanUtils.Converter} for converting {@link IMatchingEntity} into
     * {@link MatchingEntity}.
     * 
     * @author Christian Ribeaud
     */
    private final static class MatchingEntityConverter implements BeanUtils.Converter
    {

        static final MatchingEntityConverter INSTANCE = new MatchingEntityConverter();

        private MatchingEntityConverter()
        {
        }

        //
        // BeanUtils.Converter
        //

        public final EntityKind convertToEntityKind(final SearchHit matchingEntity)
        {
            return EntityKind.valueOf(matchingEntity.getEntityKind().name());
        }

        public final String convertToFieldDescription(final SearchHit matchingEntity)
        {
            return StringEscapeUtils.escapeHtml(matchingEntity.getFieldDescription());
        }

        public final String convertToTextFragment(final SearchHit matchingEntity)
        {
            return StringEscapeUtils.escapeHtml(matchingEntity.getTextFragment());
        }

    }

    /**
     * A {@link BeanUtils.Converter} for converting {@link DataTypePE} into {@link DataType}.
     * 
     * @author Christian Ribeaud
     */
    private final static class DataTypeConverter implements BeanUtils.Converter
    {
        static final DataTypeConverter INSTANCE = new DataTypeConverter();

        private DataTypeConverter()
        {
        }

        //
        // BeanUtils.Converter
        //

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
    private final static class VocabularyConverter implements BeanUtils.Converter
    {
        static final VocabularyConverter INSTANCE = new VocabularyConverter();

        private VocabularyConverter()
        {
        }

        //
        // BeanUtils.Converter
        //

        public final String convertToDescription(final VocabularyPE vocabulary)
        {
            return StringEscapeUtils.escapeHtml(vocabulary.getDescription());
        }

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
