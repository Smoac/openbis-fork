package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;

/**
 * Creates {@link Widget}s presenting a heat map scale.
 * 
 * @author Izabela Adamczyk
 */
public class HeatmapScaleFactory
{

    /**
     * @param firstLabelOrNull - if null, creates a scale widget with one color described by one
     *            label. Otherwise creates a scale widget with one color described by a range (two
     *            labels).
     */
    public static Widget create(String firstLabelOrNull, List<HeatmapScaleElement> scaleElements)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new TableLayout(2));
        container.setBorders(false);
        container.setScrollMode(Scroll.NONE);
        boolean isFirstLabel = firstLabelOrNull != null;
        if (isFirstLabel)
        {
            container.add(createLabel(isFirstLabel, firstLabelOrNull),
                    createLabelData(isFirstLabel));
            container.add(createBox(null), createBoxData());
        }
        for (HeatmapScaleElement r : scaleElements)
        {
            container.add(createLabel(isFirstLabel, r.getLabel()), createLabelData(isFirstLabel));
            container.add(createBox(r.getColor()), createBoxData());
        }
        if (isFirstLabel)
        {
            container.add(createLabel(isFirstLabel, ""), createLabelData(isFirstLabel));
            container.add(createBox(null), createBoxData());
        }
        return container;
    }

    private static TableData createBoxData()
    {
        return new TableData(HorizontalAlignment.LEFT, VerticalAlignment.TOP);
    }

    private static TableData createLabelData(boolean isFirstLabel)
    {
        if (isFirstLabel)
        {
            return new TableData(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM);
        } else
        {
            return new TableData(HorizontalAlignment.RIGHT, VerticalAlignment.MIDDLE);
        }
    }

    private static Component createBox(Color color)
    {
        Text box = new Text();
        box.setStyleAttribute("margin-left", "0.5em");
        if (color != null)
        {
            box.setStyleAttribute("background-color", color.getHexColor());
        }
        box.setWidth("1em");
        box.setHeight("2em");
        return box;
    }

    private static Component createLabel(boolean isFirstLabel, String text)
    {
        Text label = new Text(text);
        label.setStyleAttribute("font-size", "0.5em");
        label.setStyleAttribute("position", "relative");
        if (isFirstLabel)
        {
            label.setStyleAttribute("bottom", "-0.6em");
        }
        return label;
    }
}