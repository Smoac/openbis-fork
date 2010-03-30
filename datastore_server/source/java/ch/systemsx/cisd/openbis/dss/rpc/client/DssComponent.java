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

package ch.systemsx.cisd.openbis.dss.rpc.client;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDataSetDss;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * Implementation of the IDssComponent interface. It is a facade for interacting with openBIS and
 * multiple DSS servers.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssComponent implements IDssComponent
{
    private final IETLLIMSService service;

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
        this(HttpInvokerUtils.createServiceStub(IETLLIMSService.class, openBISURL + "/rmi-etl", 5));
    }

    /**
     * Internal constructor used for testing.
     * 
     * @param service
     */
    protected DssComponent(IETLLIMSService service)
    {
        this.service = service;
        this.state = new UnauthenticatedState(service);
    }

    public void login(String user, String password) throws AuthorizationFailureException
    {
        // login and transition to the authenticated state
        state.login(user, password);
        state = new AuthenticatedState(service, state.getSession());
    }

    public void checkSession() throws InvalidSessionException
    {
        state.checkSession();
    }

    public IDataSetDss getDataSet(String code)
    {
        return state.getDataSet(code);
    }

    public void logout()
    {
        // logout and transition to the unauthenticated state
        state.logout();
        state = new UnauthenticatedState(service);
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

    public void checkSession() throws InvalidSessionException
    {
        throw new UserFailureException("Please log in");
    }

    public IDataSetDss getDataSet(String code)
    {
        throw new UserFailureException("Please log in");
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

    public void login(String user, String password) throws AuthorizationFailureException
    {
        sessionOrNull = service.tryToAuthenticate(user, password);
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

    /**
     * @param service
     */
    AuthenticatedState(IETLLIMSService service, SessionContextDTO session)
    {
        super(service);
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
        service.logout(session.getSessionToken());
    }

    @Override
    public IDataSetDss getDataSet(String code)
    {
        // Contact openBIS to find out which DSS server manages the data set
        ExternalData dataSetOpenBis = service.tryGetDataSet(session.getSessionToken(), code);
        // Get an RPC service for the DSS server
        DataStore dataStore = dataSetOpenBis.getDataStore();
        // Get a proxy to the data set
        dataStore.getDownloadUrl();
        // TODO Auto-generated method stub
        return null;
    }

}
