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
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.server.business.IFileManager.FileOutput;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

/**
 * The <code>AbstractCIFEXServiceServlet</code> extension to download a data set.
 * 
 * @author Christian Ribeaud
 */
public final class FileDownloadServlet extends AbstractCIFEXServiceServlet
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
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try
            {
                final long fileId = Long.parseLong(fileIdParameter);
                final FileOutput fileOutput = domainModel.getFileManager().getFile(getUserDTO(request), fileId);
                if (fileOutput.length <= Integer.MAX_VALUE)
                {
                    response.setContentLength((int) fileOutput.length);
                }
                response.setHeader("Content-Disposition", "inline; filename=" + fileOutput.name);
                inputStream = fileOutput.inputStream;
                outputStream = response.getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            } catch (final NumberFormatException ex)
            {
                throw new ServletException(String.format("Given file id '%s' is not a number.", fileIdParameter));
            } catch (final InvalidSessionException ex)
            {
                // TODO 2008-01-27, Christian Ribeaud: should write out javascript which looks for the window.opener and
                // redirect it to login page.
            } finally
            {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }
}
