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

package ch.systemsx.cisd.cifex.server;

import javax.servlet.http.HttpServletRequest;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractFileUploadServlet extends AbstractCIFEXServiceServlet
{
    private static final long serialVersionUID = 1L;

    /*
     * Keep in mind that this constant is used by <code>FileUploadWidget</code> to check if upload
     * was successful, so if you change the value of the constant here it should also be changed in
     * the widget.
     */
    public static final int MAX_FILENAME_LENGTH = 250;

    protected final static String RECIPIENTS_FIELD_NAME = "email-addresses";

    protected final static String COMMENT_FIELD_NAME = "upload-comment";

    protected static final long MB = 1024 * 1024;
    
    protected long getMaxUploadSize(UserDTO user)
    {
        Long sizeInMB = user.getMaxUploadRequestSizeInMB();
        if (sizeInMB == null)
        {
            return domainModel.getBusinessContext().getMaxUploadRequestSizeInMB() * MB;
        }
        return sizeInMB.longValue() * MB;
    }

    protected String getURLForEmail(final HttpServletRequest request)
    {
        return HttpUtils.getURLForEmail(request, domainModel.getBusinessContext());
    }
}
