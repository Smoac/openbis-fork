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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A queue of requests for data from the server. Users of the queue can manage and control when
 * requests are actually sent. If the queue is set to process immediately, requests are not queued,
 * but executed on arrival.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ServerRequestQueue
{

    /**
     * Interface implemented by things in the queue
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IServerRequestAction
    {
        /**
         * Run the request
         */
        void onInvoke();

        /**
         * An object used to identify the request. Requests with the same identifier are considered
         * to be duplicates.
         */
        Object getIdentifier();
    }

    /**
     * A default implementation of IServerRequestAction which subclasses may extend.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static abstract class ServerRequestAction implements IServerRequestAction
    {
        final Object identifier;

        public ServerRequestAction(Object identifier)
        {
            this.identifier = identifier;
        }

        public Object getIdentifier()
        {
            return identifier;
        }
    }

    // Internal State
    private ArrayList<IServerRequestAction> requests = new ArrayList<IServerRequestAction>();

    private boolean processImmediately;

    // Public API
    /**
     * Create a new Queue. By default, do not process immediately.
     */
    public ServerRequestQueue()
    {
        setProcessImmediately(false);
    }

    /**
     * If set to true, requests will not be queued, they will be processed on add.
     */
    public void setProcessImmediately(boolean processImmediately)
    {
        this.processImmediately = processImmediately;
    }

    /**
     * Queue a request.
     */
    public void addRequestToQueue(IServerRequestAction requestAction)
    {
        if (isProcessImmediately())
        {
            requestAction.onInvoke();
        } else
        {
            requests.add(requestAction);
        }
    }

    /**
     * Process the requests in the queue. Duplicates are skipped.
     */
    public void processUniqueRequests()
    {
        HashSet<Object> processedIdentifiers = new HashSet<Object>();
        for (IServerRequestAction request : requests)
        {
            if (processedIdentifiers.contains(request.getIdentifier()) == false)
            {
                processedIdentifiers.add(request.getIdentifier());
                request.onInvoke();
            }
        }
        requests.clear();
    }

    // Internal API
    /**
     * True if requests are to be processed immediately when they are added to the queue
     */
    protected boolean isProcessImmediately()
    {
        return processImmediately;
    }

}
