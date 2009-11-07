/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.yeastlab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.ethz.bsse.cisd.yeastlab.GenerationDetection.Cell;

/**
 * @author Piotr Buczek
 */
public class GenerationDetectionAccuracyTester
{
    // the right connections for data in pos2 file: <child id, parent id>
    public static Map<Integer, Integer> correctParents;

    static void loadCorrectParents(final File file)
    {
        assert file != null : "Unspecified file";

        BufferedReader reader;
        try
        {
            reader = new BufferedReader(new FileReader(file));
            correctParents = new HashMap<Integer, Integer>();
            int lineCounter = 1;
            String line = reader.readLine();
            while ((line = reader.readLine()) != null)
            {
                // omit comments
                if (line.startsWith("#") == false)
                {
                    String[] tokens = line.split("\t");
                    if (tokens.length != 2)
                    {
                        System.err
                                .println("Every line in results file should contain exactly 2 tokens "
                                        + "but line " + lineCounter + " contains " + tokens.length);
                        correctParents = null;
                        return;
                    }
                    int childId = Integer.parseInt(tokens[0]);
                    int parentId = Integer.parseInt(tokens[1]);
                    correctParents.put(childId, parentId);
                }
                lineCounter++;
            }
        } catch (Exception ex)
        {
            System.err.println("Loading parents failed");
            ex.printStackTrace();
            correctParents = null;
            return;
        }
    }

    /**
     * Compares parent connections set in all cells in given collection to the ones specified in
     * given file.
     */
    static void computeResultsAccuracy(final List<Cell> results)
    {
        assert results != null;
        if (correctParents == null)
        {
            System.err.println("Can't compute accuracy - correct parents are not loaded");
        }

        final List<Cell> correctResults = new ArrayList<Cell>(results.size());
        final List<Cell> wrongResults = new ArrayList<Cell>(results.size());
        final List<Cell> fakeResults = new ArrayList<Cell>(results.size());
        final Set<Integer> missingChildrenIds = new HashSet<Integer>(correctParents.keySet());

        for (Cell result : results)
        {
            Integer resultParentId = result.getParentCellId();
            Integer rightParentId = correctParents.get(result.getId());
            if (rightParentId == null)
            {
                fakeResults.add(result);
            } else
            {
                if (resultParentId.equals(rightParentId))
                {
                    correctResults.add(result);
                } else
                {
                    wrongResults.add(result);
                }
            }
            missingChildrenIds.remove(result.getId());
        }

        final int correctResultsSize = correctResults.size();
        final int wrongResultsSize = wrongResults.size();
        final int fakeResultsSize = fakeResults.size();
        final int missingResultsSize = missingChildrenIds.size();
        final int resultsSize = results.size();
        final int resultsWithoutFakeSize = resultsSize - fakeResultsSize;

        System.out.println("\nAccuracy results:\n");
        System.out.println(percentageMessage("all", "", resultsSize, correctParents.size()));
        System.out.println(percentageMessage("right", "", correctResultsSize, resultsSize));
        System.out.println(percentageMessage("right", "(ignoring fake)", correctResultsSize,
                resultsWithoutFakeSize));
        System.out.println(percentageMessage("wrong", "", wrongResultsSize, resultsSize));
        System.out.println(percentageMessage("wrong", "(ignoring fake)", wrongResultsSize,
                resultsWithoutFakeSize));
        System.out.println(percentageMessage("fake", "", fakeResultsSize, resultsSize));
        System.out
                .println(percentageMessage("miss", "", missingResultsSize, correctParents.size()));
        if (missingResultsSize > 0)
        {
            System.out.println("\nMissing:\n");
            System.out.println(StringUtils.join(missingChildrenIds, ","));
        }
        System.out.println("\nAlternatives:\n");
        System.out.println(alternativesMessage("all", "", results));
        System.out.println(alternativesMessage("correct", "", correctResults));
        System.out.println(alternativesMessage("wrong", "", wrongResults));
        System.out.println(alternativesMessage("fake", "", fakeResults));
        final List<Cell> resultsWithoutFakes = new ArrayList<Cell>(results);
        results.removeAll(fakeResults);
        System.out.println(alternativesMessage("all", "(ignoring fake)", resultsWithoutFakes));
    }

    private static String percentageMessage(String prefix, String suffix, int numerator,
            int denominator)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(prefix + ":\t");
        sb.append(numerator + "/" + denominator);
        sb.append(" = " + (numerator * 100) / denominator + "%");
        sb.append(" " + suffix);
        return sb.toString();
    }

    private static String alternativesMessage(String prefix, String suffix, List<Cell> cells)
    {
        int sum = 0;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (Cell cell : cells)
        {
            final int current = cell.getAlternatives();
            sum += current;
            if (current > max)
            {
                max = current;
            }
            if (current < min)
            {
                min = current;
            }
        }
        return String.format("%s:\t min:%d\t max:%d\t mean:%1.2f %s", prefix, min, max,
                (double) sum / (double) cells.size(), suffix);
    }
}
