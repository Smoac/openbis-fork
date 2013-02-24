/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ProcessingDisplayCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.CommonConstants;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentViewer extends GenericExperimentViewer
{
    public static DatabaseModificationAwareComponent create(
            IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            BasicEntityType experimentType, IIdAndCodeHolder experimentId)
    {
        ExperimentViewer viewer =
                new ExperimentViewer(new GenericViewContext(viewContext.getCommonViewContext()),
                        viewContext, experimentType, experimentId);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    protected ExperimentViewer(IViewContext<IGenericClientServiceAsync> viewContext,
            IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext,
            BasicEntityType experimentType, IIdAndCodeHolder experimentId)
    {
        super(viewContext, experimentType, experimentId);
        this.specificViewContext = specificViewContext;
    }

    @Override
    protected List<DisposableTabContent> createAdditionalBrowserSectionPanels()
    {
        DisposableTabContent section =
                new DisposableTabContent(specificViewContext.getMessage(Dict.PROTEINS_SECTION),
                        specificViewContext, experiment)
                    {
                        @Override
                        protected IDisposableComponent createDisposableContent()
                        {
                            return ProteinByExperimentBrowserGrid.create(specificViewContext,
                                    experimentType, experiment);
                        }
                    };
        section.setIds(DisplayTypeIDGenerator.PROTEIN_SECTION);
        return Collections.<DisposableTabContent> singletonList(section);
    }

    @Override
    protected Component tryCreateLowerLeftComponent()
    {
        if (viewContext.isSimpleOrEmbeddedMode())
        {
            return null; // no processing in simple view mode
        }
        final ContentPanel contentPanel = new ContentPanel(new RowLayout());
        contentPanel.setHeading(viewContext.getMessage(Dict.DATA_SET_PROCESSING_SECTION_TITLE));
        viewContext.getCommonService().listExperimentDataSets(
                new TechId(experimentId),
                DefaultResultSetConfig
                        .<String, TableModelRowWithObject<AbstractExternalData>> createFetchAll(), true,
                new AbstractAsyncCallback<TypedTableResultSet<AbstractExternalData>>(viewContext)
                    {
                        @Override
                        protected void process(TypedTableResultSet<AbstractExternalData> result)
                        {
                            AsyncCallback<List<DatastoreServiceDescription>> callBack =
                                    createCallback(contentPanel, result);
                            viewContext.getCommonService().listDataStoreServices(
                                    DataStoreServiceKind.PROCESSING, callBack);

                        }
                    });
        return contentPanel;
    }

    private AsyncCallback<List<DatastoreServiceDescription>> createCallback(
            final ContentPanel contentPanel, TypedTableResultSet<AbstractExternalData> result)
    {
        final List<String> dataSetCodes = new ArrayList<String>();
        for (GridRowModel<TableModelRowWithObject<AbstractExternalData>> gridRowModel : result.getResultSet().getList())
        {
            dataSetCodes.add(gridRowModel.getOriginalObject().getObjectOrNull().getCode());
        }
        final DisplayedOrSelectedDatasetCriteria criteria =
                DisplayedOrSelectedDatasetCriteria.createSelectedItems(dataSetCodes);
        return new AbstractAsyncCallback<List<DatastoreServiceDescription>>(viewContext)
            {
                @Override
                protected void process(List<DatastoreServiceDescription> descriptions)
                {
                    for (final DatastoreServiceDescription description : descriptions)
                    {
                        String[] dataSetTypes = description.getDatasetTypeCodes();
                        for (String dataSetType : dataSetTypes)
                        {
                            if (dataSetType.equals(CommonConstants.PROT_RESULT_DATA_SET_TYPE))
                            {
                                Widget link = createLink(description, criteria);
                                LayoutContainer wrapper = new LayoutContainer(new FitLayout());
                                wrapper.add(link);
                                contentPanel.add(wrapper, new RowData(1, -1, new Margins(5)));
                            }
                        }
                    }
                    contentPanel.layout();
                }
            };
    }

    private Widget createLink(final DatastoreServiceDescription description,
            final DisplayedOrSelectedDatasetCriteria criteria)
    {
        String href = "";
        ClickHandler listener = new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    viewContext.getCommonService().processDatasets(description, criteria,
                            new ProcessingDisplayCallback(viewContext));
                }
            };
        String label = StringEscapeUtils.unescapeHtml(description.getLabel());
        return LinkRenderer.getLinkWidget(label, listener, href);
    }

}
