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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractFileDownloadServlet;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * {@link AbstractCommandController} extension for downloading experiment attachments.
 * 
 * @author Izabela Adamczyk
 */
@Controller
@RequestMapping(
    { "/attachment-download", "/openbis/attachment-download" })
public class AttachmentDownloadServlet extends AbstractFileDownloadServlet
{

    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer server;

    public AttachmentDownloadServlet()
    {

    }

    // For testing purposes only
    @Private
    public AttachmentDownloadServlet(final IGenericServer server)
    {
        this.server = server;
    }

    @Override
    protected FileContent getFileContent(final HttpServletRequest request) throws Exception
    {
        final int version =
                Integer.parseInt(request.getParameter(GenericConstants.VERSION_PARAMETER));
        final String fileName = request.getParameter(GenericConstants.FILE_NAME_PARAMETER);
        final String identifier = request.getParameter(GenericConstants.IDENTIFIER_PARAMETER);
        final String attachmentHolderKind =
                request.getParameter(GenericConstants.ATTACHMENT_HOLDER_PARAMETER);

        if (StringUtils.isNotBlank(fileName) && StringUtils.isNotBlank(identifier)
                && StringUtils.isNotBlank(attachmentHolderKind))
        {
            if (attachmentHolderKind.equals(AttachmentHolderKind.EXPERIMENT.name()))
            {
                return getExperimentFile(request, version, fileName, identifier);
            } else if (attachmentHolderKind.equals(AttachmentHolderKind.SAMPLE.name()))
            {
                return getSampleFile(request, version, fileName, identifier);
            } else if (attachmentHolderKind.equals(AttachmentHolderKind.PROJECT.name()))
            {
                return getProjectFile(request, version, fileName, identifier);
            }
        }
        return null;
    }

    private FileContent getExperimentFile(final HttpServletRequest request, final int version,
            final String fileName, final String identifier)
    {
        final ExperimentIdentifier experiment =
                new ExperimentIdentifierFactory(identifier).createIdentifier();
        final AttachmentPE attachment =
                server.getExperimentFileAttachment(getSessionToken(request), experiment, fileName,
                        version);
        return new FileContent(attachment.getAttachmentContent().getValue(), attachment
                .getFileName());
    }

    private FileContent getSampleFile(final HttpServletRequest request, final int version,
            final String fileName, final String identifier)
    {
        final SampleIdentifier sample = new SampleIdentifierFactory(identifier).createIdentifier();
        final AttachmentPE attachment =
                server.getSampleFileAttachment(getSessionToken(request), sample, fileName, version);
        return new FileContent(attachment.getAttachmentContent().getValue(), attachment
                .getFileName());
    }

    private FileContent getProjectFile(final HttpServletRequest request, final int version,
            final String fileName, final String identifier)
    {
        final ProjectIdentifier project =
                new ProjectIdentifierFactory(identifier).createIdentifier();
        final AttachmentPE attachment =
                server.getProjectFileAttachment(getSessionToken(request), project, fileName,
                        version);
        return new FileContent(attachment.getAttachmentContent().getValue(), attachment
                .getFileName());
    }
}
