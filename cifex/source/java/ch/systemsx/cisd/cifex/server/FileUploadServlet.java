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

package ch.systemsx.cisd.cifex.server;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for uploading file into Cifex.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileUploadServlet extends AbstractCIFEXServiceServlet
{
    private static final long serialVersionUID = 1L;

    private String msg =
            "<response success=\"false\">\n" + "<errors>\n" + "<field>\n" + "<id>upload-file1</id>\n"
                    + "<msg>msg</msg>\n" + "</field>\n" + "</errors>\n" + "</response>";

    //
    // AbstractCIFEXServiceServlet
    //

    @Override
    protected final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        byte[] contents = msg.getBytes();

        // Set to expire far in the past.
        response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");

        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");

        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");

        response.setContentType("text/xml");

        response.setContentLength(contents.length);

        OutputStream os = response.getOutputStream();
        os.write(contents);
        os.flush();
        os.close();

        // final boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        // if (isMultipart == false)
        // {
        // operationLog.warn("Request is not a multipart content file upload: " + request.getRequestURI());
        // return;
        // }
        // try
        // {
        // final UserDTO user = getUserDTO(request);
        // final ServletFileUpload upload = new ServletFileUpload();
        // final FileItemIterator iter = upload.getItemIterator(request);
        // while (iter.hasNext())
        // {
        // FileItemStream item = iter.next();
        // InputStream stream = item.openStream();
        // if (item.isFormField() == false)
        // {
        // domainModel.getFileManager().saveFile(user, item.getName(), stream);
        // } else
        // {
        // // System.out.println("Form field " + item.getFieldName() + " with value " +
        // // Streams.asString(stream) + " detected.");
        // }
        // }
        // } catch (final InvalidSessionException ex)
        // {
        // response.sendRedirect("/cifex");
        // } catch (final FileUploadException ex)
        // {
        // // TODO Auto-generated catch block
        // ex.printStackTrace();
        // }
    }
}
