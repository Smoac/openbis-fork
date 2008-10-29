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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;

import ch.systemsx.cisd.common.collections.UnmodifiableListDecorator;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;

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

    /**
     * Returns the {@link SamplePE} converter.
     */
    public final static BeanUtils.Converter getSampleConverter()
    {
        return SampleConverter.INSTANCE;
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
            if (Hibernate.isInitialized(sample) == false)
            {
                return null;
            }
            return BeanUtils.createBean(Sample.class, sample, this);
        }

        //
        // BeanUtils.Converter
        //

        public final String convertToIdentifier(final SamplePE samplePE)
        {
            return samplePE.getSampleIdentifier().toString();
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
            final List<SampleTypePropertyTypePE> sampleTypePropertyTypes =
                    sampleTypePE.getSampleTypePropertyTypes();
            if (Hibernate.isInitialized(sampleTypePropertyTypes) == false)
            {
                return new ArrayList<SampleTypePropertyType>();
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
            List<T> properties = entity.getProperties();
            if (properties instanceof UnmodifiableListDecorator)
            {
                properties = ((UnmodifiableListDecorator<T>) properties).getDecorated();
            }
            if (Hibernate.isInitialized(properties) == false)
            {
                return new ArrayList<P>();
            }
            final List<P> entityProperties = new ArrayList<P>();
            for (final T property : properties)
            {
                entityProperties.add(convert(property));
            }
            return entityProperties;
        }
    }
}
