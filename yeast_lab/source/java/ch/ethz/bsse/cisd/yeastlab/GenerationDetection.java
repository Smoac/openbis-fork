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

import static ch.ethz.bsse.cisd.yeastlab.GenerationDetectionUtils.NEW_LINE;
import static ch.ethz.bsse.cisd.yeastlab.GenerationDetectionUtils.SEPARATOR;
import static ch.ethz.bsse.cisd.yeastlab.GenerationDetectionUtils.doubleToString;
import static ch.ethz.bsse.cisd.yeastlab.GenerationDetectionUtils.intToString;
import static ch.ethz.bsse.cisd.yeastlab.GenerationDetectionUtils.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import ch.ethz.bsse.cisd.yeastlab.condition.CloseDistanceCandidateCondition;
import ch.ethz.bsse.cisd.yeastlab.condition.IBetterCandidateCondition;
import ch.ethz.bsse.cisd.yeastlab.condition.MatchingFirstFluorescencePeakCandidateCondition;
import ch.ethz.bsse.cisd.yeastlab.condition.SimilarBeginningFluorescenceCandidateCondition;
import ch.ethz.bsse.cisd.yeastlab.model.Cell;
import ch.ethz.bsse.cisd.yeastlab.model.ParentCandidate;

/**
 * Generation Detection algorithm main application.
 * 
 * @author Piotr Buczek
 */
public class GenerationDetection
{

    // properties that can be modified by the user

    private static final int ISOLATED_FAKE_CELL_REMOVAL_WINDOW;

    private static final int MAX_NEW_BORN_CELL_PIXELS; // reduce with better picture quality

    private static final int MIN_PARENT_PIXELS;

    private static final int MAX_PARENT_SHIFT;

    private static final double MIN_NEW_BORN_CELL_ECCENTRICITY;

    // if we have more parent candidates after filtering the new born cell will be ignored
    private static final int MAX_PARENT_CANDIDATES;

    // number of frames that should be ignored at the beginning
    private static final int NO_FIRST_FRAMES_TO_IGNORE;

    // number of frames that should be ignored in the end
    private static final int NO_LAST_FRAMES_TO_IGNORE;

    // when cell fluorescence gets above this level it means that cell is dying
    private static final double MAX_F_MEAN_OF_LIVING_CELL;

    // window radius used to calculate smooth fluorescence deviation values (ignore noise)
    private static final int SMOOTH_F_DEVIATION_WINDOW;

    // minimal number of frames that a real the cell should have stable nucleus area = NUCLEUS_AREA
    private static final int MIN_STABLE_NUCLEUS_AREA_FRAMES;

    private static final String PROPERTIES_FILE_PATH = "conf.properties";

    private static final ConfigParameters configParameters;

    static
    {
        configParameters = new ConfigParameters(PROPERTIES_FILE_PATH);
        log(configParameters.getDescription());
        ISOLATED_FAKE_CELL_REMOVAL_WINDOW = configParameters.getIsolatedFakeFrameRemovalWindow();
        MAX_F_MEAN_OF_LIVING_CELL = configParameters.getMaxFMeanOfLivingCell();
        MAX_NEW_BORN_CELL_PIXELS = configParameters.getMaxNewBornCellPixels();
        MAX_PARENT_CANDIDATES = configParameters.getMaxParentCandidates();
        MAX_PARENT_SHIFT = configParameters.getMaxParentShift();
        MIN_NEW_BORN_CELL_ECCENTRICITY = configParameters.getMinNewBornCellEccentricity();
        MIN_PARENT_PIXELS = configParameters.getMinParentPixels();
        MIN_STABLE_NUCLEUS_AREA_FRAMES = configParameters.getMinStableNucleusAreaFrames();
        NO_FIRST_FRAMES_TO_IGNORE = configParameters.getNumberOfFirstFramesToIgnore();
        NO_LAST_FRAMES_TO_IGNORE = configParameters.getNumberOfLastFramesToIgnore();
        SMOOTH_F_DEVIATION_WINDOW = configParameters.getSmoothFDeviationWindow();
    }

    // constants

    static final boolean PRODUCTION = true;

    static final boolean DEBUG = false;

    static final int FIRST_FRAME_NUM = 0;

    static final int FIRST_PICTURE_NUM = 0; // 0

    public static final int FRAME_OFFSET = FIRST_PICTURE_NUM - FIRST_FRAME_NUM;

    public static final int INITIAL_GENERATION = 1;

    // real cells have a.nucl value almost fixed to this value because of Cell-ID implementation.
    private static final double NUCLEUS_AREA = 49.0;

    private static final String RESULTS_FILE_EXTENSION = "res";

    private static final String PARENT_ID_COL_NAME = "motherID";

    private static final String ALTERNATIVES_COL_NAME = "alternatives";

    private static final String GENERATION_COL_NAME = "generation";

    private static String headerInputRow;

    private static double maxAxis; // maximal major axis found in input data

    private static int maxFrame; // maximal frame number (easy to read from file)

    // <id, <frame, cell>>
    private static Map<Integer, Map<Integer, Cell>> cellsByIdAndFrame;

    // <frame, cells>
    private static Map<Integer, Set<Cell>> cellsByFrame;

    // <child id, fake parent id>
    private static Map<Integer, Integer> ignoredChildrenFakeParentIds;

    // <child id, parent candidates sorted in order (first cell is most likely a parent)>
    private static Map<Integer, ParentCandidate[]> parentCandidatesByChildId;

    // <id, smooth fluorescence deviation values>
    // - smooth fluorescence deviation values are indexed by cell frame
    // - if cell didn't exist on a frame there will be null as a value
    private static Map<Integer, Double[]> cellSmoothFDeviationByIdAndFrame;

    private static IParentInformationProvider parentInformationProvider =
            createParentInformationProvider();

    private static ISmoothFluorescenceDeviationsProvider sfdProvider = createSFDProvider();

    public static Cell tryGetCellByIdAndFrame(int id, int frame)
    {
        Map<Integer, Cell> byFrameOrNull = cellsByIdAndFrame.get(id);
        return byFrameOrNull != null ? byFrameOrNull.get(frame) : null;
    }

    private static double getSmoothFDeviation(Cell cell)
    {
        final Double[] sfdsOrNull = cellSmoothFDeviationByIdAndFrame.get(cell.getId());
        if (sfdsOrNull == null)
        {
            return -1;
        } else
        {
            final Double valueOrNull = sfdsOrNull[cell.getFrame()];
            return valueOrNull == null ? -1 : valueOrNull;
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("usage: java -jar yeast_lab.jar <input file> <output file>");
            System.exit(-1);
        }
        final String inputFileName = args[0];
        final String outputFileName = args[1];
        final File input = new File(inputFileName);
        assert input.exists() && input.isFile() && input.canRead() : "There is no file in '"
                + inputFileName + "' that can be read.";
        final File output = new File(outputFileName);
        // assert output.exists() == false : "File '" + OUTPUT_FILE + "' already exists.";
        File results = null;
        if (PRODUCTION == false)
        {
            final String resultsFileName = inputFileName + "." + RESULTS_FILE_EXTENSION;
            results = new File(resultsFileName);
            if (results.exists() && results.isFile() && results.canRead())
            {
                GenerationDetectionAccuracyTester.loadCorrectParents(results);
            }
        }

        BufferedReader reader = null;
        PrintWriter writer = null;
        try
        {
            reader = new BufferedReader(new FileReader(input));
            final List<Cell> cells = load(reader, input);
            createDataStructures(cells);
            analyzeData();

            if (PRODUCTION)
            {
                writer = new PrintWriter(output);
                writer.append(headerInputRow);
                writer.append(SEPARATOR + PARENT_ID_COL_NAME);
                writer.append(SEPARATOR + ALTERNATIVES_COL_NAME);
                writer.append(SEPARATOR + GENERATION_COL_NAME);
                writer.append(SEPARATOR + "f.mean");
                writer.append(SEPARATOR + "f.deviation");
                writer.append(SEPARATOR + "f.deviation.smooth" + NEW_LINE);
                for (Cell cell : cells)
                {
                    writer.append(cell.getInputRow());
                    writer.append(SEPARATOR + intToString(cell.getParentCellId()));
                    writer.append(SEPARATOR + intToString(cell.getAlternatives()));
                    writer.append(SEPARATOR + intToString(cell.getGeneration()));
                    writer.append(SEPARATOR + doubleToString(cell.getFMean()));
                    writer.append(SEPARATOR + doubleToString(cell.getFDeviation()));
                    writer.append(SEPARATOR + doubleToString(getSmoothFDeviation(cell)));
                    writer.append(NEW_LINE);
                }
            }
            log(String.format("\nMother connections found (%d):\n", newBornCells.size()));
            log("cellID\t frame\t mother\t candidates");
            for (Cell cell : newBornCells)
            {
                log(parentInformationProvider.parentInformation(cell));
            }
            if (PRODUCTION == false && results != null)
            {
                GenerationDetectionAccuracyTester.computeResultsAccuracy(newBornCells);
            }
            log("\nIgnored newly appearing cell reason statistics:\n");
            log("code\t count\t description");
            for (IgnoreNewCellReason reason : IgnoreNewCellReason.values())
            {
                log(String.format("%4d\t %5d\t %s", reason.getFakeParentId(), reason.getCounter(),
                        reason.getDescription()));
            }
            log("\nDone!\n");
        } catch (final IOException ex)
        {
            ex.printStackTrace();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
                if (writer != null)
                {
                    writer.close();
                }
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    private static void createDataStructures(List<Cell> cells)
    {
        // common empty array to avoid null checks
        final ParentCandidate[] initialCandidates = new ParentCandidate[0];
        cellsByIdAndFrame = new TreeMap<Integer, Map<Integer, Cell>>();
        // cellsByFrameAndId = new LinkedHashMap<Integer, Map<Integer, Cell>>();
        cellsByFrame = new TreeMap<Integer, Set<Cell>>();
        parentCandidatesByChildId = new TreeMap<Integer, ParentCandidate[]>();
        ignoredChildrenFakeParentIds = new TreeMap<Integer, Integer>();
        maxAxis = 0;
        maxFrame = -1;
        for (Cell cell : cells)
        {
            // cellsByIdAndFrame
            Map<Integer, Cell> byFrame = cellsByIdAndFrame.get(cell.getId());
            if (byFrame == null)
            {
                // increasing order of frames
                byFrame = new LinkedHashMap<Integer, Cell>();
                cellsByIdAndFrame.put(cell.getId(), byFrame);
            }
            byFrame.put(cell.getFrame(), cell);
            // initialize candidates
            parentCandidatesByChildId.put(cell.getId(), initialCandidates);
            if (cell.getMajAxis() > maxAxis)
            {
                maxAxis = cell.getMajAxis();
            }
            if (cell.getFrame() > maxFrame)
            {
                maxFrame = cell.getFrame();
            }
        }

        // filter out fake cells
        final List<Integer> fakeCellIds = new ArrayList<Integer>();
        for (Map<Integer, Cell> byFrame : cellsByIdAndFrame.values())
        {
            Cell firstFrameCell = byFrame.values().iterator().next();
            if (isFakeCell(firstFrameCell))
            {
                fakeCellIds.add(firstFrameCell.getId());
            } else
            {
                for (Cell cell : byFrame.values())
                {
                    Set<Cell> frameCells = cellsByFrame.get(cell.getFrame());
                    if (frameCells == null)
                    {
                        frameCells = new LinkedHashSet<Cell>();
                        cellsByFrame.put(cell.getFrame(), frameCells);
                    }
                    frameCells.add(cell);
                }
            }
        }
        for (Integer id : fakeCellIds)
        {
            cellsByIdAndFrame.remove(id);
        }

        // mark dead cells
        for (Map<Integer, Cell> byFrame : cellsByIdAndFrame.values())
        {
            for (Cell c : byFrame.values())
            {
                if (isDying(c))
                {
                    markAsDead(c);
                    break;
                }
            }
        }

        removeIsolatedFakeFrames(cells);
        generateSmoothFDeviationValues();
    }

    private static void removeIsolatedFakeFrames(List<Cell> cells)
    {
        if (ISOLATED_FAKE_CELL_REMOVAL_WINDOW > 0)
        {
            // remove cell frames that have no frames close available
            final List<Cell> cellsToRemove = new ArrayList<Cell>();

            for (Map<Integer, Cell> byFrame : cellsByIdAndFrame.values())
            {
                Set<Integer> frames = byFrame.keySet();

                int counter = 0; // number of frames that the cell existed on in current window

                // initialize counter for first window
                for (int frame = 0; frame <= ISOLATED_FAKE_CELL_REMOVAL_WINDOW; frame++)
                {
                    if (frames.contains(frame))
                    {
                        counter++;
                    }
                }
                // At the beginning of each loop iteration counter for window for 'frame' is already
                // computed. Then the window is shifted.
                for (int frame = 0; frame <= maxFrame; frame++)
                {
                    // check counter if cell existed on current frame
                    if (frames.contains(frame))
                    {
                        if (counter == 1) // only this frame is available in the window
                        {
                            Cell cell = byFrame.get(frame);
                            cellsToRemove.add(cell);
                        }
                    }
                    // shift window:
                    // - subtract counter for first frame if present
                    final int curWindowFirstFrame = frame - ISOLATED_FAKE_CELL_REMOVAL_WINDOW;
                    if (frames.contains(curWindowFirstFrame))
                    {
                        counter--;
                    }
                    // - increase counter for next frame if present
                    final int nextWindowLastFrame = frame + ISOLATED_FAKE_CELL_REMOVAL_WINDOW + 1;
                    if (frames.contains(nextWindowLastFrame))
                    {
                        counter++;
                    }
                }
            }
            for (Cell cell : cellsToRemove)
            {
                log(String.format("Removing isolated cell id:%d frame:%d.", cell.getId(), cell
                        .getFrame()));
            }
        }
    }

    // smooth fluorescence deviation computation

    private static void generateSmoothFDeviationValues()
    {
        cellSmoothFDeviationByIdAndFrame = new LinkedHashMap<Integer, Double[]>();
        for (Integer cellId : cellsByIdAndFrame.keySet())
        {
            cellSmoothFDeviationByIdAndFrame.put(cellId, generateSmoothFDeviationValues(cellId));
        }
    }

    private static Double[] generateSmoothFDeviationValues(int cellId)
    {
        final Double[] result = new Double[maxFrame + 1];
        final Double[] fDeviations = new Double[maxFrame + 1];
        fillFDeviationArray(fDeviations, cellId);
        fillSmoothFDeviationArray(result, fDeviations);
        return result;
    }

    private static void fillFDeviationArray(Double[] result, int cellId)
    {
        final Map<Integer, Cell> byFrame = cellsByIdAndFrame.get(cellId);
        assert byFrame != null;

        for (Cell cell : byFrame.values())
        {
            result[cell.getFrame()] = cell.getFDeviation();
        }
    }

    private static void fillSmoothFDeviationArray(Double[] result, Double[] fDeviations)
    {
        double fDeviationSum = 0; // sum of cells fluorescence deviations in current window
        int counter = 0; // number of frames that the cell existed on in current window

        // initialize counter and fDeviationSum values for first window
        for (int frame = 0; frame <= SMOOTH_F_DEVIATION_WINDOW; frame++)
        {
            final Double fDeviationOrNull = fDeviations[frame];
            if (fDeviationOrNull != null)
            {
                fDeviationSum += fDeviationOrNull;
                counter++;
            }
        }
        // At the beginning of each loop iteration result[frame] value is set from already computed
        // window fDeviationSum and counter values. Then the window is shifted.
        for (int frame = 0; frame <= maxFrame; frame++)
        {
            // fill value only if cell existed on current frame
            if (fDeviations[frame] != null)
            {
                assert counter > 0;
                result[frame] = fDeviationSum / counter;
            }
            // shift window:
            // - subtract first value if present
            final int curWindowFirstFrame = frame - SMOOTH_F_DEVIATION_WINDOW;
            if (curWindowFirstFrame >= 0 && fDeviations[curWindowFirstFrame] != null)
            {
                fDeviationSum -= fDeviations[curWindowFirstFrame];
                counter--;
            }
            // - add next value if present
            final int nextWindowLastFrame = frame + SMOOTH_F_DEVIATION_WINDOW + 1;
            if (nextWindowLastFrame <= maxFrame && fDeviations[nextWindowLastFrame] != null)
            {
                fDeviationSum += fDeviations[nextWindowLastFrame];
                counter++;
            }
        }
    }

    private static void analyzeData() throws Exception
    {
        Set<Cell> cellsFromOldFrames = new LinkedHashSet<Cell>();
        // cells from older frames - one per id
        // BTW - the first appearance of cell will be held in this set (maybe it could be used).
        // instead we could use arrays with e.g. maxCellId = 1000 in C
        for (Entry<Integer, Set<Cell>> entry : cellsByFrame.entrySet())
        {
            final int currentFrame = entry.getKey();
            final Set<Cell> currentFrameCells = entry.getValue();
            log(String.format("analyzing frame %s...", currentFrame));
            // analyze genealogy of cells that appeared in current frame
            final Set<Cell> newCells = new LinkedHashSet<Cell>(currentFrameCells);
            newCells.removeAll(cellsFromOldFrames);
            analyzeNewFrameCells(currentFrame, newCells);
            // 
            cellsFromOldFrames.addAll(newCells);
        }
    }

    //

    private static List<Cell> newBornCells = new ArrayList<Cell>();

    private static void analyzeNewFrameCells(int frame, Set<Cell> newCells) throws Exception
    {
        if (frame == FIRST_FRAME_NUM)
        {
            for (Cell cell : newCells)
            {
                ignore(cell, IgnoreNewCellReason.FIRST_FRAME);
            }
            return; // nothing to analyze in the first frame
        }
        // we take cells from last few cells (for cell with certain id only one Cell will be kept in
        // the set - the one with the highest frame number)
        final Set<Cell> previousFramesCells = new HashSet<Cell>();
        addPreviousFrameCells(previousFramesCells, frame, MAX_PARENT_SHIFT);
        for (Cell cell : newCells)
        {
            // ignore cells that are not new born or not enough data is available
            if (isValidNewBornCell(cell) == false)
            {
                continue;
            }
            final List<ParentCandidate> parentCandidates = new ArrayList<ParentCandidate>(4);

            for (Cell previousFrameCell : previousFramesCells)
            {
                final ParentCandidate candidate = ParentCandidate.create(previousFrameCell, cell);
                // Its easier and more effective to get only those cells that are closer then a
                // reasonable distance to current cell then getting k closest parents to current
                // cell.
                // Parent cells have to be bigger than new born cells.
                if (candidate.isAlive() && candidate.isDistanceValid(maxAxis))
                {
                    // could be tested before candidate is created
                    if (candidate.isNumPixValid(MIN_PARENT_PIXELS))
                    {
                        parentCandidates.add(candidate);
                    } else
                    {
                        Cell parent = candidate.getParent();
                        log(String
                                .format(
                                        "Cell %d is to small (%d) to be a mother of %d that appeared on frame %d.",
                                        parent.getId(), parent.getNumPix(), cell.getId(), candidate
                                                .getDistanceSq(), frame));
                    }
                }
            }
            if (parentCandidates.size() == 0)
            {
                ignore(cell, IgnoreNewCellReason.NO_CANDIDATES);
                log(String.format("Reason: no candidate for a mother found near the cell."));
                // continue
            } else
            {
                // sort parent - best will be first
                Collections.sort(parentCandidates);
                filterCandidatesWithDistance(parentCandidates);
                if (configParameters.isBeginningFWindowFilterEnabled())
                {
                    filterCandidatesWithBeginningFluorescence(parentCandidates);
                }
                if (configParameters.isFirstPeakFilterEnabled())
                {
                    filterCandidatesWithFirstPeak(parentCandidates);
                }
                // if we have many candidates it is rather difficult to decide
                if (parentCandidates.size() <= MAX_PARENT_CANDIDATES)
                {
                    setParents(cell, parentCandidates.toArray(new ParentCandidate[0]));
                    newBornCells.add(cell);
                } else
                {
                    ignore(cell, IgnoreNewCellReason.TOO_MANY_CANDIDATES);
                    log(String
                            .format(
                                    "Reason: too many (%d) equally good candidates found near the cell to decide.",
                                    parentCandidates.size()));
                }
            }
        }

    }

    private static void addPreviousFrameCells(Set<Cell> previousFramesCells, int frame, int maxShift)
    {
        if (maxShift >= 1)
        {
            Set<Cell> previousFrameCells = cellsByFrame.get(frame - 1);
            if (previousFrameCells != null)
            {
                previousFramesCells.addAll(previousFrameCells);
            }
            addPreviousFrameCells(previousFramesCells, frame - 1, maxShift - 1);
        }
    }

    private static void filterCandidatesWithFirstPeak(List<ParentCandidate> parentCandidates)
    {
        // If some candidates have a fluorescence peak raise (short: peak) in the same point of time
        // as first peak of daughter they are better candidates and other candidates will be
        // filtered out.
        filterBetterCandidates(parentCandidates, createMatchingFirstFluoerscencePeakCondition());
    }

    private static void filterCandidatesWithBeginningFluorescence(
            List<ParentCandidate> parentCandidates)
    {
        // If some candidates have very similar fluorescence as the child cell in the first few
        // frames they are better candidates and other candidates will be filtered out
        filterBetterCandidates(parentCandidates, createSimilarBeginningFluorescenceCondition());
    }

    private static void filterCandidatesWithDistance(List<ParentCandidate> parentCandidates)
    {
        // If some candidates almost touch the child cell they are better candidates
        // and other candidates will be filtered out
        filterBetterCandidates(parentCandidates, createCloseDistanceCondition());
    }

    private static void filterBetterCandidates(List<ParentCandidate> parentCandidates,
            IBetterCandidateCondition condition)
    {
        // filter only when there is more than one candidate (otherwise filtering will always leave
        // the only one candidate that is available, so there is no need to compute anything)
        if (parentCandidates.size() == 1)
        {
            return;
        }

        final List<ParentCandidate> betterCandidates = new ArrayList<ParentCandidate>();
        for (ParentCandidate candidate : parentCandidates)
        {
            if (condition.isBetter(candidate))
            {
                betterCandidates.add(candidate);
            }
        }

        if (betterCandidates.size() > 0)
        {
            parentCandidates.retainAll(betterCandidates);
        }
    }

    private static IBetterCandidateCondition closeDistanceCondition;

    private static IBetterCandidateCondition similarBeginningFluorescenceCondition;

    private static IBetterCandidateCondition matchingFirstFluoerscencePeakCondition;

    private static IBetterCandidateCondition createCloseDistanceCondition()
    {
        if (closeDistanceCondition == null)
        {
            closeDistanceCondition = new CloseDistanceCandidateCondition();
        }
        return closeDistanceCondition;
    }

    private static IBetterCandidateCondition createSimilarBeginningFluorescenceCondition()
    {
        if (similarBeginningFluorescenceCondition == null)
        {
            final int maxOffset = configParameters.getBeginningFWindowMaxOffset();
            final int windowLength = configParameters.getBeginningFWindowLength();
            final int maxMissing = configParameters.getBeginningFWindowMaxMissing();
            final double maxAvgDiff = configParameters.getBeginningFWindowMaxAvgDiff();

            similarBeginningFluorescenceCondition =
                    new SimilarBeginningFluorescenceCandidateCondition(sfdProvider, maxOffset,
                            windowLength, maxMissing, maxAvgDiff);
        }
        return similarBeginningFluorescenceCondition;
    }

    private static IBetterCandidateCondition createMatchingFirstFluoerscencePeakCondition()
    {
        if (matchingFirstFluoerscencePeakCondition == null)
        {
            final int framesToIgnore = configParameters.getFirstPeakNumberOfFirstFramesToIgnore();
            final int maxChildOffset = configParameters.getFirstPeakMaxChildOffset();
            final int maxParentOffset = configParameters.getFirstPeakMaxParentOffset();
            final int maxMissing = configParameters.getFirstPeakMaxMissing();
            final int minLength = configParameters.getFirstPeakMinLength();
            final double minFrameHeightDiff = configParameters.getFirstPeakMinFrameHeightDiff();
            final double minTotalHeightDiff = configParameters.getFirstPeakMinTotalHeightDiff();
            final int maxFrameHeightDiffExceptions =
                    configParameters.getFirstPeakMaxFrameHeightDiffExceptions();

            matchingFirstFluoerscencePeakCondition =
                    new MatchingFirstFluorescencePeakCandidateCondition(sfdProvider,
                            framesToIgnore, maxChildOffset, maxParentOffset, maxMissing, minLength,
                            minFrameHeightDiff, minTotalHeightDiff, maxFrameHeightDiffExceptions);
        }
        return matchingFirstFluoerscencePeakCondition;
    }

    private static void setParents(Cell child, ParentCandidate... candidates)
    {
        assert candidates.length != 0;
        final Cell parent = candidates[0].getParent();
        parentCandidatesByChildId.put(child.getId(), candidates);
        increaseGeneration(parent.getId(), child); // parent is from previous frame
    }

    private static void ignore(Cell child, IgnoreNewCellReason reason)
    {
        reason.incCounter();
        ignoredChildrenFakeParentIds.put(child.getId(), reason.getFakeParentId());
        log(String.format("Ignoring new cell with id:%d appearing on frame:%d.", child.getId(),
                child.getFrame()));
    }

    /**
     * Increases generations of all cells with specified id starting from frame of the specified
     * <var>child</var> (it should be the first appearance of the cell with child id).
     */
    private static void increaseGeneration(int parentID, Cell child)
    {
        final int childFrame = child.getFrame();
        int count = 0;
        for (Cell c : cellsByIdAndFrame.get(parentID).values())
        {
            if (c.getFrame() >= childFrame)
            {
                c.increaseGeneration();
                count++;
            }
        }
        if (count == 0) // 5 cases in test data
        {
            log(String.format("Cell with id '%d' disappears on frame '%d' "
                    + "just after producing a cell with id '%d' (on previous frame).", parentID,
                    childFrame, child.getId()));
        }
    }

    /**
     * Marks specified <var>cell</var> on all frames starting from its frame as dead.
     */
    private static void markAsDead(Cell cell)
    {
        int frame = cell.getFrame();
        for (Cell c : cellsByIdAndFrame.get(cell.getId()).values())
        {
            if (c.getFrame() >= frame)
            {
                c.markAsDead();
            }
        }
        log(String.format("Cell with id '%d' dies on frame '%d'.", cell.getId(), cell.getFrame()));
    }

    // If cell is fake its parentID will be set to error code.
    // cellsByIdAndFrame need to be initialized before this check is executed.
    private static boolean isFakeCell(Cell cell)
    {
        if (isNucleusFound(cell) == false)
        {
            ignore(cell, IgnoreNewCellReason.NO_NUCLEUS);
            log(String.format("Reason: cell nucleus isn't found (nucleus data doesn't stabilize)."));
            return true;
        }
        return false;
    }

    private static boolean isAppearingToEarly(Cell cell)
    {
        return cell.getFrame() < NO_FIRST_FRAMES_TO_IGNORE + FIRST_FRAME_NUM;
    }

    private static boolean isAppearingToLate(Cell cell)
    {
        return cell.getFrame() > maxFrame - NO_LAST_FRAMES_TO_IGNORE;
    }

    private static boolean isEnoughFramesAvailable(Cell cell)
    {
        return cellsByIdAndFrame.get(cell.getId()).size() >= MIN_STABLE_NUCLEUS_AREA_FRAMES;
    }

    private static boolean isNucleusFound(Cell cell)
    {
        // MIN_STABLE_NUCLEUS_AREA_FRAMES cell frames need to have correct nucleus area
        int counter = 0;
        for (Cell c : cellsByIdAndFrame.get(cell.getId()).values())
        {
            if (c.getANucleus() == NUCLEUS_AREA)
            {
                counter++;
                if (counter == MIN_STABLE_NUCLEUS_AREA_FRAMES)
                {
                    return true;
                }
            }
        }
        return false;
    }

    // if cell is considered not to be a valid new born cell parentID will be set to error code
    private static boolean isValidNewBornCell(Cell cell)
    {
        if (cell.getNumPix() > MAX_NEW_BORN_CELL_PIXELS)
        {
            ignore(cell, IgnoreNewCellReason.TOO_BIG);
            log(String.format(
                    "Reason: size=%d exceeds maximal allowed value for a new born cell (%d).", cell
                            .getNumPix(), MAX_NEW_BORN_CELL_PIXELS));
            return false;
        }
        // The shape of a new born cell should not be too far from a circle
        if (cell.getEccentricity() < MIN_NEW_BORN_CELL_ECCENTRICITY)
        {
            ignore(cell, IgnoreNewCellReason.WRONG_SHAPE);
            log(String
                    .format(
                            "Reason: minAxis/majAxis ratio=%1.2f is below minimal allowed value for a new born cell (%1.2f).",
                            cell.getEccentricity(), MIN_NEW_BORN_CELL_ECCENTRICITY));
            return false;
        }
        // not enough data
        if (isAppearingToEarly(cell))
        {
            ignore(cell, IgnoreNewCellReason.NOT_ENOUGH_DATA);
            log(String
                    .format("Reason: there is not enough data about the cell (it appears on one of first few frames)"));
            return false;
        }
        if (isAppearingToLate(cell))
        {
            ignore(cell, IgnoreNewCellReason.NOT_ENOUGH_DATA);
            log(String
                    .format("Reason: there is not enough data about the cell (it appears on one of last few frames)"));
            return false;
        }
        if (isEnoughFramesAvailable(cell) == false)
        {
            ignore(cell, IgnoreNewCellReason.NOT_ENOUGH_DATA);
            log(String
                    .format("Reason: there is not enough data about the cell (only a few frames)."));
            return false;
        }

        return true;
    }

    // dying cell has very high fluorescence mean (additionally size drops ~30%)
    private static boolean isDying(Cell cell)
    {
        return cell.getFMean() > MAX_F_MEAN_OF_LIVING_CELL;
    }

    private static List<Cell> load(final BufferedReader reader, final File file) throws IOException
    {
        assert reader != null : "Unspecified reader";
        assert file != null : "Unspecified file";

        final List<Cell> result = new ArrayList<Cell>();
        String line;
        line = reader.readLine();
        headerInputRow = line;
        while ((line = reader.readLine()) != null)
        {
            result.add(new Cell(line, parentInformationProvider));
        }
        return result;
    }

    // helper interfaces for outside usage

    /** interface allowing to access information about smooth fluorescence deviations of a cell */
    public interface ISmoothFluorescenceDeviationsProvider
    {
        Double[] getSFDs(int cellId);
    }

    private static ISmoothFluorescenceDeviationsProvider createSFDProvider()
    {
        return new ISmoothFluorescenceDeviationsProvider()
            {
                public Double[] getSFDs(int cellId)
                {
                    return cellSmoothFDeviationByIdAndFrame.get(cellId);
                }
            };
    }

    /** interface allowing to access information about cells parent */
    public interface IParentInformationProvider
    {
        ParentCandidate[] getParentCandidates(Cell cell);

        int getParentCellId(Cell cell);

        int getAlternatives(Cell cell);

        public String parentInformation(Cell cell);
    }

    private static IParentInformationProvider createParentInformationProvider()
    {
        return new IParentInformationProvider()
            {
                public ParentCandidate[] getParentCandidates(Cell cell)
                {
                    // not null - initialized at startup
                    return parentCandidatesByChildId.get(cell.getId());
                }

                public int getAlternatives(Cell cell)
                {
                    return getParentCandidates(cell).length - 1;
                }

                public int getParentCellId(Cell cell)
                {
                    final ParentCandidate[] candidates = getParentCandidates(cell);
                    if (candidates.length == 0)
                    {
                        return ignoredChildrenFakeParentIds.get(cell.getId());
                    } else
                    {
                        return candidates[0].getParent().getId();
                    }
                }

                public String parentInformation(Cell cell)
                {
                    final StringBuilder sb = new StringBuilder();
                    final ParentCandidate[] candidates = getParentCandidates(cell);
                    for (ParentCandidate candidate : candidates)
                    {
                        sb.append(candidateInformation(candidate) + ",");
                    }
                    return String.format("%d \t f:%d \t p:%d \t c(%d):%s", cell.getId(), cell
                            .getFrame()
                            + FRAME_OFFSET, cell.getParentCellId(), candidates.length, sb
                            .toString());
                }

                private String candidateInformation(ParentCandidate candidate)
                {
                    if (PRODUCTION)
                    {
                        return Integer.toString(candidate.getParent().getId());
                    } else
                    {
                        String prefix = "";
                        Integer rightParentIdOrNull =
                                GenerationDetectionAccuracyTester.correctParents.get(candidate
                                        .getChild().getId());
                        if (rightParentIdOrNull == null)
                        {
                            prefix = "F"; // fake
                        } else if (rightParentIdOrNull.equals(candidate.getParent().getId()))
                        {
                            prefix = "+"; // ok
                        } else
                        {
                            prefix = "-"; // wrong
                        }

                        Cell parent = candidate.getParent();
                        // Cell nextParent = candidate.nextFrameParent;

                        StringBuilder sb = new StringBuilder();
                        sb.append("\n" + SEPARATOR + prefix + SEPARATOR + candidate.getDistanceSq()
                                + SEPARATOR + parent.longToString());
                        return sb.toString();
                    }
                }
            };
    }

}
