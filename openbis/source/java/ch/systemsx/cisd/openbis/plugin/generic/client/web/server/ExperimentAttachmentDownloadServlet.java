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
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractFileDownloadServlet;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * {@link AbstractCommandController} extension for downloading experiment attachments.
 * 
 * @author Izabela Adamczyk
 */
@Controller
@RequestMapping(
    { "/experiment-attachment-download", "/openbis/experiment-attachment-download" })
public class ExperimentAttachmentDownloadServlet extends AbstractFileDownloadServlet
{

    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer server;

    public ExperimentAttachmentDownloadServlet()
    {

    }

    // For testing purposes only
    @Private
    public ExperimentAttachmentDownloadServlet(final IGenericServer server)
    {
        this.server = server;
    }

    @Override
    protected FileContent getFileContent(final HttpServletRequest request) throws Exception
    {
        final int version =
                Integer.parseInt(request.getParameter(GenericConstants.VERSION_PARAMETER));
        final String fileName = request.getParameter(GenericConstants.FILE_NAME_PARAMETER);
        final String project = request.getParameter(GenericConstants.PROJECT_PARAMETER);
        final String experiment = request.getParameter(GenericConstants.EXPERIMENT_PARAMETER);
        final String group = request.getParameter(GenericConstants.GROUP_PARAMETER);
        final String dbInstance = request.getParameter(GenericConstants.DATABASE_PARAMETER);

        if (StringUtils.isNotBlank(fileName) && StringUtils.isNotBlank(project)
                && StringUtils.isNotBlank(experiment))
        {
            final ExperimentIdentifier experimentIdentifier =
                    group == null ? new ExperimentIdentifier(project, experiment)
                            : new ExperimentIdentifier(dbInstance, group, project, experiment);
            final AttachmentPE experimentAttachment =
                    server.getExperimentFileAttachment(getSessionToken(request),
                            experimentIdentifier, fileName, version);
            byte[] value = experimentAttachment.getAttachmentContent().getValue();
            final String attachmentFileName = experimentAttachment.getFileName();
            return new FileContent(value, attachmentFileName);
        } else
        {
            return null;
        }
    }
}
