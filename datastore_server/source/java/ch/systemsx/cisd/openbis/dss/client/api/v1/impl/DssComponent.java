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

package ch.systemsx.cisd.openbis.dss.client.api.v1.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyException;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

import ch.systemsx.cisd.common.api.IRpcServiceFactory;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceDTO;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.io.ConcatenatedContentInputStream;
import ch.systemsx.cisd.common.io.FileBasedContentNode;
import ch.systemsx.cisd.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.client.api.v1.FileInfoDssDownloader;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataStoreApiUrlUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationScriptRunner;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * Implementation of the IDssComponent interface. It is a facade for interacting with openBIS and
 * multiple DSS servers.
 * <p>
 * The DssComponent manages a connection to openBIS (IETLLIMSService) as well as connections to data
 * store servers (IDssServiceRpc) to present a simplified interface to downloading datasets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssComponent implements IDssComponent
{
    private final IGeneralInformationService generalOpenBisService;

    private final IRpcServiceFactory dssServiceFactory;

    private AbstractDssComponentState state;

    /**
     * Public factory method for creating a DssComponent with a username and password.
     * 
     * @param user The user name
     * @param password The user's password
     * @param openBISUrl The URL to openBIS
     * @param timeoutInMillis network timeout when connecting to remote services
     */
    public static DssComponent tryCreate(String user, String password, String openBISUrl,
            long timeoutInMillis)
    {
        DssComponent component = new DssComponent(openBISUrl, null, timeoutInMillis);
        try
        {
            component.login(user, password);
        } catch (AuthorizationFailureException e)
        {
            // User name / Password is incorrect.
            return null;
        }
        return component;
    }

    /**
     * Public factory method for creating a DssComponent for a user that has already been
     * authenticated.
     * 
     * @param sessionToken The session token provided by authentication
     * @param openBISUrl The URL to openBIS
     * @param timeoutInMillis network timeout when connecting to remote services
     */
    public static DssComponent tryCreate(String sessionToken, String openBISUrl,
            long timeoutInMillis)
    {
        DssComponent component = new DssComponent(openBISUrl, sessionToken, timeoutInMillis);
        try
        {
            component.checkSession();
        } catch (InvalidSessionException e)
        {
            // Session has expired
            return null;
        }
        return component;
    }

    private static IGeneralInformationService createGeneralInformationService(String openBISURL,
            long timeoutInMillis)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        IGeneralInformationService service =
                generalInformationServiceFinder.createService(IGeneralInformationService.class,
                        openBISURL, timeoutInMillis);
        return service;
    }

    /**
     * Create a DSS component that connects to the openBIS instance specified by the URL.
     * <p>
     * The DSS component needs to connect to openBIS to find out which DSS manages a given data set.
     * Once it has a connection to openBIS, it can figure out how to connect to DSS servers itself.
     * 
     * @param openBISURL The url to connect to openBIS
     * @param sessionTokenOrNull A session token; If null is passed in, then login needs to be
     *            called.
     * @param timeoutInMillis a timeout for network operations
     */
    private DssComponent(String openBISURL, String sessionTokenOrNull, long timeoutInMillis)
    {
        this(createGeneralInformationService(openBISURL, timeoutInMillis),
                new DssServiceRpcFactory(timeoutInMillis), sessionTokenOrNull);
    }

    /**
     * Constructor for clients that already have an IETLLIMSService; also used for testing.
     * 
     * @param service A proxy to the openBIS application server.
     * @param dssServiceFactory A proxy to the DSS server.
     * @param sessionTokenOrNull A session token, if the user has already logged in, or null
     *            otherwise.
     */
    public DssComponent(IGeneralInformationService service, IRpcServiceFactory dssServiceFactory,
            String sessionTokenOrNull)
    {
        this.generalOpenBisService = service;
        this.dssServiceFactory = dssServiceFactory;
        if (sessionTokenOrNull == null)
        {
            this.state = new UnauthenticatedState(service);
        } else
        {
            this.state = new AuthenticatedState(service, dssServiceFactory, sessionTokenOrNull);
        }
    }

    public String getSessionToken()
    {
        return state.getSessionToken();
    }

    public void checkSession() throws InvalidSessionException
    {
        state.checkSession();
    }

    public IDataSetDss getDataSet(String code) throws EnvironmentFailureException,
            IllegalStateException
    {
        return state.getDataSet(code);
    }

    public void logout()
    {
        // logout and transition to the unauthenticated state
        state.logout();
        state = new UnauthenticatedState(generalOpenBisService);
    }

    /**
     * Authenticates the <code>user</code> with given <code>password</code>.
     * 
     * @throws AuthorizationFailureException Thrown if the username / password do not authenticate.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    void login(String user, String password) throws AuthorizationFailureException,
            EnvironmentFailureException
    {
        // login and transition to the authenticated state
        state.login(user, password);
        state =
                new AuthenticatedState(generalOpenBisService, dssServiceFactory,
                        state.getSessionToken());
    }

    public IDataSetDss putDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        return state.putDataSet(newDataset, dataSetFile);
    }

    public List<ValidationError> validateDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        return state.validateDataSet(newDataset, dataSetFile);
    }

    public Map<String, String> extractMetadata(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        return state.extractMetadata(newDataset, dataSetFile);
    }
}

/**
 * Superclass for component states, which make the state machine of the DSS component explicit.
 * <p>
 * By default, all methods just throw an exception. Subclasses should override the methods they
 * accept.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractDssComponentState implements IDssComponent
{
    protected final IGeneralInformationService service;

    AbstractDssComponentState(IGeneralInformationService service)
    {
        this.service = service;
    }

    public void checkSession() throws IllegalStateException
    {
        throw new IllegalStateException("Please log in");
    }

    public IDataSetDss getDataSet(String code) throws IllegalStateException
    {
        throw new IllegalStateException("Please log in");
    }

    public IDataSetDss putDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        throw new IllegalStateException("Please log in");
    }

    public List<ValidationError> validateDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        throw new IllegalStateException("Please log in");
    }

    public Map<String, String> extractMetadata(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        throw new IllegalStateException("Please log in");
    }

    /**
     * Authenticates the <code>user</code> with given <code>password</code>.
     * 
     * @throws AuthorizationFailureException Thrown if the username / password do not authenticate.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    abstract void login(final String user, final String password)
            throws AuthorizationFailureException, EnvironmentFailureException;

    /**
     * Package visible method used to transfer context information between states.
     */
    public abstract String getSessionToken();
}

/**
 * An object representing an unauthenticated state. Being in this state implies that the user has
 * not yet logged in. Only login and logout are allowed in this state.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class UnauthenticatedState extends AbstractDssComponentState
{
    private String sessionTokenOrNull;

    UnauthenticatedState(IGeneralInformationService generalOpenBisService)
    {
        super(generalOpenBisService);
    }

    @Override
    public String getSessionToken()
    {
        if (sessionTokenOrNull == null)
            throw new IllegalStateException("Please log in");
        return sessionTokenOrNull;
    }

    @Override
    void login(String user, String password) throws AuthorizationFailureException,
            EnvironmentFailureException
    {
        try
        {
            sessionTokenOrNull = service.tryToAuthenticateForAllServices(user, password);
        } catch (RemoteConnectFailureException e)
        {
            throw new EnvironmentFailureException("Could not connect to server", e);
        }
        if (sessionTokenOrNull == null)
            throw new AuthorizationFailureException("Login or Password invalid");
    }

    public void logout()
    {
        return;
    }
}

/**
 * An object representing an authenticated state. Being in this state means that the user has logged
 * in and all operations are available, except login.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class AuthenticatedState extends AbstractDssComponentState
{
    private final String sessionToken;

    private final IRpcServiceFactory dssServiceFactory;

    private final HashMap<String, ValidationScriptRunner> validationScriptRunnerCache =
            new HashMap<String, ValidationScriptRunner>();

    AuthenticatedState(IGeneralInformationService generalOpenBisService,
            IRpcServiceFactory dssServiceFactory, String sessionToken)
    {
        super(generalOpenBisService);
        this.dssServiceFactory = dssServiceFactory;
        this.sessionToken = sessionToken;
    }

    @Override
    void login(String user, String password) throws AuthorizationFailureException
    {
        throw new IllegalStateException("Already logged in.");
    }

    @Override
    public void checkSession()
    {
        if (service.isSessionActive(getSessionToken()) == false)
        {
            throw new InvalidSessionException("Session has expired");
        }
    }

    public void logout()
    {
        service.logout(getSessionToken());
    }

    @Override
    public IDataSetDss getDataSet(String dataSetCode) throws IllegalArgumentException,
            EnvironmentFailureException, RemoteAccessException
    {
        // Contact openBIS to find out which DSS server manages the data set
        String dataStoreBaseURL = service.tryGetDataStoreBaseURL(getSessionToken(), dataSetCode);
        if (null == dataStoreBaseURL)
        {
            throw new IllegalArgumentException("Could not retrieve data set with code "
                    + dataSetCode);
        }
        String url = getDataStoreUrlFromDataStore(dataStoreBaseURL);

        IDssServiceRpcGeneric dssService = getDssServiceForUrl(url);
        // Return a proxy to the data set
        return new DataSetDss(dataSetCode, dssService, this);
    }

    @Override
    public IDataSetDss putDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        IDssServiceRpcGeneric dssService = getServiceForPutDataStore();
        ConcatenatedContentInputStream fileInputStream =
                new ConcatenatedContentInputStream(true, getContentForFileInfos(
                        dataSetFile.getPath(), newDataset.getFileInfos()));
        String code = dssService.putDataSet(sessionToken, newDataset, fileInputStream);
        return new DataSetDss(code, dssService, this);
    }

    private IDssServiceRpcGeneric getServiceForPutDataStore()
    {
        String url = service.getDefaultPutDataStoreBaseURL(sessionToken);
        url = DataStoreApiUrlUtilities.getDataStoreUrlFromDownloadUrl(url);
        IDssServiceRpcGeneric dssService = getDssServiceForUrl(url);
        return dssService;
    }

    private List<IHierarchicalContentNode> getContentForFileInfos(String filePath,
            List<FileInfoDssDTO> fileInfos)
    {
        List<IHierarchicalContentNode> files = new ArrayList<IHierarchicalContentNode>();
        File parent = new File(filePath);
        if (false == parent.isDirectory())
        {
            return Collections
                    .<IHierarchicalContentNode> singletonList(new FileBasedContentNode(parent));
        }

        for (FileInfoDssDTO fileInfo : fileInfos)
        {
            File file = new File(parent, fileInfo.getPathInDataSet());
            if (false == file.exists())
            {
                throw new IllegalArgumentException("File does not exist " + file);
            }
            // Skip directories
            if (false == file.isDirectory())
            {
                files.add(new FileBasedContentNode(file));
            }
        }

        return files;
    }

    /**
     * Package visible method to communicate with the server and get a list of files contained in
     * this data set.
     */
    FileInfoDssDTO[] listFiles(DataSetDss dataSetDss, String startPath, boolean isRecursive)
            throws InvalidSessionException
    {
        return dataSetDss.getService().listFilesForDataSet(getSessionToken(), dataSetDss.getCode(),
                startPath, isRecursive);
    }

    /**
     * Package visible method to communicate with the server and get a list of files contained in
     * this data set.
     */
    InputStream getFile(DataSetDss dataSet, String path) throws InvalidSessionException
    {
        return dataSet.getService().getFileForDataSet(getSessionToken(), dataSet.getCode(), path);
    }

    /**
     * Package visible method to communicate with the server and get a link to the file in the DSS.
     * Returns null if link couldn't be retrieved (e.g. when the <var>dataSetDss</var> is a
     * container).
     */
    File tryLinkToContents(DataSetDss dataSetDss, String overrideStoreRootPathOrNull)
            throws InvalidSessionException, EnvironmentFailureException
    {
        int minorVersion = dataSetDss.getService().getMinorVersion();
        if (minorVersion < 1)
        {
            throw new EnvironmentFailureException("Server does not support this feature.");
        }

        // Get the path
        String path =
                dataSetDss.getService().getPathToDataSet(getSessionToken(), dataSetDss.getCode(),
                        overrideStoreRootPathOrNull);

        // Check if the file referenced by the path exists, if so return it.
        // NOTE: the path will never exist if the data set is a container.
        File contents = new File(path);
        if (contents.exists())
        {
            return contents;
        }

        // Otherwise return null
        return null;
    }

    /**
     * Package visible method to get a link to the contents of the data set in DSS', if possible,
     * otherwise copy the contents locally.
     */
    File getLinkOrCopyOfContents(DataSetDss dataSetDss, String overrideStoreRootPathOrNull,
            File downloadDir, String pathInDataSetOrNull) throws InvalidSessionException
    {
        if (pathInDataSetOrNull == null)
        {
            return getLinkOrCopyOfContents(dataSetDss, overrideStoreRootPathOrNull, downloadDir);
        }
        File link = tryLinkToContents(dataSetDss, overrideStoreRootPathOrNull);
        if (null != link)
        {
            File file = new File(link, pathInDataSetOrNull);
            if (file.exists() == false)
            {
                throw new IllegalArgumentException("Data set " + dataSetDss.getCode()
                        + " has no file/folder with path '" + pathInDataSetOrNull + "'.");
            }
            return file;
        }

        FileInfoDssDTO[] fileInfos =
                dataSetDss.getService().listFilesForDataSet(getSessionToken(),
                        dataSetDss.getCode(), pathInDataSetOrNull, true);
        File result = new File(new File(downloadDir, dataSetDss.getCode()), pathInDataSetOrNull);
        File outputDir = result;
        if (fileInfos.length == 1 && fileInfos[0].isDirectory() == false)
        {
            outputDir = result.getParentFile();
        }
        outputDir.mkdirs();
        FileInfoDssDownloader downloader =
                new FileInfoDssDownloader(dataSetDss, fileInfos, outputDir);
        downloader.downloadFiles();
        return result;
    }

    private File getLinkOrCopyOfContents(DataSetDss dataSetDss, String overrideStoreRootPathOrNull,
            File downloadDir) throws InvalidSessionException
    {
        File link = tryLinkToContents(dataSetDss, overrideStoreRootPathOrNull);
        if (null != link)
        {
            return link;
        }

        File outputDir = new File(downloadDir, dataSetDss.getCode());
        // Create any directories necessary
        outputDir.mkdirs();

        FileInfoDssDTO[] fileInfos =
                dataSetDss.getService().listFilesForDataSet(getSessionToken(),
                        dataSetDss.getCode(), "/", true);
        FileInfoDssDownloader downloader =
                new FileInfoDssDownloader(dataSetDss, fileInfos, outputDir);
        downloader.downloadFiles();

        return outputDir;
    }

    /**
     * Create a connection to the DSS server referenced by url
     */
    private IDssServiceRpcGeneric getDssServiceForUrl(String url)
    {
        // Get an RPC service for the DSS server
        String serverURL = url;
        try
        {
            IDssServiceRpcGeneric dssService = basicGetDssServiceForUrl(serverURL);
            return dssService;
        } catch (RemoteAccessException e)
        {
            // if the url begins with https, try http
            if (serverURL.startsWith("https://"))
            {
                // https:// has 8 characters
                serverURL = "http://" + serverURL.substring(8);
                IDssServiceRpcGeneric dssService = basicGetDssServiceForUrl(serverURL);
                return dssService;
            }

            // Rethrow the exception
            throw e;
        }
    }

    /**
     * A less sophisticated implementation of getDssServiceForUrl
     */
    private IDssServiceRpcGeneric basicGetDssServiceForUrl(String serverURL)
    {
        IDssServiceRpcGeneric dssService = null;
        Collection<RpcServiceInterfaceDTO> ifaces =
                dssServiceFactory.getSupportedInterfaces(serverURL, false);

        for (RpcServiceInterfaceDTO iface : ifaces)
        {
            if (IDssServiceRpcGeneric.DSS_SERVICE_NAME.equals(iface.getInterfaceName()))
            {
                for (RpcServiceInterfaceVersionDTO ifaceVersion : iface.getVersions())
                {
                    if (1 == ifaceVersion.getMajorVersion())
                    {
                        dssService =
                                dssServiceFactory.getService(ifaceVersion,
                                        IDssServiceRpcGeneric.class, serverURL, false);
                        return dssService;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Server does not support the "
                + IDssServiceRpcGeneric.DSS_SERVICE_NAME + " interface.");
    }

    /**
     * The data store only stores the download url, get the data store url
     */
    private String getDataStoreUrlFromDataStore(String dataStoreDownloadUrl)
    {
        return DataStoreApiUrlUtilities.getDataStoreUrlFromDownloadUrl(dataStoreDownloadUrl);
    }

    @Override
    public String getSessionToken()
    {
        return sessionToken;
    }

    @Override
    public List<ValidationError> validateDataSet(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        ValidationScriptRunner runner = null;
        try
        {
            runner = getValidationScriptRunner(newDataset.tryDataSetType());
        } catch (Throwable ex)
        {
            return createValidationError("Script error", ex);
        }

        try
        {
            return runner.validate(dataSetFile);
        } catch (Throwable ex)
        {
            System.err.println("Could not run validation script: ");
            System.err.println(runner.getScriptString());
            ex.printStackTrace(System.err);
            return createValidationError("Script execution error", ex);
        }
    }

    @Override
    public Map<String, String> extractMetadata(NewDataSetDTO newDataset, File dataSetFile)
            throws IllegalStateException, EnvironmentFailureException
    {
        ValidationScriptRunner runner = null;
        try
        {
            runner = getValidationScriptRunner(newDataset.tryDataSetType());
        } catch (Throwable t)
        {
            // ignore the error here since, if it is significant, it will appear again in
            // validation.
            return Collections.emptyMap();
        }

        try
        {
            return runner.extractMetadata(dataSetFile);
        } catch (Throwable t)
        {
            System.err.println("Could not run metadata extraction script: ");
            System.err.println(runner.getScriptString());
            t.printStackTrace(System.err);
            return Collections.emptyMap();
        }
    }

    private ValidationScriptRunner getValidationScriptRunner(String dataSetTypeOrNull)
            throws PyException, Throwable
    {
        IDssServiceRpcGeneric dssService = getServiceForPutDataStore();

        // Validation script support was introduced in minor version 2. Skip validating if the
        // server doesn't support it.
        if (dssService.getMinorVersion() < 2)
        {
            return new ValidationScriptRunner.NullValidationScriptRunner();
        }

        ValidationScriptRunner runner;

        // Check if the script is in the cache
        if (validationScriptRunnerCache.containsKey(dataSetTypeOrNull))
        {
            runner = validationScriptRunnerCache.get(dataSetTypeOrNull);
        } else
        {
            String validationScript =
                    dssService.getValidationScript(sessionToken, dataSetTypeOrNull);
            try
            {
                runner = ValidationScriptRunner.createValidatorFromScriptString(validationScript);
                validationScriptRunnerCache.put(dataSetTypeOrNull, runner);
            } catch (Throwable ex)
            {
                System.err.println("Could not create validation script ");
                System.err.println(validationScript);
                System.err.println(ex);
                ex.printStackTrace(System.err);
                System.err.println(Py.getSystemState().modules);
                throw ex;
            }
        }
        return runner;
    }

    private List<ValidationError> createValidationError(String messagePrefix, Throwable throwable)
    {
        return Arrays.asList(ValidationError.createFileValidationError(messagePrefix + ": "
                + throwable));
    }
}
