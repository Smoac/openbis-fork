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

package ch.systemsx.cisd.openbis.datasetdownload;

import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.lims.base.ExternalData;

/**
 * Factory for rendering file system view in plain text.
 * 
 * @author Tomasz Pylak
 */
public class PlainTextRendererFactory implements IRendererFactory
{

    public IDirectoryRenderer createDirectoryRenderer(RenderingContext context)
    {
        return new PlainTextDirectoryRenderer();
    }

    public IErrorRenderer createErrorRenderer()
    {
        return new IErrorRenderer()
            {
                private PrintWriter writer;

                public void printErrorMessage(String errorMessage)
                {
                    writer.println(errorMessage);
                }

                public void setWriter(PrintWriter writer)
                {
                    this.writer = writer;
                }
            };
    }

    public String getContentType()
    {
        return "text";
    }

    private static class PlainTextDirectoryRenderer implements IDirectoryRenderer
    {
        private PrintWriter writer;

        public String getContentType()
        {
            return "text";
        }

        public void printDirectory(String name, String relativePath)
        {
            writer.print(name + "\n");
        }

        public void printFile(String name, String relativePath, long size)
        {
            writer.format("%s\t%s\n", name, FileUtils.byteCountToDisplaySize(size));
        }

        public void printFooter()
        {
        }

        public void printHeader(ExternalData dataSet)
        {
        }

        public void printLinkToParentDirectory(String relativePath)
        {
        }

        public void setWriter(PrintWriter writer)
        {
            this.writer = writer;
        }

    }
}
