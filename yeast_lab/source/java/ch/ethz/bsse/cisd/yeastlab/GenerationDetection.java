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
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Piotr Buczek
 */
public class GenerationDetection
{
    private static final String INPUT_FILE = "resource/examples/input/pos2";

    private static final String OUTPUT_FILE = "resource/examples/output/pos2";

    private static final boolean DEBUG = true;

    private static final int FIRST_FRAME_NUM = 0;

    private static final int INITIAL_GENERATION = 1;

    private static final int FAKE_CELL_GENERATION = 0;

    @SuppressWarnings("unused")
    private static final String PARENT_ID_COL_NAME = "parentID";

    @SuppressWarnings("unused")
    private static final String ALTERNATIVES_COL_NAME = "alternatives";

    @SuppressWarnings("unused")
    private static final String GENERATION_COL_NAME = "generation";

    // Could be used as a parameter that user needs to specify but on test data
    // using value that is higher produces more results that should rather be ignored.
    private static final int MAX_PIXELS_PER_NEW_BORN_CELL = 300;

    private static final int MIN_PIXELS_PER_PARENT = MAX_PIXELS_PER_NEW_BORN_CELL;

    private static final String SEPARATOR = "\t";

    private static final String NEW_LINE = "\n";

    // column positions - will they be fixed? or should we read them from file header?
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

    private static final int ROT_VOL_POSITION = 15;

    private static final int CON_VOL_POSITION = 16;

    private static final int A_VACUOLE_POSITION = 17;

    private static final int F_VACUOLE_POSITION = 18;

    private static final int A_SURF_POSITION = 58;

    private static final int SPHERE_VOL_POSITION = 60;

    @SuppressWarnings("unused")
    private static String headerInputRow;

    private static double maxAxis; // maximal major axis found in input data

    private static int maxFrame; // maximal frame number (easy to read from file)

    /** information about a cell in given */
    static class Cell
    {
        /** cell identification number */
        private final int id; // cellID

        /** time frame of the cell (0 through n-1 where n = number of points in the time course) */
        // NOTE Not every cell is necessarily found in every time point.
        private final int frame; // t.frame

        /** x coordinate of the centroid of the cell */
        private final int x; // xpos

        /** y coordinate of the centroid of the cell */
        private final int y; // ypos

        /** number of pixels associated with the cell */
        private final int numPix; // num.pix

        /** a measure of circularity - 0 means a perfect circle */
        private final double fftStat;

        /** circumference of the cell in pixel units */
        private final double perim;

        /** length of the major axis in pixel units */
        private final double majAxis;

        /** length of the minor axis in pixel units */
        private final double minAxis;

        /** sum of the fluorescence image for all the pixels found in that cell */
        private final double fTot; // f.tot

        /** area of the cell in pixels */
        private final double aTot; // a.tot

        /** volume of rotation of the cell around its major axis */
        private final double rotVol; // rot.vol

        /** volume of the cell as determined by the conical volume method */
        private final double conVol; // con.vol

        private final double aVacuole;

        private final double fVacuole;

        /** surface area as calculated by the union of spheres method */
        private final double aSurf; // a.surf

        /** volume as measured by the union of spheres method */
        private final double sphereVol; // sphere.vol

        /** whole input data row */
        transient private final String inputRow;

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

            this.aVacuole = Double.parseDouble(StringUtils.trim(tokens[A_VACUOLE_POSITION]));
            this.fVacuole = Double.parseDouble(StringUtils.trim(tokens[F_VACUOLE_POSITION]));

            this.fTot = Double.parseDouble(StringUtils.trim(tokens[F_TOT_POSITION]));
            this.aTot = Double.parseDouble(StringUtils.trim(tokens[A_TOT_POSITION]));
            this.rotVol = Double.parseDouble(StringUtils.trim(tokens[ROT_VOL_POSITION]));
            this.conVol = Double.parseDouble(StringUtils.trim(tokens[CON_VOL_POSITION]));
            this.aSurf = Double.parseDouble(StringUtils.trim(tokens[A_SURF_POSITION]));
            this.sphereVol = Double.parseDouble(StringUtils.trim(tokens[SPHERE_VOL_POSITION]));
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
                return -1;
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

        public double getFTot()
        {
            return fTot;
        }

        public double getATot()
        {
            return aTot;
        }

        public double getRotVol()
        {
            return rotVol;
        }

        public double getConVol()
        {
            return conVol;
        }

        public double getASurf()
        {
            return aSurf;
        }

        public double getSphereVol()
        {
            return sphereVol;
        }

        public double getFftStat()
        {
            return fftStat;
        }

        public double getPerim()
        {
            return perim;
        }

        public double getAVacuole()
        {
            return aVacuole;
        }

        public double getFVacuole()
        {
            return fVacuole;
        }

        public String getInputRow()
        {
            return inputRow;
        }

        public int getAlternatives()
        {
            return getParentCandidates().length - 1;
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
                sb.append(candidate.parent.id + " " + candidate.distanceSq + ", ");
            }
            return String.format("%d \t f:%d \t p:%d \t c(%d):%s", id, frame + 1,
                    getParentCellId(), candidates.length, sb.toString());
        }

        public String longToString()
        {
            // ToStringBuilder.reflectionToString(this);
            String[] tokens =
                        { Integer.toString(id), Integer.toString(frame + 1), Integer.toString(x),
                                Integer.toString(y), Integer.toString(numPix),
                                Double.toString(majAxis), Double.toString(minAxis),
                                Double.toString(fftStat), Double.toString(perim),
                                Double.toString(aVacuole), Double.toString(fVacuole),
                                Double.toString(fTot), Double.toString(aTot),
                                Double.toString(rotVol), Double.toString(conVol),
                                Double.toString(aSurf), Double.toString(sphereVol),
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

        @SuppressWarnings("unused")
        private final Cell previousFrameParent;

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
            this.child = child;
            this.distanceSq = distanceSq(parent, child);
        }

        private Integer getDistanceSq()
        {
            return distanceSq;
        }

        // The tightest condition that we could use as a threshold for valid distance is
        // (maxAxis+currentMajorAxis)/2
        // which is the longest possible distance that current cell can be from another cell
        // assuming they touch each other. We take 120% of this value because sometimes there is a
        // bit of a space between parent and child.
        // We could use a less tighter threshold like maxAxis if we find that we have some false
        // negatives but on the test data the results are better with this tighter threshold.
        // Of course:
        // maxAxis > (maxAxis+currentMajorAxis)/2.
        public boolean isDistanceValid()
        {
            double maxValidDistance = 1 * (maxAxis + child.getMajAxis()) / 2;
            return getDistanceSq() <= square(maxValidDistance);
        }

        public boolean isNumPixValid()
        {
            return parent.getNumPix() >= MIN_PIXELS_PER_PARENT;
        }

        @Override
        public String toString()
        {
            return "id:" + parent.getId() + " dist:" + getDistanceSq();
        }

        //
        // Comparable
        //

        // simplest comparator - parent that is closer is better
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

    // <child id, parent candidates sorted in order (first cell is most likely a parent)>
    private static Map<Integer, ParentCandidate[]> parentCandidatesByChildId;

    public static void main(String[] args)
    {
        final long start = System.currentTimeMillis();
        // final String fileName = args[0];
        // assert fileName != null;
        final File input = new File(INPUT_FILE);
        assert input.exists() && input.isFile() && input.canRead() : "There is no file in '"
                + INPUT_FILE + "' that can be read.";
        final File output = new File(OUTPUT_FILE);

        // assert output.exists() == false : "File '" + OUTPUT_FILE + "' already exists.";

        BufferedReader reader = null;
        PrintWriter writer = null;
        try
        {
            reader = new BufferedReader(new FileReader(input));
            final List<Cell> cells = load(reader, input);
            createDataStructures(cells);
            analyzeData();

            writer = new PrintWriter(output);
            // writer.append(headerInputRow);
            // writer.append(SEPARATOR + PARENT_ID_COL_NAME);
            // writer.append(SEPARATOR + ALTERNATIVE_COL_NAME);
            // writer.append(SEPARATOR + GENERATION_COL_NAME + NEW_LINE);

            String[] tokens =
                        { "cellID", "frame", "x", "y", "numPix", "maxAxis", "minAxis", "fftStat",
                                "perim", "aVacuole", "fVacuole", "fTot", "aTot", "rotVol",
                                "conVol", "aSurf", "sphereVol", "parent", "alternatives",
                                "generation" };
            writer.append(StringUtils.join(tokens, SEPARATOR));
            writer.append(NEW_LINE);
            for (Cell cell : cells)
            {
                writer.append(cell.longToString());
                writer.append(NEW_LINE);
                // writer.append(cell.getOutputString());
            }
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

        System.err.println((System.currentTimeMillis() - start) / 1000.0);

        if (DEBUG)
        {
            for (Cell cell : newBornCells)
            {
                System.out.println(cell.parentInformation());
            }
            GenerationDetectionAccuracyTester.computeResultsAccuracy(newBornCells);
        }
    }

    private static void createDataStructures(List<Cell> cells)
    {

        // common empty array to avoid null checks
        final ParentCandidate[] initialCandidates = new ParentCandidate[0];
        cellsByIdAndFrame = new LinkedHashMap<Integer, Map<Integer, Cell>>();
        // cellsByFrameAndId = new LinkedHashMap<Integer, Map<Integer, Cell>>();
        cellsByFrame = new LinkedHashMap<Integer, Set<Cell>>();
        parentCandidatesByChildId = new LinkedHashMap<Integer, ParentCandidate[]>();
        maxAxis = 0;
        maxFrame = -1;
        for (Cell cell : cells)
        {
            // cellsByIdAndFrame
            Map<Integer, Cell> byFrame = cellsByIdAndFrame.get(cell.getId());
            if (byFrame == null)
            {
                // increasing order of frames (not needed)
                byFrame = new LinkedHashMap<Integer, Cell>();
                cellsByIdAndFrame.put(cell.getId(), byFrame);
            }
            byFrame.put(cell.getFrame(), cell);
            // // cellsByFrameAndId
            // Map<Integer, Cell> byId = cellsByFrameAndId.get(cell.getFrame());
            // if (byId == null)
            // {
            // byId = new HashMap<Integer, Cell>();
            // }
            // byId.put(cell.getId(), cell);
            // cellsByFrame
            Set<Cell> frameCells = cellsByFrame.get(cell.getFrame());
            if (frameCells == null)
            {
                frameCells = new LinkedHashSet<Cell>();
                cellsByFrame.put(cell.getFrame(), frameCells);
            }
            frameCells.add(cell);
            // initialize candidates
            parentCandidatesByChildId.put(cell.getId(), initialCandidates);
            //
            if (cell.getMajAxis() > maxAxis)
            {
                maxAxis = cell.getMajAxis();
            }
            if (cell.getFrame() > maxFrame)
            {
                maxFrame = cell.getFrame();
            }
        }
    }

    private static void analyzeData() throws Exception
    {
        // Set<Cell> firstFrame = cellsByFrame.get(FIRST_FRAME_NUM);
        Set<Cell> cellsFromOldFrames = new LinkedHashSet<Cell>();
        // cells from older frames - one per id
        // BTW - the first appearance of cell will be held in this set (maybe it could be used).
        // instead we could use arrays with e.g. maxCellId = 1000 in C
        for (Entry<Integer, Set<Cell>> entry : cellsByFrame.entrySet())
        {
            final int currentFrame = entry.getKey();
            final Set<Cell> currentFrameCells = entry.getValue();
            System.err.println(String.format("analyzing frame %s...", currentFrame));
            // analyze genealogy of cells that appeared in current frame
            final Set<Cell> newCells = new LinkedHashSet<Cell>(currentFrameCells);
            newCells.removeAll(cellsFromOldFrames);
            analyzeNewFrameCells(currentFrame, newCells);
            // 
            cellsFromOldFrames.addAll(newCells);
        }
    }

    private static List<Cell> newBornCells = new ArrayList<Cell>();

    private static void analyzeNewFrameCells(int frame, Set<Cell> newCells) throws Exception
    {
        if (frame == FIRST_FRAME_NUM)
        {
            return; // nothing to analyze in the first frame
        }
        final int previousFrame = frame - 1;
        for (Cell cell : newCells)
        {
            if (isValidNewBornCell(cell) == false) // ignore cells that are not new born
            {
                cell.generation = FAKE_CELL_GENERATION;
                continue;
            }
            // could take previousFrameCells from previous step
            final Set<Cell> previousFrameCells = cellsByFrame.get(previousFrame);
            assert previousFrameCells != null;
            // if (previousFrameCells == null)
            // {
            // throw new Exception(String.format("no cells found in frame %s", previousFrame));
            // }
            final List<ParentCandidate> parentCandidates = new ArrayList<ParentCandidate>(4);

            for (Cell previousFrameCell : previousFrameCells)
            {
                final ParentCandidate candidate = new ParentCandidate(previousFrameCell, cell);
                // Its easier and more effective to get only those cells that are closer then a
                // reasonable distance to current cell then getting k closest parents to current
                // cell.
                // Parent cells have to be bigger than new born cells.
                if (candidate.isDistanceValid()) // simplify
                {
                    if (candidate.isNumPixValid()) // could be tested before candidate is created
                    {
                        parentCandidates.add(candidate);
                    } else
                    {
                        System.err
                                .println(String
                                        .format(
                                                "Cell %d is to small (%d) to be a parent of %d that appeared on frame %d.",
                                                candidate.parent.id, +candidate.parent.numPix,
                                                cell.id, candidate.distanceSq, frame));
                    }
                }
            }
            if (parentCandidates.size() == 0)
            {
                System.err.println(String.format("Couldn't find a parent for cell with id '%d'",
                        cell.getId()));
                // continue
            } else
            {
                // sort parent - best will be first
                Collections.sort(parentCandidates);
                setParents(cell, parentCandidates.toArray(new ParentCandidate[0]));
                newBornCells.add(cell);
            }
        }

    }

    private static void setParents(Cell child, ParentCandidate... candidates)
    {
        assert candidates.length != 0;
        final Cell parent = candidates[0].parent;
        parentCandidatesByChildId.put(child.id, candidates);
        increaseGeneration(parent.getId(), child); // parent is from previous frame
    }

    /**
     * Increases generations of all cells with specified id starting from frame of the specified
     * <var>child</var> (it should be the first appearance of the cell with child id).
     */
    private static void increaseGeneration(int parentID, Cell child)
    {
        final int childFrame = child.getFrame();
        final Map<Integer, Cell> parentByFrame = cellsByIdAndFrame.get(parentID);
        int count = 0;
        for (Entry<Integer, Cell> entry : parentByFrame.entrySet())
        {
            final Integer frame = entry.getKey();
            final Cell cell = entry.getValue();
            if (frame >= childFrame)
            {
                cell.increaseGeneration();
                count++;
            }
        }
        if (count == 0) // 5 cases in test data
        {
            System.err.println(String.format("Cell with id '%d' disappears on frame '%d' "
                    + "just after producing cell with id '%d' (on previous frame).", parentID,
                    childFrame, child.getId()));
        }
    }

    private static boolean isValidNewBornCell(Cell cell)
    {
        if (cell.getNumPix() < MAX_PIXELS_PER_NEW_BORN_CELL)
        {
            return true;
        } else
        {
            System.err.println(String.format("Cell with id '%d' that appears on frame '%d' "
                    + "for the first time with '%d'px size is ignored by generation detection "
                    + "algorithm because the size exceeds maximal size of a new born cell (%d).",
                    cell.getId(), cell.getFrame(), cell.getNumPix(), MAX_PIXELS_PER_NEW_BORN_CELL));
            return false;
        }
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
}
