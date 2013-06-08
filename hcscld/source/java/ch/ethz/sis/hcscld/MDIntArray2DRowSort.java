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

import ch.systemsx.cisd.base.mdarray.MDIntArray;

/**
 * A method to sort the rows of a {@link MDIntArray} of rank 2 by a given sort column.
 * <p>
 * Based on code in java.lang.Arrays by Josh Bloch and Neal Gafter.
 * 
 * @author Bernd Rinn
 * @author Josh Bloch
 * @author Neal Gafter
 */
class MDIntArray2DRowSort
{

    /**
     * Searches the specified array of ints for the specified value using the binary search
     * algorithm. The array <strong>must</strong> be of rank 2 and sorted on <var>column</var> (as
     * by the <tt>sort</tt> method, below) prior to making this call. If it is not sorted, the
     * results are undefined. If the array contains multiple elements with the specified value, it
     * will return the lowest index.
     * 
     * @param a the array to be searched.
     * @param column the (sorted) column that is used for searching.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the list; otherwise,
     *         <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion point</i> is defined as
     *         the point at which the key would be inserted into the list: the index of the first
     *         element greater than the key, or <tt>list.size()</tt>, if all elements in the list
     *         are less than the specified key. Note that this guarantees that the return value will
     *         be &gt;= 0 if and only if the key is found.
     * @see #rowSort(MDIntArray, int)
     */
    static int binaryRowSearch(MDIntArray a, int column, int key)
    {
        return binaryRowSearch(a, 0, a.dimensions()[0], column, key);
    }

    /**
     * Searches the specified array of ints for the specified value using the binary search
     * algorithm. The array <strong>must</strong> be of rank 2 and sorted on <var>column</var> (as
     * by the <tt>sort</tt> method, below) prior to making this call. If it is not sorted, the
     * results are undefined. If the array contains multiple elements with the specified value, it
     * will return the lowest index.
     * 
     * @param a the array to be searched.
     * @param startRow the first row to consider.
     * @param endRow the last to consider plus 1.
     * @param column the (sorted) column that is used for searching.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the list; otherwise,
     *         <tt>(-(<i>insertion point</i>) - 1)</tt>. The <i>insertion point</i> is defined as
     *         the point at which the key would be inserted into the list: the index of the first
     *         element greater than the key, or <tt>list.size()</tt>, if all elements in the list
     *         are less than the specified key. Note that this guarantees that the return value will
     *         be &gt;= 0 if and only if the key is found.
     * @see #rowSort(MDIntArray, int)
     */
    static int binaryRowSearch(MDIntArray a, int startRow, int endRow, int column, int key)
    {
        if (a.rank() != 2)
        {
            throw new IllegalArgumentException("binarySearch expects MDIntArray of rank 2");
        }

        int low = startRow;
        int high = endRow - 1;

        while (low <= high)
        {
            int mid = (low + high) >> 1;
            int midVal = a.get(mid, column);

            if (midVal < key)
            {
                low = mid + 1;
            } else if (midVal > key)
            {
                high = mid - 1;
            } else
            {
                while (mid > 0 && key == a.get(mid - 1, column))
                {
                    --mid;
                }
                return mid; // key found
            }
        }
        return -(low + 1); // key not found.
    }

    /**
     * Returns the last row that has the same value as <var>firstRow</var> in <var>column</var>.
     */
    static int findLastRow(MDIntArray a, int column, int firstRow)
    {
        if (firstRow < 0)
        {
            return firstRow;
        }
        final int numRows = a.dimensions()[0];
        final int val = a.get(firstRow, column);
        int lastRow = firstRow;
        do
        {
            ++lastRow;
        } while (lastRow < numRows && a.get(lastRow, column) == val);
        return lastRow;
    }

    /**
     * Sorts the specified matrix (of rank 2) of ints by column <var>sortCol</var> into ascending
     * numerical order. The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and
     * M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice and Experience, Vol.
     * 23(11) P. 1249-1265 (November 1993). This algorithm offers n*log(n) performance on many data
     * sets that cause other quicksorts to degrade to quadratic performance.
     * 
     * @param a the array to be sorted. <strong>Must</strong> be of rank 2.
     * @param sortCol The column to sort by.
     */
    static void rowSort(MDIntArray a, int sortCol)
    {
        final int[] arr = a.getAsFlatArray();
        final int[] dims = a.dimensions();
        rowSort1(arr, dims[0], dims[1], sortCol, 0);
    }

    /**
     * Sorts the specified matrix (of rank 2) of ints by column <var>sortCol</var> into ascending
     * numerical order. The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and
     * M. Douglas McIlroy's "Engineering a Sort Function", Software-Practice and Experience, Vol.
     * 23(11) P. 1249-1265 (November 1993). This algorithm offers n*log(n) performance on many data
     * sets that cause other quicksorts to degrade to quadratic performance.
     * 
     * @param a the array to be sorted. <strong>Must</strong> be of rank 2.
     * @param primarySortCol The first column to sort by.
     * @param secondarySortCol The second column to sort by.
     */
    static void twoStepRowSort(MDIntArray a, int primarySortCol, int secondarySortCol)
    {
        final int[] arr = a.getAsFlatArray();
        final int[] dims = a.dimensions();
        final int rowLen = dims[1];
        final int numberOfRowsTotal = dims[0];
        rowSort1(arr, numberOfRowsTotal, rowLen, primarySortCol, 0);
        int mainRowIdxStart = 0;
        int mainRowIdxEnd = 0;
        while (mainRowIdxStart < numberOfRowsTotal)
        {
            final int mainColValue = a.get(mainRowIdxStart, primarySortCol);
            mainRowIdxEnd = mainRowIdxStart + 1;
            while (mainRowIdxEnd < numberOfRowsTotal
                    && a.get(mainRowIdxEnd, primarySortCol) == mainColValue)
            {
                ++mainRowIdxEnd;
            }
            final int numberOfRowsToSort = mainRowIdxEnd - mainRowIdxStart;
            if (numberOfRowsToSort > 1)
            {
                rowSort1(arr, numberOfRowsToSort, rowLen, secondarySortCol, mainRowIdxStart);
            }
            mainRowIdxStart = mainRowIdxEnd;
        }
    }

    /**
     * Sorts the specified range of the specified array of ints into ascending numerical order. The
     * range to be sorted extends from index <tt>fromIndex</tt>, inclusive, to index
     * <tt>toIndex</tt>, exclusive. (If <tt>fromIndex==toIndex</tt>, the range to be sorted is
     * empty.)
     * <p>
     * The sorting algorithm is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas
     * McIlroy's "Engineering a Sort Function", Software-Practice and Experience, Vol. 23(11) P.
     * 1249-1265 (November 1993). This algorithm offers n*log(n) performance on many data sets that
     * cause other quicksorts to degrade to quadratic performance.
     * 
     * @param a the array to be sorted.
     * @param sortCol The column to sort by.
     * @param fromIndex the index of the first element (inclusive) to be sorted.
     * @param toIndex the index of the last element (exclusive) to be sorted.
     * @throws IllegalArgumentException if <tt>fromIndex &gt; toIndex</tt>
     * @throws ArrayIndexOutOfBoundsException if <tt>fromIndex &lt; 0</tt> or
     *             <tt>toIndex &gt; a.length</tt>
     */
    static void rowSort(MDIntArray a, int sortCol, int fromIndex, int toIndex)
    {
        if (a.rank() != 2)
        {
            throw new IllegalArgumentException("sort expects MDIntArray of rank 2");
        }
        final int[] arr = a.getAsFlatArray();
        final int[] dims = a.dimensions();
        rangeCheck(dims[0], fromIndex, toIndex);
        rowSort1(arr, toIndex - fromIndex, dims[1], sortCol, fromIndex);
    }

    /**
     * Check that fromIndex and toIndex are in range, and throw an appropriate exception if they
     * aren't.
     */
    private static void rangeCheck(int arrayLen, int fromIndex, int toIndex)
    {
        if (fromIndex > toIndex)
        {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex
                    + ")");
        }
        if (fromIndex < 0)
        {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > arrayLen)
        {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        }
    }

    private static int computeIndex(int rowLen, int col, int row)
    {
        return rowLen * row + col;
    }

    /**
     * Sorts the specified sub-array of integers into ascending order.
     */
    private static void rowSort1(int[] x, int colLen, int rowLen, int sortCol, int off)
    {
        // Insertion sort on smallest arrays
        if (colLen < 7)
        {
            for (int i = off; i < colLen + off; i++)
            {
                for (int j = i; j > off
                        && x[computeIndex(rowLen, sortCol, j - 1)] > x[computeIndex(rowLen,
                                sortCol, j)]; j--)
                {
                    swap(x, rowLen, j, j - 1);
                }
            }
            return;
        }

        // Choose a partition element, v
        int m = off + (colLen >> 1); // Small arrays, middle element
        if (colLen > 7)
        {
            int l = off;
            int n = off + colLen - 1;
            if (colLen > 40)
            { // Big arrays, pseudomedian of 9
                int s = colLen / 8;
                l = med3(x, rowLen, sortCol, l, l + s, l + 2 * s);
                m = med3(x, rowLen, sortCol, m - s, m, m + s);
                n = med3(x, rowLen, sortCol, n - 2 * s, n - s, n);
            }
            m = med3(x, rowLen, sortCol, l, m, n); // Mid-size, med of 3
        }
        int v = x[computeIndex(rowLen, sortCol, m)];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + colLen - 1, d = c;
        while (true)
        {
            int w;
            while (b <= c && (w = x[computeIndex(rowLen, sortCol, b)]) <= v)
            {
                if (w == v)
                {
                    swap(x, rowLen, a++, b);
                }
                b++;
            }
            while (c >= b && (w = x[computeIndex(rowLen, sortCol, c)]) >= v)
            {
                if (w == v)
                {
                    swap(x, rowLen, c, d--);
                }
                c--;
            }
            if (b > c)
            {
                break;
            }
            swap(x, rowLen, b++, c--);
        }

        // Swap partition elements back to middle
        int s, n = off + colLen;
        s = Math.min(a - off, b - a);
        vecswap(x, rowLen, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(x, rowLen, b, n - s, s);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1)
        {
            rowSort1(x, s, rowLen, sortCol, off);
        }
        if ((s = d - c) > 1)
        {
            rowSort1(x, s, rowLen, sortCol, n - s);
        }
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(int[] x, int rowLen, int a, int b)
    {
        int idxA = rowLen * a;
        int idxB = rowLen * b;
        if (rowLen < 5)
        {
            for (int i = 0; i < rowLen; ++i, ++idxA, ++idxB)
            {
                int t = x[idxA];
                x[idxA] = x[idxB];
                x[idxB] = t;
            }
        } else
        {
            final int[] t = new int[rowLen];
            System.arraycopy(x, idxA, t, 0, rowLen);
            System.arraycopy(x, idxB, x, idxA, rowLen);
            System.arraycopy(t, 0, x, idxB, rowLen);
        }
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(int x[], int rowLen, int a, int b, int n)
    {
        int idxA = rowLen * a;
        int idxB = rowLen * b;
        final int swapLen = rowLen * n;
        if (swapLen < 5)
        {
            for (int i = 0; i < swapLen; ++i, ++idxA, ++idxB)
            {
                int t = x[idxA];
                x[idxA] = x[idxB];
                x[idxB] = t;
            }
        } else
        {
            final int[] t = new int[swapLen];
            System.arraycopy(x, idxA, t, 0, swapLen);
            System.arraycopy(x, idxB, x, idxA, swapLen);
            System.arraycopy(t, 0, x, idxB, swapLen);
        }
    }

    /**
     * Returns the index of the median of the three indexed integers.
     */
    private static int med3(int x[], int rowLen, int sortCol, int a, int b, int c)
    {
        final int xa = x[computeIndex(rowLen, sortCol, a)];
        final int xb = x[computeIndex(rowLen, sortCol, b)];
        final int xc = x[computeIndex(rowLen, sortCol, c)];
        return (xa < xb ? (xb < xc ? b : xa < xc ? c : a) : (xb > xc ? b : xa > xc ? c : a));
    }
}
