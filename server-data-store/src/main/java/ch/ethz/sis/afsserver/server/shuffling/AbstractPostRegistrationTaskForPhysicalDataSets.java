/*
 * Copyright ETH 2013 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.server.shuffling;

import java.util.Properties;

/**
 * Abstract super class for post registration of physical data sets.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractPostRegistrationTaskForPhysicalDataSets extends AbstractPostRegistrationTask
{

    public AbstractPostRegistrationTaskForPhysicalDataSets(Properties properties,
            IEncapsulatedOpenBISService service)
    {
        super(properties, service);
    }

    /**
     * do not allow concurrent maintenance tasks to run if they alter the data store contents.
     */
    @Override
    public boolean requiresDataStoreLock()
    {
        return true;
    }

    @Override
    public final IPostRegistrationTaskExecutor createExecutor(String dataSetCode, boolean container)
    {
        if (container)
        {
            return DummyPostRegistrationTaskExecutor.INSTANCE;
        }
        return createExecutor(dataSetCode);
    }

    protected abstract IPostRegistrationTaskExecutor createExecutor(String dataSetCode);

}
