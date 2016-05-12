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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch;

import java.util.Map;

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.LazyLoadedProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
public abstract class MapBatchProcessor<K, V>
{

    public abstract void process(K key, V value);

    public abstract IProgress createProgress(K key, V value, int objectIndex, int totalObjectCount);

    public MapBatchProcessor(IOperationContext context, MapBatch<K, V> batch)
    {
        int objectIndex = batch.getFromObjectIndex() + 1;
        int totalObjectCount = batch.getTotalObjectCount();

        for (Map.Entry<K, V> entry : batch.getObjects().entrySet())
        {
            IProgress progress = new MapBatchLazyLoadedProgress(entry.getKey(), entry.getValue(), objectIndex, totalObjectCount);
            context.pushProgress(progress);
            process(entry.getKey(), entry.getValue());
            context.popProgress();
            objectIndex++;
        }
    }

    private class MapBatchLazyLoadedProgress extends LazyLoadedProgress
    {

        private static final long serialVersionUID = 1L;

        private K key;

        private V value;

        private int objectIndex;

        private int totalObjectCount;

        public MapBatchLazyLoadedProgress(K key, V value, int objectIndex, int totalObjectCount)
        {
            this.key = key;
            this.value = value;
            this.objectIndex = objectIndex;
            this.totalObjectCount = totalObjectCount;
        }

        @Override
        protected IProgress load()
        {
            return createProgress(key, value, objectIndex, totalObjectCount);
        }

    }

}
