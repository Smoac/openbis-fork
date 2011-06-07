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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Izabela Adamczyk
 */
public class EntityHelper
{
    /**
     * Creates {@link EntityType} appropriate for given {@link EntityKind}.
     */
    public static EntityType createEntityType(EntityKind kind, String code)
    {
        EntityType type = null;
        switch (kind)
        {
            case DATA_SET:
                type = new DataSetType();
                break;
            case EXPERIMENT:
                type = new ExperimentType();
                break;
            case MATERIAL:
                type = new MaterialType();
                break;
            case SAMPLE:
                type = new SampleType();
                break;
        }
        assert type != null;
        type.setCode(code);
        return type;
    }

    /**
     * @return finds and returns an {@link IEntityProperty} for a specified code. Returns
     *         <code>null</code> if no matching property is found.
     */
    public static IEntityProperty tryFindProperty(Iterable<IEntityProperty> properties,
            final String propertyCode)
    {
        for (final IEntityProperty property : properties)
        {
            final PropertyType propertyType = property.getPropertyType();
            if (propertyType.getCode().equalsIgnoreCase(propertyCode))
            {
                return property;
            }
        }
        return null;
    }

    /**
     * does the same as {@link #tryFindProperty(Iterable, String)} but with arrays.
     */
    public static IEntityProperty tryFindProperty(IEntityProperty[] properties,
            final String propertyCode)
    {
        for (final IEntityProperty property : properties)
        {
            final PropertyType propertyType = property.getPropertyType();
            if (propertyType.getCode().equalsIgnoreCase(propertyCode))
            {
                return property;
            }
        }
        return null;
    }

    public static NewProperty tryFindProperty(List<NewProperty> properties, String propertyCode)
    {
        for (final NewProperty property : properties)
        {
            if (property.getPropertyCode().equalsIgnoreCase(propertyCode))
            {
                return property;
            }
        }
        return null;
    }

    public static String tryFindPropertyValue(IEntityPropertiesHolder holder, String propertyCode)
    {
        IEntityProperty property = null;

        if (holder.getProperties() != null)
        {
            property = EntityHelper.tryFindProperty(holder.getProperties(), propertyCode);
        }

        return (property != null) ? property.tryGetOriginalValue() : null;
    }

    public static void updateSampleProperties(ICommonServer server, String sessionToken,
            Sample sample, Map<String, String> properties)
    {
        List<IEntityProperty> props = translatePropertiesMapToList(properties);
        Experiment experiment = sample.getExperiment();
        ExperimentIdentifier experimentIdentifier =
                experiment == null ? null : ExperimentIdentifierFactory.parse(experiment
                        .getIdentifier());
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(sample.getIdentifier());
        Sample container = sample.getContainer();
        String containerIdentifier = container == null ? null : container.getIdentifier();
        SampleUpdatesDTO updates =
                new SampleUpdatesDTO(new TechId(sample), props, experimentIdentifier,
                        Collections.<NewAttachment> emptySet(), sample.getModificationDate(),
                        sampleIdentifier, containerIdentifier, null);
        server.updateSample(sessionToken, updates);
    }

    public static void updateSampleProperties(ICommonServer server, String sessionToken, TechId id,
            Map<String, String> properties)
    {
        Sample sample = server.getSampleInfo(sessionToken, id).getParent();
        updateSampleProperties(server, sessionToken, sample, properties);
    }

    public static List<IEntityProperty> translatePropertiesMapToList(Map<String, String> properties)
    {
        List<IEntityProperty> props = new ArrayList<IEntityProperty>();
        for (Entry<String, String> entry : properties.entrySet())
        {
            props.add(createNewProperty(entry.getKey(), entry.getValue()));
        }
        return props;
    }

    /**
     * Creates a property with specified code and value. An already existing property with same code
     * will be removed.
     */
    public static void createOrUpdateProperty(IEntityPropertiesHolder holder, String propertyCode,
            String propertyValue)
    {
        IEntityProperty newProperty = createNewProperty(propertyCode, propertyValue);
        List<IEntityProperty> properties = holder.getProperties();
        for (int i = 0; i < properties.size(); i++)
        {
            IEntityProperty property = properties.get(i);
            PropertyType propertyType = property.getPropertyType();
            if (propertyType.getCode().equalsIgnoreCase(propertyCode))
            {
                properties.set(i, newProperty);
                return;
            }
        }
        properties.add(newProperty);
    }

    public static IEntityProperty createNewProperty(String propertyCode, String propertyValue)
    {
        IEntityProperty property = createNewProperty(propertyCode);
        property.setValue(propertyValue);
        return property;
    }

    private static IEntityProperty createNewProperty(String propertyCode)
    {
        IEntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyCode);
        property.setPropertyType(propertyType);
        return property;
    }
}
