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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProcedurePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * An <code>IDirectoryRenderer</code> implementation which renders on HTML pages.
 * 
 * @author Franz-Josef Elmer
 */
final class HTMLDirectoryRenderer implements IDirectoryRenderer
{
    private static final String DATASET_DESCRIPTION =
            "${group}/${project}/${experiment}/${sample}/${dataset}";

    private static final String DATASET_DOWNLOAD_SERVICE = "Data Set Download Service";

    private static final String TITLE =
            "<title> " + DATASET_DOWNLOAD_SERVICE + ": " + DATASET_DESCRIPTION + "</title>";

    private static final String CSS =
            "<style type='text/css'> "
                    + "* { margin: 3px; }"
                    + "html { height: 100%;  }"
                    + "body { height: 100%; font-family: verdana, tahoma, helvetica; font-size: 11px; text-align:left; }"
                    + "h1 { text-align: center; padding: 1em; color: #1E4E8F;}"
                    + ".td_hd { border: 1px solid #FFFFFF; padding 3px; background-color: #DDDDDD; height: 1.5em; }"
                    + ".div_hd { background-color: #1E4E8F; color: white; font-weight: bold; padding: 3px; }"
                    + "table { border-collapse: collapse; padding: 1em; }"
                    + "tr, td { font-family: verdana, tahoma, helvetica; font-size: 11px; }"
                    + ".td_file { font-family: verdana, tahoma, helvetica; font-size: 11px; height: 1.5em }"
                    + ".wrapper { min-height: 100%; height: auto !important; height: 100%; margin: 0em auto -4em; }"
                    + ".footer { height: 4em; text-align: center; }" + "</style>";

    private static final Template ROW_TEMPLATE =
            new Template(
                    "<tr><td class='td_file'><a href='${path}'>${name}</td><td>${size}</td></tr>");

    private static final Template HEADER_TEMPLATE =
            new Template("<html><head>" + TITLE + CSS + "</head>"
                    + "<body><div class='wrapper'><h1>" + DATASET_DOWNLOAD_SERVICE + "</h1>"
                    + "<div class='div_hd'>Information about data set</div>" + "<table>"
                    + "<tr><td class='td_hd'>Group:</td><td>${group}</td></tr>"
                    + "<tr><td class='td_hd'>Project:</td><td>${project}</td></tr>"
                    + "<tr><td class='td_hd'>Experiment:</td><td>${experiment}</td></tr>"
                    + "<tr><td class='td_hd'>Sample:</td><td>${sample}</td></tr>"
                    + "<tr><td class='td_hd'>Data Set Code:</td><td>${dataset}</td></tr></table> "
                    + "<div class='div_hd'>Files</div>" + "<table> " + "${folder}" + "");

    private static final Template FOOTER_TEMPLATE =
            new Template("</table> </div> <div class='footer'>${footer} </div> </body></html>");

    private PrintWriter writer;

    private final String urlPrefix;

    private final String relativePathOrNull;

    HTMLDirectoryRenderer(final RenderingContext context)
    {
        this.relativePathOrNull = context.getRelativePathOrNull();
        final String prefix = context.getUrlPrefix();
        this.urlPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
    }

    public void setWriter(final PrintWriter writer)
    {
        this.writer = writer;
    }

    public void printHeader(final ExternalDataPE dataSet)
    {
        final String datasetCode = dataSet.getCode();
        final String sampleCode = dataSet.getAssociatedSampleCode();
        final ProcedurePE procedure = dataSet.getProcedure();
        final ExperimentPE experiment = procedure.getExperiment();
        final String experimentCode = experiment.getCode();
        final ProjectPE project = experiment.getProject();
        final String projectCode = project.getCode();
        final GroupPE group = project.getGroup();
        final String groupCode = group.getCode();
        final Template template = HEADER_TEMPLATE.createFreshCopy();
        template.bind("group", groupCode);
        template.bind("project", projectCode);
        template.bind("experiment", experimentCode);
        template.bind("sample", sampleCode);
        template.bind("dataset", datasetCode);
        if (StringUtils.isNotBlank(relativePathOrNull))
        {
            template.bind("folder", "<tr><td class='td_hd'>Folder:</td><td>" + relativePathOrNull
                    + "</td></tr>");
        } else
        {
            template.bind("folder", "");
        }
        writer.println(template.createText());
    }

    public void printLinkToParentDirectory(final String relativePath)
    {
        printRow("..", relativePath, "");
    }

    public void printDirectory(final String name, final String relativePath)
    {
        printRow(name, relativePath, "");
    }

    public void printFile(final String name, final String relativePath, final long size)
    {
        printRow(name, relativePath, renderFileSize(size));
    }

    private void printRow(final String name, final String relativePath, final String fileSize)
    {
        final Template template = ROW_TEMPLATE.createFreshCopy();
        template.bind("path", urlPrefix + encodeURL(relativePath));
        template.bind("name", name);
        template.bind("size", fileSize);
        writer.println(template.createText());
    }

    private String encodeURL(final String url)
    {
        try
        {
            return URLEncoder.encode(url, "UTF-8");
        } catch (final UnsupportedEncodingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private final static String renderFileSize(final long size)
    {
        return FileUtils.byteCountToDisplaySize(size);
    }

    public void printFooter()
    {
        final Template template = FOOTER_TEMPLATE.createFreshCopy();
        template
                .bind("footer",
                        "Copyright &copy; 2008 ETHZ - <a href='http://www.cisd.systemsx.ethz.ch/'>CISD</a>");
        writer.println(template.createText());
    }

}
