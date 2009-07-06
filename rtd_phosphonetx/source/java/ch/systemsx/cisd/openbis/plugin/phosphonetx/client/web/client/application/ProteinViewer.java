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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ProteinInfo;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinViewer extends ContentPanel implements IDatabaseModificationObserver
{
    private static final String PREFIX = "protein-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;
    
    static ITabItemFactory createTabItemFactory(final IViewContext<IPhosphoNetXClientServiceAsync> viewContext, final ProteinInfo proteinInfo)
    {
        return new ITabItemFactory()
            {
                public String getId()
                {
                    return createWidgetID(proteinInfo.getId());
                }
        
                public ITabItem create()
                {
                    ProteinViewer viewer = new ProteinViewer(viewContext, proteinInfo.getId());
                    DatabaseModificationAwareComponent c = new DatabaseModificationAwareComponent(viewer, viewer);
                    return DefaultTabItem.create("Protein: " + proteinInfo.getDescription(), c, viewContext, false);
                }
            };
    }
    
    static String createWidgetID(TechId proteinID)
    {
        return ID_PREFIX + proteinID.getId();
    }
    
    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;
    private final TechId proteinID;
    private final String widgetID;
    
    private ProteinViewer(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            TechId proteinID)
    {
        widgetID = createWidgetID(proteinID);
        this.viewContext = viewContext;
        this.proteinID = proteinID;
    }
    
    private void createUI()
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        properties.put("ID", proteinID);
        add(propertyGrid);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
    }

}
