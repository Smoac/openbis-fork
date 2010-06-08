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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.attachment;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * Utility class for downloading attachments.
 * 
 * @author Piotr Buczek
 */
public class AttachmentDownloadHelper
{
    public static void download(String fileName, int version, IAttachmentHolder holder)
    {
        WindowUtils.openWindow(createURL(version, fileName, holder));
    }

    private final static String createURL(final int version, final String fileName,
            final IAttachmentHolder attachmentHolder)
    {
        URLMethodWithParameters methodWithParameters =
                new URLMethodWithParameters(GenericConstants.ATTACHMENT_DOWNLOAD_SERVLET_NAME);
        methodWithParameters.addParameter(GenericConstants.VERSION_PARAMETER, version);
        methodWithParameters.addParameter(GenericConstants.FILE_NAME_PARAMETER, fileName);
        methodWithParameters.addParameter(GenericConstants.ATTACHMENT_HOLDER_PARAMETER,
                attachmentHolder.getAttachmentHolderKind().name());
        // NOTE: this exp.getId() could be null if exp is a proxy
        methodWithParameters.addParameter(GenericConstants.TECH_ID_PARAMETER, attachmentHolder
                .getId());
        return methodWithParameters.toString();
    }
}
