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
import java.util.Comparator;
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

import ch.systemsx.cisd.common.io.IHierarchicalContent;
import ch.systemsx.cisd.common.io.IHierarchicalContentNode;
import ch.systemsx.cisd.common.io.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
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

    private static final String TEMPLATE = "template";

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

    private static class EvaluatedDataSetPath
    {
        ExternalData dataSet;

        // will only be filled when the ${fileName} variable
        // is used in the template
        String fileName = StringUtils.EMPTY;

        String evaluatedTemplate;

        IHierarchicalContentNode contentNode;
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

        EvaluatedDataSetPath evaluationResult =
                tryExtractDataSetAndFileName(path, service, sessionToken);
        if (evaluationResult == null)
        {
            return null;
        }

        String nestedSubPath = extractNestedSubPath(path);
        String relativePath = evaluationResult.fileName;
        if (false == StringUtils.isBlank(nestedSubPath))
        {
            relativePath += FtpConstants.FILE_SEPARATOR + nestedSubPath;
        }

        IHierarchicalContentNodeFilter fileFilter = getFileFilter(evaluationResult.dataSet);
        IHierarchicalContentProvider provider = ServiceProvider.getHierarchicalContentProvider();
        IHierarchicalContent content =
                provider.asContent(evaluationResult.dataSet.getDataSetCode());
        IHierarchicalContentNode contentNode = content.getNode(relativePath);
        if (fileFilter.accept(contentNode))
        {
            return new HierarchicalContentToFtpFileAdapter(path, contentNode, fileFilter);
        } else
        {
            return null;
        }
    }

    private EvaluatedDataSetPath tryExtractDataSetAndFileName(String path, IETLLIMSService service,
            String sessionToken)
    {
        String experimentId = extractExperimentIdentifier(path);
        Experiment experiment = tryExtractExperiment(experimentId, service, sessionToken);
        if (experiment == null)
        {
            // cannot resolve an existing experiment from the specified path
            return null;
        }
        List<ExternalData> dataSets =
                service.listDataSetsByExperimentID(sessionToken, new TechId(experiment));
        String pathWithEndSlash = path + FtpConstants.FILE_SEPARATOR;

        for (EvaluatedDataSetPath evaluatedPath : evaluateDataSetPaths(dataSets))
        {
            String fullEvaluatedPath =
                    experimentId + FtpConstants.FILE_SEPARATOR + evaluatedPath.evaluatedTemplate
                            + FtpConstants.FILE_SEPARATOR;
            if (pathWithEndSlash.startsWith(fullEvaluatedPath))
            {
                return evaluatedPath;
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
        if (levels.length > 4)
        {
            return StringUtils.join(levels, FtpConstants.FILE_SEPARATOR, 4, levels.length);
        } else
        {
            return StringUtils.EMPTY;
        }
    }

    private Experiment tryExtractExperiment(String experimentId, IETLLIMSService service,
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

    private String extractExperimentIdentifier(String path)
    {
        String[] levels = StringUtils.split(path, FtpConstants.FILE_SEPARATOR);
        String experimentId =
                FtpConstants.ROOT_DIRECTORY
                        + StringUtils.join(levels, FtpConstants.FILE_SEPARATOR, 0, 3);
        return experimentId;
    }

    public List<FtpFile> listExperimentChildrenPaths(Experiment experiment, String parentPath,
            FtpPathResolverContext context)
    {
        IETLLIMSService service = context.getService();
        String sessionToken = context.getSessionToken();

        List<FtpFile> result = new ArrayList<FtpFile>();
        List<ExternalData> dataSets =
                service.listDataSetsByExperimentID(sessionToken, new TechId(experiment));
        for (EvaluatedDataSetPath evaluationResult : evaluateDataSetPaths(dataSets))
        {
            IHierarchicalContentNodeFilter fileFilter = getFileFilter(evaluationResult.dataSet);
            if (fileFilter.accept(evaluationResult.contentNode))
            {
                String childPath =
                        parentPath + FtpConstants.FILE_SEPARATOR
                                + evaluationResult.evaluatedTemplate;
                FtpFile childFtpFile =
                        new HierarchicalContentToFtpFileAdapter(childPath,
                                evaluationResult.contentNode, fileFilter);
                result.add(childFtpFile);
            }
        }
        return result;
    }

    private List<EvaluatedDataSetPath> evaluateDataSetPaths(List<ExternalData> dataSets)
    {
        List<EvaluatedDataSetPath> result = new ArrayList<EvaluatedDataSetPath>();
        sortDataSetsById(dataSets);

        for (int disambiguationIdx = 0; disambiguationIdx < dataSets.size(); disambiguationIdx++)
        {
            ExternalData dataSet = dataSets.get(disambiguationIdx);
            try
            {
                List<EvaluatedDataSetPath> paths = evaluateDataSetPaths(dataSet, disambiguationIdx);
                result.addAll(paths);
            } catch (Throwable t)
            {
                operationLog.warn("Failed to evaluate data set paths for dataset "
                        + dataSet.getCode() + ": " + t.getMessage());
            }
        }

        return result;
    }

    private List<EvaluatedDataSetPath> evaluateDataSetPaths(ExternalData dataSet,
            int disambiguationIndex)
    {
        List<EvaluatedDataSetPath> result = new ArrayList<EvaluatedDataSetPath>();

        IHierarchicalContentNode rootNode = getDataSetFileListRoot(dataSet);
        String disambiguationVar = computeDisambiguation(disambiguationIndex);

        for (IHierarchicalContentNode fileNode : getFileNamesRequiredByTemplate(rootNode))
        {
            EvaluatedDataSetPath evaluatedPath = new EvaluatedDataSetPath();
            evaluatedPath.dataSet = dataSet;
            evaluatedPath.fileName = fileNode.getRelativePath();
            evaluatedPath.evaluatedTemplate =
                    evaluateTemplate(dataSet, fileNode.getName(), disambiguationVar);
            evaluatedPath.contentNode = fileNode;
            result.add(evaluatedPath);
        }

        return result;
    }

    private IHierarchicalContentNode getDataSetFileListRoot(ExternalData dataSet)
    {
        IHierarchicalContentProvider provider = ServiceProvider.getHierarchicalContentProvider();
        IHierarchicalContent content = provider.asContent(dataSet.getCode());
        String fileListSubPathOrNull = getFileListSubPath(dataSet);

        if (false == StringUtils.isBlank(fileListSubPathOrNull))
        {
            List<IHierarchicalContentNode> matchingNodes =
                    content.listMatchingNodes(fileListSubPathOrNull);
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
        return content.getRootNode();
    }

    private void sortDataSetsById(List<ExternalData> dataSets)
    {
        Collections.sort(dataSets, new Comparator<ExternalData>()
            {
                public int compare(ExternalData o1, ExternalData o2)
                {
                    long id1 = o1.getId();
                    long id2 = o2.getId();
                    // do not return directly "id1 - id2" to avoid overflow
                    if (id1 > id2)
                    {
                        return 1;
                    } else if (id1 < id2)
                    {
                        return -1;
                    } else
                    {
                        return 0;
                    }
                }

            });
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
        ExtendedProperties properties = new ExtendedProperties();
        properties.put(DATA_SET_CODE_VARNAME, dataSet.getCode());
        properties.put(DATA_SET_TYPE_VARNAME, dataSet.getDataSetType().getCode());
        String dataSetDate = extractDateValue(dataSet.getRegistrationDate());
        properties.put(DATA_SET_DATE_VARNAME, dataSetDate);
        properties.put(FILE_NAME_VARNAME, fileName);
        properties.put(DISAMBIGUATION_VARNAME, disambiguation);

        properties.put(TEMPLATE, template);
        return properties.getProperty(TEMPLATE);
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
        ExtendedProperties properties = new ExtendedProperties();
        // try to replace the variable with something different
        properties.put(variableName, variableName + variableName);

        properties.put(TEMPLATE, template);
        String evaluatedTemplate = properties.getProperty(TEMPLATE);
        return false == evaluatedTemplate.equals(template);
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
