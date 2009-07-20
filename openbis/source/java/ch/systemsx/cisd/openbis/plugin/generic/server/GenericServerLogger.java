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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
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
            final boolean invocationSuccessful, final long elapsedTime)
    {
        super(sessionManager, invocationSuccessful, elapsedTime);
    }

    //
    // IGenericServer
    //

    public final SampleGenerationDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logAccess(sessionToken, "get_sample_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public final SampleGenerationDTO getSampleInfo(final String sessionToken, final TechId sampleId)
    {
        logAccess(sessionToken, "get_sample_info", "ID(%s)", sampleId);
        return null;
    }

    public void registerSample(final String sessionToken, final NewSample newSample,
            List<AttachmentPE> attachments)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S) ATTACHMENTS(%S)",
                newSample.getSampleType(), newSample.getIdentifier(), attachments.size());
    }

    public ExperimentPE getExperimentInfo(final String sessionToken,
            final ExperimentIdentifier identifier)
    {
        logAccess(sessionToken, "get_experiment_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public ExperimentPE getExperimentInfo(final String sessionToken, final TechId experimentId)
    {
        logAccess(sessionToken, "get_experiment_info", "ID(%s)", experimentId);
        return null;
    }

    public MaterialPE getMaterialInfo(final String sessionToken, final TechId materialId)
    {
        logAccess(sessionToken, "get_material_info", "ID(%s)", materialId);
        return null;
    }

    public ExternalDataPE getDataSetInfo(final String sessionToken, final TechId datasetId)
    {
        logAccess(sessionToken, "get_data_set_info", "ID(%s)", datasetId);
        return null;
    }

    public final void registerSamples(final String sessionToken, final SampleType sampleType,
            final List<NewSample> newSamples) throws UserFailureException
    {
        logAccess(sessionToken, "register_samples", "SAMPLE_TYPE(%s) SAMPLES(%s)", sampleType,
                CollectionUtils.abbreviate(newSamples, 20));
    }

    public void registerExperiment(String sessionToken, NewExperiment experiment,
            List<AttachmentPE> attachments)
    {
        logTracking(sessionToken, "register_experiment",
                "EXPERIMENT_TYPE(%s) EXPERIMENT(%S) ATTACHMENTS(%S)", experiment
                        .getExperimentTypeCode(), experiment.getIdentifier(), attachments.size());
    }

    public void registerMaterials(String sessionToken, String materialTypeCode,
            List<NewMaterial> newMaterials)
    {
        logAccess(sessionToken, "register_materials", "MATERIAL_TYPE(%s) MATERIALS(%s)",
                materialTypeCode, CollectionUtils.abbreviate(newMaterials, 20));
    }

    public AttachmentPE getExperimentFileAttachment(final String sessionToken,
            final TechId experimentId, final String filename, final int version)
            throws UserFailureException
    {
        logAccess(sessionToken, "get_attachment", "EXPERIMENT_ID(%s) FILE(%s) VERSION(%s)",
                experimentId, filename, version);
        return null;
    }

    public AttachmentPE getProjectFileAttachment(String sessionToken, TechId projectId,
            String fileName, int version)
    {
        logAccess(sessionToken, "get_attachment", "PROJECT_ID(%s) FILE(%s) VERSION(%s)", projectId,
                fileName, version);
        return null;
    }

    public AttachmentPE getSampleFileAttachment(String sessionToken, TechId sampleId,
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
                "EXPERIMENT(%s) ATTACHMENTS_ADDED(%s) NEW_PROJECT(%s) SAMPLES(%s)", updates
                        .getExperimentId(), updates.getAttachments().size(), updates
                        .getProjectIdentifier(), StringUtils.join(updates.getSampleCodes(), ","));
        return null;
    }

    public Date updateMaterial(String sessionToken, TechId materialId,
            List<MaterialProperty> properties, Date version)
    {
        logTracking(sessionToken, "edit_material", "MATERIAL(%s)", materialId);
        return null;
    }

    public Date updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        logTracking(sessionToken, "edit_sample",
                "SAMPLE(%s), CHANGE_TO_EXPERIMENT(%s) ATTACHMENTS(%s)", updates.getSampleId(),
                updates.getExperimentIdentifierOrNull(), updates.getAttachments().size());
        return null;
    }

    public Date updateDataSet(String sessionToken, TechId datasetId,
            SampleIdentifier sampleIdentifier, List<DataSetProperty> properties, Date version)
    {
        logTracking(sessionToken, "edit_data_set", "DATA_SET(%s)", datasetId);
        return null;
    }

}
