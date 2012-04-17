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

package ch.systemsx.cisd.common.jython;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.Py;
import org.python.core.PyException;

/**
 * @author pkupczyk
 */
public class JythonScriptSplitter
{

    private static final int DEFAULT_BATCH_SIZE = 100;

    private int batchSize = DEFAULT_BATCH_SIZE;

    public List<String> split(String scriptToSplit) throws PyException
    {
        List<JythonScriptBatch> batches = new ArrayList<JythonScriptBatch>();
        JythonScriptBatch batch = new JythonScriptBatch();
        JythonScript script = new JythonScript(scriptToSplit);

        for (String line : script.getLines())
        {
            if (isEmptyLine(line))
            {
                batch.addLine(line);
                continue;
            }
            if (batch.getSize() < getBatchSize())
            {
                batch.addLine(line);
            } else
            {
                if (compile(line))
                {
                    batches.add(batch);
                    batch = new JythonScriptBatch();
                    batch.addLine(line);
                } else
                {
                    batch.addLine(line);
                }
            }
        }

        if (batch.getSize() > 0)
        {
            batches.add(batch);
        }

        return convert(batches);
    }

    private boolean compile(String line)
    {
        if (isIndentedLine(line))
        {
            return false;
        } else
        {
            try
            {
                Py.compile_command_flags(line, "<input>", CompileMode.single, new CompilerFlags(),
                        true);
                return true;
            } catch (PyException e)
            {
                return false;
            }
        }
    }

    private List<String> convert(List<JythonScriptBatch> batches)
    {
        Iterator<JythonScriptBatch> iterator = batches.iterator();
        List<String> list = new ArrayList<String>();

        while (iterator.hasNext())
        {
            String lines = iterator.next().getLines();

            if (iterator.hasNext())
            {
                lines += "\n";
            }

            list.add(lines);
        }
        return list;
    }

    private boolean isEmptyLine(String line)
    {
        return line.trim().length() == 0;
    }

    private boolean isIndentedLine(String line)
    {
        return Character.isWhitespace(line.charAt(0));
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
