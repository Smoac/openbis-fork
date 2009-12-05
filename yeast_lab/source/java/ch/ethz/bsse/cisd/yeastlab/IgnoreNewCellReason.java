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

/**
 * Reasons why a new born cell can be ignored by Generation Detection algorithm.
 * <p>
 * There is a certain error code and description connected with each reason. Additionally this
 * enumeration stores a counter of each each reason occurrence.
 * 
 * @author Piotr Buczek
 */
public enum IgnoreNewCellReason
{
    /** cell existed on the first frame */
    FIRST_FRAME(-1, "cell existed on the first frame"),

    /** cell is too big to be a new born cell */
    TOO_BIG(-2, "cell is too big to be a new born cell"),

    /** cell has wrong shape (very flat - far from circle) */
    WRONG_SHAPE(-3, "cell has wrong shape (very flat - far from circle)"),

    /** cell has too many cells near that are good candidates for a mother */
    TOO_MANY_CANDIDATES(-4, "cell has too many cells near that are good candidates for a mother"),

    /** cell has no cells near that are good candidates for a mother (usually isolated cell) */
    NO_CANDIDATES(-5,
            "cell has no cells near that are good candidates for a mother (usually isolated cell)"),

    /** cell nucleus isn't found (nucleus data doesn't stabilize) */
    NO_NUCLEUS(-10, "cell nucleus isn't found (nucleus data doesn't stabilize)"),

    /** there is not enough data about the cell */
    // (it appeared on one of first/last few frames or is only visible on a few frames)
    NOT_ENOUGH_DATA(-11, "there is not enough data about the cell");

    private final int fakeParentId;

    private final String description;

    private int counter = 0;

    IgnoreNewCellReason(int fakeParentId, String description)
    {
        this.fakeParentId = fakeParentId;
        this.description = description;
    }

    /** fake value used in parentID column to inform about the reason why cell is ignored */
    public int getFakeParentId()
    {
        return fakeParentId;
    }

    /** short description of the reason */
    public String getDescription()
    {
        return description;
    }

    /** number of cells ignored with this reason */
    public int getCounter()
    {
        return counter;
    }

    /** increase number of cells ignored with this reason */
    public void incCounter()
    {
        counter++;
    }
}
