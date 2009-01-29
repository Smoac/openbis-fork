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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractFileUploadServlet extends AbstractCIFEXServiceServlet
{
    /*
     * Keep in mind that this constant is used by <code>FileUploadWidget</code> to check if upload
     * was successful, so if you change the value of the constant here it should also be changed in
     * the widget.
     */
    public static final int MAX_FILENAME_LENGTH = 250;
    
    protected final static String RECIPIENTS_FIELD_NAME = "email-addresses";

    protected final static String COMMENT_FIELD_NAME = "upload-comment";

    private static final String MAX_UPLOAD_SIZE = "max-upload-size";

    /**
     * The maximum allow upload size (in bytes).
     */
    protected long maxUploadSizeInBytes;

    private final long getMaxUploadSizeInMegabytes()
    {
        final String value = serviceProperties.getProperty(MAX_UPLOAD_SIZE);
        long longValue = -1;
        if (StringUtils.isNotBlank(value))
        {
            try
            {
                longValue = Long.parseLong(value);
            } catch (final NumberFormatException e)
            {
            }
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Maximum upload size set to %d megabytes (-1 means no limit).", longValue));
        }
        return longValue;
    }

    @Override
    protected final void postInitialization()
    {
        maxUploadSizeInBytes = getMaxUploadSizeInMegabytes() * FileUtils.ONE_MB;
    }

    protected String getURLForEmail(final HttpServletRequest request)
    {
        final String overrideURL = domainModel.getBusinessContext().getOverrideURL();
        if (StringUtils.isBlank(overrideURL))
        {
            return HttpUtils.getBasicURL(request);
        } else
        {
            return overrideURL;
        }
    }


}
