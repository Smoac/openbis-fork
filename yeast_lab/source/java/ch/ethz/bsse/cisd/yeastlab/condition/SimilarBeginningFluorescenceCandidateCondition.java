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
 * {@link IBetterCandidateCondition} checking whether parent candidate has similar fluorescence as
 * the child at the beginning of child's life. It is using smooth fluorescence deviation values.
 * 
 * @author Piotr Buczek
 */
public class SimilarBeginningFluorescenceCandidateCondition implements IBetterCandidateCondition
{
    private final ISmoothFluorescenceDeviationsProvider sfdProvider;

    private final int maxOffset;

    private final int windowLength;

    private final int maxMissing;

    private final double maxAvgDiff;

    public SimilarBeginningFluorescenceCandidateCondition(
            ISmoothFluorescenceDeviationsProvider sfdProvider, int maxOffset, int windowLength,
            int maxMissing, double maxAvgDiff)
    {
        this.sfdProvider = sfdProvider;

        this.maxOffset = maxOffset;
        this.windowLength = windowLength;
        this.maxMissing = maxMissing;
        this.maxAvgDiff = maxAvgDiff;
    }

    public boolean isBetter(ParentCandidate candidate)
    {
        final int childId = candidate.getChild().getId();
        final int candidateId = candidate.getParent().getId();
        final Double[] childSFDs = sfdProvider.getSFDs(childId);
        final Double[] candidateSFDs = sfdProvider.getSFDs(candidateId);

        for (int candidateOffset = -maxOffset; candidateOffset <= maxOffset; candidateOffset++)
        {
            final int firstChildFrame = candidate.getChild().getFrame();
            final int firstCandidateFrame = firstChildFrame + candidateOffset;
            double currentDiff = 0;
            int counter = 0;
            for (int windowOffset = 0; windowOffset < windowLength; windowOffset++)
            {
                if (firstChildFrame + windowOffset < childSFDs.length
                        && firstCandidateFrame + windowOffset < candidateSFDs.length
                        && firstCandidateFrame + windowOffset >= 0)
                {
                    final Double childSFD = childSFDs[firstChildFrame + windowOffset];
                    final Double candidateSFD = candidateSFDs[firstCandidateFrame + windowOffset];
                    if (childSFD != null && candidateSFD != null)
                    {
                        currentDiff += Math.abs(childSFD - candidateSFD);
                        counter++;
                    }
                }
            }
            if (windowLength - counter > maxMissing)
            {
                debug(String.format(
                        "too many missing frames (%d) for child:%d, candidate:%d and offset:%d",
                        windowLength - counter, childId, candidateId, candidateOffset));
            } else
            {
                double currentAvgDiff = currentDiff / counter;
                debug(String.format(
                        "child:%d, candidate:%d, missing:%d, offset:%d, avgDiff:%1.2f = %s",
                        childId, candidateId, windowLength - counter, candidateOffset,
                        currentAvgDiff, currentAvgDiff <= maxAvgDiff ? "OK" : "too high"));
                if (currentAvgDiff <= maxAvgDiff)
                {
                    return true;
                }
            }
        }
        return false;
    }
}
