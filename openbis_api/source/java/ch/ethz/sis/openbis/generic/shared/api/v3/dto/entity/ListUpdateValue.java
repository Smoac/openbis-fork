package ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("ListUpdateValue")
public class ListUpdateValue<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonObject("ListUpdateAction")
    public static class ListUpdateAction<T1> implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private Collection<T1> ids;

        public Collection<T1> getIds()
        {
            return ids;
        }

        public void setIds(Collection<T1> ids)
        {
            this.ids = ids;
        }
    }

    @JsonObject("AddAction")
    public static class ListUpdateActionAdd<T1> extends ListUpdateAction<T1>
    {
        private static final long serialVersionUID = 1L;
    }

    @JsonObject("RemoveAction")
    public static class ListUpdateActionRemove<T1> extends ListUpdateAction<T1>
    {
        private static final long serialVersionUID = 1L;
    }

    @JsonObject("SetAction")
    public static class ListUpdateActionSet<T1> extends ListUpdateAction<T1>
    {
        private static final long serialVersionUID = 1L;
    }

    private List<ListUpdateAction<T>> listOfActions = new LinkedList<ListUpdateAction<T>>();

    public void setActions(List<ListUpdateAction<T>> actions)
    {
        listOfActions = new LinkedList<ListUpdateValue.ListUpdateAction<T>>(actions);
    }

    public List<ListUpdateAction<T>> getActions()
    {
        return listOfActions;
    }

    public boolean hasActions()
    {
        return getActions() != null && getActions().size() > 0;
    }

    public void remove(T... items)
    {
        ListUpdateActionRemove<T> action = new ListUpdateActionRemove<T>();
        action.setIds(Arrays.asList(items));
        listOfActions.add(action);
    }

    public void add(T... items)
    {
        ListUpdateActionAdd<T> action = new ListUpdateActionAdd<T>();
        action.setIds(Arrays.asList(items));
        listOfActions.add(action);
    }

    public void set(T... items)
    {
        ListUpdateActionSet<T> action = new ListUpdateActionSet<T>();
        if (items == null)
        {
            action.setIds(Collections.<T> emptyList());
        } else
        {
            action.setIds(Arrays.asList(items));
        }
        listOfActions.add(action);
    }

}
