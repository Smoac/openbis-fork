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

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleToRegister;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleToRegisterDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityDataType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityPropertyValue;

/**
 * @author Izabela Adamczyk
 */
public class SampleToRegisterTranslator
{

    public static SampleToRegisterDTO translate(SampleToRegister sample)
    {
        SampleToRegisterDTO result = new SampleToRegisterDTO();

        // Sample
        if (StringUtils.isBlank(sample.getSampleIdentifier()) == false)
            result.setSampleIdentifier(SampleIdentifierFactory.parse(sample.getSampleIdentifier()));
        else
            throw new UserFailureException("Sample code incorrect");

        // Parents
        if (StringUtils.isBlank(sample.getContainerParent()) == false)
            result.setContainerParent(SampleIdentifierFactory.parse(sample.getContainerParent()));
        if (StringUtils.isBlank(sample.getGeneratorParent()) == false)
            result.setGeneratorParent(SampleIdentifierFactory.parse(sample.getGeneratorParent()));

        // Properties
        List<SimpleEntityProperty> simpleProperties = new ArrayList<SimpleEntityProperty>();
        List<SampleProperty> properties = sample.getProperties();
        for (SampleProperty property : properties)
        {
            PropertyType propertyType = property.getEntityTypePropertyType().getPropertyType();
            EntityDataType dataType = DataTypeTranslator.translate(propertyType.getDataType());
            simpleProperties.add(EntityPropertyValue.createFromUntyped(property.getValue(),
                    dataType).createSimple(propertyType.getCode(), propertyType.getLabel()));
        }
        result.setProperties(simpleProperties.toArray(new SimpleEntityProperty[0]));

        // Type
        result.setSampleTypeCode(sample.getType());
        return result;
    }
}
