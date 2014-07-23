/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.EntityPropertyTranslator;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author pkupczyk
 */
public class PropertyTranslator extends AbstractCachingTranslator<IEntityPropertiesHolder, Map<String, String>, PropertyFetchOptions>
{

    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    public PropertyTranslator(TranslationContext translationContext, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            PropertyFetchOptions fetchOptions)
    {
        super(translationContext, fetchOptions);
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    @Override
    protected Map<String, String> createObject(IEntityPropertiesHolder entity)
    {
        if (false == entity.isPropertiesInitialized())
        {
            HibernateUtils.initialize(entity.getProperties());
        }

        HashMap<String, String> properties = new HashMap<String, String>();

        List<IEntityProperty> propertiesPE = EntityPropertyTranslator
                .translate(entity.getProperties(),
                        new HashMap<PropertyTypePE, PropertyType>(),
                        managedPropertyEvaluatorFactory);

        for (IEntityProperty iEntityProperty : propertiesPE)
        {
            properties.put(iEntityProperty.getPropertyType().getCode(),
                    iEntityProperty.tryGetAsString());
        }

        return properties;
    }

    @Override
    protected void updateObject(IEntityPropertiesHolder input, Map<String, String> output)
    {

    }

}
