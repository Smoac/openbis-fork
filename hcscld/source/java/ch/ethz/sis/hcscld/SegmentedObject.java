/*
 * Copyright 2011 ETH Zuerich, CISD
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

import java.util.BitSet;

/**
 * A class representing an ImageSegment.
 * 
 * @author Bernd Rinn
 */
public class SegmentedObject extends SegmentedObjectBox
{
    private BitSet mask;

    private BitSet edgeMaskOrNull;

    private ObjectType objectType;

    private int objectIndex = -1;

    public SegmentedObject()
    {
        super();
    }

    public SegmentedObject(ObjectType objectType, short minx, short miny, short maxx, short maxy)
    {
        this(objectType, minx, miny, maxx, maxy, 0, -1, null, null);
    }

    public SegmentedObject(ObjectType objectType, short minx, short miny, short maxx, short maxy,
            BitSet mask)
    {
        this(objectType, minx, miny, maxx, maxy, 0, -1, mask, null);
    }

    public SegmentedObject(short minx, short miny, short maxx, short maxy, BitSet mask,
            BitSet edgeMaskOrNull)
    {
        this(null, minx, miny, maxx, maxy, 0, -1, mask, edgeMaskOrNull);
    }

    SegmentedObject(short minx, short miny, short maxx, short maxy, int offsetInPixels,
            int objectIndex, BitSet mask, BitSet edgeMaskOrNull)
    {
        this(null, minx, miny, maxx, maxy, offsetInPixels, objectIndex, mask, edgeMaskOrNull);
    }

    public SegmentedObject(short minx, short miny, short maxx, short maxy)
    {
        this(null, minx, miny, maxx, maxy, 0, -1, null, null);
    }

    public SegmentedObject(short minx, short miny, short maxx, short maxy, BitSet mask)
    {
        this(null, minx, miny, maxx, maxy, 0, -1, mask, null);
    }

    SegmentedObject(short minx, short miny, short maxx, short maxy, int offsetInPixels,
            int objectIndex, BitSet mask)
    {
        this(null, minx, miny, maxx, maxy, offsetInPixels, objectIndex, mask, null);
    }

    SegmentedObject(SegmentedObjectBox box, int objectIndex, BitSet mask)
    {
        this(box.minx, box.miny, box.maxx, box.maxy, box.offsetInPixels, objectIndex, mask);
    }

    SegmentedObject(SegmentedObjectBox box, int objectIndex, BitSet mask, BitSet edgeMaskOrNull)
    {
        this(box.minx, box.miny, box.maxx, box.maxy, box.offsetInPixels, objectIndex, mask,
                edgeMaskOrNull);
    }

    public SegmentedObject(ObjectType objectTypeOrNull, short minx, short miny, short maxx,
            short maxy, BitSet mask, BitSet edgeMaskOrNull)
    {
        this(objectTypeOrNull, minx, miny, maxx, maxy, 0, -1, mask, edgeMaskOrNull);
    }

    SegmentedObject(ObjectType objectTypeOrNull, short minx, short miny, short maxx, short maxy,
            int offsetInPixels, int objectIndex, BitSet mask, BitSet edgeMaskOrNull)
    {
        super(minx, miny, maxx, maxy, offsetInPixels);
        this.objectType = objectTypeOrNull;
        this.mask = mask;
        this.objectIndex = objectIndex;
        initializeMask();
        this.edgeMaskOrNull = edgeMaskOrNull;
    }

    /**
     * Returns the object type of this segmented object.
     */
    public ObjectType getObjectType()
    {
        return objectType;
    }

    void setObjectTypeOrNull(ObjectType objectTypeOrNull)
    {
        this.objectType = objectTypeOrNull;
    }

    /**
     * Returns the object index for this segmented object.
     */
    public int getObjectIndex()
    {
        return objectIndex;
    }

    void setObjectIndex(int objectIndex)
    {
        this.objectIndex = objectIndex;
    }

    /**
     * Returns the bit field mask of this segmented object.
     */
    public BitSet getMask()
    {
        return mask == null ? new BitSet() : mask;
    }

    /**
     * Returns the mask point (<var>x</var>,<var>y</var>).
     */
    public boolean getMaskPoint(int x, int y)
    {
        return inBox(x, y) && mask.get(getRelativeBitIndex(x, y));
    }

    /**
     * Returns the edge mask point (<var>x</var>,<var>y</var>).
     */
    public boolean getEdgeMaskPoint(int x, int y)
    {
        return edgeMaskOrNull != null ? this.edgeMaskOrNull.get(getRelativeBitIndex(x, y)) : false;
    }

    /**
     * Sets the mask point (<var>x</var>,<var>y</var>).
     */
    public void setMaskPoint(int x, int y)
    {
        initializeMask();
        this.mask.set(getRelativeBitIndex(x, y));
    }

    /**
     * Sets all mask points in the bounding box.
     */
    public void setAllMaskPoints()
    {
        initializeMask();
        final int len = getSizeInPixels();
        for (int i = 0; i < len; ++i)
        {
            this.mask.set(i);
        }
    }

    private void initializeMask()
    {
        if (mask == null)
        {
            this.mask = new BitSet(getSizeInPixels());
        }
    }

    /**
     * Returns the bit field mask of the edge of the segmented object, <code>null</code>, if no edge
     * mask has been set.
     */
    BitSet tryGetEdgeMask()
    {
        return edgeMaskOrNull;
    }

    /**
     * Sets the edge mask point (<var>x</var>,<var>y</var>).
     */
    public void setEdgeMaskPoint(int x, int y)
    {
        if (edgeMaskOrNull == null)
        {
            this.edgeMaskOrNull = new BitSet(getSizeInPixels());
        }
        this.edgeMaskOrNull.set(getRelativeBitIndex(x, y));
    }

}
