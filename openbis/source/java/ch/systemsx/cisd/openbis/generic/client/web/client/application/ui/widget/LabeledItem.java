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
    private final T item;

    private final String label;

    public LabeledItem(T item, String label)
    {
        this.item = item;
        this.label = label;
    }

    public T getItem()
    {
        return item;
    }

    @Override
    public String toString()
    {
        return label;
    }
}