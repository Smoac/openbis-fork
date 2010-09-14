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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractDatabaseModificationObserverWithCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserverWithMainObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityPropertyUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> material viewer.
 * 
 * @author Piotr Buczek
 */
abstract public class GenericMaterialViewer extends AbstractViewer<Material> implements
        IDatabaseModificationObserver
{
    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "material-properties-section_";

    private static final String GENERIC_MATERIAL_VIEWER = "generic-material-viewer";

    private static final String PREFIX = GENERIC_MATERIAL_VIEWER + "_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private PropertyGrid propertyGrid;

    private String displayIdSuffix;

    private final IViewContext<?> viewContext;

    protected final TechId materialId;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext, final TechId materialId)
    {

        final GenericMaterialViewer viewer = new GenericMaterialViewer(viewContext, materialId)
            {
                @Override
                protected void loadMaterialInfo(TechId materialTechId,
                        AsyncCallback<Material> material)
                {
                    viewContext.getService().getMaterialInfo(materialTechId, material);
                }
            };
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    protected abstract void loadMaterialInfo(TechId materialTechId, AsyncCallback<Material> material);

    protected GenericMaterialViewer(final IViewContext<?> viewContext, final TechId materialId)
    {
        super(viewContext, createId(materialId));
        this.viewContext = viewContext;
        this.materialId = materialId;
        setLayout(new BorderLayout());
    }

    public static String createId(final TechId materialId)
    {
        return ID_PREFIX + materialId;
    }

    @Override
    public void updateOriginalData(Material newData)
    {
        super.updateOriginalData(newData);
    }

    private final Component createRightPanel(Material material)
    {
        displayIdSuffix = getDisplayIdSuffix(material.getMaterialType().getCode());

        final SectionsPanel container = new SectionsPanel(viewContext.getCommonViewContext());
        List<TabContent> additionalPanels = createAdditionalSectionPanels();
        for (TabContent panel : additionalPanels)
        {
            container.addPanel(panel);
        }
        container.layout();
        moduleSectionManager.initialize(container, displayIdSuffix, material);
        return container;
    }

    private static final String getDisplayIdSuffix(String suffix)
    {
        return GENERIC_MATERIAL_VIEWER + "-" + suffix;
    }

    protected void reloadAllData()
    {
        reloadMaterialData(new MaterialInfoCallback(viewContext, this));
    }

    /**
     * Load the {@link Material} information.
     */
    protected void reloadMaterialData(AbstractAsyncCallback<Material> material)
    {
        loadMaterialInfo(materialId, material);
    }

    /**
     * To be subclassed. Creates additional panels of the viewer in the right side section besides
     * components, datasets and attachments
     */
    protected List<TabContent> createAdditionalSectionPanels()
    {
        return new ArrayList<TabContent>();
    }

    private final Component createLeftPanel(final Material material)
    {
        final ContentPanel panel = new ContentPanel();
        panel.setScrollMode(Scroll.AUTOY);
        panel.setHeading(viewContext.getMessage(Dict.MATERIAL_PROPERTIES_HEADING));
        propertyGrid = createPropertyGrid(material, viewContext);
        panel.add(propertyGrid);
        return panel;
    }

    public PropertyGrid createPropertyGrid(final Material material)
    {
        return createPropertyGrid(material, viewContext);
    }

    private static final PropertyGrid createPropertyGrid(Material material,
            final IViewContext<?> viewContext)
    {
        final Map<String, Object> properties = createProperties(viewContext, material);
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
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
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + material.getIdentifier());
        return propertyGrid;
    }

    private static final Map<String, Object> createProperties(
            final IMessageProvider messageProvider, Material material)
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

    public final void updateProperties(final Material material)
    {
        final Map<String, Object> properties = createProperties(viewContext, material);
        propertyGrid.resizeRows(properties.size());
        propertyGrid.setProperties(properties);
    }

    private void configureLeftPanel()
    {
        // displayIdSuffix must be initialized first -- this happens in createRightPanel, so that
        // method must be called before this
        if (isLeftPanelInitiallyCollapsed(displayIdSuffix))
        {
            viewContext.log(displayIdSuffix + " Initially Collapsed");
            ((BorderLayout) getLayout()).collapse(com.extjs.gxt.ui.client.Style.LayoutRegion.WEST);
        }

        // Add the listeners after configuring the panel, so as not to cause confusion
        addLeftPanelCollapseExpandListeners(displayIdSuffix);
    }

    private static final class MaterialInfoCallback extends AbstractAsyncCallback<Material>
    {
        private final GenericMaterialViewer viewer;

        private MaterialInfoCallback(final IViewContext<?> viewContext,
                final GenericMaterialViewer viewer)
        {
            super(viewContext);
            this.viewer = viewer;
        }

        @Override
        protected final void process(final Material result)
        {
            viewer.updateOriginalData(result);
            viewer.removeAll();
            // Left panel
            final Component leftPanel = viewer.createLeftPanel(result);
            viewer.add(leftPanel, createLeftBorderLayoutData());
            // Right panel
            final Component rightPanel = viewer.createRightPanel(result);
            viewer.add(rightPanel, createRightBorderLayoutData());
            viewer.configureLeftPanel();
            viewer.layout();
        }

    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createDatabaseModificationObserver().getRelevantModifications();
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createDatabaseModificationObserver().update(observedModifications);
    }

    private IDatabaseModificationObserver createDatabaseModificationObserver()
    {
        return new CompositeDatabaseModificationObserverWithMainObserver(
                new PropertyGridDatabaseModificationObserver());
    }

    private class PropertyGridDatabaseModificationObserver extends
            AbstractDatabaseModificationObserverWithCallback
    {

        public DatabaseModificationKind[] getRelevantModifications()
        {
            return new DatabaseModificationKind[]
                {
                        DatabaseModificationKind.edit(ObjectKind.MATERIAL),
                        DatabaseModificationKind
                                .createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                        DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                        DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM),
                        DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM) };
        }

        public void update(Set<DatabaseModificationKind> observedModifications)
        {
            reloadMaterialData(new ReloadPropertyGridCallback(viewContext,
                    GenericMaterialViewer.this));
        }

        private final class ReloadPropertyGridCallback extends AbstractAsyncCallback<Material>
        {
            private final GenericMaterialViewer viewer;

            private ReloadPropertyGridCallback(final IViewContext<?> viewContext,
                    final GenericMaterialViewer viewer)
            {
                super(viewContext);
                this.viewer = viewer;
            }

            //
            // AbstractAsyncCallback
            //

            /** This method triggers reloading of the {@link PropertyGrid} data. */
            @Override
            protected final void process(final Material result)
            {
                viewer.updateOriginalData(result);
                viewer.updateProperties(result);
                executeSuccessfulUpdateCallback();
            }

            @Override
            public void finishOnFailure(Throwable caught)
            {
                viewer.setupRemovedEntityView();
            }
        }

    }

}
