/*
 * Copyright 2012-2013 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Random;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.hcscld.MDIntArray2DRowSort;
import ch.systemsx.cisd.base.mdarray.MDIntArray;

/**
 * Test cases for {@link MDIntArray2DRowSort}.
 * 
 * @author Bernd Rinn
 */
public class MDIntArray2DRowSortTest
{
    private static void checkSorted(MDIntArray a, int column, int startRow, int endRow)
    {
        int oldValue = a.get(startRow, column);
        for (int row = startRow + 1; row < endRow; ++row)
        {
            final int newValue = a.get(row, column);
            assertTrue(newValue >= oldValue);
            oldValue = newValue;
        }
    }

    @DataProvider(name = "lengths")
    static Object[][] getLengths()
    {
        return new Object[][]
            { 
                new Object[]
                { 10, 0, 1 }, new Object[]
                { 100, 0, 1 }, new Object[]
                { 1000, 0, 1 }, new Object[]
                { 10000, 0, 1 }, 
                new Object[]
                { 10, 1, 0 }, new Object[]
                { 100, 1, 0 }, new Object[]
                { 1000, 1, 0 }, new Object[]
                { 10000, 1, 0 } 
                };
    }

    @Test(invocationCount = 3, dataProvider = "lengths")
    public void testTwoStepSort(int n, int primSortCol, int secSortCol)
    {
        final Random rng = new Random();
        final MDIntArray arr = new MDIntArray(new int[]
            { n, 2 });
        for (int x = 0; x < arr.dimensions()[0]; ++x)
        {
            for (int y = 0; y < arr.dimensions()[1]; ++y)
            {
                arr.set(rng.nextInt(1000), x, y);
            }
        }
        MDIntArray2DRowSort.twoStepRowSort(arr, primSortCol, secSortCol);
        checkSorted(arr, primSortCol, 0, arr.dimensions()[primSortCol]);
        int startIdx = 0;
        while (startIdx < arr.dimensions()[primSortCol])
        {
            int endIdx = MDIntArray2DRowSort.findLastRow(arr, primSortCol, startIdx);
            if (startIdx != endIdx)
            {
                checkSorted(arr, secSortCol, startIdx, endIdx);
            }
            startIdx = endIdx;
        }
    }

    @Test(invocationCount = 3, dataProvider = "lengths")
    public void testBinarySearch(int n, int primSortCol, int secSortCol)
    {
        final Random rng = new Random();
        final MDIntArray arr = new MDIntArray(new int[]
            { n, 2 });
        for (int x = 0; x < arr.dimensions()[0]; ++x)
        {
            for (int y = 0; y < arr.dimensions()[1]; ++y)
            {
                arr.set(rng.nextInt(1000), x, y);
            }
        }
        MDIntArray2DRowSort.twoStepRowSort(arr, primSortCol, secSortCol);
        checkSorted(arr, primSortCol, 0, arr.dimensions()[primSortCol]);
        final int searchKey = rng.nextInt(1000);
        int startIdx = MDIntArray2DRowSort.binaryRowSearch(arr, primSortCol, searchKey);
        int endIdx = MDIntArray2DRowSort.findLastRow(arr, primSortCol, startIdx);
        if (startIdx != endIdx)
        {
            checkSorted(arr, secSortCol, startIdx, endIdx);
        } else
        {
            for (int row = 0; row < arr.size(0); ++row)
            {
                if (arr.get(row, primSortCol) == searchKey)
                {
                    throw new AssertionError(searchKey);
                }
            }
        }
    }

}
