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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Piotr Buczek
 */
public class GenerationDetection
{

    // properties that can be modified by the user

    private static final int MAX_NEW_BORN_CELL_PIXELS; // reduce with better picture quality

    private static final int MIN_PARENT_PIXELS;

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
        MAX_F_MEAN_OF_LIVING_CELL = configParameters.getMaxFMeanOfLivingCell();
        MAX_NEW_BORN_CELL_PIXELS = configParameters.getMaxNewBornCellPixels();
        MAX_PARENT_CANDIDATES = configParameters.getMaxParentCandidates();
        MIN_NEW_BORN_CELL_ECCENTRICITY = configParameters.getMinNewBornCellEccentricity();
        MIN_PARENT_PIXELS = configParameters.getMinParentPixels();
        MIN_STABLE_NUCLEUS_AREA_FRAMES = configParameters.getMinStableNucleusAreaFrames();
        NO_FIRST_FRAMES_TO_IGNORE = configParameters.getNumberOfFirstFramesToIgnore();
        NO_LAST_FRAMES_TO_IGNORE = configParameters.getNumberOfLastFramesToIgnore();
        SMOOTH_F_DEVIATION_WINDOW = configParameters.getSmoothFDeviationWindow();
    }

    // constants

    private static final boolean PRODUCTION = true;

    private static final boolean DEBUG = true;

    private static final int FIRST_FRAME_NUM = 0;

    private static final int FIRST_PICTURE_NUM = 0; // 0

    private static final int FRAME_OFFSET = FIRST_PICTURE_NUM - FIRST_FRAME_NUM;

    private static final int INITIAL_GENERATION = 1;

    // real cells have a.nucl value almost fixed to this value because of Cell-ID implementation.
    private static final double NUCLEUS_AREA = 49.0;

    private static final String RESULTS_FILE_EXTENSION = "res";

    private static final String PARENT_ID_COL_NAME = "motherID";

    private static final String ALTERNATIVES_COL_NAME = "alternatives";

    private static final String GENERATION_COL_NAME = "generation";

    private static final String SEPARATOR = "\t";

    private static final String NEW_LINE = "\n";

    // column positions (values from resource/columns.txt - 1)
    private static final int CELL_ID_POSITION = 0;

    private static final int T_FRAME_POSITION = 1;

    private static final int X_POS_POSITION = 3;

    private static final int Y_POS_POSITION = 4;

    private static final int F_TOT_POSITION = 5;

    private static final int A_TOT_POSITION = 6;

    private static final int NUM_PIX_POSITION = 7;

    private static final int FFT_STAT_POSITION = 8;

    private static final int PERIM_POSITION = 9;

    private static final int MAJ_AXIS_POSITION = 10;

    private static final int MIN_AXIS_POSITION = 11;

    private static final int F_NUCL_POSITION = 12;

    private static final int A_NUCL_POSITION = 13;

    private static final int F_BG_POSITION = 19;

    private static String headerInputRow;

    private static double maxAxis; // maximal major axis found in input data

    private static int maxFrame; // maximal frame number (easy to read from file)

    private static enum IgnoreNewCellReason
    {
        /** cell existed on the first frame */
        FIRST_FRAME(-1, "cell existed on the first frame"),

        /** cell is too big to be a new born cell */
        TOO_BIG(-2, "cell is too big to be a new born cell"),

        /** cell has wrong shape (very flat - far from circle) */
        WRONG_SHAPE(-3, "cell has wrong shape (very flat - far from circle)"),

        /** cell has too many cells near that are good candidates for a mother */
        TOO_MANY_CANDIDATES(-4,
                "cell has too many cells near that are good candidates for a mother"),

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

    /** information about a cell in given */
    static class Cell
    {
        /** cell identification number */
        private final int id; // cellID

        /** time frame of the cell (0 through n-1 where n = number of points in the time course) */
        // NOTE: not every cell is necessarily found in every time point.
        private final int frame; // t.frame

        /** x coordinate of the centroid of the cell */
        private final int x; // xpos

        /** y coordinate of the centroid of the cell */
        private final int y; // ypos

        /** number of pixels associated with the cell */
        private final int numPix; // num.pix == a.tot

        /** a measure of circularity - 0 means a perfect circle */
        private final double fftStat;

        /** circumference of the cell in pixel units */
        private final double perim;

        /** length of the major axis in pixel units */
        private final double majAxis;

        /** length of the minor axis in pixel units */
        private final double minAxis;

        /** sum of the fluorescence for all the pixels found in the cell */
        private final double fTotal; // f.tot

        /** area of the cell in pixels */
        private final double aTotal; // a.tot == num.pix (but double, not int)

        /** sum of nucleus fluorescence */
        private final double fNucleus; // f.nucl

        /** area of nucleus (max 49; 49 if brightest area in cell is completely inside the cell) */
        private final double aNucleus; // a.nucl

        /** fluorescence of the background (mean) */
        private final double fBackground; // f.bg

        // f.nucl/a.nucl - f.tot/a.tot
        /** fluorescence deviation (mean of nucleus fluorescence - mean of cell fluorescence) */
        private final double fDeviation;

        // f.tot/a.tot - f.bg
        /** cell fluorescence (mean of cell fluorescence - background fluorescence) */
        private final double fMean;

        /** minAxis/majAxis (circle == 1) */
        private final double eccentricity;

        /** whole input data row */
        transient private final String inputRow;

        private boolean dead = false;

        // new data to output

        private int generation = INITIAL_GENERATION; // number of children produced +1

        public Cell(final String inputRow)
        {
            this.inputRow = inputRow;
            final String[] tokens = inputRow.split(SEPARATOR);
            assert tokens.length == 61;
            this.id = Integer.parseInt(StringUtils.trim(tokens[CELL_ID_POSITION]));
            this.frame = Integer.parseInt(StringUtils.trim(tokens[T_FRAME_POSITION]));
            this.x = Integer.parseInt(StringUtils.trim(tokens[X_POS_POSITION]));
            this.y = Integer.parseInt(StringUtils.trim(tokens[Y_POS_POSITION]));
            this.numPix = Integer.parseInt(StringUtils.trim(tokens[NUM_PIX_POSITION]));

            this.fftStat = Double.parseDouble(StringUtils.trim(tokens[FFT_STAT_POSITION]));
            this.perim = Double.parseDouble(StringUtils.trim(tokens[PERIM_POSITION]));

            this.majAxis = Double.parseDouble(StringUtils.trim(tokens[MAJ_AXIS_POSITION]));
            this.minAxis = Double.parseDouble(StringUtils.trim(tokens[MIN_AXIS_POSITION]));
            this.eccentricity = minAxis / majAxis;

            this.fBackground = Double.parseDouble(StringUtils.trim(tokens[F_BG_POSITION]));
            this.fTotal = Double.parseDouble(StringUtils.trim(tokens[F_TOT_POSITION]));
            this.aTotal = Double.parseDouble(StringUtils.trim(tokens[A_TOT_POSITION]));
            this.fNucleus = Double.parseDouble(StringUtils.trim(tokens[F_NUCL_POSITION]));
            this.aNucleus = Double.parseDouble(StringUtils.trim(tokens[A_NUCL_POSITION]));
            this.fDeviation = (fNucleus / aNucleus) - (fTotal / aTotal);
            this.fMean = (fTotal / aTotal) - fBackground;
        }

        private ParentCandidate[] getParentCandidates()
        {
            return parentCandidatesByChildId.get(id); // not null - initialized at startup
        }

        public int getParentCellId()
        {
            final ParentCandidate[] candidates = getParentCandidates();
            if (candidates.length == 0)
            {
                return ignoredChildrenFakeParentIds.get(this.id);
            } else
            {
                return candidates[0].parent.id;
            }
        }

        public int getGeneration()
        {
            return generation;
        }

        public void increaseGeneration()
        {
            this.generation++;
        }

        public int getId()
        {
            return id;
        }

        public int getFrame()
        {
            return frame;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }

        public int getNumPix()
        {
            return numPix;
        }

        public double getMajAxis()
        {
            return majAxis;
        }

        public double getMinAxis()
        {
            return minAxis;
        }

        public double getFftStat()
        {
            return fftStat;
        }

        public double getPerim()
        {
            return perim;
        }

        public double getFBackground()
        {
            return fBackground;
        }

        public double getFTotal()
        {
            return fTotal;
        }

        public double getATotal()
        {
            return aTotal;
        }

        public double getFNucleus()
        {
            return fNucleus;
        }

        public double getANucleus()
        {
            return aNucleus;
        }

        public double getFDeviation()
        {
            return fDeviation;
        }

        public double getSmoothFDeviation()
        {
            return cellSmoothFDeviationByIdAndFrame.get(this.id)[this.frame];
        }

        public double getFMean()
        {
            return fMean;
        }

        public double getEccentricity()
        {
            return eccentricity;
        }

        public String getInputRow()
        {
            return inputRow;
        }

        public int getAlternatives()
        {
            return getParentCandidates().length - 1;
        }

        public boolean isDead()
        {
            return dead;
        }

        public void markAsDead()
        {
            this.dead = true;
        }

        @Override
        public String toString()
        {
            return parentInformation();
        }

        public String parentInformation()
        {
            final StringBuilder sb = new StringBuilder();
            final ParentCandidate[] candidates = getParentCandidates();
            for (ParentCandidate candidate : candidates)
            {
                sb.append(candidateInformation(candidate) + ",");
            }
            return String.format("%d \t f:%d \t p:%d \t c(%d):%s", id, frame + FRAME_OFFSET,
                    getParentCellId(), candidates.length, sb.toString());
        }

        private String candidateInformation(ParentCandidate candidate)
        {
            if (PRODUCTION)
            {
                return Integer.toString(candidate.parent.id);
            } else
            {
                String prefix = "";
                Integer rightParentIdOrNull =
                        GenerationDetectionAccuracyTester.correctParents.get(candidate.child.id);
                if (rightParentIdOrNull == null)
                {
                    prefix = "F"; // fake
                } else if (rightParentIdOrNull.equals(candidate.parent.id))
                {
                    prefix = "+"; // ok
                } else
                {
                    prefix = "-"; // wrong
                }

                Cell previousParent = candidate.previousFrameParent;
                Cell parent = candidate.parent;
                // Cell nextParent = candidate.nextFrameParent;

                StringBuilder sb = new StringBuilder();
                sb.append("\n" + SEPARATOR + prefix + SEPARATOR + candidate.distanceSq + SEPARATOR
                        + previousParent.longToString());
                sb.append("\n" + SEPARATOR + prefix + SEPARATOR + candidate.distanceSq + SEPARATOR
                        + parent.longToString());
                // sb.append("\n" + SEPARATOR + prefix + SEPARATOR + candidate.distanceSq +
                // SEPARATOR
                // + nextParent.longToString());
                return sb.toString();
            }
        }

        public String longToString()
        {
            String[] tokens =
                        { Integer.toString(id), Integer.toString(frame + FRAME_OFFSET),
                                Integer.toString(x), Integer.toString(y), Integer.toString(numPix),
                                doubleToString(majAxis), doubleToString(minAxis),
                                doubleToString(eccentricity), doubleToString(fftStat),
                                doubleToString(perim), doubleToString(fBackground),
                                doubleToString(fTotal), doubleToString(aTotal),
                                doubleToString(fNucleus), doubleToString(aNucleus),
                                doubleToString(fMean), doubleToString(fDeviation),
                                Integer.toString(getParentCellId()),
                                Integer.toString(getAlternatives()), Integer.toString(generation) };
            return StringUtils.join(tokens, SEPARATOR);
        }

        public String getOutputString()
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(inputRow);
            sb.append(SEPARATOR);
            sb.append(getParentCellId());
            sb.append(SEPARATOR);
            sb.append(getAlternatives());
            sb.append(SEPARATOR);
            sb.append(getGeneration());
            return sb.toString();
        }

        /** uses only cell id (without time frame) to be able to easily operate on sets of cells */
        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (obj instanceof Cell == false)
            {
                return false;
            }
            final Cell that = (Cell) obj;
            return new EqualsBuilder().append(this.id, that.id).isEquals();
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder().append(id).toHashCode();
        }

    }

    /** candidate for the cell parent */
    private static class ParentCandidate implements Comparable<ParentCandidate>
    {

        private final Cell previousFrameParent;

        @SuppressWarnings("unused")
        private final Cell nextFrameParent;

        /**
         * parent cell from the same frame that the child appeared on or from the previous frame if
         * parent disappeared just after division
         */
        private final Cell parent;

        private final Cell child;

        private final Integer distanceSq;

        public ParentCandidate(Cell previousFrameParent, Cell child)
        {
            this.previousFrameParent = previousFrameParent;
            // on the frame that child appeared parent could disappear (5 cases in test data)
            final Cell childFrameParentOrNull =
                    cellsByIdAndFrame.get(previousFrameParent.id).get(child.frame);
            this.parent =
                    (childFrameParentOrNull != null) ? childFrameParentOrNull : previousFrameParent;
            final Cell nextFrameParentOrNull =
                    cellsByIdAndFrame.get(previousFrameParent.id).get(child.frame + 1);
            this.nextFrameParent = (nextFrameParentOrNull != null) ? nextFrameParentOrNull : parent;

            this.child = child;
            this.distanceSq = distanceSq(parent, child);
        }

        private Integer getDistanceSq()
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
        public boolean isDistanceValid()
        {
            double maxValidDistance = 1.1 * ((maxAxis + child.getMajAxis()) / 2);
            return getDistanceSq() <= square(maxValidDistance);
        }

        public boolean isNumPixValid()
        {
            return parent.getNumPix() >= MIN_PARENT_PIXELS;
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

    // <id, <frame, cell>>
    private static Map<Integer, Map<Integer, Cell>> cellsByIdAndFrame;

    //
    // // <frame, <id, cell>>
    // private static Map<Integer, Map<Integer, Cell>> cellsByFrameAndId;

    // <frame, cells>
    private static Map<Integer, Set<Cell>> cellsByFrame;

    // <id, smooth fluorescence deviation values>
    // - smooth fluorescence deviation values are indexed by cell frame
    // - if cell didn't exist on a frame there will be null as a value
    private static Map<Integer, Double[]> cellSmoothFDeviationByIdAndFrame;

    // <child id, parent candidates sorted in order (first cell is most likely a parent)>
    private static Map<Integer, ParentCandidate[]> parentCandidatesByChildId;

    // <child id, fake parent id>
    private static Map<Integer, Integer> ignoredChildrenFakeParentIds;

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

            writer = new PrintWriter(output);
            if (PRODUCTION)
            {
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
                    writer.append(SEPARATOR + doubleToString(cell.getSmoothFDeviation()));
                    // writer.append(cell.getOutputString());
                    writer.append(NEW_LINE);
                }
            } else
            {
                String[] tokens =
                            { "cellID", "frame", "x", "y", "numPix", "majAxis", "minAxis",
                                    "eccentricity", "fftStat", "perim", "background", "fTotal",
                                    "aTotal", "fNucleus", "aNucleus", "fMean", "fDeviation",
                                    "parent", "alternatives", "generation" };
                writer.append(StringUtils.join(tokens, SEPARATOR));
                writer.append(NEW_LINE);
                Integer previousId = -1;
                for (Cell cell : cells)
                {
                    if (cell.getParentCellId() >= 0 && (previousId.equals(cell.getId()) == false))
                    {
                        writer.append(cell.longToString());
                        writer.append(NEW_LINE);
                        previousId = cell.getId();
                    }
                }
            }
            log(String.format("\nMother connections found (%d):\n", newBornCells.size()));
            log("cellID\t frame\t mother\t candidates");
            for (Cell cell : newBornCells)
            {
                log(cell.parentInformation());
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
        generateSmoothFDeviationValues();

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
    }

    // smooth fluorescence deviation computation - refactor

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
            result[cell.frame] = cell.getFDeviation();
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
            // TODO 2009-12-02, Piotr Buczek: check what happens if this check is removed
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
        final int previousFrame = frame - 1;
        // TODO 2009-11-06, Piotr Buczek: take 2 or 3 frames
        final Set<Cell> previousFrameCells = cellsByFrame.get(previousFrame);
        for (Cell cell : newCells)
        {
            // ignore cells that are not new born or not enough data is available
            if (isValidNewBornCell(cell) == false)
            {
                continue;
            }
            final List<ParentCandidate> parentCandidates = new ArrayList<ParentCandidate>(4);

            for (Cell previousFrameCell : previousFrameCells)
            {
                final ParentCandidate candidate = new ParentCandidate(previousFrameCell, cell);
                // Its easier and more effective to get only those cells that are closer then a
                // reasonable distance to current cell then getting k closest parents to current
                // cell.
                // Parent cells have to be bigger than new born cells.
                if (candidate.isAlive() && candidate.isDistanceValid())
                {
                    if (candidate.isNumPixValid()) // could be tested before candidate is created
                    {
                        parentCandidates.add(candidate);
                    } else
                    {
                        log(String
                                .format(
                                        "Cell %d is to small (%d) to be a mother of %d that appeared on frame %d.",
                                        candidate.parent.id, candidate.parent.numPix, cell.id,
                                        candidate.distanceSq, frame));
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
                // filter only when filter is enabled and there is more than one candidate
                // (otherwise filtering will always leave the only one candidate that is available)
                if (parentCandidates.size() > 1)
                {
                    filterCandidatesWithDistance(parentCandidates);
                }
                if (parentCandidates.size() > 1
                        && configParameters.isBeginningFWindowFilterEnabled())
                {
                    filterCandidatesWithBeginningFluorescence(parentCandidates);
                }
                if (parentCandidates.size() > 1 && configParameters.isFirstPeakFilterEnabled())
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

    // TODO 2009-12-02, Piotr Buczek: extract filterBetterCandidates
    private static void filterCandidatesWithFirstPeak(List<ParentCandidate> parentCandidates)
    {
        // If some candidates have a fluorescence peak raise (short: peak) in the same point of time
        // as first peak of daughter they are better candidates and other candidates will be
        // filtered out.

        final List<ParentCandidate> betterCandidates = new ArrayList<ParentCandidate>();
        final int framesToIgnore = configParameters.getFirstPeakNumberOfFirstFramesToIgnore();
        final int maxChildOffset = configParameters.getFirstPeakMaxChildOffset();
        final int maxParentOffset = configParameters.getFirstPeakMaxParentOffset();
        final int maxMissing = configParameters.getFirstPeakMaxMissing();
        final int minLength = configParameters.getFirstPeakMinLength();
        final double minFrameHeightDiff = configParameters.getFirstPeakMinFrameHeightDiff();
        final double minTotalHeightDiff = configParameters.getFirstPeakMinTotalHeightDiff();
        final int maxFrameHeightDiffExceptions =
                configParameters.getFirstPeakMaxFrameHeightDiffExceptions();

        for (ParentCandidate candidate : parentCandidates)
        {
            if (hasSimilarFirstPeak(candidate, framesToIgnore, maxChildOffset, maxParentOffset,
                    maxMissing, minLength, minFrameHeightDiff, minTotalHeightDiff,
                    maxFrameHeightDiffExceptions))
            {
                betterCandidates.add(candidate);
            }
        }

        if (betterCandidates.size() > 0)
        {
            parentCandidates.retainAll(betterCandidates);
        }
    }

    private static boolean hasSimilarFirstPeak(ParentCandidate candidate, int framesToIgnore,
            int maxChildOffset, int maxParentOffset, int maxMissing, int minLength,
            double minFrameHeightDiff, double minTotalHeightDiff, int maxFrameHeightDiffExceptions)
    {
        final int childId = candidate.child.id;
        final int candidateId = candidate.parent.id;

        int minFrame = candidate.child.frame + framesToIgnore;

        Integer childFirstPeakStartOrNull =
                tryFindPeakStart(childId, minFrame, maxChildOffset, minLength, minFrameHeightDiff,
                        minTotalHeightDiff, maxMissing, maxFrameHeightDiffExceptions);
        if (childFirstPeakStartOrNull != null)
        {
            if (tryFindPeakStart(candidateId, childFirstPeakStartOrNull - maxParentOffset,
                    maxParentOffset * 2, minLength, minFrameHeightDiff, minTotalHeightDiff,
                    maxMissing, maxFrameHeightDiffExceptions) != null)
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

    private static Integer tryFindPeakStart(int cellId, int minFrame, int maxOffset, int minLength,
            double minFrameHeightDiff, double minTotalHeightDiff, int maxMissing,
            int maxFrameHeightDiffExceptions)
    {
        final Double[] cellSFDeviations = cellSmoothFDeviationByIdAndFrame.get(cellId);

        int minFrameIndex = Math.max(minFrame, 0);
        int maxFrameIndex = Math.min(minFrame + maxOffset, maxFrame);

        for (int frame = minFrameIndex; frame <= maxFrameIndex; frame++)
        {
            if (cellSFDeviations[frame] == null)
            {
                continue; // cell needs to exist on the first frame of a peak
            }
            double currentSFD = cellSFDeviations[frame];
            double totalHeightDiff = 0;
            int missing = 0;
            int exceptions = 0;
            int nextFrame = frame + 1;

            while (missing <= maxMissing && exceptions <= maxFrameHeightDiffExceptions
                    && nextFrame <= maxFrame)
            {
                if (cellSFDeviations[nextFrame] != null)
                {
                    double nextFrameSFD = cellSFDeviations[nextFrame];
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

    private static void filterCandidatesWithBeginningFluorescence(
            List<ParentCandidate> parentCandidates)
    {
        // If some candidates have very similar fluorescence as the child cell in the first few
        // frames they are better candidates and other candidates will be filtered out
        final List<ParentCandidate> betterCandidates = new ArrayList<ParentCandidate>();
        final int maxOffset = configParameters.getBeginningFWindowMaxOffset();
        final int windowLength = configParameters.getBeginningFWindowLength();
        final int maxMissing = configParameters.getBeginningFWindowMaxMissing();
        final double maxAvgDiff = configParameters.getBeginningFWindowMaxAvgDiff();

        for (ParentCandidate candidate : parentCandidates)
        {
            if (hasSimilarBeginningFluorescence(candidate, maxOffset, windowLength, maxMissing,
                    maxAvgDiff))
            {
                betterCandidates.add(candidate);
            }
        }

        if (betterCandidates.size() > 0)
        {
            parentCandidates.retainAll(betterCandidates);
        }
    }

    private static boolean hasSimilarBeginningFluorescence(ParentCandidate candidate,
            final int maxOffset, final int windowLength, final int maxMissing,
            final double maxAvgDiff)
    {
        final int childId = candidate.child.id;
        final int candidateId = candidate.parent.id;
        final Double[] childSFDeviations = cellSmoothFDeviationByIdAndFrame.get(childId);
        final Double[] candidateSFDeviations = cellSmoothFDeviationByIdAndFrame.get(candidateId);

        for (int candidateOffset = -maxOffset; candidateOffset <= maxOffset; candidateOffset++)
        {
            final int firstChildFrame = candidate.child.frame;
            final int firstCandidateFrame = firstChildFrame + candidateOffset;
            double currentDiff = 0;
            int counter = 0;
            for (int windowOffset = 0; windowOffset < windowLength; windowOffset++)
            {
                final Double childSFD = childSFDeviations[firstChildFrame + windowOffset];
                final Double candidateSFD =
                        candidateSFDeviations[firstCandidateFrame + windowOffset];
                if (childSFD != null && candidateSFD != null)
                {
                    currentDiff += Math.abs(childSFD - candidateSFD);
                    counter++;
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

    private static void filterCandidatesWithDistance(List<ParentCandidate> parentCandidates)
    {
        // If some candidates almost touch the child cell they are better candidates
        // and other candidates will be filtered out
        final List<ParentCandidate> betterCandidates = new ArrayList<ParentCandidate>();
        for (ParentCandidate candidate : parentCandidates)
        {
            if (hasCloseDistance(candidate))
            {
                betterCandidates.add(candidate);
            }
        }
        if (betterCandidates.size() > 0)
        {
            parentCandidates.retainAll(betterCandidates);
        }
    }

    private static boolean hasCloseDistance(ParentCandidate candidate)
    {
        final double closeDistance =
                (candidate.parent.getMajAxis() + candidate.child.getMajAxis()) / 2;
        final double maxDistanceSq = square(1.2 * closeDistance);
        return maxDistanceSq >= candidate.distanceSq;
    }

    private static void setParents(Cell child, ParentCandidate... candidates)
    {
        assert candidates.length != 0;
        final Cell parent = candidates[0].parent;
        parentCandidatesByChildId.put(child.id, candidates);
        increaseGeneration(parent.getId(), child); // parent is from previous frame
    }

    private static void ignore(Cell child, IgnoreNewCellReason reason)
    {
        reason.incCounter();
        ignoredChildrenFakeParentIds.put(child.id, reason.getFakeParentId());
        log(String.format("Ignoring new cell with id:%d appearing on frame:%d.", child.id,
                child.frame));
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
        // if (isAppearingToLate(cell))
        // {
        // ignore(cell, IgnoreNewCellReason.NOT_ENOUGH_DATA);
        // log(String
        // .format("Reason: there is not enough data about the cell (it appears on one of last few frames)"));
        // return true;
        // }
        // if (isEnoughFramesAvailable(cell) == false)
        // {
        // ignore(cell, IgnoreNewCellReason.NOT_ENOUGH_DATA);
        // log(String
        // .format("Reason: there is not enough data about the cell (only a few frames)."));
        // return true;
        // }
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
        // MIN_STABLE_NUCLEUS_AREA_FRAMES consecutive cell frames need to have correct nucleus area
        int counter = 0;
        int previousFrame = cell.getFrame() - 1;
        for (Cell c : cellsByIdAndFrame.get(cell.getId()).values())
        {
            final int currentFrame = c.getFrame();
            if (c.getANucleus() == NUCLEUS_AREA && currentFrame == previousFrame + 1)
            {
                counter++;
                if (counter == MIN_STABLE_NUCLEUS_AREA_FRAMES)
                {
                    return true;
                }
            } else
            {
                counter = 0;
            }
            previousFrame = currentFrame;
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
                    .format("Reason: there is not enough data about the cell (it appears on one of last few frames)"));
            return true;
        }
        if (isAppearingToLate(cell))
        {
            ignore(cell, IgnoreNewCellReason.NOT_ENOUGH_DATA);
            log(String
                    .format("Reason: there is not enough data about the cell (it appears on one of first few frames)"));
            return true;
        }
        if (isEnoughFramesAvailable(cell) == false)
        {
            ignore(cell, IgnoreNewCellReason.NOT_ENOUGH_DATA);
            log(String
                    .format("Reason: there is not enough data about the cell (only a few frames)."));
            return true;
        }

        return true;
    }

    // dying cell has very high fluorescence mean (additionally size drops ~30%)
    private static boolean isDying(Cell cell)
    {
        return cell.getFMean() > MAX_F_MEAN_OF_LIVING_CELL;
    }

    private static int distanceSq(Cell c1, Cell c2)
    {
        final int dx = c1.x - c2.x;
        final int dy = c1.y - c2.y;
        return dx * dx + dy * dy;
    }

    /** @returns ceiling of given value squared */
    private static int square(double value)
    {
        return (int) Math.ceil(value * value);
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
            result.add(new Cell(line));
        }
        return result;
    }

    private static String intToString(int value)
    {
        return String.format("%d", value);
    }

    private static String doubleToString(double value)
    {
        return String.format("%1.2f", value);
    }

    /** log (on stderr) */
    private static void log(String message)
    {
        System.err.println(message);
    }

    /** log (on stderr) if debug messages are enabled */
    private static void debug(String message)
    {
        if (DEBUG)
        {
            System.err.println("DEBUG: " + message);
        }
    }
}
