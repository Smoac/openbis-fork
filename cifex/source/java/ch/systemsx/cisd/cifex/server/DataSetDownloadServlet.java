/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.UserFailureException;
import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.client.dto.File;

/**
 * The <code>AbstractCIFEXServiceServlet</code> extension to download a data set.
 * 
 * @author Christian Ribeaud
 */
public final class DataSetDownloadServlet extends AbstractCIFEXServiceServlet
{

    private static final long serialVersionUID = 1L;

    //
    // AbstractCIFEXServiceServlet
    //

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        final String fileIdParameter = request.getParameter(Constants.FILE_ID_PARAMETER);
        if (StringUtils.isNotBlank(fileIdParameter))
        {
            try
            {
                final long fileId = Long.parseLong(fileIdParameter);
                final File file = cifexService.tryGetFile(fileId);

                // final byte[] value = experimentFileProperty.getValue();
                // response.setContentLength(value.length);
                // response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
                // final ServletOutputStream outputStream = response.getOutputStream();
                // outputStream.write(value);
                // outputStream.flush();
                // outputStream.close();
            } catch (final NumberFormatException ex)
            {
                throw new ServletException(String.format("Given file id '%s' is not a number.", fileIdParameter));
            } catch (final InvalidSessionException ex)
            {
                response.sendRedirect("/cifex");
            } catch (final UserFailureException ex)
            {
                operationLog.error(String.format("Download data set with id '%s' failed.", fileIdParameter), ex);
                throw new ServletException(ex);
            }
        }
    }
}
