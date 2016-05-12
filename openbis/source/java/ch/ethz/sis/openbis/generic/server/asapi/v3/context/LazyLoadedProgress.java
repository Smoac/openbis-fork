/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.context;

/**
 * @author pkupczyk
 */
public abstract class LazyLoadedProgress implements IProgress
{

    private static final long serialVersionUID = 1L;

    private IProgress loadedProgress;

    protected abstract IProgress load();

    @Override
    public String getLabel()
    {
        return getLoadedProgress().getLabel();
    }

    @Override
    public String getDetails()
    {
        return getLoadedProgress().getDetails();
    }

    @Override
    public Integer getTotalItemsToProcess()
    {
        return getLoadedProgress().getTotalItemsToProcess();
    }

    @Override
    public Integer getNumItemsProcessed()
    {
        return getLoadedProgress().getNumItemsProcessed();
    }

    @Override
    public String toString()
    {
        return getLoadedProgress().toString();
    }

    private IProgress getLoadedProgress()
    {
        if (loadedProgress == null)
        {
            loadedProgress = load();
        }
        return loadedProgress;
    }

}
