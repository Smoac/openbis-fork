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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * An <code>IDirectoryRenderer</code> implementation which renders on HTML pages.
 * 
 * @author Franz-Josef Elmer
 */
final class HTMLDirectoryRenderer implements IDirectoryRenderer
{
    private static final Template SAMPLE_DATASET_DESCRIPTION_TEMPLATE =
            new Template("${group}/${project}/${experiment}/${sample}/${dataset}");

    private static final Template EXPERIMENT_DATASET_DESCRIPTION_TEMPLATE =
            new Template("${group}/${project}/${experiment}/${dataset}");

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
                    + ".wrapper { min-height: 100%; height: auto; margin: 0em auto -4em; }"
                    + ".footer { height: 4em; text-align: center; }" + "</style>";

    private static final Template ROW_TEMPLATE =
            new Template(
                    "<tr><td class='td_file'><a href='${path}'>${name}</td><td>${size}</td></tr>");

    private static final Template HEADER_TEMPLATE =
            new Template(
                    "<html><head><title>Data Set Download Service: ${dataset-description}</title>"
                            + CSS
                            + "</head>"
                            + "<body><div class='wrapper'><h1>"
                            + "Data Set Download Service"
                            + "</h1>"
                            + "<div class='div_hd'>Information about data set</div>"
                            + "<table>"
                            + "<tr><td class='td_hd'>Space:</td><td>${group}</td></tr>"
                            + "<tr><td class='td_hd'>Project:</td><td>${project}</td></tr>"
                            + "<tr><td class='td_hd'>Experiment:</td><td>${experiment}</td></tr>"
                            + "${sampleLine}"
                            + "<tr><td class='td_hd'>Data Set Code:</td><td>${dataset}</td></tr></table> "
                            + "<div class='div_hd'>Files</div>" + "<table> " + "${folder}" + "");

    private static final Template SAMPLE_LINE_TEMPLATE =
            new Template("<tr><td class='td_hd'>Sample:</td><td>${sample}</td></tr>");

    private static final Template FOOTER_TEMPLATE =
            new Template("</table> <div class='footer'>${footer} </div> </div> </body></html>");

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

    public void printHeader(final ExternalData dataSet)
    {
        final String datasetCode = dataSet.getCode();
        final String sampleCode = dataSet.getSampleCode();
        final Experiment experiment = dataSet.getExperiment();
        final String experimentCode = experiment.getCode();
        final Project project = experiment.getProject();
        final String projectCode = project.getCode();
        final Space space = project.getSpace();
        final String groupCode = space.getCode();
        final Template template = HEADER_TEMPLATE.createFreshCopy();
        template.bind("dataset-description", renderDataSetDescription(dataSet));
        template.bind("group", groupCode);
        template.bind("project", projectCode);
        template.bind("experiment", experimentCode);
        template.bind("sampleLine", createSampleLine(sampleCode));
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

    private String renderDataSetDescription(ExternalData dataSet)
    {
        String sampleCode = dataSet.getSampleCode();
        Template template;
        if (sampleCode != null)
        {
            template = SAMPLE_DATASET_DESCRIPTION_TEMPLATE.createFreshCopy();
            template.bind("sample", sampleCode);
        } else
        {
            template = EXPERIMENT_DATASET_DESCRIPTION_TEMPLATE.createFreshCopy();
        }
        Experiment experiment = dataSet.getExperiment();
        template.bind("experiment", experiment.getCode());
        Project project = experiment.getProject();
        template.bind("project", project.getCode());
        template.bind("group", project.getSpace().getCode());
        template.bind("dataset", dataSet.getCode());
        return template.createText();
    }

    private String createSampleLine(final String sampleCode)
    {
        if (sampleCode == null)
        {
            return "";
        }
        Template sampleLineTemplate = SAMPLE_LINE_TEMPLATE.createFreshCopy();
        sampleLineTemplate.bind("sample", sampleCode);
        return sampleLineTemplate.createText();
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
            return URLEncoder.encode(url, "UTF-8").replace("%2F", "/");
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
