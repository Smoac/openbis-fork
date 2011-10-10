/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.api.v1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.common.utilities.HierarchicalContentUtils;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.generic.server.IStreamRepository;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IDssServiceRpcGenericInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataSetFileDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.HierarchicalFileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;

/**
 * Implementation of the generic RPC interface.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcGeneric extends AbstractDssServiceRpc<IDssServiceRpcGenericInternal>
        implements IDssServiceRpcGenericInternal
{
    private final PutDataSetService putService;
    
    /**
     * The designated constructor.
     */
    public DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService)
    {
        // NOTE: IShareIdManager and IHierarchicalContentProvider will be lazily created by spring
        this(openBISService, null, null);
    }

    DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService,
            IShareIdManager shareIdManager, IHierarchicalContentProvider contentProvider)
    {
        this(openBISService, null, shareIdManager, contentProvider, new PutDataSetService(openBISService,
                operationLog));
    }

    /**
     * A constructor for testing.
     */
    public DssServiceRpcGeneric(IEncapsulatedOpenBISService openBISService, IStreamRepository streamRepository,
            IShareIdManager shareIdManager, IHierarchicalContentProvider contentProvider,
            PutDataSetService service)
    {
        super(openBISService, streamRepository, shareIdManager, contentProvider);
        putService = service;
        operationLog.info("[rpc] Started DSS API V1 service.");
    }
    
    public IDssServiceRpcGenericInternal createLogger(IInvocationLoggerContext context)
    {
        return new DssServiceRpcGenericLogger(context);
    }

    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, String dataSetCode,
            String startPath, boolean isRecursive) throws IllegalArgumentException
    {
        IHierarchicalContent content = null;
        try
        {
            content = getHierarchicalContent(dataSetCode);
            IHierarchicalContentNode startPathNode = getContentNode(content, startPath);
            ArrayList<FileInfoDssDTO> list = new ArrayList<FileInfoDssDTO>();
            if (startPathNode.isDirectory())
            {
                appendFileInfosForFile(startPathNode, list, isRecursive);
            } else
            {
                list.add(new FileInfoDssDTO(startPathNode.getRelativePath(), startPathNode
                        .getName(), false, startPathNode.getFileLength()));
            }
            FileInfoDssDTO[] fileInfos = new FileInfoDssDTO[list.size()];
            return list.toArray(fileInfos);
        } catch (IOException ex)
        {
            operationLog.info("listFiles: " + startPath + " caused an exception", ex);
            throw new IOExceptionUnchecked(ex);
        } catch (RuntimeException ex)
        {
            operationLog.info("listFiles: " + startPath + " caused an exception", ex);
            throw ex;
        } finally
        {
            if (content != null)
            {
                content.close();
            }
        }
    }

    public InputStream getFileForDataSet(String sessionToken, String dataSetCode, String path)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        IHierarchicalContent content = null;
        try
        {
            content = getHierarchicalContent(dataSetCode);
            IHierarchicalContentNode contentNode = getContentNode(content, path);
            return HierarchicalContentUtils.getInputStreamAutoClosingContent(contentNode, content);
        } catch (RuntimeException ex)
        {
            operationLog.info("getFile: " + path + " caused an exception", ex);
            if (content != null)
            {
                // close content only on exception, otherwise stream close should close the content
                content.close();
            }
            throw ex;
        }
    }

    public String getDownloadUrlForFileForDataSet(String sessionToken, String dataSetCode, String path)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        InputStream stream = getFileForDataSet(sessionToken, dataSetCode, path);
        return addToRepositoryAndReturnDownloadUrl(stream);
    }

    private IHierarchicalContentNode getContentNode(IHierarchicalContent content, String startPath)
    {
        // handle both relative and absolute paths for backward compatibility
        return content.getNode(startPath.startsWith("/") ? startPath.substring(1) : startPath);
    }

    public String putDataSet(String sessionToken, NewDataSetDTO newDataSet, InputStream inputStream)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return putService.putDataSet(sessionToken, newDataSet, inputStream);
    }

    public int getMajorVersion()
    {
        return 1;
    }

    public int getMinorVersion()
    {
        return 4;
    }

    /**
     * Append file info for the requested node of a file or file hierarchy. Assumes that the
     * parameters have been verified already.
     * 
     * @param requestedFile A file known to be accessible by the user
     * @param dataSetRoot The root of the file hierarchy; used to determine the absolute path of the
     *            file
     * @param listingRootNode The node which is a root of the list hierarchy; used to determine the
     *            relative path of the file
     * @param list The list the files infos are appended to
     * @param isRecursive If true, directories will be recursively appended to the list
     */
    private void appendFileInfosForFile(IHierarchicalContentNode listingRootNode,
            ArrayList<FileInfoDssDTO> list, boolean isRecursive) throws IOException
    {
        HierarchicalFileInfoDssBuilder factory =
                new HierarchicalFileInfoDssBuilder(listingRootNode);
        factory.appendFileInfos(list, isRecursive);
    }

    public InputStream getFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return this.getFileForDataSet(sessionToken, fileOrFolder.getDataSetCode(),
                fileOrFolder.getPath());
    }

    public String getDownloadUrlForFileForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return addToRepositoryAndReturnDownloadUrl(getFileForDataSet(sessionToken, fileOrFolder));
    }

    public FileInfoDssDTO[] listFilesForDataSet(String sessionToken, DataSetFileDTO fileOrFolder)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return this.listFilesForDataSet(sessionToken, fileOrFolder.getDataSetCode(),
                fileOrFolder.getPath(), fileOrFolder.isRecursive());
    }

    @Override
    public void setStoreDirectory(File aFile)
    {
        super.setStoreDirectory(aFile);
        putService.setStoreDirectory(aFile);
    }

    @Override
    public void setIncomingDirectory(File aFile)
    {
        putService.setIncomingDir(aFile);
    }

    public String getPathToDataSet(String sessionToken, String dataSetCode,
            String overrideStoreRootPathOrNull) throws IOExceptionUnchecked,
            IllegalArgumentException
    {
        // see NOTE in interface documentation
        @SuppressWarnings("deprecation")
        File rootDir = getRootDirectory(dataSetCode);
        return convertPath(getStoreDirectory(), rootDir, overrideStoreRootPathOrNull);
    }

    public static String convertPath(File storeRoot, File dataSetRoot,
            String overrideStoreRootPathOrNull)
    {
        String dataStoreRootPath = storeRoot.getAbsolutePath();
        String dataSetPath = dataSetRoot.getAbsolutePath();

        // No override specified; give the user the path as we understand it.
        if (null == overrideStoreRootPathOrNull
                || false == dataSetPath.startsWith(dataStoreRootPath))
        {
            return dataSetPath;
        }

        // Make the path begin with the user's store root override
        File usersPath =
                new File(overrideStoreRootPathOrNull, dataSetPath.substring(dataStoreRootPath
                        .length()));
        return usersPath.getPath();
    }

    public String getValidationScript(String sessionToken, String dataSetTypeOrNull)
            throws IOExceptionUnchecked, IllegalArgumentException
    {
        return putService.getValidationScript(dataSetTypeOrNull);
    }
}
