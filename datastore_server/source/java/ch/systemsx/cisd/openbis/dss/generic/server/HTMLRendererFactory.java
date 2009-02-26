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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.PrintWriter;

/**
 * Factory for rendering file system view in HTML.
 *
 * @author Franz-Josef Elmer
 */
public class HTMLRendererFactory implements IRendererFactory
{
    public String getContentType()
    {
        return "text/html";
    }


    public IDirectoryRenderer createDirectoryRenderer(RenderingContext context)
    {
        return new HTMLDirectoryRenderer(context);
    }

    public IErrorRenderer createErrorRenderer()
    {
        return new HTMLErrorRenderer();
    }

    private static class HTMLErrorRenderer implements IErrorRenderer
    {
        private PrintWriter writer;

        public void setWriter(PrintWriter writer)
        {
            this.writer = writer;
        }
        
        public void printErrorMessage(String errorMessage)
        {
            writer.println("<html><body><h1>Error</h1>");
            writer.println(errorMessage);
            writer.println("</body></html>");
        }

    }
}
