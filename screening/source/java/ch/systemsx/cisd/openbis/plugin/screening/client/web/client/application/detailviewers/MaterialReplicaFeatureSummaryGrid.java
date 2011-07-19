/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.AnalysisProcedureChooser.IAnalysisProcedureSelectionListener;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialFeaturesOneExpCriteria;

/**
 * A grid showing replica feature vector summaries for a combination of experiment and material.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialReplicaFeatureSummaryGrid extends
        TypedTableGrid<MaterialReplicaFeatureSummary>
{
    private static final String ID = "material_replica_feature_summary";

    private static final String PREFIX = GenericConstants.ID_PREFIX + ID;

    public static final String BROWSER_ID = PREFIX + "_main";

    private final IViewContext<IScreeningClientServiceAsync> specificViewContext;

    private final TechId experimentId;

    private final TechId materialId;

    private AnalysisProcedureCriteria analysisProcedureCriteria;

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext, TechId experimentId,
            TechId materialId, AnalysisProcedureListenerHolder analysisProcedureListenerHolder)
    {
        return new MaterialReplicaFeatureSummaryGrid(viewContext, experimentId, materialId,
                analysisProcedureListenerHolder).asDisposableWithoutToolbar();
    }

    MaterialReplicaFeatureSummaryGrid(IViewContext<IScreeningClientServiceAsync> viewContext,
            TechId experimentId, TechId materialId,
            AnalysisProcedureListenerHolder analysisProcedureListenerHolder)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, false,
                DisplayTypeIDGenerator.MATERIAL_REPLICA_SUMMARY_SECTION);
        this.specificViewContext = viewContext;
        this.experimentId = experimentId;
        this.materialId = materialId;

        setBorders(true);
        IAnalysisProcedureSelectionListener analysisProcedureListener =
                createAnalysisProcedureListener();
        analysisProcedureListenerHolder.setAnalysisProcedureListener(analysisProcedureListener);
    }

    private IAnalysisProcedureSelectionListener createAnalysisProcedureListener()
    {
        return new IAnalysisProcedureSelectionListener()
            {
                public void analysisProcedureSelected(AnalysisProcedureCriteria criteria)
                {
                    analysisProcedureCriteria = criteria;
                    refresh(true);
                }
            };
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<MaterialReplicaFeatureSummary>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<MaterialReplicaFeatureSummary>> callback)
    {
        MaterialFeaturesOneExpCriteria criteria =
                new MaterialFeaturesOneExpCriteria(materialId, analysisProcedureCriteria,
                        experimentId);
        specificViewContext.getService().listMaterialReplicaFeatureSummary(resultSetConfig,
                criteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<MaterialReplicaFeatureSummary>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportMaterialReplicaFeatureSummary(exportCriteria,
                callback);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return ID + "_" + columnID.toUpperCase();
    }

    public void dispose()
    {
        asDisposableWithoutToolbar().dispose();
    }
}
