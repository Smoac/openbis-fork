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
import java.util.EnumMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IResult;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.ResultType;

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
        Map<ResultType, Integer> counts = createCountMap();

        for (String dataSet : results.keySet())
        {
            IResult result = results.get(dataSet);
            result.printTo(dataSet, out);
            ResultType type = result.getType();
            counts.put(type, counts.get(type) + 1);
        }

        printTotals(counts);
    }

    private Map<ResultType, Integer> createCountMap()
    {
        Map<ResultType, Integer> counts = new EnumMap<ResultType, Integer>(ResultType.class);
        for (ResultType type : ResultType.values())
        {
            counts.put(type, 0);
        }
        return counts;
    }

    private void printTotals(Map<ResultType, Integer> counts)
    {
        int ok = counts.get(ResultType.OK);
        int failed = counts.get(ResultType.FAILED);
        int notTested = counts.get(ResultType.SKIPPED);
        int fatal = counts.get(ResultType.FATAL);
        int total = ok + failed;

        if (fatal > 0)
        {
            return;
        }

        out.println();
        out.println("---");
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
