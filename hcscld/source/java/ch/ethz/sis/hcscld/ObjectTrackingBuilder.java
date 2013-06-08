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
 * A class that supports building a tracking of objects, represented as a linking of parent to child
 * objects.
 * 
 * @author Bernd Rinn
 */
public class ObjectTrackingBuilder
{
    private final static int INITIAL_CAPACITY = 200;

    private final static int PARENT_COL = 0;

    private final static int CHILD_COL = 1;

    private MDIntArray linking;

    private int largestIndex = -1;

    enum StorageForm
    {
        UNSIGNED_BYTE(1), UNSIGNED_SHORT(2), UNSIGNED_INT(2);

        final int sizeInBytes;

        StorageForm(int sizeInBytes)
        {
            this.sizeInBytes = sizeInBytes;
        }

        int getSizeInBytes()
        {
            return sizeInBytes;
        }
    }

    public ObjectTrackingBuilder()
    {
        this.linking = new MDIntArray(new int[]
            { 0, 2 }, INITIAL_CAPACITY);
    }

    /**
     * Add a new link from <var>parentId</var> to <var>childId</var>.
     */
    public void addLink(int parentId, int childId)
    {
        linking.incNumberOfHyperRows(1);
        linking.set(parentId, linking.size(0) - 1, PARENT_COL);
        linking.set(childId, linking.size(0) - 1, CHILD_COL);
        largestIndex = Math.max(Math.max(largestIndex, parentId), childId);
    }

    /**
     * Returns the sorted linking.
     */
    MDIntArray getLinking()
    {
        MDIntArray2DRowSort.twoStepRowSort(linking, PARENT_COL, CHILD_COL);
        final int noRows = linking.size(0);
        int lastParentId = -1, lastChildId = -1;
        final MDIntArray duplicateRows = new MDIntArray(new int[]
            { 0 });
        for (int row = 0; row < noRows; ++row)
        {
            final int parentId = linking.get(row, PARENT_COL);
            final int childId = linking.get(row, CHILD_COL);
            if (lastParentId == parentId && lastChildId == childId)
            {
                duplicateRows.incNumberOfHyperRows(1);
                duplicateRows.set(row, duplicateRows.size() - 1);
            }
            lastParentId = parentId;
            lastChildId = childId;
        }
        if (duplicateRows.size() > 0)
        {
            final int[] duplicates = duplicateRows.getAsFlatArray();
            final int[] oldLinkingArr = linking.getAsFlatArray();
            linking = new MDIntArray(new int[]
                { linking.size(0) - duplicateRows.size(), 2 });
            final int[] newLinkingArr = linking.getAsFlatArray();
            int startRow = 0, oldStartIdx = 0, newStartIdx = 0;
            for (int i = 0; i < duplicateRows.size(); ++i)
            {
                final int length = 2 * (duplicates[i] - startRow);
                System.arraycopy(oldLinkingArr, oldStartIdx, newLinkingArr, newStartIdx, length);
                startRow = duplicates[i] + 1;
                oldStartIdx += length + 2;
                newStartIdx += length;
            }
            System.arraycopy(oldLinkingArr, oldStartIdx, newLinkingArr, newStartIdx,
                    newLinkingArr.length - newStartIdx);
        }
        return linking;
    }

    StorageForm getStorageForm()
    {
        if (largestIndex < 256)
        {
            return StorageForm.UNSIGNED_BYTE;
        } else if (largestIndex < 65536)
        {
            return StorageForm.UNSIGNED_SHORT;
        } else
            return StorageForm.UNSIGNED_INT;
    }

    int getTotalSizeInBytes()
    {
        return getLinking().size() * getStorageForm().getSizeInBytes();
    }
    
}
