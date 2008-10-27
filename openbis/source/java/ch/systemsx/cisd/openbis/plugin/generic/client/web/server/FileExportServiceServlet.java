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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CommonColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ParentColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyColumns;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * Servlet service providing <em>.tsv</em> file with samples.
 * 
 * @author Izabela Adamczyk
 */
@Controller
@RequestMapping(
    { "/file-export", "/genericopenbis/file-export" })
public final class FileExportServiceServlet implements InitializingBean,
        org.springframework.web.servlet.mvc.Controller
{
    @Resource(name = ResourceNames.GENERIC_SERVICE)
    private IGenericClientService service;

    public FileExportServiceServlet()
    {

    }

    private final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {

            final SampleRequestParameters parameters = new SampleRequestParameters(request);

            final CommonColumns commonColumns = new CommonColumns();
            final ParentColumns parentColumns = new ParentColumns();
            parentColumns.define(parameters.getSampleType());
            final PropertyColumns propertyColumns = new PropertyColumns();
            propertyColumns.define(parameters.getSampleType());

            final List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

            columns.addAll(commonColumns.getColumns());
            columns.addAll(parentColumns.getColumns());
            columns.addAll(propertyColumns.getColumns());

            final List<Sample> samples =
                    service.listSamples(createListCriteria(parameters), parameters
                            .getPropertyTypes());

            final List<SampleModel> sampleModels = new ArrayList<SampleModel>();
            for (final Sample s : samples)
            {
                sampleModels.add(new SampleModel(s));
            }

            writeFileResponse(response, parameters.getFileName(), createFile(sampleModels, columns));
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        }

    }

    private static ListSampleCriteria createListCriteria(SampleRequestParameters parameters)
    {
        ListSampleCriteria criteria = new ListSampleCriteria();
        criteria.setSampleType(parameters.getSampleType());
        criteria.setGroupCode(parameters.getGroup());
        criteria.setIncludeGroup(parameters.isIncludeGroup());
        criteria.setIncludeInstance(parameters.isIncludeShared());
        return criteria;
    }

    private final String createFile(final List<SampleModel> models, final List<ColumnConfig> columns)
    {

        final StringBuilder sb = new StringBuilder();
        List<String> lineColumns = new ArrayList<String>();
        for (final ColumnConfig cc : columns)
        {
            lineColumns.add(cc.getHeader());
        }
        sb.append(createLine(lineColumns, true));
        for (final SampleModel s : models)

        {
            lineColumns = new ArrayList<String>();
            for (final ColumnConfig cc : columns)
            {
                lineColumns.add(s.get(cc.getId()) != null ? s.get(cc.getId()).toString() : "");
            }
            sb.append(createLine(lineColumns, false));

        }
        return sb.toString();
    }

    private final String createLine(final List<String> columns, final boolean isHeader)
    {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final String c : columns)
        {
            if (first)
            {
                if (isHeader)
                {
                    sb.append("#");
                }
                first = false;
            } else
            {
                sb.append("\t");
            }
            sb.append(c);
        }
        sb.append("\n");
        return sb.toString();
    }

    private final void writeResponse(final HttpServletResponse response, final String value)
            throws IOException
    {
        final PrintWriter writer = response.getWriter();
        writer.write(value);
        writer.flush();
        writer.close();
    }

    private final void writeFileResponse(final HttpServletResponse response, final String fileName,
            final String fileContent) throws IOException
    {
        response.setHeader("Content-disposition", String
                .format("attachment; filename=%s", fileName));
        writeResponse(response, fileContent);
    }

    //
    // Controller
    //

    public final ModelAndView handleRequest(final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
    {
        doGet(request, response);
        return null;
    }

    //
    // InitializingBean
    //

    public final void afterPropertiesSet() throws Exception
    {
        LogInitializer.init();
        if (service == null)
        {
            throw new ServletException("The generic client service has not been set.");
        }
    }
}
