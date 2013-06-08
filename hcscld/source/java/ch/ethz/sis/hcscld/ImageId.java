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

/**
 * An identifier for an image. Images of an image sequence within an HCS screen are identified by:
 * <ul>
 * <li>Well (row and column)</li>
 * <li>Field of sight</li>
 * <li>Sequence index</li>
 * </ul>
 * All indices start with 0.
 * <p>
 * Image sequences can be time series or depth scans or can be independent of time and depth. For
 * identifying an image in a sequence only the sequence index is needed, while e.g. the timepoint is
 * considered an annotation of the sequence index.
 * <p>
 * Special cases are image sequences with only one image and image sequences not performed in a
 * screening setup (i.e. that have only one well and one field).
 * 
 * @author Bernd Rinn
 */
public class ImageId extends ImageSequenceId
{
    private final int seqIdx;

    /**
     * Creates an identifier for the given indices.
     * 
     * @param row The row index of the well, starting with 0.
     * @param column The column index of the well, starting with 0.
     * @param field The field index, starting with 0.
     * @param seqIdx The sequence id, starting with 0.
     */
    public ImageId(int row, int column, int field, int seqIdx)
    {
        super(row, column, field);
        this.seqIdx = seqIdx;
    }

    /**
     * Creates an identifier for the given indices of a degenerate sequence with a sequence length
     * of one.
     * 
     * @param row The row index of the well, starting with 0.
     * @param column The column index of the well, starting with 0.
     * @param field The field index, starting with 0.
     */
    public ImageId(int row, int column, int field)
    {
        this(row, column, field, 0);
    }

    /**
     * Creates an identifier for the sequence index for a non-screening image sequence.
     * 
     * @param seqIdx The sequence id, starting with 0.
     */
    public ImageId(int seqIdx)
    {
        this(0, 0, 0, seqIdx);
    }

    /**
     * Returns the sequence index (0-based).
     */
    public int getSequenceIndex()
    {
        return seqIdx;
    }

    @Override
    String createObjectName(String... prefixes)
    {
        final String prefix;
        if (prefixes.length == 0)
        {
            prefix = "";
        } else
        {
            final StringBuilder builder = new StringBuilder();
            for (String p : prefixes)
            {
                builder.append(p);
                builder.append("__");
            }
            prefix = builder.toString();
        }
        return String.format("%sR%d_C%d_F%d_S%d", prefix, row, column, field, seqIdx);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        return super.hashCode() * prime + seqIdx;
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
        final ImageId other = (ImageId) obj;
        if (column != other.column)
        {
            return false;
        }
        if (field != other.field)
        {
            return false;
        }
        if (row != other.row)
        {
            return false;
        }
        if (seqIdx != other.seqIdx)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ImageId [row=" + row + ", column=" + column + ", field=" + field + ", seqIdx="
                + seqIdx + "]";
    }

}
