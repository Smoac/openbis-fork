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

package ch.ethz.bsse.cisd.yeastlab.model;

import ch.ethz.bsse.cisd.yeastlab.GenerationDetection;
import ch.ethz.bsse.cisd.yeastlab.GenerationDetectionUtils;

/**
 * Encapsulation of child {@link Cell} and a candidate for its parent.
 * 
 * @author Piotr Buczek
 */
public class ParentCandidate implements Comparable<ParentCandidate>
{

    public static ParentCandidate create(Cell previousFrameParentCandidateCell, Cell childCell)
    {
        // on the frame that child appeared parent could disappear
        final Cell childFrameParentOrNull =
                GenerationDetection.tryGetCellByIdAndFrame(
                        previousFrameParentCandidateCell.getId(), childCell.getFrame());
        Cell parentCandidateCell =
                (childFrameParentOrNull != null) ? childFrameParentOrNull
                        : previousFrameParentCandidateCell;
        return new ParentCandidate(parentCandidateCell, childCell);
    }

    /**
     * parent cell from the same frame that the child appeared on or from the previous frame if
     * parent disappeared just after division
     */
    private final Cell parent;

    private final Cell child;

    private final Integer distanceSq;

    private ParentCandidate(Cell parent, Cell child)
    {
        this.parent = parent;
        this.child = child;
        this.distanceSq = GenerationDetectionUtils.distanceSq(parent, child);
    }

    public Cell getParent()
    {
        return parent;
    }

    public Cell getChild()
    {
        return child;
    }

    public Integer getDistanceSq()
    {
        return distanceSq;
    }

    public boolean isAlive()
    {
        return parent.isDead() == false;
    }

    // The tightest condition that we could use as a threshold for valid distance is
    // (maxAxis+currentMajorAxis)/2
    // which is the longest possible distance that current cell can be from another cell
    // assuming they touch each other. We take 110% of this value because sometimes there is a
    // bit of a space between parent and child.
    // We could use a less tighter threshold like maxAxis if we find that we have some false
    // negatives but on the test data the results are better with this tighter threshold.
    // Of course:
    // maxAxis > (maxAxis+currentMajorAxis)/2.
    // More strict condition using parent majAxis instead of maxAxis is used in additional step.
    public boolean isDistanceValid(double maxAxis)
    {
        double maxValidDistance = 1.1 * ((maxAxis + child.getMajAxis()) / 2);
        return getDistanceSq() <= GenerationDetectionUtils.square(maxValidDistance);
    }

    public boolean isNumPixValid(int minParentPixels)
    {
        return parent.getNumPix() >= minParentPixels;
    }

    @Override
    public String toString()
    {
        return "id:" + parent.getId() + " dist:" + getDistanceSq();
    }

    //
    // Comparable
    //

    // simple comparator - closer is better
    public int compareTo(ParentCandidate o)
    {
        return this.getDistanceSq().compareTo(o.getDistanceSq());
    }

}
