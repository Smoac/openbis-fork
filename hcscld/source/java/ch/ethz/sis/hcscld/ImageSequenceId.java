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

/**
 * An identifier for an image sequence. Images sequence in an HCS screen are identified by:
 * <ul>
 * <li>Well (row and column)</li>
 * <li>Field of sight</li>
 * </ul>
 * All indices start with 0.
 * <p>
 * Image sequences can be time series or depth scans or can be independent of time and depth.
 * <p>
 * Special cases are image sequences with only one image and image sequences not performed in a
 * screening setup (i.e. that have only one well and one field).
 * 
 * @author Bernd Rinn
 */
public class ImageSequenceId extends WellId
{
    protected final int field;

    public ImageSequenceId(int row, int column, int field)
    {
        super(row, column);
        this.field = field;
    }

    /**
     * Returns the field index (0-based).
     */
    public int getField()
    {
        return field;
    }

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
        return String.format("%sR%d_C%d_F%d", prefix, row, column, field);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        return super.hashCode() * prime + field;
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
        final ImageSequenceId other = (ImageSequenceId) obj;
        return super.equals(obj) && (field == other.field);
    }

    @Override
    public String toString()
    {
        return "ImageSequenceId [row=" + row + ", column=" + column + ", field=" + field + "]";
    }

}