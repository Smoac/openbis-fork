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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.io.hierarchical_content.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpConstants;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpFileFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpPathResolverContext;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.FtpServerConfig;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.IFtpPathResolver;
import ch.systemsx.cisd.openbis.dss.generic.server.ftp.resolver.FtpFileEvaluationContext.EvaluatedElement;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * Resolves paths like
 * "/<space-code>/<project-code>/<experiment-code>/<dataset-template>[/<sub-path>]*" to
 * {@link FtpFile} objects.
 * <p>
 * Subpaths are resolved as relative paths starting from the root of a dataset.
 * <p>
 * 
 * @author Kaloyan Enimanev
 */
public class TemplateBasedDataSetResourceResolver implements IFtpPathResolver,
        IExperimentChildrenLister
{
    private static final String DATA_SET_CODE_VARNAME = "dataSetCode";

    private static final String DATA_SET_TYPE_VARNAME = "dataSetType";

    private static final String DATA_SET_DATE_VARNAME = "dataSetDate";

    private static final String FILE_NAME_VARNAME = "fileName";

    private static final String DISAMBIGUATION_VARNAME = "disambiguation";

    private static final String DATA_SET_DATE_FORMAT = "yyyy-MM-dd-HH-mm";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            TemplateBasedDataSetResourceResolver.class);

    /**
     * helper class holding the configuration properties for a data set type.
     */
    private static class DataSetTypeConfig
    {
        /**
         * a regex specifying the root of the listable hierarchy within a data set.
         */
        String fileListSubPath = StringUtils.EMPTY;

        /**
         * a filter to be applied to files within a certain data set type.
         */
        IHierarchicalContentNodeFilter fileFilter = IHierarchicalContentNodeFilter.MATCH_ALL;
    }

    /**
     * a template, that can contain special variables.
     * 
     * @see #evaluateTemplate(ExternalData, String) to find out what variables are understood and
     *      interpreted.
     */
    private final String template;

    private final Map<String /* dataset type */, DataSetTypeConfig> dataSetTypeConfigs;

    private final DataSetTypeConfig defaultDSTypeConfig;

    public TemplateBasedDataSetResourceResolver(FtpServerConfig ftpServerConfig)
    {
        this.template = ftpServerConfig.getDataSetDisplayTemplate();
        this.dataSetTypeConfigs = initializeDataSetTypeConfigs(ftpServerConfig);
        this.defaultDSTypeConfig = new DataSetTypeConfig();

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
        String experimentId = extractExperimentIdFromPath(path);
        FtpFileEvaluationContext evalContext =
                evaluateExperimentDataSets(experimentId, resolverContext);
        if (evalContext == null)
        {
            return null;
        }

        try
        {
            return extractMatchingFileOrNull(path, experimentId, evalContext);
        } finally
        {
            evalContext.close();
        }
    }

    private FtpFile extractMatchingFileOrNull(String path, String experimentId,
            FtpFileEvaluationContext evalContext)
    {
        EvaluatedElement matchingElement =
                tryFindMatchingEvalElement(path, experimentId, evalContext);

        String pathInDataSet = extractPathInDataSet(path);
        String hierarchicalNodePath =
                constructHierarchicalNodePath(matchingElement.pathInDataSet, pathInDataSet);

        ExternalData dataSet = matchingElement.dataSet;
        IHierarchicalContentNodeFilter fileFilter = getFileFilter(dataSet);
        IHierarchicalContent content = evalContext.getHierarchicalContent(dataSet);
        IHierarchicalContentNode contentNode = content.getNode(hierarchicalNodePath);
        if (fileFilter.accept(contentNode))
        {
            return FtpFileFactory.createFtpFile(dataSet.getCode(), path, contentNode, fileFilter);
        } else
        {
            return null;
        }
    }

    private EvaluatedElement tryFindMatchingEvalElement(String path, String experimentId,
            FtpFileEvaluationContext evalContext)
    {
        String pathWithEndSlash = path + FtpConstants.FILE_SEPARATOR;
        for (EvaluatedElement evalElement : evalContext.getEvalElements())
        {
            String fullEvaluatedPath =
                    experimentId + FtpConstants.FILE_SEPARATOR + evalElement.evaluatedTemplate
                            + FtpConstants.FILE_SEPARATOR;
            if (pathWithEndSlash.startsWith(fullEvaluatedPath))
            {
                return evalElement;
            }
        }
        return null;
    }

    /**
     * @param path the path we are trying to resolve
     * @return the nested path within the data set (i.e. under the dataset directory level)
     */
    private String extractPathInDataSet(String path)
    {
        String[] levels = StringUtils.split(path, FtpConstants.FILE_SEPARATOR);
        if (levels.length > 4)
        {
            return StringUtils.join(levels, FtpConstants.FILE_SEPARATOR, 4, levels.length);
        } else
        {
            return StringUtils.EMPTY;
        }
    }

    private String constructHierarchicalNodePath(String relativePath, String pathInDataSet)
    {
        String result = relativePath;
        if (false == StringUtils.isBlank(pathInDataSet))
        {
            if (StringUtils.isBlank(relativePath))
            {
                result = pathInDataSet;
            } else
            {
                result += FtpConstants.FILE_SEPARATOR + pathInDataSet;
            }
        }
        return result;
    }

    private Experiment tryGetExperiment(String experimentId, IETLLIMSService service,
            String sessionToken)
    {
        ExperimentIdentifier experimentIdentifier =
                new ExperimentIdentifierFactory(experimentId).createIdentifier();

        Experiment result = null;
        try
        {
            result = service.tryToGetExperiment(sessionToken, experimentIdentifier);
        } catch (Throwable t)
        {
            operationLog.warn("Failed to get experiment with identifier :" + experimentId, t);
        }

        return result;
    }

    private String extractExperimentIdFromPath(String path)
    {
        String[] levels = StringUtils.split(path, FtpConstants.FILE_SEPARATOR);
        String experimentId =
                FtpConstants.ROOT_DIRECTORY
                        + StringUtils.join(levels, FtpConstants.FILE_SEPARATOR, 0, 3);
        return experimentId;
    }

    /**
     * @see IExperimentChildrenLister
     */
    public List<FtpFile> listExperimentChildrenPaths(Experiment experiment,
            final String parentPath, FtpPathResolverContext context)
    {
        IETLLIMSService service = context.getService();
        String sessionToken = context.getSessionToken();

        List<ExternalData> dataSets =
                service.listDataSetsByExperimentID(sessionToken, new TechId(experiment));

        FtpFileEvaluationContext evalContext = evaluateDataSetPaths(dataSets);
        try
        {
            return createFtpFilesFromEvaluationResult(parentPath, evalContext);
        } finally
        {
            evalContext.close();
        }
    }

    private List<FtpFile> createFtpFilesFromEvaluationResult(final String parentPath,
            FtpFileEvaluationContext evalResult)
    {
        ArrayList<FtpFile> result = new ArrayList<FtpFile>();
        for (EvaluatedElement evalElement : evalResult.getEvalElements())
        {
            IHierarchicalContentNodeFilter fileFilter = getFileFilter(evalElement.dataSet);
            if (fileFilter.accept(evalElement.contentNode))
            {
                String childPath =
                        parentPath + FtpConstants.FILE_SEPARATOR + evalElement.evaluatedTemplate;
                FtpFile childFtpFile =
                        FtpFileFactory.createFtpFile(evalElement.dataSet.getCode(), childPath,
                                evalElement.contentNode, fileFilter);
                result.add(childFtpFile);
            }
        }

        return result;
    }

    private FtpFileEvaluationContext evaluateExperimentDataSets(String experimentId,
            FtpPathResolverContext resolverContext)
    {
        IETLLIMSService service = resolverContext.getService();
        String sessionToken = resolverContext.getSessionToken();

        Experiment experiment = tryGetExperiment(experimentId, service, sessionToken);
        if (experiment == null)
        {
            // cannot resolve an existing experiment from the specified path
            return null;
        }
        List<ExternalData> dataSets =
                service.listDataSetsByExperimentID(sessionToken, new TechId(experiment));

        return evaluateDataSetPaths(dataSets);
    }

    private FtpFileEvaluationContext evaluateDataSetPaths(List<ExternalData> dataSets)
    {
        FtpFileEvaluationContext evalContext = new FtpFileEvaluationContext();

        for (int disambiguationIdx = 0; disambiguationIdx < dataSets.size(); disambiguationIdx++)
        {
            ExternalData dataSet = dataSets.get(disambiguationIdx);
            try
            {
                IHierarchicalContent hierarchicalContent =
                        evalContext.getHierarchicalContent(dataSet);
                IHierarchicalContentNode rootNode =
                        getDataSetFileListRoot(dataSet, hierarchicalContent);
                List<EvaluatedElement> paths =
                        evaluateDataSetPaths(dataSet, rootNode, disambiguationIdx);

                evalContext.addEvaluatedElements(paths);
            } catch (Throwable t)
            {
                operationLog.warn("Failed to evaluate data set paths for dataset "
                        + dataSet.getCode() + ": " + t.getMessage());
            }
        }

        return evalContext;
    }

    private List<EvaluatedElement> evaluateDataSetPaths(ExternalData dataSet,
            IHierarchicalContentNode rootNode, int disambiguationIndex)
    {
        List<EvaluatedElement> result = new ArrayList<EvaluatedElement>();
        String disambiguationVar = computeDisambiguation(disambiguationIndex);

        for (IHierarchicalContentNode fileNode : getFileNamesRequiredByTemplate(rootNode))
        {
            EvaluatedElement evalElement = new EvaluatedElement();
            evalElement.dataSet = dataSet;
            evalElement.pathInDataSet = fileNode.getRelativePath();
            evalElement.evaluatedTemplate =
                    evaluateTemplate(dataSet, fileNode.getName(), disambiguationVar);
            evalElement.contentNode = fileNode;
            result.add(evalElement);
        }

        return result;
    }

    private IHierarchicalContentNode getDataSetFileListRoot(ExternalData dataSet,
            IHierarchicalContent hierachicalContent)
    {
        String fileListSubPathOrNull = getFileListSubPath(dataSet);

        if (false == StringUtils.isBlank(fileListSubPathOrNull))
        {
            List<IHierarchicalContentNode> matchingNodes =
                    hierachicalContent.listMatchingNodes(fileListSubPathOrNull);
            if (false == matchingNodes.isEmpty())
            {
                if (matchingNodes.size() == 1)
                {
                    return matchingNodes.get(0);
                } else
                {
                    String message =
                            String.format("Multiple nodes in dataset '%s' match "
                                    + "pattern '%s'. Will use data set root for file listing.",
                                    dataSet.getCode(), fileListSubPathOrNull);
                    operationLog.warn(message);
                }
            } else
            {
                String message =
                        String.format("No nodes in dataset '%s' match "
                                + "pattern '%s'. Will use data set root for file listings.",
                                dataSet.getCode(), fileListSubPathOrNull);
                operationLog.warn(message);
            }
        }
        return hierachicalContent.getRootNode();
    }

    /**
     * Given a zero-based disambiguation index, produces the values for
     * {@value #DISAMBIGUATION_VARNAME}.
     */
    private String computeDisambiguation(int disambiguationIdx)
    {
        int number = 10 + disambiguationIdx;
        return Integer.toString(number, 36).toUpperCase();
    }

    /**
     * @return all values to be used when evaluating the template "${fileName}" variable.
     */
    private List<IHierarchicalContentNode> getFileNamesRequiredByTemplate(IHierarchicalContentNode rootNode)
    {
        if (isVariablePresentInTemplate(FILE_NAME_VARNAME))
        {
            if (rootNode.isDirectory())
            {
                return rootNode.getChildNodes();
            }
        }
        return Collections.singletonList(rootNode);
    }

    private String evaluateTemplate(ExternalData dataSet, String fileName, String disambiguation)
    {
        Template eval = new Template(template);
        eval.attemptToBind(DATA_SET_CODE_VARNAME, dataSet.getCode());
        eval.attemptToBind(DATA_SET_TYPE_VARNAME, dataSet.getDataSetType().getCode());
        String dataSetDate = extractDateValue(dataSet.getRegistrationDate());
        eval.attemptToBind(DATA_SET_DATE_VARNAME, dataSetDate);
        if (fileName != null)
        {
            eval.attemptToBind(FILE_NAME_VARNAME, fileName);
        }
        eval.attemptToBind(DISAMBIGUATION_VARNAME, disambiguation);

        return eval.createText();
    }

    /**
     * formats a date as it will appear when a {@link #DATA_SET_DATE_VARNAME} is replaced.
     */
    private String extractDateValue(Date dataSetDate)
    {
        return DateFormatUtils.format(dataSetDate, DATA_SET_DATE_FORMAT);
    }

    /**
     * @return true if the specified variable is present in the template.
     */
    private boolean isVariablePresentInTemplate(String variableName)
    {
        Template parsedTemplate = new Template(template);
        return parsedTemplate.getPlaceholderNames().contains(variableName);
    }

    private Map<String, DataSetTypeConfig> initializeDataSetTypeConfigs(
            FtpServerConfig ftpServerConfig)
    {
        Map<String, DataSetTypeConfig> result = new HashMap<String, DataSetTypeConfig>();
        Map<String, String> fileListSubPaths = ftpServerConfig.getFileListSubPaths();
        Map<String, String> fileListFilters = ftpServerConfig.getFileListFilters();

        for (Entry<String, String> subPathEntry : fileListSubPaths.entrySet())
        {
            DataSetTypeConfig dsConfig = new DataSetTypeConfig();
            dsConfig.fileListSubPath = subPathEntry.getValue();
            result.put(subPathEntry.getKey(), dsConfig);
        }

        for (Entry<String, String> filterEntry : fileListFilters.entrySet())
        {
            String dataSetType = filterEntry.getKey();
            String fileFilterPattern = filterEntry.getValue();
            DataSetTypeConfig dsConfig = result.get(dataSetType);
            if (dsConfig == null)
            {
                dsConfig = new DataSetTypeConfig();
            }
            dsConfig.fileFilter = createFilter(fileFilterPattern);
            result.put(dataSetType, dsConfig);
        }
        return result;
    }

    private IHierarchicalContentNodeFilter createFilter(final String fileFilterPattern)
    {
        return new IHierarchicalContentNodeFilter()
            {
                private final Pattern compiledPattern = Pattern.compile(fileFilterPattern);

                public boolean accept(IHierarchicalContentNode node)
                {
                    if (node.isDirectory())
                    {
                        // no filtering for directories
                        return true;
                    }

                    return compiledPattern.matcher(node.getName()).matches();
                }
            };
    }

    private DataSetTypeConfig getDataSetTypeConfig(ExternalData dataSet)
    {
        String dataSetType = dataSet.getDataSetType().getCode();
        DataSetTypeConfig dsConfig = dataSetTypeConfigs.get(dataSetType);
        if (dsConfig != null)
        {
            return dsConfig;
        } else
        {
            return defaultDSTypeConfig;
        }
    }

    private IHierarchicalContentNodeFilter getFileFilter(ExternalData dataSet)
    {
        return getDataSetTypeConfig(dataSet).fileFilter;
    }

    private String getFileListSubPath(ExternalData dataSet)
    {
        return getDataSetTypeConfig(dataSet).fileListSubPath;
    }

}
