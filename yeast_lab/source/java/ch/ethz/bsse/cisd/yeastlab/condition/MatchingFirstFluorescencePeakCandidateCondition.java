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

package ch.ethz.bsse.cisd.yeastlab.condition;

import static ch.ethz.bsse.cisd.yeastlab.GenerationDetectionUtils.debug;

import ch.ethz.bsse.cisd.yeastlab.GenerationDetection.ISmoothFluorescenceDeviationsProvider;
import ch.ethz.bsse.cisd.yeastlab.model.ParentCandidate;

/**
 * {@link IBetterCandidateCondition} checking whether parent candidate has a fluorescence peak raise
 * (in brief: peak) starting very close in time to the first peak of child cell.
 * 
 * @author Piotr Buczek
 */
public class MatchingFirstFluorescencePeakCandidateCondition implements IBetterCandidateCondition
{
    private final ISmoothFluorescenceDeviationsProvider sfdProvider;

    private final int framesToIgnore;

    private final int maxChildOffset;

    private final int maxParentOffset;

    private final int maxMissing;

    private final int minLength;

    private final double minFrameHeightDiff;

    private final double minTotalHeightDiff;

    private final int maxFrameHeightDiffExceptions;

    private final int maxFrame;

    public MatchingFirstFluorescencePeakCandidateCondition(
            ISmoothFluorescenceDeviationsProvider sfdProvider, int framesToIgnore,
            int maxChildOffset, int maxParentOffset, int maxMissing, int minLength,
            double minFrameHeightDiff, double minTotalHeightDiff, int maxFrameHeightDiffExceptions,
            int maxFrame)
    {
        this.sfdProvider = sfdProvider;

        this.framesToIgnore = framesToIgnore;
        this.maxChildOffset = maxChildOffset;
        this.maxParentOffset = maxParentOffset;
        this.maxMissing = maxMissing;
        this.minLength = minLength;
        this.minFrameHeightDiff = minFrameHeightDiff;
        this.minTotalHeightDiff = minTotalHeightDiff;
        this.maxFrameHeightDiffExceptions = maxFrameHeightDiffExceptions;
        this.maxFrame = maxFrame;
    }

    public boolean isBetter(ParentCandidate candidate)
    {
        final int childId = candidate.getChild().getId();
        final int candidateId = candidate.getParent().getId();

        int minFrame = candidate.getChild().getFrame() + framesToIgnore;

        Integer childFirstPeakStartOrNull = tryFindPeakStart(childId, minFrame, maxChildOffset);
        if (childFirstPeakStartOrNull != null)
        {
            if (tryFindPeakStart(candidateId, childFirstPeakStartOrNull - maxParentOffset,
                    maxParentOffset * 2) != null)
            {
                debug(String.format("found candidate %d peak ", candidateId));
                return true;
            } else
            {
                debug(String.format("didn't find candidate %d peak ", candidateId));
                return false;
            }
        } else
        {
            debug(String.format("didn't find child %d peak ", childId));
            return false;
        }
    }

    private Integer tryFindPeakStart(int cellId, int minFrame, int maxOffset)
    {
        final Double[] cellSFDs = sfdProvider.getSFDs(cellId);

        int minFrameIndex = Math.max(minFrame, 0);
        int maxFrameIndex = Math.min(minFrame + maxOffset, maxFrame);

        for (int frame = minFrameIndex; frame <= maxFrameIndex; frame++)
        {
            if (cellSFDs[frame] == null)
            {
                continue; // cell needs to exist on the first frame of a peak
            }
            double currentSFD = cellSFDs[frame];
            double totalHeightDiff = 0;
            int missing = 0;
            int exceptions = 0;
            int nextFrame = frame + 1;

            while (missing <= maxMissing && exceptions <= maxFrameHeightDiffExceptions
                    && nextFrame <= maxFrame)
            {
                if (cellSFDs[nextFrame] != null)
                {
                    double nextFrameSFD = cellSFDs[nextFrame];
                    double frameHeightDiff = nextFrameSFD - currentSFD;
                    if (frameHeightDiff < minFrameHeightDiff)
                    {
                        if (nextFrame > frame + 1)
                        {
                            exceptions++;
                        } else
                        {
                            // exceptions at the beginning of peak are not allowed
                            break;
                        }
                    } else
                    {
                        currentSFD = nextFrameSFD;
                        totalHeightDiff += frameHeightDiff;
                    }
                } else
                {
                    missing++;
                }
                nextFrame++;
            }

            // nextFrame is bigger than the last frame of the peak
            if (nextFrame - frame > minLength && totalHeightDiff > minTotalHeightDiff)
            {
                debug(String.format(
                        "found a peak of cell %d (start frame:%d, length:%d, height:%1.2f)",
                        cellId, frame, nextFrame - 1 - frame, totalHeightDiff));
                return frame;
            }
        }
        return null;
    }

}
