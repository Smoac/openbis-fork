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

import java.util.Iterator;

/**
 * The quantity structure (number of rows, columns, fields, and the length of the sequence of an HCS
 * dataset derived from an image sequence).
 * 
 * @author Bernd Rinn
 */
public class ImageQuantityStructure implements Iterable<ImageId>
{
    /** The type of the image sequence. */
    public enum SequenceType
    {
        /** No image sequence. */
        NONE,
        /** A custom image sequence. */
        CUSTOM,
        /** A time series, each sequence index corresponds to a value of t. */
        TIMESERIES,
        /** A scan of the focal plane, each sequence index corresponds to a value of z. */
        DEPTHSSCAN,
        /**
         * A combined time series and depth scan, each sequence index corresponds to a value pair
         * (t, z).
         */
        TIMESERIES_DEPTHSCAN,
    }

    private int numberOfRows;

    private int numberOfColumns;

    private int numberOfFields;

    private int sequenceLength;

    private SequenceType sequenceType;

    private boolean objectsIdenticalInSequence;

    // Used by JHDF5 when constructing the geometry from a compound.
    ImageQuantityStructure()
    {
    }

    /**
     * Creates a new quantity structure with the given well rows, well columns, fields per well and
     * length of sequence.
     * 
     * @param numberOfRows The number of well rows of the HCS plate.
     * @param numberOfColumns The number of well columns of the HCS plate.
     * @param numberOfFields The number of fields per well of the HCS dataset.
     * @param sequenceLength The length of the image sequence.
     * @param sequenceType The type of the image sequence.
     * @param objectsIdenticalInSequence <code>true</code>, if the object ids correspond to each
     *            other for all images in the sequence. That means that e.g. object 17 for sequence
     *            index 0 and sequence index 100 identify the same object.
     */
    public ImageQuantityStructure(int numberOfRows, int numberOfColumns, int numberOfFields,
            int sequenceLength, SequenceType sequenceType, boolean objectsIdenticalInSequence)
    {
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.numberOfFields = numberOfFields;
        this.sequenceLength = sequenceLength;
        this.sequenceType = sequenceType;
        this.objectsIdenticalInSequence = objectsIdenticalInSequence;
    }

    /**
     * Creates a new quantity structure with the given well rows, well columns, fields per well for
     * a sequence of length 1.
     * 
     * @param numberOfRows The number of well rows of the HCS plate.
     * @param numberOfColumns The number of well columns of the HCS plate.
     * @param numberOfFields The number of fields per well of the HCS dataset.
     */
    public ImageQuantityStructure(int numberOfRows, int numberOfColumns, int numberOfFields)
    {
        this(numberOfRows, numberOfColumns, numberOfFields, 1, SequenceType.NONE, false);
    }

    /**
     * Creates a new quantity structure for a non-screening image sequence with the given length.
     * 
     * @param sequenceLength The length of the image sequence.
     * @param sequenceType The type of the image sequence.
     * @param objectsIdenticalInSequence <code>true</code>, if the object ids correspond to each
     *            other for all images in the sequence. That means that e.g. object 17 for sequence
     *            index 0 and sequence index 100 identify the same object.
     */
    public ImageQuantityStructure(int sequenceLength, SequenceType sequenceType,
            boolean objectsIdenticalInSequence)
    {
        this(1, 1, 1, sequenceLength, sequenceType, objectsIdenticalInSequence);
    }

    void checkInBounds(ImageId id) throws IndexOutOfBoundsException
    {
        if (id.getRow() >= numberOfRows || id.getColumn() >= numberOfColumns
                || id.getField() >= numberOfFields || id.getSequenceIndex() >= sequenceLength)
        {
            throw new IndexOutOfBoundsException(id + " out of bounds of " + this);
        }

    }

    void checkInBounds(ImageSequenceId id) throws IndexOutOfBoundsException
    {
        if (id.getRow() >= numberOfRows || id.getColumn() >= numberOfColumns
                || id.getField() >= numberOfFields)
        {
            throw new IndexOutOfBoundsException(id + " out of bounds of " + this);
        }

    }

    @Override
    public Iterator<ImageId> iterator()
    {
        return ImageRunner.iterator(ImageQuantityStructure.this, null);
    }

    /**
     * Returns the number of well rows of an HCS plate.
     */
    public int getNumberOfRows()
    {
        return numberOfRows;
    }

    /**
     * Returns the number of well columns of an HCS plate.
     */
    public int getNumberOfColumns()
    {
        return numberOfColumns;
    }

    /**
     * Returns the number of fields (images) per well of an HCS plate.
     */
    public int getNumberOfFields()
    {
        return numberOfFields;
    }

    /**
     * Returns the length of the image sequence.
     */
    public int getSequenceLength()
    {
        return sequenceLength;
    }

    /**
     * Returns the type of the sequence, e.g. whether it is a time series.
     */
    public SequenceType getSequenceType()
    {
        return sequenceType;
    }

    /**
     * Returns <code>true</code>, if the object ids correspond to each other for all images in the
     * sequence. That means that e.g. object 17 for sequence index 0 and sequence index 100 identify
     * the same object.
     */
    public boolean isObjectsIdenticalInSequence()
    {
        return objectsIdenticalInSequence;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + numberOfColumns;
        result = prime * result + numberOfFields;
        result = prime * result + numberOfRows;
        result = prime * result + (objectsIdenticalInSequence ? 1231 : 1237);
        result = prime * result + sequenceLength;
        result = prime * result + ((getSequenceType() == null) ? 0 : sequenceType.hashCode());
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
        final ImageQuantityStructure other = (ImageQuantityStructure) obj;
        if (numberOfColumns != other.numberOfColumns)
        {
            return false;
        }
        if (numberOfFields != other.numberOfFields)
        {
            return false;
        }
        if (numberOfRows != other.numberOfRows)
        {
            return false;
        }
        if (objectsIdenticalInSequence != other.objectsIdenticalInSequence)
        {
            return false;
        }
        if (sequenceLength != other.sequenceLength)
        {
            return false;
        }
        if (getSequenceType() != other.getSequenceType())
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ImageQuantityStructure [numberOfRows=" + numberOfRows + ", numberOfColumns="
                + numberOfColumns + ", numberOfFields=" + numberOfFields + ", sequenceLength="
                + sequenceLength + ", sequenceType=" + getSequenceType()
                + ", objectsIdenticalInSequence=" + objectsIdenticalInSequence + "]";
    }

}
