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

package ch.systemsx.cisd.openbis.dss.component.impl;

import java.io.InputStream;
import java.util.List;

import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

import ch.systemsx.cisd.common.api.RpcServiceInterfaceDTO;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.component.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.component.IDssComponent;
import ch.systemsx.cisd.openbis.dss.rpc.client.DssServiceRpcFactory;
import ch.systemsx.cisd.openbis.dss.rpc.client.IDssServiceRpcFactory;
import ch.systemsx.cisd.openbis.dss.rpc.shared.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

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
    private static final int SERVIER_TIMEOUT_MIN = 5;

    private final IETLLIMSService openBisService;

    private final IDssServiceRpcFactory dssServiceFactory;

    private AbstractDssComponentState state;

    /**
     * Create a DSS component that connects to the openBIS instance specified by the URL.
     * <p>
     * The DSS component needs to connect to openBIS to find out which DSS manages a given data set.
     * Once it has a connection to openBIS, it can figure out how to connect to DSS servers itself.
     * 
     * @param openBISURL The url to connect to openBIS
     */
    public DssComponent(String openBISURL)
    {
        this(HttpInvokerUtils.createServiceStub(IETLLIMSService.class, openBISURL + "/rmi-etl",
                SERVIER_TIMEOUT_MIN), new DssServiceRpcFactory());
    }

    /**
     * Internal constructor used for testing.
     * 
     * @param service
     */
    protected DssComponent(IETLLIMSService service, IDssServiceRpcFactory dssServiceFactory)
    {
        this.openBisService = service;
        this.dssServiceFactory = dssServiceFactory;
        this.state = new UnauthenticatedState(service);
    }

    public void login(String user, String password) throws AuthorizationFailureException,
            EnvironmentFailureException
    {
        // login and transition to the authenticated state
        state.login(user, password);
        state = new AuthenticatedState(openBisService, dssServiceFactory, state.getSession());
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
        state = new UnauthenticatedState(openBisService);
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
    protected final IETLLIMSService service;

    AbstractDssComponentState(IETLLIMSService service)
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

    /**
     * Package visible method used to transfer context information between states.
     */
    abstract SessionContextDTO getSession();
}

/**
 * An object representing an unauthenticated state. Being in this state implies that the user has
 * not yet logged in. Only login and logout are allowed in this state.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class UnauthenticatedState extends AbstractDssComponentState
{
    private SessionContextDTO sessionOrNull;

    UnauthenticatedState(IETLLIMSService service)
    {
        super(service);
    }

    @Override
    SessionContextDTO getSession()
    {
        if (sessionOrNull == null)
            throw new UserFailureException("Please log in");
        return sessionOrNull;
    }

    public void login(String user, String password) throws AuthorizationFailureException,
            EnvironmentFailureException
    {
        try
        {
            sessionOrNull = service.tryToAuthenticate(user, password);
        } catch (RemoteConnectFailureException e)
        {
            throw new EnvironmentFailureException("Could not connect to server", e);
        }
        if (sessionOrNull == null)
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
    private final SessionContextDTO session;

    private final IDssServiceRpcFactory dssServiceFactory;

    /**
     * @param service
     */
    AuthenticatedState(IETLLIMSService service, IDssServiceRpcFactory dssServiceFactory,
            SessionContextDTO session)
    {
        super(service);
        this.dssServiceFactory = dssServiceFactory;
        this.session = session;
    }

    @Override
    SessionContextDTO getSession()
    {
        return session;
    }

    public void login(String user, String password) throws AuthorizationFailureException
    {
        throw new UserFailureException("Already logged in.");
    }

    public void logout()
    {
        service.logout(getSessionToken());
    }

    @Override
    public IDataSetDss getDataSet(String code) throws IllegalArgumentException,
            EnvironmentFailureException, RemoteAccessException
    {
        // Contact openBIS to find out which DSS server manages the data set
        ExternalData dataSetOpenBis = service.tryGetDataSet(getSessionToken(), code);
        if (null == dataSetOpenBis)
        {
            throw new IllegalArgumentException("Could not retrieve data set with code " + code);
        }
        DataStore dataStore = dataSetOpenBis.getDataStore();

        String url = dataStore.getDownloadUrl();

        IDssServiceRpcGeneric dssService = getDssServiceForUrl(url);
        // Return a proxy to the data set
        return new DataSetDss(code, dssService, this);

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
    InputStream getFile(DataSetDss dataSetDss, String path) throws InvalidSessionException
    {
        return dataSetDss.getService().getFileForDataSet(getSessionToken(), dataSetDss.getCode(),
                path);
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
        List<RpcServiceInterfaceDTO> ifaces =
                dssServiceFactory.getSupportedInterfaces(serverURL, false);

        for (RpcServiceInterfaceDTO iface : ifaces)
        {
            if (IDssServiceRpcGeneric.DSS_SERVICE_NAME.equals(iface.getInterfaceName()))
            {
                for (RpcServiceInterfaceVersionDTO ifaceVersion : iface.getVersions())
                {
                    if (1 == ifaceVersion.getInterfaceMajorVersion())
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

    private String getSessionToken()
    {
        return session.getSessionToken();
    }
}
