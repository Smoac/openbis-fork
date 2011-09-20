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

package ch.ethz.cisd.hcscld;

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

    public SegmentedObject()
    {
        super();
    }

    public SegmentedObject(short minx, short miny, short maxx, short maxy, BitSet mask)
    {
        this(minx, miny, maxx, maxy, mask, null);
    }

    SegmentedObject(SegmentedObjectBox box, BitSet mask)
    {
        this(box.minx, box.miny, box.maxx, box.maxy, mask);
    }

    public SegmentedObject(short minx, short miny, short maxx, short maxy, BitSet mask,
            BitSet edgeMaskOrNull)
    {
        super(minx, miny, maxx, maxy);
        assert mask != null;
        this.mask = mask;
        this.edgeMaskOrNull = edgeMaskOrNull;
    }

    SegmentedObject(SegmentedObjectBox box, BitSet mask, BitSet edgeMaskOrNull)
    {
        this(box.minx, box.miny, box.maxx, box.maxy, mask, edgeMaskOrNull);
    }

    /**
     * Returns the bit field mask of this segmented object.
     */
    public BitSet getMask()
    {
        return mask;
    }
    
    /**
     * Returns the mask point (<var>x</var>,<var>y</var>).
     */
    public boolean getMaskPoint(int x, int y)
    {
        return this.mask.get(getRelativeBitIndex(x, y));
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
        if (mask == null)
        {
            this.mask = new BitSet(getSizeInPixels());
        }
        this.mask.set(getRelativeBitIndex(x, y));
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
