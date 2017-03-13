/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.ServiceFinderUtils;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
final class AttachmentSynchronizationTaskExecutor implements ITaskExecutor<Identifier<?>>
{
    private final Date lastSyncTimestamp;

    private final SyncConfig config;

    private final IEncapsulatedOpenBISService service;

    public AttachmentSynchronizationTaskExecutor(IEncapsulatedOpenBISService service, Date lastSyncTimestamp, SyncConfig config)
    {
        this.service = service;
        this.lastSyncTimestamp = lastSyncTimestamp;
        this.config = config;
    }

    @Override
    public Status execute(Identifier<?> item)
    {
        V3Utils dssFileUtils = V3Utils.create(config.getDataSourceOpenbisURL(), config.getDataSourceDSSURL());
        String sessionToken = dssFileUtils.login(config.getUser(), config.getPassword());
        if (item instanceof NewExperiment)
        {
            List<Attachment> incomingAttachments = dssFileUtils.getExperimentAttachments(sessionToken, new ExperimentPermId(item.getPermID()));

            // place the incoming attachments in a map
            Map<String, Attachment> incomingAttachmentMap = new HashMap<String, Attachment>();
            for (Attachment incoming : incomingAttachments)
            {
                incomingAttachmentMap.put(incoming.getFileName(), incoming);
            }

            Experiment experiment = service.tryGetExperiment(ExperimentIdentifierFactory.parse(item.getIdentifier()));
            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> existingAttachments =
                    service.listAttachments(AttachmentHolderKind.EXPERIMENT, experiment.getId());
            Map<String, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> existingAttachmentMap =
                    new HashMap<String, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment>();

            // place the existing attachments in a map
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment attachment : existingAttachments)
            {
                existingAttachmentMap.put(attachment.getFileName(), attachment);
            }

            ICommonServer commonServer = ServiceFinderUtils.getCommonServer(ServiceProvider.getConfigProvider().getOpenBisServerUrl());
            String localSessionToken = ServiceFinderUtils.login(commonServer, config.getHarvesterUser(), config.getHarvesterPass());
            TechId experimentId = new TechId(experiment.getId());
            for (Attachment incoming : incomingAttachments)
            {
                ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment existingAttachment =
                        existingAttachmentMap.get(incoming.getFileName());
                if (existingAttachment == null)
                {
                    addAttachments(incoming, 1, experimentId, commonServer, localSessionToken);
                }
                else
                {
                    int version = existingAttachment.getVersion();
                    if (incoming.getVersion() < version)
                    {
                        // Harvester has a later version of the attachment. Delete it from harvester
                        commonServer.deleteExperimentAttachments(localSessionToken, experimentId,
                                Arrays.asList(incoming.getFileName()), "Synchronization from data source " + config.getDataSourceAlias());
                        addAttachments(incoming, 1, experimentId, commonServer, localSessionToken);
                    }
                    else if (incoming.getVersion() == version)
                    {
                        // check last sync date and meta data
                        if (incoming.getRegistrationDate().after(lastSyncTimestamp))
                        {
                            // Data source has the same version number but with a later registration date than the last sync timestamp:
                            // This means, the attachment was probably deleted in the data source and re-registered. Delete it from harvester
                            // and re-register
                            commonServer.deleteExperimentAttachments(localSessionToken, experimentId,
                                    Arrays.asList(incoming.getFileName()), "Synchronization from data source " + config.getDataSourceAlias());
                            addAttachments(incoming, 1, experimentId, commonServer, localSessionToken);
                        }
                        else
                        {
                            // check if meta data changed
                            if (incoming.getTitle().equals(existingAttachment.getTitle()) == false
                                    || incoming.getDescription().equals(existingAttachment.getDescription()))
                            {
                                ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment updateDTO =
                                        new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment();
                                updateDTO.setFileName(existingAttachment.getFileName());
                                updateDTO.setVersion(existingAttachment.getVersion());
                                updateDTO.setTitle(incoming.getTitle());
                                updateDTO.setDescription(incoming.getDescription());
                                commonServer.updateExperimentAttachments(localSessionToken, experimentId, updateDTO);
                            }
                        }
                    }
                    else
                    {
                        // add all new versions from the incoming (do we need to check last sync date)
                        // Attachment attachmentVersion = getVersion(incoming, version);
                        addAttachments(incoming, version + 1, experimentId, commonServer, localSessionToken);
                    }
                }
            }
            // loop through existing attachments and if they no longer exist in data source, delete them.
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment existing : existingAttachments)
            {
                if (incomingAttachmentMap.get(existing.getFileName()) == null)
                {
                    commonServer.deleteExperimentAttachments(localSessionToken, experimentId,
                            Arrays.asList(existing.getFileName()), "Synchronization from data source " + config.getDataSourceAlias()
                                    + " Attachment no longer exists on data source.");
                }
            }
        }
        return null;
    }

    private void addAttachments(Attachment attachment, int fromVersion, TechId techId, ICommonServer commonServer, String sessionToken)
    {

        Integer version = attachment.getVersion();
        Deque<NewAttachment> versions = new ArrayDeque<NewAttachment>();
        for (int i = version; i >= fromVersion; i--)
        {
            NewAttachment newAttachment = new NewAttachment();
            newAttachment.setTitle(attachment.getTitle());
            newAttachment.setDescription(attachment.getDescription());
            newAttachment.setFilePath(attachment.getFileName());
            newAttachment.setContent(attachment.getContent());
            versions.add(newAttachment);
            if (i == 1)
            {
                break;
            }
            attachment = attachment.getPreviousVersion();
        }
        NewAttachment earliestVersion = versions.pollLast();
        while (earliestVersion != null)
        {
            commonServer.addExperimentAttachment(sessionToken, techId, earliestVersion);
            earliestVersion = versions.pollLast();
        }
    }
}
