package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.common.update.ListUpdateValue")
public class ListUpdateValue<ADD, REMOVE, SET, ACTION> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonObject("as.dto.common.update.ListUpdateAction")
    public static class ListUpdateAction<T> implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private Collection<? extends T> items;

        public Collection<? extends T> getItems()
        {
            return items;
        }

        public void setItems(Collection<? extends T> items)
        {
            this.items = items;
        }

    }

    @JsonObject("as.dto.common.update.ListUpdateActionAdd")
    public static class ListUpdateActionAdd<ADD> extends ListUpdateAction<ADD>
    {
        private static final long serialVersionUID = 1L;
    }

    @JsonObject("as.dto.common.update.ListUpdateActionRemove")
    public static class ListUpdateActionRemove<REMOVE> extends ListUpdateAction<REMOVE>
    {
        private static final long serialVersionUID = 1L;
    }

    @JsonObject("as.dto.common.update.ListUpdateActionSet")
    public static class ListUpdateActionSet<SET> extends ListUpdateAction<SET>
    {
        private static final long serialVersionUID = 1L;
    }

    private List<ListUpdateAction<ACTION>> actions = new LinkedList<ListUpdateAction<ACTION>>();

    public void setActions(List<ListUpdateAction<ACTION>> actions)
    {
        this.actions = new LinkedList<ListUpdateAction<ACTION>>(actions);
    }

    public List<ListUpdateAction<ACTION>> getActions()
    {
        return actions;
    }

    public boolean hasActions()
    {
        return getActions() != null && getActions().size() > 0;
    }

    @SuppressWarnings("unchecked")
    public void remove(REMOVE... items)
    {
        ListUpdateActionRemove<REMOVE> action = new ListUpdateActionRemove<REMOVE>();
        action.setItems(Arrays.asList(items));
        actions.add((ListUpdateAction<ACTION>) action);
    }

    @SuppressWarnings("unchecked")
    public void add(ADD... items)
    {
        ListUpdateActionAdd<ADD> action = new ListUpdateActionAdd<ADD>();
        action.setItems(Arrays.asList(items));
        actions.add((ListUpdateAction<ACTION>) action);
    }

    @SuppressWarnings("unchecked")
    public void set(SET... items)
    {
        ListUpdateActionSet<SET> action = new ListUpdateActionSet<SET>();
        if (items == null)
        {
            action.setItems(Collections.<SET> emptyList());
        } else
        {
            action.setItems(Arrays.asList(items));
        }
        actions.add((ListUpdateAction<ACTION>) action);
    }

}
