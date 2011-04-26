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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.ftpserver.ftplet.FtpFile;

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpServerConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolver;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * Resolves paths like
 * /<space-code>/<project-code>/<experiment-code>/<dataset-template>[/<sub-path>]*
 * <p>
 * Subpaths are resolved as a relative paths starting from the root of a dataset.
 * <p>
 * TODO KE: add disambiguation checks - append an uniqueness factor to (?another variable) to the
 * path if no "dataSetCode" variable is used in the template
 * 
 * @author Kaloyan Enimanev
 */
public class TemplateBasedDataSetResourceResolver implements IFtpPathResolver, IExperimentChildrenLister
{
    private static final String DATA_SET_CODE_VARNAME = "dataSetCode";

    private static final String DATA_SET_TYPE_VARNAME = "dataSetType";

    private static final String DATA_SET_DATE_VARNAME = "dataSetDate";

    private static final String FILE_NAME_VARNAME = "fileName";

    private static final String DATA_SET_DATE_FORMAT = "yyyy-MM-dd-HH-mm";

    /**
     * a template, that can contain special variables.
     * 
     * @see #evaluateTemplate(ExternalData, String) to find out what variables are understood and
     *      interpreted.
     */
    private final String template;

    private static class DataSetAndFileName
    {
        ExternalData dataSet;

        // will only be filled when the ${fileName} variable
        // is used in the template
        String fileName = StringUtils.EMPTY;
    }

    public TemplateBasedDataSetResourceResolver(FtpServerConfig ftpServerConfig)
    {
        this.template = ftpServerConfig.getDataSetDisplayTemplate();
    }

    /**
     * @return <code>true</code> for paths containing at least 4 nested directory levels.
     */
    public boolean canResolve(String path)
    {
        int nestedLevels = StringUtils.countMatches(path, FtpConstants.FILE_SEPARATOR);
        return nestedLevels >= 4;
    }

    public FtpFile resolve(String path, final FtpPathResolverContext resolverContext)
    {
        IETLLIMSService service = resolverContext.getService();
        String sessionToken = resolverContext.getSessionToken();

        DataSetAndFileName dataSetAndFileName =
                extractDataSetAndFileName(path, service, sessionToken);
        if (dataSetAndFileName == null)
        {
            return null;
        }
        
        String nestedSubPath = extractNestedSubPath(path);
        String relativePath =
                dataSetAndFileName.fileName + FtpConstants.FILE_SEPARATOR + nestedSubPath;

        IHierarchicalContentProvider provider = ServiceProvider.getHierarchicalContentProvider();
        IHierarchicalContent content =
                provider.asContent(dataSetAndFileName.dataSet.getDataSetCode());
        IHierarchicalContentNode contentNode =
                StringUtils.isBlank(relativePath) ? content.getRootNode() : content
                        .getNode(relativePath);
        return new HierarchicalContentToFtpFileAdapter(path, contentNode);
    }

    private DataSetAndFileName extractDataSetAndFileName(String path, IETLLIMSService service,
            String sessionToken)
    {
        String experimentId = extractExperimentIdentifier(path);
        Experiment experiment = extractExperiment(experimentId, service, sessionToken);
        if (experiment == null)
        {
            // cannot resolve an existing experiment from the specified path
            return null;
        }
        List<ExternalData> dataSets =
                service.listDataSetsByExperimentID(sessionToken, new TechId(experiment));
        String pathWithEndSlash = path + FtpConstants.FILE_SEPARATOR;
        for (ExternalData dataSet : dataSets)
        {
            Map<String /* fileName */, String /* evaluated */> evaluatedPaths =
                    getEvaluatedDataSetPaths(dataSet);
            for (Entry<String, String> entry : evaluatedPaths.entrySet())
            {
                String fullEvaluatedPath =
                        experimentId + FtpConstants.FILE_SEPARATOR + entry.getValue()
                                + FtpConstants.FILE_SEPARATOR;
                if (pathWithEndSlash.startsWith(fullEvaluatedPath))
                {
                    DataSetAndFileName result = new DataSetAndFileName();
                    result.dataSet = dataSet;
                    result.fileName = entry.getKey();
                    return result;
                }
            }

        }

        return null;
    }

    /**
     * @return the nested path within the data set (i.e. under the dataset directory level)
     */
    private String extractNestedSubPath(String path)
    {
        String[] levels = StringUtils.split(path, FtpConstants.FILE_SEPARATOR);
        if (levels.length > 4) {
            return StringUtils.join(levels, FtpConstants.FILE_SEPARATOR, 4, levels.length);
        } else {
            return StringUtils.EMPTY;
        }
    }

    private Experiment extractExperiment(String experimentId, IETLLIMSService service,
            String sessionToken)
    {
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(experimentId).createIdentifier();

        return service.tryToGetExperiment(sessionToken, experimentIdentifier);
    }

    private String extractExperimentIdentifier(String path)
    {
        String[] levels = StringUtils.split(path, FtpConstants.FILE_SEPARATOR);
        String experimentId =
                FtpConstants.ROOT_DIRECTORY
                        + StringUtils.join(levels, FtpConstants.FILE_SEPARATOR, 0, 3);
        return experimentId;
    }

    public List<String> listExperimentChildrenPaths(Experiment experiment, FtpPathResolverContext context)
    {
        IETLLIMSService service = context.getService();
        String sessionToken = context.getSessionToken();

        List<ExternalData> dataSets = service.listDataSetsByExperimentID(sessionToken, new TechId(experiment));
        List<String> result = new ArrayList<String>();
        for (ExternalData dataSet : dataSets)
        {
            Map<String, String> pathsForDataSet = getEvaluatedDataSetPaths(dataSet);
            result.addAll(pathsForDataSet.values());
        }
        return result;
    }

    private Map<String /* fileName */, String /* evaluated template */> getEvaluatedDataSetPaths(
            ExternalData dataSet)
    {
        Map<String, String> result = new HashMap<String, String>();
        
        IHierarchicalContentProvider provider = ServiceProvider.getHierarchicalContentProvider();
        IHierarchicalContent content = provider.asContent(dataSet.getCode());
        IHierarchicalContentNode rootNode = content.getRootNode();

        for (String fileName : extractFileNames(rootNode))
        {
            String path = evaluateTemplate(dataSet, fileName);
            result.put(fileName, path);
        }
        
        return result;
    }

    /**
     * @return all values to be used when evaluating the template "${fileName}" variable.
     */
    private List<String> extractFileNames(IHierarchicalContentNode rootNode)
    {
        if (rootNode.isDirectory())
        {
            List<String> childrenFileNames = new ArrayList<String>();
            for (IHierarchicalContentNode child : rootNode.getChildNodes())
            {
                childrenFileNames.add(child.getName());
            }
            return childrenFileNames;
        } else
        {
            return Collections.singletonList(StringUtils.EMPTY);
        }
    }

    public String evaluateTemplate(ExternalData dataSet,
            String fileName)
    {
        ExtendedProperties properties = new ExtendedProperties();
        properties.put(DATA_SET_CODE_VARNAME, dataSet.getCode());
        properties.put(DATA_SET_TYPE_VARNAME, dataSet.getDataSetType().getCode());
        String dataSetDate = extractDateValue(dataSet.getRegistrationDate());
        properties.put(DATA_SET_DATE_VARNAME, dataSetDate);
        properties.put(FILE_NAME_VARNAME, fileName);

        String templatePropName = "template";
        properties.put(templatePropName, template);
        return properties.getProperty(templatePropName);
    }


    private String extractDateValue(Date dataSetDate)
    {
        return DateFormatUtils.format(dataSetDate, DATA_SET_DATE_FORMAT);
    }


}
