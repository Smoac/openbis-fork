package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;

/**
 * Item with label which can be conveniently used in {@link SimpleComboBox} or
 * {@link CheckBoxGroupWithModel}.
 * 
 * @author Tomasz Pylak
 */
public class LabeledItem<T>
{
    public static final String LABEL_FIELD = "label";

    public static final String TOOLTIP_FIELD = "tooltip";

    private final T item;

    private final String label;

    private final String tooltip;

    public LabeledItem(T item, String label)
    {
        this(item, label, label);
    }

    public LabeledItem(T item, String label, String tooltip)
    {
        this.item = item;
        this.label = label;
        this.tooltip = tooltip;
    }

    public T getItem()
    {
        return item;
    }

    public String getLabel()
    {
        return label;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    @Override
    public String toString()
    {
        return label;
    }

}