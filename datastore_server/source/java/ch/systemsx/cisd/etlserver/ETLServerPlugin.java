/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;


/**
 * ETL Server plugin as a bean.
 * 
 * @author Franz-Josef Elmer
 */
public class ETLServerPlugin implements IETLServerPlugin
{
    private final IDataSetInfoExtractor codeExtractor;

    private final ITypeExtractor typeExtractor;

    private final IStorageProcessor storageProcessor;

    /**
     * Creates an instance with the specified extractors.
     */
    public ETLServerPlugin(final IDataSetInfoExtractor codeExtractor,
            final ITypeExtractor typeExtractor,
            final IStorageProcessor storageProcessor)
    {
        assert codeExtractor != null : "Missing code extractor";
        assert typeExtractor != null : "Missing type extractor";
        assert storageProcessor != null : "Missing storage processor";

        this.codeExtractor = codeExtractor;
        this.typeExtractor = typeExtractor;
        this.storageProcessor = storageProcessor;
    }

    //
    // IETLServerPlugin
    //

    public final IDataSetInfoExtractor getDataSetInfoExtractor()
    {
        return codeExtractor;
    }

    public final ITypeExtractor getTypeExtractor()
    {
        return typeExtractor;
    }

    public final IStorageProcessor getStorageProcessor()
    {
        return storageProcessor;
    }

    public IDataSetHandler getDataSetHandler(IDataSetHandler primaryDataSetHandler)
    {
        return primaryDataSetHandler;
    }
}
