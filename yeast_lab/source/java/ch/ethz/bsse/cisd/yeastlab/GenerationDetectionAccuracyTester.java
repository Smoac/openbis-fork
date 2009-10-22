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
    public final static Map<Integer, Integer> parents = new HashMap<Integer, Integer>();

    static
    {
        parents.put(27, 20);
        parents.put(28, 5);
        parents.put(29, 10);
        parents.put(30, 1);
        parents.put(31, 12);
        parents.put(32, 16);
        parents.put(36, 0);
        parents.put(39, 7);
        parents.put(40, 34);
        parents.put(41, 21);
        parents.put(46, 3);
        parents.put(48, 47);
        parents.put(49, 20);
        parents.put(51, 24);
        parents.put(52, 13);
        parents.put(54, 48);
        parents.put(55, 25);
        parents.put(58, 48);
        parents.put(56, 7);
        parents.put(61, 45);
        parents.put(60, 5);
        parents.put(65, 14);
        parents.put(68, 47);
        parents.put(70, 46);
        parents.put(71, 20);
        parents.put(77, 24);
        parents.put(78, 12);
        parents.put(79, 25);
        parents.put(81, 56);
        parents.put(84, 5);
        parents.put(85, 49);
        parents.put(89, 20);
        parents.put(93, 76);
        parents.put(96, 92);
        parents.put(98, 3);
        parents.put(101, 15);
        parents.put(103, 78);
        parents.put(102, 7);
        parents.put(106, 70);
        parents.put(118, 24);
        parents.put(123, 5);
        parents.put(124, 102);
        parents.put(125, 92);
        parents.put(128, 23);
        parents.put(129, 25);
        parents.put(131, 44);
        parents.put(132, 31);
    }

    public static void computeResultsAccuracy(List<Cell> results)
    {
        final List<Cell> rightResults = new ArrayList<Cell>(results.size());
        final List<Cell> wrongResults = new ArrayList<Cell>(results.size());
        final List<Cell> fakeResults = new ArrayList<Cell>(results.size());
        final Set<Integer> missingChildrenIds = new HashSet<Integer>(parents.keySet());

        for (Cell result : results)
        {
            Integer resultParentId = result.getParentCellId();
            Integer rightParentId = parents.get(result.getId());
            if (rightParentId == null)
            {
                fakeResults.add(result);
            } else
            {
                if (resultParentId.equals(rightParentId))
                {
                    rightResults.add(result);
                } else
                {
                    wrongResults.add(result);
                }
            }
            missingChildrenIds.remove(result.getId());
        }

        final int rightResultsSize = rightResults.size();
        final int wrongResultsSize = wrongResults.size();
        final int fakeResultsSize = fakeResults.size();
        final int missingResultsSize = missingChildrenIds.size();
        final int resultsSize = results.size();
        final int resultsWithoutFakeSize = resultsSize - fakeResultsSize;

        System.out.println("\nAccuracy results:\n");
        System.out.println(percentageMessage("all", "", resultsSize, parents.size()));
        System.out.println(percentageMessage("right", "", rightResultsSize, resultsSize));
        System.out.println(percentageMessage("right", "(ignoring fake)", rightResultsSize,
                resultsWithoutFakeSize));
        System.out.println(percentageMessage("wrong", "", wrongResultsSize, resultsSize));
        System.out.println(percentageMessage("wrong", "(ignoring fake)", wrongResultsSize,
                resultsWithoutFakeSize));
        System.out.println(percentageMessage("fake", "", fakeResultsSize, resultsSize));
        System.out.println(percentageMessage("miss", "", missingResultsSize, parents.size()));
        if (missingResultsSize > 0)
        {
            System.out.println("\nMissing:\n");
            System.out.println(StringUtils.join(missingChildrenIds, ","));
        }
        System.out.println("\nAlternatives:\n");
        System.out.println(alternativesMessage("all", "", results));
        System.out.println(alternativesMessage("right", "", rightResults));
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
