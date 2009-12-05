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

import static ch.ethz.bsse.cisd.yeastlab.GenerationDetectionUtils.SEPARATOR;
import static ch.ethz.bsse.cisd.yeastlab.GenerationDetectionUtils.doubleToString;
import static ch.ethz.bsse.cisd.yeastlab.GenerationDetectionUtils.intToString;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.ethz.bsse.cisd.yeastlab.GenerationDetection;
import ch.ethz.bsse.cisd.yeastlab.GenerationDetection.IParentInformationProvider;

/**
 * Information about a cell on one frame.
 * 
 * @author Piotr Buczek
 */
public class Cell
{
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

    //

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
    private final double fftStat; // fft.stat

    /** circumference of the cell in pixel units */
    private final double perim; // perim

    /** length of the major axis in pixel units */
    private final double majAxis; // maj.axis

    /** length of the minor axis in pixel units */
    private final double minAxis; // min.axis

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

    /** whole input data row */
    private final String inputRow;

    // new data

    /** cells shape eccentricity (min == 0, max == circle == 1) */
    private final double eccentricity; // min.axis / maj.axis

    /** fluorescence deviation (mean of nucleus fluorescence - mean of cell fluorescence) */
    private final double fDeviation; // f.nucl/a.nucl - f.tot/a.tot

    /** cell fluorescence (mean of cell fluorescence - background fluorescence) */
    private final double fMean; // f.tot/a.tot - f.bg

    /** number of children produced +1 */
    private int generation = GenerationDetection.INITIAL_GENERATION;

    /** is the cell dead */
    private boolean dead = false;

    //

    private final IParentInformationProvider parentInformationProvider;

    public Cell(final String inputRow, final IParentInformationProvider parentInformationProvider)
    {
        this.inputRow = inputRow;
        this.parentInformationProvider = parentInformationProvider;
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

    public int getGeneration()
    {
        return generation;
    }

    public void increaseGeneration()
    {
        this.generation++;
    }

    public boolean isDead()
    {
        return dead;
    }

    public int getParentCellId()
    {
        return parentInformationProvider.getParentCellId(this);
    }

    public int getAlternatives()
    {
        return parentInformationProvider.getAlternatives(this);
    }

    public void markAsDead()
    {
        this.dead = true;
    }

    // for debug
    public String longToString()
    {
        String[] tokens =
                    { intToString(id), intToString(frame + GenerationDetection.FRAME_OFFSET),
                            intToString(x), intToString(y), intToString(numPix),
                            doubleToString(majAxis), doubleToString(minAxis),
                            doubleToString(eccentricity), doubleToString(fftStat),
                            doubleToString(perim), doubleToString(fBackground),
                            doubleToString(fTotal), doubleToString(aTotal),
                            doubleToString(fNucleus), doubleToString(aNucleus),
                            doubleToString(fMean), doubleToString(fDeviation),
                            intToString(generation) };
        return StringUtils.join(tokens, SEPARATOR);
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
