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

import ch.systemsx.cisd.cifex.server.business.FileInformation;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.dto.FileContent;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.exception.InvalidSessionException;
import ch.systemsx.cisd.common.exception.UserFailureException;

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
            throws ServletException, IOException, InvalidSessionException
    {
        final IUserActionLog log = domainModel.getBusinessContext().getUserActionLogHttp();
        final UserDTO requestUser = getUserDTO(request); // Throws exception if session is not
        // valid.
        final String fileIdParameter = request.getParameter(Constants.FILE_ID_PARAMETER);
        if (StringUtils.isNotBlank(fileIdParameter))
        {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try
            {
                final long fileId = Long.parseLong(fileIdParameter);
                final IFileManager fileManager = domainModel.getFileManager();
                final FileInformation fileInfo = fileManager.getFileInformation(fileId);
                if (fileInfo.isFileAvailable() == false)
                {
                    log.logDownloadFileFailedNotFound(fileInfo.getFileDTO());
                    throw new UserFailureException(fileInfo.getErrorMessage());
                }
                if (fileManager.isAllowedAccess(requestUser, fileInfo.getFileDTO()) == false)
                {
                    log.logDownloadFileFailedNotAuthorized(fileInfo.getFileDTO());
                    // Note: we send back the exact same error message as for a file that cannot
                    // be found.
                    // We do not want to give information out on whether the file exists or not.
                    throw UserFailureException.fromTemplate(Constants
                            .getErrorMessageForFileNotFound(fileId));
                }
                final FileContent fileOutput = fileManager.getFileContent(fileInfo.getFileDTO());
                final Long size = fileOutput.getBasicFile().getSize();
                if (size != null)
                {
                    if (size <= Integer.MAX_VALUE)
                    {
                        response.setContentLength(size.intValue());
                    } else
                    {
                        response.addHeader("Content-Length", Long.toString(size));
                    }
                }
                response.setContentType("application/x-unknown");
                response.setHeader("Content-Disposition", "attachment; filename=\""
                        + fileOutput.getBasicFile().getName() + "\"");
                inputStream = fileOutput.getInputStream();
                outputStream = response.getOutputStream();
                log.logDownloadFileStart(fileInfo.getFileDTO(), 0L);
                boolean success = false;
                try
                {
                    IOUtils.copy(inputStream, outputStream);
                    success = true;
                } finally
                {
                    log.logDownloadFileFinished(fileInfo.getFileDTO(), success);
                }
            } catch (final NumberFormatException ex)
            {
                throw new ServletException(String.format("Given file id '%s' is not a number.",
                        fileIdParameter), ex);
            } catch (final InvalidSessionException ex)
            {
                throw new ServletException(ex.getMessage(), ex);
            } catch (final UserFailureException ex)
            {
                operationLog.error(String.format("Problem while accessing file id '%s'.",
                        fileIdParameter), ex);
                throw new ServletException(ex.getMessage(), ex);
            } finally
            {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }
}
