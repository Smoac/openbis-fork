/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsCount;

/**
 * @author pkupczyk
 */
public class MetaprojectEntities extends Composite implements IDisposableComponent
{

    public static final String ID_SUFFIX = "_metaproject-entities";

    private IViewContext<?> viewContext;

    private Panel panel;

    private SectionsPanel sectionsPanel;

    private Map<EntityKind, DisposableTabContent> sectionsMap;

    private Long currentMetaprojectId;

    private EntityKind currentEntityKind;

    public MetaprojectEntities(IViewContext<?> viewContext, String idPrefix)
    {
        this.viewContext = viewContext;

        panel = new SimplePanel();
        panel.getElement().setId(idPrefix + ID_SUFFIX);

        initWidget(panel);
    }

    private void initSections(final Long metaprojectId, final IDelegatedAction callback)
    {
        currentMetaprojectId = metaprojectId;

        viewContext.getCommonService().getMetaprojectAssignmentsCount(metaprojectId,
                new AbstractAsyncCallback<MetaprojectAssignmentsCount>(viewContext)
                    {
                        @Override
                        protected void process(MetaprojectAssignmentsCount count)
                        {
                            panel.clear();

                            sectionsPanel =
                                    new SectionsPanel(viewContext.getCommonViewContext(),
                                            getElement().getId());
                            sectionsPanel.setDisplayID(
                                    DisplayTypeIDGenerator.GENERIC_METAPROJECT_VIEWER, null);
                            sectionsMap = new HashMap<EntityKind, DisposableTabContent>();

                            if (count.getExperimentCount() > 0)
                            {
                                addSection(EntityKind.EXPERIMENT,
                                        new MetaprojectExperimentsSection(viewContext, new TechId(
                                                metaprojectId)));
                            }
                            if (count.getSampleCount() > 0)
                            {
                                addSection(EntityKind.SAMPLE, new MetaprojectSamplesSection(
                                        viewContext, new TechId(metaprojectId)));
                            }
                            if (count.getDataSetCount() > 0)
                            {
                                addSection(EntityKind.DATA_SET, new MetaprojectDataSetsSection(
                                        viewContext, new TechId(metaprojectId)));
                            }
                            if (count.getMaterialCount() > 0)
                            {
                                addSection(EntityKind.MATERIAL, new MetaprojectMaterialsSection(
                                        viewContext, new TechId(metaprojectId)));
                            }

                            panel.add(sectionsPanel);
                        }
                    });
    }

    private void addSection(EntityKind entityKind, DisposableTabContent section)
    {
        sectionsPanel.addSection(section);
        sectionsMap.put(entityKind, section);
    }

    private void selectSection(EntityKind entityKind)
    {
        DisposableTabContent section = sectionsMap.get(entityKind);
        if (section != null)
        {
            sectionsPanel.selectSection(section);
            currentEntityKind = entityKind;
        }
    }

    public void showEntities(Long metaprojectId)
    {
        if (currentMetaprojectId != metaprojectId)
        {
            initSections(metaprojectId, new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        selectSection(currentEntityKind);
                    }
                });
        }
    }

    public void showEntities(Long metaprojectId, final EntityKind entityKind)
    {
        if (currentMetaprojectId != metaprojectId)
        {
            initSections(metaprojectId, new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        selectSection(entityKind);
                    }
                });
        } else
        {
            selectSection(entityKind);
        }
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Component getComponent()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void dispose()
    {
        // TODO Auto-generated method stub

    }

}
