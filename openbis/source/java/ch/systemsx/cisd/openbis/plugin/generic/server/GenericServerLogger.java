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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSetsWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Logger class for {@link GenericServer} which creates readable logs of method invocations.
 * 
 * @author Franz-Josef Elmer
 */
final class GenericServerLogger extends AbstractServerLogger implements IGenericServer
{
    /**
     * Creates an instance for the specified session manager invocation status and elapsed time. The
     * session manager is used to retrieve user information which will be a part of the log message.
     */
    GenericServerLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    //
    // IGenericServer
    //

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "get_sample_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        logAccess(sessionToken, "get_sample_info", "ID(%s)", sampleId);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample,
            final Collection<NewAttachment> attachments)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S) ATTACHMENTS(%S)",
                newSample.getSampleType(), newSample.getIdentifier(), attachments.size());
    }

    public Material getMaterialInfo(final String sessionToken, final TechId materialId)
    {
        logAccess(sessionToken, "get_material_info", "ID(%s)", materialId);
        return null;
    }

    public ExternalData getDataSetInfo(final String sessionToken, final TechId datasetId)
    {
        logAccess(sessionToken, "get_data_set_info", "ID(%s)", datasetId);
        return null;
    }

    public void registerExperiment(String sessionToken, NewExperiment experiment,
            final Collection<NewAttachment> attachments)
    {
        logTracking(sessionToken, "register_experiment",
                "EXPERIMENT_TYPE(%s) EXPERIMENT(%S) ATTACHMENTS(%S)",
                experiment.getExperimentTypeCode(), experiment.getIdentifier(), attachments.size());
    }

    public void registerMaterials(String sessionToken, String materialTypeCode,
            List<NewMaterial> newMaterials)
    {
        logTracking(sessionToken, "register_materials", "MATERIAL_TYPE(%s) MATERIALS(%s)",
                materialTypeCode, CollectionUtils.abbreviate(newMaterials, 20));
    }

    public int updateMaterials(String sessionToken, String materialTypeCode,
            List<NewMaterial> newMaterials, boolean ignoreUnregisteredMaterials)
            throws UserFailureException
    {
        logTracking(sessionToken, "update_materials",
                "MATERIAL_TYPE(%s) IGNORE_UNREGISTERED_MATERIALS(%s) MATERIALS(%s)",
                materialTypeCode, ignoreUnregisteredMaterials,
                CollectionUtils.abbreviate(newMaterials, 20));
        return 0;
    }

    public AttachmentWithContent getExperimentFileAttachment(final String sessionToken,
            final TechId experimentId, final String filename, final int version)
            throws UserFailureException
    {
        logAccess(sessionToken, "get_attachment", "EXPERIMENT_ID(%s) FILE(%s) VERSION(%s)",
                experimentId, filename, version);
        return null;
    }

    public AttachmentWithContent getProjectFileAttachment(String sessionToken, TechId projectId,
            String fileName, int version)
    {
        logAccess(sessionToken, "get_attachment", "PROJECT_ID(%s) FILE(%s) VERSION(%s)", projectId,
                fileName, version);
        return null;
    }

    public AttachmentWithContent getSampleFileAttachment(String sessionToken, TechId sampleId,
            String fileName, int version)
    {
        logAccess(sessionToken, "get_attachment", "SAMPLE_ID(%s) FILE(%s) VERSION(%s)", sampleId,
                fileName, version);
        return null;
    }

    public List<String> generateCodes(String sessionToken, String prefix, int number)
    {
        logAccess(sessionToken, "generate_codes", "PREFIX(%s) NUMBER(%s)", prefix, number);
        return null;
    }

    public ExperimentUpdateResult updateExperiment(String sessionToken, ExperimentUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_experiment",
                "EXPERIMENT(%s) ATTACHMENTS_ADDED(%s) NEW_PROJECT(%s) SAMPLES(%s)",
                updates.getExperimentId(), updates.getAttachments().size(),
                updates.getProjectIdentifier(), StringUtils.join(updates.getSampleCodes(), ","));
        return null;
    }

    public Date updateMaterial(String sessionToken, TechId materialId,
            List<IEntityProperty> properties, Date version)
    {
        logTracking(sessionToken, "edit_material", "MATERIAL(%s)", materialId);
        return null;
    }

    public SampleUpdateResult updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_sample",
                "SAMPLE(%s), CHANGE_TO_EXPERIMENT(%s) ATTACHMENTS(%s)",
                updates.getSampleIdOrNull(), updates.getExperimentIdentifierOrNull(), updates
                        .getAttachments().size());
        return null;
    }

    public DataSetUpdateResult updateDataSet(String sessionToken, DataSetUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_data_set", "DATA_SET(%s) SAMPLE(%s) MODIFIED_PARENTS(%s)",
                updates.getDatasetId(), updates.getSampleIdentifierOrNull(),
                StringUtils.join(updates.getModifiedParentDatasetCodesOrNull(), ","));
        return null;
    }

    public void registerSamples(String sessionToken, List<NewSamplesWithTypes> newSamplesWithType)
            throws UserFailureException
    {
        StringBuilder sb = new StringBuilder();
        for (NewSamplesWithTypes s : newSamplesWithType)
        {
            if (sb.length() > 0)
            {
                sb.append(",");
            }
            sb.append(s.getSampleType().getCode() + ":" + s.getNewSamples().size());
        }
        logTracking(sessionToken, "register_samples", sb.toString());

    }

    public void updateSamples(String sessionToken, List<NewSamplesWithTypes> updatedSamplesWithType)
            throws UserFailureException
    {
        StringBuilder sb = new StringBuilder();
        for (NewSamplesWithTypes s : updatedSamplesWithType)
        {
            if (sb.length() > 0)
            {
                sb.append(",");
            }
            sb.append(s.getSampleType().getCode() + ":" + s.getNewSamples().size());
        }
        logTracking(sessionToken, "update_samples", sb.toString());
    }

    public void registerOrUpdateMaterials(String sessionToken, String materialTypeCode,
            List<NewMaterial> materials)
    {
        logTracking(sessionToken, "registerOrUpdateMaterials", "type(%s) numberOfMaterials(%s)",
                materialTypeCode, materials.size());
    }

    public void registerOrUpdateSamples(String sessionToken,
            List<NewSamplesWithTypes> newSamplesWithType) throws UserFailureException
    {
        StringBuilder sb = new StringBuilder();
        for (NewSamplesWithTypes s : newSamplesWithType)
        {
            if (sb.length() > 0)
            {
                sb.append(",");
            }
            sb.append(s.getSampleType().getCode() + ":" + s.getNewSamples().size());
        }
        logTracking(sessionToken, "register_or_update_samples", sb.toString());
    }

    public void updateDataSets(String sessionToken, NewDataSetsWithTypes dataSets)
            throws UserFailureException
    {
        logTracking(sessionToken, "update_data_sets",
                (dataSets.getDataSetType().getCode() + ":" + dataSets.getNewDataSets().size()));
    }

    public void registerExperiments(String sessionToken, NewExperimentsWithType experiments)
            throws UserFailureException
    {
        logTracking(sessionToken, "register_experiments", "TYPE(%s) EXPERIMENTS(%s)",
                experiments.getExperimentTypeCode(), experiments.getNewExperiments().size());
    }

    public void updateExperiments(String sessionToken, UpdatedExperimentsWithType experiments)
            throws UserFailureException
    {
        logTracking(sessionToken, "update_experiments", "TYPE(%s) EXPERIMENTS(%s)", experiments
                .getExperimentType().getCode(), experiments.getUpdatedExperiments().size());
    }

}
