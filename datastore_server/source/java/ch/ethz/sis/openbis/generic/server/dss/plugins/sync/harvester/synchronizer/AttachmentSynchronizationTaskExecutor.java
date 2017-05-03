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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.ServiceFinderUtils;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.ResourceListParserData.IncomingEntity;
import ch.systemsx.cisd.cifex.shared.basic.UserFailureException;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
final class AttachmentSynchronizationTaskExecutor implements ITaskExecutor<IncomingEntity<?>>
{
    private final Date lastSyncTimestamp;

    private final SyncConfig config;

    private final IEncapsulatedOpenBISService service;

    private final List<String> notSyncedAttachmentHolderPermIds;

    /**
     * @param notSyncedAttachmentHolderPermIds will contain ids of attachment holders for which the attachment sync failed during parallel execution
     * @param service
     * @param lastSyncTimestamp
     * @param config
     */
    public AttachmentSynchronizationTaskExecutor(List<String> notSyncedAttachmentHolderPermIds,
            IEncapsulatedOpenBISService service,
            Date lastSyncTimestamp, SyncConfig config)
    {
        this.notSyncedAttachmentHolderPermIds = notSyncedAttachmentHolderPermIds;
        this.service = service;
        this.lastSyncTimestamp = lastSyncTimestamp;
        this.config = config;
    }

    @Override
    public Status execute(IncomingEntity<?> item)
    {
        try
        {
            TechId techId = null;
            ICommonServer commonServer = ServiceFinderUtils.getCommonServer(ServiceProvider.getConfigProvider().getOpenBisServerUrl());
            String localSessionToken = ServiceFinderUtils.login(commonServer, config.getHarvesterUser(), config.getHarvesterPass());
            IAttachmentsOperationsHandler attachmentsOperationsHandler = null;
            if (item.getEntityKind() == SyncEntityKind.EXPERIMENT)
            {
                Experiment experiment = service.tryGetExperiment(ExperimentIdentifierFactory.parse(item.getEntity().getIdentifier()));
                techId = new TechId(experiment.getId());
                attachmentsOperationsHandler = new ExperimentAttachmentsOperationsHandler(config, commonServer, localSessionToken);
            }
            else if (item.getEntityKind() == SyncEntityKind.SAMPLE)
            {
                Sample sample = service.tryGetSampleByPermId(item.getEntity().getPermID());
                techId = new TechId(sample.getId());
                attachmentsOperationsHandler = new SampleAttachmentsOperationsHandler(config, commonServer, localSessionToken);
            }
            else if (item.getEntityKind() == SyncEntityKind.PROJECT)
            {
                Project project = service.tryGetProject(ProjectIdentifierFactory.parse(item.getEntity().getIdentifier()));
                techId = new TechId(project.getId());
                attachmentsOperationsHandler = new ProjectAttachmentsOperationsHandler(config, commonServer, localSessionToken);
            }
            else
            {
                return Status.createError("Attachments can be synchronized for only Projects, Experiments and Samples");
            }
            Map<String, Attachment> incomingAttachmentMap = new HashMap<String, Attachment>();
            List<Attachment> incomingAttachments = new ArrayList<Attachment>();
            // check if the entity had attachments in XML resource list. This way we do not unnecessarily try to retrieve the attachments
            // that do not have attachments.
            if (item.hasAttachments() == true)
            {
                incomingAttachments = attachmentsOperationsHandler.listDataSourceAttachments(config, item.getPermID());

                // place the incoming attachments in a map
                for (Attachment incoming : incomingAttachments)
                {
                    incomingAttachmentMap.put(incoming.getFileName(), incoming);
                }
            }

            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> existingAttachments =
                    attachmentsOperationsHandler.listHarvesterAttachments(item.getPermID());
            Map<String, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> existingAttachmentMap =
                    new HashMap<String, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment>();

            // place the existing attachments in a map
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment attachment : existingAttachments)
            {
                existingAttachmentMap.put(attachment.getFileName(), attachment);
            }

            for (Attachment incoming : incomingAttachments)
            {
                ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment existingAttachment =
                        existingAttachmentMap.get(incoming.getFileName());
                if (existingAttachment == null)
                {
                    addAttachments(incoming, 1, techId, attachmentsOperationsHandler);
                }
                else
                {
                    int version = existingAttachment.getVersion();
                    if (incoming.getVersion() < version)
                    {
                        // Harvester has a later version of the attachment. Delete it from harvester
                        replaceAttachment(techId, incoming, attachmentsOperationsHandler);
                    }
                    else if (incoming.getVersion() == version)
                    {
                        // check last sync date and meta data
                        if (incoming.getRegistrationDate().after(lastSyncTimestamp))
                        {
                            replaceAttachment(techId, incoming, attachmentsOperationsHandler);
                        }
                        else
                        {
                            // check if meta data changed
                            if (equalsNullable(incoming.getTitle(), existingAttachment.getTitle()) == false
                                    || equalsNullable(incoming.getDescription(), existingAttachment.getDescription()) == false)
                            {
                                ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment updateDTO =
                                        new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment();
                                updateDTO.setFileName(existingAttachment.getFileName());
                                updateDTO.setVersion(existingAttachment.getVersion());
                                updateDTO.setTitle(incoming.getTitle());
                                updateDTO.setDescription(incoming.getDescription());
                                attachmentsOperationsHandler.updateAttachment(techId, updateDTO);
                            }
                        }
                    }
                    else
                    {
                        // add all new versions from the incoming (do we need to check last sync date)
                        // Attachment attachmentVersion = getVersion(incoming, version);
                        addAttachments(incoming, version + 1, techId, attachmentsOperationsHandler);
                    }
                }
            }
            // loop through existing attachments and if they no longer exist in data source, delete them.
            for (ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment existing : existingAttachments)
            {
                if (incomingAttachmentMap.get(existing.getFileName()) == null)
                {
                    attachmentsOperationsHandler.deleteAttachment(techId, existing.getFileName());
                }
            }
        }
 catch (Exception e)
        {
            try
            {
                addToNotSyncedList(item);
            } catch (UserFailureException e1)
            {
                return Status.createError("Error synchronizing attachments for entity:" + item.getPermID() + ":" + e.getMessage());
            }
            return Status.createError("Error synchronizing attachments for entity:" + item.getPermID() + ":" + e.getMessage());
        }
        return Status.OK;
    }

    private void addToNotSyncedList(IncomingEntity<?> item) throws UserFailureException
    {
        notSyncedAttachmentHolderPermIds.add(item.getEntityKind().getLabel() + "-" + item.getPermID());
    }

    private boolean equalsNullable(String s1OrNull, String s2OrNull)
    {
        if (s1OrNull == null)
        {
            return s2OrNull == null ? true : false;
        } else if (s2OrNull == null)
        {
            return false;
        } else
        {
            return s1OrNull.equals(s2OrNull);
        }
    }

    private void replaceAttachment(TechId techId, Attachment incoming, IAttachmentsOperationsHandler attachmentsOperationsHandler)
    {
        attachmentsOperationsHandler.deleteAttachment(techId, incoming.getFileName());
        addAttachments(incoming, 1, techId, attachmentsOperationsHandler);
    }

    private void addAttachments(Attachment attachment, int fromVersion, TechId techId, IAttachmentsOperationsHandler handler)
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
            handler.addAttachment(techId, earliestVersion);
            earliestVersion = versions.pollLast();
        }
    }
}

interface IAttachmentsOperationsHandler
{
    List<Attachment> listDataSourceAttachments(SyncConfig config, String permId);
    
    List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> listHarvesterAttachments(String permId);
    
    void addAttachment(TechId techId, NewAttachment attachment);

    void deleteAttachment(TechId experimentId, String fileName);

    void updateAttachment(TechId experimentId, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment attachment);
}

abstract class AbstractEntityAttachmentsOperationsHandler implements IAttachmentsOperationsHandler
{
    final ICommonServer commonServer;

    final String sessionToken;

    final SyncConfig config;

    AbstractEntityAttachmentsOperationsHandler(SyncConfig config, ICommonServer commonServer, String sessionToken)
    {
        this.commonServer = commonServer;
        this.sessionToken = sessionToken;
        this.config = config;
    }

    @Override
    public abstract List<Attachment> listDataSourceAttachments(SyncConfig config, String permId);

    @Override
    public abstract List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> listHarvesterAttachments(String permId);

    @Override
    public abstract void addAttachment(TechId techId, NewAttachment attachment);

    @Override
    public abstract void deleteAttachment(TechId experimentId, String fileName);
    
    @Override
    public abstract void updateAttachment(TechId experimentId, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment attachment);
}

class ExperimentAttachmentsOperationsHandler extends AbstractEntityAttachmentsOperationsHandler
{
    ExperimentAttachmentsOperationsHandler(SyncConfig config, ICommonServer commonServer, String sessionToken)
    {
        super(config, commonServer, sessionToken);
    }

    @Override
    public List<Attachment> listDataSourceAttachments(SyncConfig config, String permId)
    {
        V3Utils v3Utils = V3Utils.create(config.getDataSourceOpenbisURL(), config.getDataSourceDSSURL());
        String sessionToken = v3Utils.login(config.getUser(), config.getPassword());
        return v3Utils.getExperimentAttachments(sessionToken, new ExperimentPermId(permId));
    }

    @Override
    public List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> listHarvesterAttachments(String permId)
    {
        IEncapsulatedOpenBISService service = ServiceProvider.getOpenBISService();
        Experiment experiment = service.tryGetExperimentByPermId(permId);
        return service.listAttachments(AttachmentHolderKind.EXPERIMENT, experiment.getId());
    }

    @Override
    public void addAttachment(TechId techId, NewAttachment attachment)
    {
        commonServer.addExperimentAttachment(sessionToken, techId, attachment);
    }

    @Override
    public void deleteAttachment(TechId experimentId, String fileName)
    {
        commonServer.deleteExperimentAttachments(sessionToken, experimentId, Arrays.asList(fileName),
                "Synchronization from data source " + config.getDataSourceAlias()
                        + " Attachment no longer exists on data source.");
    }

    @Override
    public void updateAttachment(TechId experimentId, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment attachment)
    {
        commonServer.updateExperimentAttachments(sessionToken, experimentId, attachment);
    }
}

class SampleAttachmentsOperationsHandler extends AbstractEntityAttachmentsOperationsHandler
{
    SampleAttachmentsOperationsHandler(SyncConfig config, ICommonServer commonServer, String sessionToken)
    {
        super(config, commonServer, sessionToken);
    }

    @Override
    public List<Attachment> listDataSourceAttachments(SyncConfig config, String permId)
    {
        V3Utils dssFileUtils = V3Utils.create(config.getDataSourceOpenbisURL(), config.getDataSourceDSSURL());
        String sessionToken = dssFileUtils.login(config.getUser(), config.getPassword());
        return dssFileUtils.getSampleAttachments(sessionToken, new SamplePermId(permId));
    }

    @Override
    public List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> listHarvesterAttachments(String permId)
    {
        IEncapsulatedOpenBISService service = ServiceProvider.getOpenBISService();
        Sample sample = service.tryGetSampleByPermId(permId);
        return service.listAttachments(AttachmentHolderKind.SAMPLE, sample.getId());
    }

    @Override
    public void addAttachment(TechId techId, NewAttachment attachment)
    {
        commonServer.addSampleAttachments(sessionToken, techId, attachment);
    }

    @Override
    public void deleteAttachment(TechId sampleId, String fileName)
    {
        commonServer.deleteSampleAttachments(sessionToken, sampleId, Arrays.asList(fileName),
                "Synchronization from data source " + config.getDataSourceAlias()
                        + " Attachment no longer exists on data source.");
    }

    @Override
    public void updateAttachment(TechId sampleId, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment attachment)
    {
        commonServer.updateSampleAttachments(sessionToken, sampleId, attachment);
    }
}

class ProjectAttachmentsOperationsHandler extends AbstractEntityAttachmentsOperationsHandler
{
    ProjectAttachmentsOperationsHandler(SyncConfig config, ICommonServer commonServer, String sessionToken)
    {
        super(config, commonServer, sessionToken);
    }

    @Override
    public List<Attachment> listDataSourceAttachments(SyncConfig config, String permId)
    {
        V3Utils dssFileUtils = V3Utils.create(config.getDataSourceOpenbisURL(), config.getDataSourceDSSURL());
        String sessionToken = dssFileUtils.login(config.getUser(), config.getPassword());
        return dssFileUtils.getProjectAttachments(sessionToken, new ProjectPermId(permId));
    }

    @Override
    public List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment> listHarvesterAttachments(String permId)
    {
        IEncapsulatedOpenBISService service = ServiceProvider.getOpenBISService();
        Project project = service.tryGetProjectByPermId(permId);
        return service.listAttachments(AttachmentHolderKind.PROJECT, project.getId());
    }

    @Override
    public void addAttachment(TechId techId, NewAttachment attachment)
    {
        commonServer.addProjectAttachments(sessionToken, techId, attachment);
    }

    @Override
    public void deleteAttachment(TechId projectId, String fileName)
    {
        commonServer.deleteProjectAttachments(sessionToken, projectId, Arrays.asList(fileName),
                "Synchronization from data source " + config.getDataSourceAlias()
                        + " Attachment no longer exists on data source.");
    }

    @Override
    public void updateAttachment(TechId projectId, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment attachment)
    {
        commonServer.updateProjectAttachments(sessionToken, projectId, attachment);
    }
}