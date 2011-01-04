package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.List;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A simple combobox which provides a model class {@link LabeledItem} to store any value and the its
 * label.
 * 
 * @author Tomasz Pylak
 */
public class SimpleModelComboBox<T> extends SimpleComboBox<LabeledItem<T>>
{

    /**
     * Creates a combobox and selects the first value. For each item there should be one
     * corresponding label.
     */
    public SimpleModelComboBox(IMessageProvider messageProvider, List<T> items,
            List<String> labels, int widthPx)
    {
        assert items.size() == labels.size() : "for each item there should be one corresponding label";

        configure(messageProvider, widthPx);
        setModel(items, labels);
        GWTUtils.autoselect(this);
    }

    /**
     * Creates a combobox with a specified model.
     */
    public SimpleModelComboBox(IMessageProvider messageProvider, List<LabeledItem<T>> model,
            int widthPx)
    {
        configure(messageProvider, widthPx);
        add(model);
        GWTUtils.autoselect(this);
    }

    private void configure(IMessageProvider messageProvider, int widthPx)
    {
        setTriggerAction(TriggerAction.ALL);
        setAllowBlank(false);
        setEditable(false);
        setEmptyText(messageProvider.getMessage(Dict.COMBO_BOX_CHOOSE));
        setWidth("" + widthPx);
    }

    private void setModel(List<T> items, List<String> labels)
    {
        int i = 0;
        for (T item : items)
        {
            add(new LabeledItem<T>(item, labels.get(i)));
            i++;
        }
    }

    public T getChosenItem()
    {
        return getSimpleValue().getItem();
    }

    public static <T> T getChosenItem(SelectionChangedEvent<SimpleComboValue<LabeledItem<T>>> se)
    {
        return se.getSelectedItem().getValue().getItem();
    }
}
