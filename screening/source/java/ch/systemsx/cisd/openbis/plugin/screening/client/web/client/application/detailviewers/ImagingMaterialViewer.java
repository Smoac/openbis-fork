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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;

/**
 * Material detail viewer in screening context.
 * 
 * @author Tomasz Pylak
 */
public class ImagingMaterialViewer extends GenericMaterialViewer
{

    private static final boolean SUMMARY_SECTION_IMPLEMENTED = false; // FIXME

    /**
     * @param experimentCriteriaOrNull if the experiment criteria are specified, they will be chosen
     *            automatically when the window opens.
     */
    public static DatabaseModificationAwareComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext, TechId materialId,
            ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        ImagingMaterialViewer viewer =
                new ImagingMaterialViewer(viewContext, materialId, experimentCriteriaOrNull);
        viewer.reloadAllData();
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final ExperimentSearchCriteria initialExperimentCriteriaOrNull;

    private ImagingMaterialViewer(IViewContext<IScreeningClientServiceAsync> viewContext,
            TechId materialTechId, ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        super(viewContext, materialTechId);
        this.screeningViewContext = viewContext;
        this.initialExperimentCriteriaOrNull = experimentCriteriaOrNull;
    }

    @Override
    protected void loadMaterialInfo(TechId materialTechId, AsyncCallback<Material> callback)
    {
        screeningViewContext.getCommonService().getMaterialInfo(materialTechId, callback);
    }

    @Override
    protected List<TabContent> createAdditionalSectionPanels(Material material)
    {

        List<TabContent> sections = new ArrayList<TabContent>();

        WellSearchMaterialSection wellSearchSection =
                new WellSearchMaterialSection(screeningViewContext, materialId,
                        initialExperimentCriteriaOrNull);
        sections.add(wellSearchSection);

        boolean restrictGlobalScopeLinkToProject =
                isRestrictGlobalScopeLinkToProject(initialExperimentCriteriaOrNull);
        if (SUMMARY_SECTION_IMPLEMENTED)
        {
            MaterialMergedSummarySection summarySection =
                    new MaterialMergedSummarySection(screeningViewContext, material,
                            initialExperimentCriteriaOrNull, restrictGlobalScopeLinkToProject);
            sections.add(summarySection);
        }

        String experimentPermId = tryGetExperimentPermId(initialExperimentCriteriaOrNull);
        if (experimentPermId != null)
        {
            MaterialReplicaSummarySection replicaSummarySection =
                    new MaterialReplicaSummarySection(screeningViewContext, material,
                            experimentPermId, restrictGlobalScopeLinkToProject);
            sections.add(replicaSummarySection);
        }
        ExperimentSearchByProjectCriteria experimentCriteria =
                tryConvert(initialExperimentCriteriaOrNull);
        MaterialFeaturesFromAllExpermentsSection featuresFromAllExperimentsSection =
                new MaterialFeaturesFromAllExpermentsSection(screeningViewContext, material,
                        experimentCriteria);
        sections.add(featuresFromAllExperimentsSection);
        return sections;
    }

    private static boolean isRestrictGlobalScopeLinkToProject(
            ExperimentSearchCriteria experimentCriteriaOrNull)
    {

        if (experimentCriteriaOrNull == null)
        {
            return false;
        } else
        {
            return experimentCriteriaOrNull.getRestrictGlobalSearchLinkToProject();
        }
    }

    private static ExperimentSearchByProjectCriteria tryConvert(
            ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        if (experimentCriteriaOrNull == null)
        {
            return null;
        } else
        {
            return experimentCriteriaOrNull.tryAsSearchByProjectCriteria();
        }
    }

    private static String tryGetExperimentPermId(ExperimentSearchCriteria experimentCriteriaOrNull)
    {
        if (experimentCriteriaOrNull != null)
        {
            SingleExperimentSearchCriteria experimentCriteria =
                    experimentCriteriaOrNull.tryGetExperiment();
            if (experimentCriteria != null)
            {
                return experimentCriteria.getExperimentPermId();
            }
        }
        return null;
    }

}
