/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.cli;

import java.io.PrintStream;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IResult;

/**
 * Prints the results of a dataset archive batch verification.
 * 
 * @author anttil
 */
public class ResultPrinter
{

    private final PrintStream out;

    public ResultPrinter(PrintStream out)
    {
        this.out = out;
    }

    public void print(Map<String, IResult> results)
    {
        int ok = 0;
        int failed = 0;
        int notTested = 0;

        for (String dataSet : results.keySet())
        {
            IResult result = results.get(dataSet);
            boolean success = result.success();
            String file = result.getFile();

            if (success)
            {
                out.println("OK - " + dataSet + " (" + file + ")");
                ok++;
            } else if (file != null)
            {
                out.println("FAILED - " + dataSet + " (" + file + ")");
                for (String error : result.getErrors())
                {
                    out.println("  " + error);
                }
                failed++;
            } else
            {
                out.println("NOT TESTED - " + dataSet + " (file not found)");
                notTested++;
            }
        }

        out.println();
        out.println("---");
        int total = ok + failed;
        out.println("Total of " + total + " dataset archives tested.");
        if (failed == 0)
        {
            out.println("No errors found");
        } else
        {
            out.println("Errors found in " + failed + " archive file(s).");
        }

        if (notTested > 0)
        {
            out.println("Could not find archive file for " + notTested + " dataset(s).");
        }
    }
}
