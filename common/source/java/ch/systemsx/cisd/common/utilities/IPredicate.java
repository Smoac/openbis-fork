package ch.systemsx.cisd.common.utilities;

/**
 * Delegate for a function T => boolean
 * 
 * @author jakubs
 */
public interface IPredicate<T>
{
    boolean execute(T arg);
}
