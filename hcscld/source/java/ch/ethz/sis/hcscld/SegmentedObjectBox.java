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

import ch.systemsx.cisd.hdf5.BitSetConversionUtils;

/**
 * A class representing a box that contains a segmented object.
 *
 * @author Bernd Rinn
 */
class SegmentedObjectBox
{
    short minx, miny, maxx, maxy;

    int offsetInPixels;

    public SegmentedObjectBox()
    {
        this.minx = Short.MAX_VALUE;
        this.miny = Short.MAX_VALUE;
    }
    
    public SegmentedObjectBox(short minx, short miny, short maxx, short maxy, int offsetInPixels)
    {
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
        this.offsetInPixels = offsetInPixels;
    }

    /**
     * Returns the x coordinate of the left upper corner of the box.
     */
    public short getLeftUpperX()
    {
        return minx;
    }
    
    /**
     * Updates the left upper x coordinate if <var>newMinX</var> is smaller than the old value.
     */
    public void updateLeftUpperX(int newMinX)
    {
        this.minx = (short) Math.min(this.minx, newMinX);
    }

    /**
     * Returns the y coordinate of the left upper corner of the box.
     */
    public short getLeftUpperY()
    {
        return miny;
    }

    /**
     * Updates the left upper y coordinate if <var>newMinY</var> is smaller than the old value.
     */
    public void updateLeftUpperY(int newMinY)
    {
        this.miny = (short) Math.min(this.miny, newMinY);
    }

    /**
     * Returns the x coordinate of the right lower corner of the box.
     */
    public short getRightLowerX()
    {
        return maxx;
    }

    /**
     * Updates the right lower x coordinate if <var>newMaxX</var> is larger than the old value.
     */
    public void updateRightLowerX(int newMaxX)
    {
        this.maxx = (short) Math.max(this.maxx, newMaxX);
    }

    /**
     * Returns the y coordinate of the right lower corner of the box.
     */
    public short getRightLowerY()
    {
        return maxy;
    }
    
    /**
     * Updates the right lower y coordinate if <var>newMaxY</var> is larger than the old value.
     */
    public void updateRightLowerY(int newMaxY)
    {
        this.maxy = (short) Math.max(this.maxy, newMaxY);
    }

    /**
     * Returns the number of pixels of the box in the x dimension.
     */
    public short getWidth()
    {
        return (short) (maxx - minx + 1);
    }

    /**
     * Returns the number of pixels  of the box in the y dimension.
     */
    public short getHeight()
    {
        return (short) (maxy - miny + 1);
    }

    void checkInitialized() throws UninitalizedSegmentationException
    {
        if (minx == Short.MAX_VALUE  || miny == Short.MAX_VALUE)
        {
            throw new UninitalizedSegmentationException();
        }
    }
    
    boolean inBox(int x, int y)
    {
        return x >= minx && x <= maxx && y >= miny && y <= maxy;        
    }
    
    int getRelativeBitIndex(int x, int y)
    {
        return (x - minx) * (maxy - miny + 1) + (y - miny);
    }

    int getAbsoluteBitIndex(int x, int y)
    {
        return getOffsetInPixels() + getRelativeBitIndex(x, y);
    }

    /**
     * Returns the size of the segmented objects (in pixels). 
     */
    public int getSizeInPixels()
    {
        return getWidth() * getHeight();
    }
    
    /**
     * Returns the size of the segmented objects (in 64 bit words). 
     */
    public int getSizeInWords()
    {
        return BitSetConversionUtils.getWordIndex(getSizeInPixels() - 1) + 1;
    }
    
    /**
     * Returns the offset of this box (in bits) in the segmentation mask object.
     */
    int getOffsetInPixels()
    {
        return offsetInPixels;
    }

    /**
     * Returns the offset of this box (in 64 bit words) in the segmentation mask object.
     */
    int getOffsetInWords()
    {
        return BitSetConversionUtils.getWordIndex(getOffsetInPixels() - 1) + 1;
    }

    /**
     * Sets the offset (in pixels) of this box in the segmentation mask object.
     */
    void setOffset(int offsetInPixels)
    {
        this.offsetInPixels = offsetInPixels;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + maxx;
        result = prime * result + maxy;
        result = prime * result + minx;
        result = prime * result + miny;
        result = prime * result + offsetInPixels;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        SegmentedObjectBox other = (SegmentedObjectBox) obj;
        if (maxx != other.maxx)
        {
            return false;
        }
        if (maxy != other.maxy)
        {
            return false;
        }
        if (minx != other.minx)
        {
            return false;
        }
        if (miny != other.miny)
        {
            return false;
        }
        if (offsetInPixels != other.offsetInPixels)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "SegmentedObjectBox [minx=" + minx + ", miny=" + miny + ", maxx=" + maxx
                + ", maxy=" + maxy + ", offsetInPixels=" + offsetInPixels + "]";
    }
}
