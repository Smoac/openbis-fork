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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityPropertyUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermValueEntityProperty;

/**
 * {@link ContentPanel} containing dataset properties.
 * 
 * @author Piotr Buczek
 */
public class DataSetPropertiesPanel extends ContentPanel
{
    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "dataset-properties-section_";

    private final ExternalData dataset;

    private final IViewContext<?> viewContext;

    public DataSetPropertiesPanel(final ExternalData dataset, final IViewContext<?> viewContext)
    {
        setHeading("Data Set Properties");
        this.dataset = dataset;
        this.viewContext = viewContext;
        final PropertyGrid propertyGrid = createPropertyGrid();
        add(propertyGrid);
    }

    private final PropertyGrid createPropertyGrid()
    {
        final Map<String, Object> properties = createProperties(viewContext);
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + dataset.getIdentifier());
        propertyGrid.registerPropertyValueRenderer(Person.class, PropertyValueRenderers
                .createPersonPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(DataSetType.class, PropertyValueRenderers
                .createDataSetTypePropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(Invalidation.class, PropertyValueRenderers
                .createInvalidationPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(Project.class, PropertyValueRenderers
                .createProjectPropertyValueRenderer(viewContext));
        final IPropertyValueRenderer<IEntityProperty> propertyRenderer =
                PropertyValueRenderers.createEntityPropertyPropertyValueRenderer(viewContext);
        propertyGrid.registerPropertyValueRenderer(EntityProperty.class, propertyRenderer);
        propertyGrid.registerPropertyValueRenderer(GenericValueEntityProperty.class,
                propertyRenderer);
        propertyGrid.registerPropertyValueRenderer(VocabularyTermValueEntityProperty.class,
                propertyRenderer);
        propertyGrid.registerPropertyValueRenderer(MaterialValueEntityProperty.class,
                propertyRenderer);
        propertyGrid.registerPropertyValueRenderer(Sample.class, PropertyValueRenderers
                .createSamplePropertyValueRenderer(viewContext, true));
        propertyGrid.registerPropertyValueRenderer(Experiment.class, PropertyValueRenderers
                .createExperimentPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(ExternalData.class, PropertyValueRenderers
                .createExternalDataPropertyValueRenderer(viewContext));
        propertyGrid.registerPropertyValueRenderer(DataStore.class, PropertyValueRenderers
                .createDataStorePropertyValueRenderer(viewContext));
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    private final Map<String, Object> createProperties(final IMessageProvider messageProvider)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final DataSetType datasetType = dataset.getDataSetType();
        final Invalidation invalidation = dataset.getInvalidation();
        final Sample sample = dataset.getSample();

        properties.put(messageProvider.getMessage(Dict.DATA_SET), new ExternalHyperlink(dataset
                .getPermId(), dataset.getPermlink()));
        properties.put(messageProvider.getMessage(Dict.DATA_SET_TYPE), datasetType);

        properties.put(messageProvider.getMessage(Dict.SOURCE_TYPE), dataset.getSourceType());
        properties.put(messageProvider.getMessage(Dict.LOCATION), dataset.getLocation());
        if (viewContext.getModel().getApplicationInfo().isArchivingConfigured())
        {
            properties.put(messageProvider.getMessage(Dict.ARCHIVING_STATUS), dataset.getStatus()
                    .getDescription());
        }
        properties.put(messageProvider.getMessage(Dict.DATA_STORE), dataset.getDataStore());
        properties.put(messageProvider.getMessage(Dict.IS_COMPLETE), dataset.getComplete());
        properties.put(messageProvider.getMessage(Dict.FILE_FORMAT_TYPE), dataset
                .getFileFormatType().getCode());
        properties.put(messageProvider.getMessage(Dict.DATA_PRODUCER_CODE), dataset
                .getDataProducerCode());
        properties.put(messageProvider.getMessage(Dict.PRODUCTION_DATE), dataset
                .getProductionDate());

        properties.put(messageProvider.getMessage(Dict.REGISTRATOR), dataset.getRegistrator());
        properties.put(messageProvider.getMessage(Dict.REGISTRATION_DATE), dataset
                .getRegistrationDate());
        properties.put(messageProvider.getMessage(Dict.PROJECT), dataset.getExperiment()
                .getProject());
        properties.put(messageProvider.getMessage(Dict.EXPERIMENT), dataset.getExperiment());
        if (sample != null)
        {
            properties.put(messageProvider.getMessage(Dict.SAMPLE), sample);
        }
        if (invalidation != null)
        {
            properties.put(messageProvider.getMessage(Dict.INVALIDATION), invalidation);
        }

        final List<IEntityProperty> datasetProperties = dataset.getProperties();
        Collections.sort(datasetProperties);
        List<PropertyType> types = EntityPropertyUtils.extractTypes(datasetProperties);
        for (final IEntityProperty property : datasetProperties)
        {
            final String label =
                    PropertyTypeRenderer.getDisplayName(property.getPropertyType(), types);
            properties.put(label, property);
        }
        return properties;
    }
}
