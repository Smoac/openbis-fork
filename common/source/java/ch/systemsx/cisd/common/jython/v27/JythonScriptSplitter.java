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

package ch.systemsx.cisd.common.jython.v27;

import java.util.List;

import org.python27.core.PyException;

import ch.systemsx.cisd.common.jython.JythonScript;
import ch.systemsx.cisd.common.jython.JythonScriptBatch;
import ch.systemsx.cisd.common.jython.JythonScriptBatches;

/**
 * Splits jython code into smaller batches to overcome 64KB script size limitation.
 * 
 * @author pkupczyk
 */
public class JythonScriptSplitter
{

    private static final int DEFAULT_BATCH_SIZE = 100;

    private int batchSize = DEFAULT_BATCH_SIZE;

    public List<String> split(String scriptToSplit) throws PyException
    {
        JythonScript script = new JythonScript(scriptToSplit);
        JythonScriptBatches batches = new JythonScriptBatches();
        JythonScriptBatch batch = new JythonScriptBatch();
        JythonScriptCommand command = new JythonScriptCommand();

        for (String line : script.getLines())
        {
            if (command.getSize() > 0 && command.isNextCommand(line))
            {
                if (batch.getSize() > 0 && batch.getSize() + command.getSize() > getBatchSize())
                {
                    batches.addBatch(batch);
                    batch = new JythonScriptBatch();
                }
                batch.addLines(command);
                command = new JythonScriptCommand();
                command.addLine(line);
            } else
            {
                command.addLine(line);
            }
        }

        if (command.getSize() > 0)
        {
            batch.addLines(command);
        }
        if (batch.getSize() > 0)
        {
            batches.addBatch(batch);
        }

        return batches.getLines();
    }

    public int getBatchSize()
    {
        return batchSize;
    }

    public void setBatchSize(int batchSize)
    {
        if (batchSize <= 0)
        {
            throw new IllegalArgumentException("Batch size must be > 0");
        }
        this.batchSize = batchSize;
    }

}
