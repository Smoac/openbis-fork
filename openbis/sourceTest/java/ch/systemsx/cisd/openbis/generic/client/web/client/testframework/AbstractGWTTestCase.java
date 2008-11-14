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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import java.util.Date;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Client;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/**
 * Abstract super class of all GWT System Tests.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractGWTTestCase extends GWTTestCase
{
    protected RemoteConsole remoteConsole;

    protected Client client;

    /**
     * Terminates test. Wrapper of {@link #finishTest()}. Will be used in {@link RemoteConsole}.
     */
    void terminateTest()
    {
        finishTest();
    }

    /**
     * Delays test termination until the specified timeout (in milliseconds). Wrapper of
     * {@link #delayTestFinish(int)}. Will be used in {@link RemoteConsole}.
     */
    void delayTestTermination(final int timeoutMillis)
    {
        delayTestFinish(timeoutMillis);
    }

    protected void setUpTest() throws Exception
    {
    }

    protected void tearDownTest() throws Exception
    {
    }

    //
    // GWTTestCase
    //

    @Override
    public final String getModuleName()
    {
        return "ch.systemsx.cisd.openbis.OpenBIS";
    }

    @Override
    protected final void gwtSetUp() throws Exception
    {
        System.out.println("TEST: " + getName() + " (started at " + new Date() + ")");
        remoteConsole = new RemoteConsole(this);
        client = new Client();
        GWTUtils.testing();
        setUpTest();
    }

    @Override
    protected final void gwtTearDown() throws Exception
    {
        remoteConsole.cancelTimer();
        AbstractAsyncCallback.setAllCallbackObjectsSilent();
        final IViewContext<IGenericClientServiceAsync> viewContext = client.tryToGetViewContext();
        if (viewContext != null)
        {
            viewContext.getService().logout(new AsyncCallback<Void>()
                {
                    public void onSuccess(final Void result)
                    {
                    }

                    public void onFailure(final Throwable caught)
                    {
                        System.out.println("LOGOUT FAILED: " + caught);
                    }
                });
        }
        tearDownTest();
    }

}
