/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.IRealNumberRenderer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;

/**
 * @author Izabela Adamczyk
 */
public class NumberHeatmapRendererTest
{
    private static final String COLOR1 = "#67001F";

    private static final String COLOR2 = "#B2182B";

    @Test
    public void testFirstLabel() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        AssertJUnit.assertEquals(3f + "", renderer.tryGetFirstLabel());
    }

    private NumberHeatmapRenderer createMinNeg1Max3Renderer(List<String> colors)
    {
        return new NumberHeatmapRenderer(-1, 3, colors, createDummyRealRenderer());
    }

    private NumberHeatmapRenderer createMin0Max3Renderer(List<String> colors)
    {
        return new NumberHeatmapRenderer(0, 3, colors, createDummyRealRenderer());
    }

    private IRealNumberRenderer createDummyRealRenderer()
    {
        return new IRealNumberRenderer()
            {
                public String render(float value)
                {
                    return "" + value;
                }
            };
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValueTooSmall() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(-1f).getHexColor());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testValueTooBig() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(10f).getHexColor());
    }

    @Test
    public void testMiddleValueOneColor() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(1.5f).getHexColor());
    }

    @Test
    public void testMaxValueOneColor() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(3f).getHexColor());
    }

    @Test
    public void testMinValueOneColor() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(1f).getHexColor());
    }

    @Test
    public void testTwoColors() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR2, COLOR1);
        NumberHeatmapRenderer renderer = createMin0Max3Renderer(colors);
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(0f).getHexColor());
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(1f).getHexColor());
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(1.5f).getHexColor());
        AssertJUnit.assertEquals(COLOR2, renderer.getColor(2f).getHexColor());
        AssertJUnit.assertEquals(COLOR2, renderer.getColor(3f).getHexColor());
    }

    @Test
    public void testManyColors() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1, COLOR2);
        NumberHeatmapRenderer renderer = createMinNeg1Max3Renderer(colors);
        AssertJUnit.assertEquals(COLOR2, renderer.getColor(-1f).getHexColor());
        AssertJUnit.assertEquals(COLOR2, renderer.getColor(1f).getHexColor());
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(1.5f).getHexColor());
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(2f).getHexColor());
        AssertJUnit.assertEquals(COLOR1, renderer.getColor(3f).getHexColor());
    }

    @Test
    public void testScaleOneColor() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1);
        NumberHeatmapRenderer renderer = createMinNeg1Max3Renderer(colors);
        List<HeatmapScaleElement> scale = renderer.calculateScale();
        AssertJUnit.assertEquals(1, scale.size());
        HeatmapScaleElement element = scale.get(0);
        AssertJUnit.assertEquals(3f + "", renderer.tryGetFirstLabel());
        AssertJUnit.assertEquals(COLOR1, element.getColor().getHexColor());
        AssertJUnit.assertEquals(-1f + "", element.getLabel());
    }

    @Test
    public void testScaleTwoColor() throws Exception
    {
        List<String> colors = Arrays.asList(COLOR1, COLOR2);
        NumberHeatmapRenderer renderer = createMinNeg1Max3Renderer(colors);
        List<HeatmapScaleElement> scale = renderer.calculateScale();
        AssertJUnit.assertEquals(2, scale.size());
        AssertJUnit.assertEquals(3f + "", renderer.tryGetFirstLabel());
        HeatmapScaleElement element1 = scale.get(0);
        AssertJUnit.assertEquals(COLOR1, element1.getColor().getHexColor());
        AssertJUnit.assertEquals(1.0 + "", element1.getLabel());
        HeatmapScaleElement element2 = scale.get(1);
        AssertJUnit.assertEquals(COLOR2, element2.getColor().getHexColor());
        AssertJUnit.assertEquals(-1f + "", element2.getLabel());
    }
}
