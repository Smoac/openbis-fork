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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchOperationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Serves the template files for entity batch registration.
 * 
 * @author Izabela Adamczyk
 */
@Controller
@RequestMapping(
    { "/template-download", "/openbis/template-download" })
public class FileTemplateServiceServlet extends AbstractFileDownloadServlet
{
    @Resource(name = ResourceNames.COMMON_SERVICE)
    private ICommonClientService service;

    @Override
    protected FileContent getFileContent(final HttpServletRequest request) throws Exception
    {
        final String kind = request.getParameter(GenericConstants.ENTITY_KIND_KEY_PARAMETER);
        final String type = request.getParameter(GenericConstants.ENTITY_TYPE_KEY_PARAMETER);
        final String autoGenerate = request.getParameter(GenericConstants.AUTO_GENERATE);
        final String withExperimentsParameter =
                request.getParameter(GenericConstants.WITH_EXPERIMENTS);
        final boolean withExperiments =
                withExperimentsParameter != null && Boolean.parseBoolean(withExperimentsParameter) ? true
                        : false;
        final String operationKindParameter =
                request.getParameter(GenericConstants.BATCH_OPERATION_KIND);
        final BatchOperationKind operationKind = BatchOperationKind.valueOf(operationKindParameter);
        if (StringUtils.isNotBlank(kind) && StringUtils.isNotBlank(type))
        {
            String fileContent =
                    service.getTemplate(EntityKind.valueOf(kind), type, Boolean
                            .parseBoolean(autoGenerate), withExperiments, operationKind);
            byte[] value = fileContent.getBytes();
            String fileName = kind + "-" + type + "-template.tsv";
            return new FileContent(value, fileName);
        } else
        {
            return null;
        }
    }

}
