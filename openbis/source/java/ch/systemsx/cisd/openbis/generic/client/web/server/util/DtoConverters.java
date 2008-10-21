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
import java.util.Collections;
import java.util.List;

import org.hibernate.Hibernate;

import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;

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
                                final SampleTypePE entityType =
                                        (SampleTypePE) property.getEntityTypePropertyType()
                                                .getEntityType();
                                return SamplePropertyTranslator.translate(property,
                                        SampleTypeTranslator.translate(entityType));
                            }
                        };
        }

        //
        // BeanUtils.Converter
        //

        public final List<SampleProperty> convertToProperties(
                final IEntityPropertiesHolder<SamplePropertyPE> entity)
        {
            return propertiesConverter.convertToProperties(entity);
        }
    }

    private static abstract class EntityPropertiesConverter<T extends EntityPropertyPE, R extends EntityType, S extends EntityTypePropertyType<R>, P extends EntityProperty<R, S>>
            implements BeanUtils.Converter
    {

        private EntityPropertiesConverter()
        {
        }

        protected abstract P convert(final T property);

        //
        // Converter
        //

        public final List<P> convertToProperties(final IEntityPropertiesHolder<T> entity)
        {
            final List<T> properties = entity.getProperties();
            if (Hibernate.isInitialized(properties) == false)
            {
                return Collections.emptyList();
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
