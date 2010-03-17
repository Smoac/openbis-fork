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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityPropertyUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermValueEntityProperty;

/**
 * {@link SingleSectionPanel} containing material properties.
 * 
 * @author Piotr Buczek
 */
public class MaterialPropertiesSection extends SingleSectionPanel
{
    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "material-properties-section_";

    private final Material material;

    public MaterialPropertiesSection(final Material material, final IViewContext<?> viewContext)
    {
        super("Material Properties", viewContext);
        this.material = material;
    }

    @Override
    protected void showContent()
    {
        final Map<String, Object> properties = createProperties(viewContext);
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + material.getIdentifier());
        propertyGrid.registerPropertyValueRenderer(Person.class, PropertyValueRenderers
                .createPersonPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(MaterialType.class, PropertyValueRenderers
                .createMaterialTypePropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(Invalidation.class, PropertyValueRenderers
                .createInvalidationPropertyValueRenderer(viewContext));
        final IPropertyValueRenderer<IEntityProperty> propertyRenderer =
                PropertyValueRenderers.createEntityPropertyPropertyValueRenderer(viewContext);
        propertyGrid.registerPropertyValueRenderer(EntityProperty.class, propertyRenderer);
        propertyGrid.registerPropertyValueRenderer(GenericValueEntityProperty.class,
                propertyRenderer);
        propertyGrid.registerPropertyValueRenderer(VocabularyTermValueEntityProperty.class,
                propertyRenderer);
        propertyGrid.registerPropertyValueRenderer(MaterialValueEntityProperty.class,
                propertyRenderer);
        propertyGrid.setProperties(properties);
        add(propertyGrid);
    }

    private final Map<String, Object> createProperties(final IMessageProvider messageProvider)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final MaterialType materialType = material.getMaterialType();

        properties.put(messageProvider.getMessage(Dict.MATERIAL), material.getCode());
        properties.put(messageProvider.getMessage(Dict.MATERIAL_TYPE), materialType);
        properties.put(messageProvider.getMessage(Dict.REGISTRATOR), material.getRegistrator());
        properties.put(messageProvider.getMessage(Dict.REGISTRATION_DATE), material
                .getRegistrationDate());

        final List<IEntityProperty> materialProperties = material.getProperties();
        Collections.sort(materialProperties);
        List<PropertyType> types = EntityPropertyUtils.extractTypes(materialProperties);
        for (final IEntityProperty property : materialProperties)
        {
            final String label =
                    PropertyTypeRenderer.getDisplayName(property.getPropertyType(), types);
            properties.put(label, property);
        }
        return properties;
    }

}
