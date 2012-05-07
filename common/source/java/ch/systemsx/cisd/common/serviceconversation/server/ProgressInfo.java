/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.serviceconversation.server;

import java.io.Serializable;

public class ProgressInfo implements Serializable
{
    
    private static final long serialVersionUID = -3692359907946988354L;

    private String label;
    private int totalItemsToProcess;
    private int numItemsProcessed;
  
    public ProgressInfo(String label, int totalItemsToProcess, int numItemsProcessed) {
        this.label = label;
        this.totalItemsToProcess = totalItemsToProcess;
        this.numItemsProcessed = numItemsProcessed;
    }

    public String getLabel()
    {
        return label;
    }

    public int getTotalItemsToProcess()
    {
        return totalItemsToProcess;
    }

    public int getNumItemsProcessed()
    {
        return numItemsProcessed;
    }
    
    @Override
    public String toString() {
        return "ProgressInfo: "+this.label+" "+this.numItemsProcessed+"/"+this.totalItemsToProcess;
    }

}
