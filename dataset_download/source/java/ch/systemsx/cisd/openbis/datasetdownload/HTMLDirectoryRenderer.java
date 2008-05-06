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
import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.lims.base.ExternalData;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class HTMLDirectoryRenderer implements IDirectoryRenderer
{
    private static final DecimalFormat FORMAT_KB = new DecimalFormat("0.0 KB");
    private static final DecimalFormat FORMAT_MB = new DecimalFormat("0.0 MB");
    private static final DecimalFormat FORMAT_GB = new DecimalFormat("0.0 GB");

    private static final int UNIT_KB = 1024;

    private static final int UNIT_MB = UNIT_KB * UNIT_KB;

    private static final int UNIT_GB = UNIT_MB * UNIT_KB;

    private static final Template ROW_TEMPLATE 
        = new Template("<tr><td><a href='${path}'>${name}</td><td>${size}</td></tr>");
    
    private PrintWriter writer;

    private final String urlPrefix;

    private final String relativePathOrNull;

    HTMLDirectoryRenderer(RenderingContext context)
    {
        this.relativePathOrNull = context.getRelativePathOrNull();
        String prefix = context.getUrlPrefix();
        this.urlPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
    }

    public void setWriter(PrintWriter writer)
    {
        this.writer = writer;
    }

    public String getContentType()
    {
        return "text/html";
    }

    public void printHeader(ExternalData dataSet)
    {
        writer.println("<html><body>");
        writer.println("<h1>Data Set " + dataSet.getCode() + "</h1>");
        if (StringUtils.isNotBlank(relativePathOrNull))
        {
            writer.println("Folder: " + relativePathOrNull);
        }
        writer.println("<table border='0' cellpadding='5' cellspacing='0'>");
    }
    
    public void printLinkToParentDirectory(String relativePath)
    {
        printRow("..", relativePath, "");
    }

    public void printDirectory(String name, String relativePath)
    {
        printRow(name, relativePath, "");
    }
    
    public void printFile(String name, String relativePath, long size)
    {
        printRow(name, relativePath, renderFileSize(size));
    }

    private void printRow(String name, String relativePath, String fileSize)
    {
        Template template = ROW_TEMPLATE.createFreshCopy();
        template.bind("path", urlPrefix + relativePath);
        template.bind("name", name);
        template.bind("size", fileSize);
        writer.println(template.createText());
    }
    
    private String renderFileSize(long size)
    {
        if (size < 10 * UNIT_KB)
        {
            return Long.toString(size) + " Bytes";
        }
        if (size < 10 * UNIT_MB)
        {
            return FORMAT_KB.format(size / (double) UNIT_KB);
        }
        if (size < 10 * UNIT_GB)
        {
            return FORMAT_MB.format(size / (double) UNIT_MB);
        }
        return FORMAT_GB.format(size / (double) UNIT_GB);
    }

    public void printFooter()
    {
        writer.println("</table></body></html>");
    }

}
