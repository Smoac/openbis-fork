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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * {@link SectionPanel} containing experiment properties.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentPropertiesSection extends SectionPanel
{
    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "experiment-properties-section_";

    private final Experiment experiment;

    private final IViewContext<?> viewContext;

    public ExperimentPropertiesSection(final Experiment experiment,
            final IViewContext<?> viewContext)
    {
        super("Experiment Properties");
        this.experiment = experiment;
        this.viewContext = viewContext;
        final PropertyGrid propertyGrid = createPropertyGrid();
        add(propertyGrid);
    }

    private final PropertyGrid createPropertyGrid()
    {
        final Map<String, Object> properties = createProperties(viewContext);
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + experiment.getIdentifier());
        propertyGrid.registerPropertyValueRenderer(Person.class, PropertyValueRenderers
                .createPersonPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(ExperimentType.class, PropertyValueRenderers
                .createExperimentTypePropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(Invalidation.class, PropertyValueRenderers
                .createInvalidationPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(ExperimentProperty.class, PropertyValueRenderers
                .createExperimentPropertyPropertyValueRenderer(viewContext));
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    private final Map<String, Object> createProperties(final IMessageProvider messageProvider)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final ExperimentType experimentType = experiment.getExperimentType();
        final Invalidation invalidation = experiment.getInvalidation();
        properties.put(messageProvider.getMessage(Dict.EXPERIMENT), experiment.getCode());
        properties.put(messageProvider.getMessage(Dict.EXPERIMENT_TYPE), experimentType);
        properties.put(messageProvider.getMessage(Dict.REGISTRATOR), experiment.getRegistrator());
        properties.put(messageProvider.getMessage(Dict.REGISTRATION_DATE), experiment
                .getRegistrationDate());
        if (invalidation != null)
        {
            properties.put(messageProvider.getMessage(Dict.INVALIDATION), invalidation);
        }
        for (final ExperimentProperty property : experiment.getProperties())
        {
            final String simpleCode =
                    property.getEntityTypePropertyType().getPropertyType().getLabel();
            properties.put(simpleCode, property);
        }
        return properties;
    }
}