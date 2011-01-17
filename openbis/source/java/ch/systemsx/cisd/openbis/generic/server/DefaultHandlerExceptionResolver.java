/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Handler for uncaught exceptions propagated to the Spring {@link DispatcherServlet}.
 * Please note, that this handler is executed in extremely rare cases, where GWT-specific exception
 * translators do not kick-in. Uploading files is an example for such a rare occasion.
 * <p>
 * Exceptions of type {@link MaxUploadSizeExceededException} are recognized by the implementation
 * and result in a nicely formatted error message.
 * 
 * @author Kaloyan Enimanev
 */
public class DefaultHandlerExceptionResolver implements HandlerExceptionResolver
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DefaultHandlerExceptionResolver.class);

    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Unhandled exception caught :", ex);
        }

        response.reset();
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        try
        {
            PrintWriter writer = new PrintWriter(response.getWriter());
            writer.write(getErrorMessage(ex));
            writer.flush();
            writer.close();
        } catch (IOException ioex)
        {
            // socket error ? - nothing to do except for logging
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("I/O error while processing unhandled exception :", ioex);
            }
        }

        // null, because the error is already handled above
        return null;
    }

    private String getErrorMessage(Exception ex)
    {
        if (ex instanceof MaxUploadSizeExceededException)
        {
            MaxUploadSizeExceededException musee = ((MaxUploadSizeExceededException) ex);
            String maxSize = FileUtilities.byteCountToDisplaySize(musee.getMaxUploadSize());
            String errorMessage = String.format("Maximum file upload size (%s) exceeded.", maxSize);
            return errorMessage;
        }
        return ex.getMessage();
    }

}
