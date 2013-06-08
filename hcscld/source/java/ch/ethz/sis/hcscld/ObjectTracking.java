/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.ethz.sis.hcscld;

import java.util.Arrays;
import java.util.NoSuchElementException;

import ch.systemsx.cisd.base.mdarray.MDIntArray;

/**
 * A class that provides a tracking of objects, represented by a linking of parent to child objects.
 * 
 * @author Bernd Rinn
 */
public class ObjectTracking
{
    private final static int PARENT_COL = 0;

    private final static int CHILD_COL = 1;

    private final static int[] EMPTY = new int[0];

    private final static IIndexList EMPTY_INDEX_LIST = new IIndexList()
        {
            @Override
            public int[] toArray()
            {
                return EMPTY;
            }

            @Override
            public int size()
            {
                return 0;
            }

            @Override
            public IIndexIterator iterator()
            {
                return EMPTY_INDEX_ITERATOR;
            }

            @Override
            public boolean isEmpty()
            {
                return true;
            }

            @Override
            public int get(int index)
            {
                throw new IndexOutOfBoundsException();
            }

            @Override
            public boolean contains(int o)
            {
                return false;
            }
            
            @Override
            public String toString()
            {
                return "IndexList []";
            }
        };

    private final static IIndexIterator EMPTY_INDEX_ITERATOR = new IIndexIterator()
        {
            @Override
            public int previous()
            {
                throw new NoSuchElementException();
            }

            @Override
            public int next()
            {
                throw new NoSuchElementException();
            }

            @Override
            public boolean hasPrevious()
            {
                return false;
            }

            @Override
            public boolean hasNext()
            {
                return false;
            }

            @Override
            public int nextIndex()
            {
                return 0;
            }

            @Override
            public int previousIndex()
            {
                return -1;
            }
        };

    private final ObjectTrackingType objectTrackingType;

    private final MDIntArray linking;

    private MDIntArray reverseSortedLinking;

    /**
     * A list of integer indices.
     * <p>
     * Based on Josh Bloch's and Neal Gafter's {@link java.util.List}.
     * 
     * @author Bernd Rinn
     */
    public interface IIndexList
    {
        /**
         * Returns the number of elements in this list. If this list contains more than
         * <tt>Integer.MAX_VALUE</tt> elements, returns <tt>Integer.MAX_VALUE</tt>.
         * 
         * @return the number of elements in this list.
         */
        int size();

        /**
         * Returns the element at the specified position in this list.
         * 
         * @param index index of element to return.
         * @return the element at the specified position in this list.
         * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index
         *             &gt;= size()).
         */
        int get(int index);

        /**
         * Returns <tt>true</tt> if this list contains no elements.
         * 
         * @return <tt>true</tt> if this list contains no elements.
         */
        boolean isEmpty();

        /**
         * Returns <tt>true</tt> if this list contains the specified element. More formally, returns
         * <tt>true</tt> if and only if this list contains at least one element <tt>e</tt> such that
         * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
         * 
         * @param o element whose presence in this list is to be tested.
         * @return <tt>true</tt> if this list contains the specified element.
         * @throws ClassCastException if the type of the specified element is incompatible with this
         *             list (optional).
         * @throws NullPointerException if the specified element is null and this list does not
         *             support null elements (optional).
         */
        boolean contains(int o);

        /**
         * Returns an iterator over the elements in this list in proper sequence.
         * 
         * @return an iterator over the elements in this list in proper sequence.
         */
        IIndexIterator iterator();

        /**
         * Returns an array containing all of the elements in this list in proper sequence. Obeys
         * the general contract of the <tt>Collection.toArray</tt> method.
         * 
         * @return an array containing all of the elements in this list in proper sequence.
         * @see Arrays#asList(Object[])
         */
        int[] toArray();
    }

    /**
     * An iterator for integer indices.
     * <p>
     * Based on Josh Bloch's {@link java.util.ListIterator}.
     * 
     * @author Bernd Rinn
     */
    public interface IIndexIterator
    {
        /**
         * Returns <tt>true</tt> if this list iterator has more elements when traversing the list in
         * the forward direction. (In other words, returns <tt>true</tt> if <tt>next</tt> would
         * return an element rather than throwing an exception.)
         * 
         * @return <tt>true</tt> if the list iterator has more elements when traversing the list in
         *         the forward direction.
         */
        boolean hasNext();

        /**
         * Returns the next element in the list. This method may be called repeatedly to iterate
         * through the list, or intermixed with calls to <tt>previous</tt> to go back and forth.
         * (Note that alternating calls to <tt>next</tt> and <tt>previous</tt> will return the same
         * element repeatedly.)
         * 
         * @return the next element in the list.
         * @exception NoSuchElementException if the iteration has no next element.
         */
        int next();

        /**
         * Returns <tt>true</tt> if this list iterator has more elements when traversing the list in
         * the reverse direction. (In other words, returns <tt>true</tt> if <tt>previous</tt> would
         * return an element rather than throwing an exception.)
         * 
         * @return <tt>true</tt> if the list iterator has more elements when traversing the list in
         *         the reverse direction.
         */
        boolean hasPrevious();

        /**
         * Returns the previous element in the list. This method may be called repeatedly to iterate
         * through the list backwards, or intermixed with calls to <tt>next</tt> to go back and
         * forth. (Note that alternating calls to <tt>next</tt> and <tt>previous</tt> will return
         * the same element repeatedly.)
         * 
         * @return the previous element in the list.
         * @exception NoSuchElementException if the iteration has no previous element.
         */
        int previous();

        /**
         * Returns the index of the element that would be returned by a subsequent call to
         * <tt>next</tt>. (Returns list size if the list iterator is at the end of the list.)
         * 
         * @return the index of the element that would be returned by a subsequent call to
         *         <tt>next</tt>, or list size if list iterator is at end of list.
         */
        int nextIndex();

        /**
         * Returns the index of the element that would be returned by a subsequent call to
         * <tt>previous</tt>. (Returns -1 if the list iterator is at the beginning of the list.)
         * 
         * @return the index of the element that would be returned by a subsequent call to
         *         <tt>previous</tt>, or -1 if list iterator is at beginning of list.
         */
        int previousIndex();
    }

    /**
     * An implemention of {@link IIndexList}.
     */
    static class IndexList implements IIndexList
    {
        private final MDIntArray linking;

        private final int startRow;

        private final int endRow;

        private final int size;

        private final int column;

        IndexList(MDIntArray linking, int startRow, int endRow, int column)
        {
            this.linking = linking;
            this.startRow = startRow;
            this.endRow = endRow;
            this.size = endRow - startRow;
            this.column = column;
        }

        @Override
        public int[] toArray()
        {
            final int[] childIds = new int[size];
            for (int i = 0; i < childIds.length; ++i)
            {
                childIds[i] = linking.get(startRow + i, column);
            }
            return childIds;
        }

        @Override
        public int size()
        {
            return size;
        }

        @Override
        public IIndexIterator iterator()
        {
            return new IIndexIterator()
                {
                    int row = startRow;

                    @Override
                    public int previous()
                    {
                        if (hasPrevious())
                        {
                            return linking.get(--row, column);
                        } else
                        {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override
                    public int next()
                    {
                        if (hasNext())
                        {
                            return linking.get(row++, column);
                        } else
                        {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override
                    public boolean hasPrevious()
                    {
                        return row > startRow;
                    }

                    @Override
                    public boolean hasNext()
                    {
                        return row < endRow;
                    }

                    @Override
                    public int nextIndex()
                    {
                        return row;
                    }

                    @Override
                    public int previousIndex()
                    {
                        return row - 1;
                    }
                };
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }

        @Override
        public int get(int index)
        {
            if (index >= 0 && index < size)
            {
                return linking.get(startRow + index, column);
            } else
            {
                throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public boolean contains(int o)
        {
            return MDIntArray2DRowSort.binaryRowSearch(linking, startRow, endRow, column, o) >= 0;
        }

        @Override
        public String toString()
        {
            StringBuilder buf = new StringBuilder();
            buf.append("IndexList [");
            IIndexIterator i = iterator();
            boolean hasNext = i.hasNext();
            while (hasNext)
            {
                int o = i.next();
                buf.append(Integer.toString(o));
                hasNext = i.hasNext();
                if (hasNext)
                    buf.append(", ");
            }

            buf.append("]");
            return buf.toString();
        }
    }

    ObjectTracking(ObjectTrackingType objectTrackingType, MDIntArray linking)
    {
        this.objectTrackingType = objectTrackingType;
        if (linking.size(1) != 2)
        {
            throw new RuntimeException("linking needs to have exactly two columns, "
                    + linking.size(1) + " found.");
        }
        this.linking = linking;
    }

    /**
     * Returns the object tracking type of this tracking.
     */
    public ObjectTrackingType getObjectTrackingType()
    {
        return objectTrackingType;
    }

    /**
     * Returns the list of child ids for the given <var>parentId</var>.
     * 
     * @param parentId The id to get the child id links for.
     * @return The list of links.
     */
    public IIndexList getChildIds(int parentId)
    {
        final int startRow = MDIntArray2DRowSort.binaryRowSearch(linking, PARENT_COL, parentId);
        if (startRow < 0)
        {
            return EMPTY_INDEX_LIST;
        } else
        {
            final int endRow = MDIntArray2DRowSort.findLastRow(linking, PARENT_COL, startRow);
            return new IndexList(linking, startRow, endRow, CHILD_COL);
        }
    }

    /**
     * Returns the list of parent ids for the given <var>childId</var>.
     * 
     * @param childId The id to get the parent id links for.
     * @return The list of links.
     */
    public IIndexList getParentIds(int childId)
    {
        if (reverseSortedLinking == null)
        {
            this.reverseSortedLinking =
                    new MDIntArray(linking.getCopyAsFlatArray(), linking.dimensions());
            MDIntArray2DRowSort.twoStepRowSort(reverseSortedLinking, CHILD_COL, PARENT_COL);
        }
        final int startRow =
                MDIntArray2DRowSort.binaryRowSearch(reverseSortedLinking, CHILD_COL, childId);
        if (startRow < 0)
        {
            return EMPTY_INDEX_LIST;
        } else
        {
            final int endRow =
                    MDIntArray2DRowSort.findLastRow(reverseSortedLinking, CHILD_COL, startRow);
            return new IndexList(reverseSortedLinking, startRow, endRow, PARENT_COL);
        }
    }

    @Override
    public String toString()
    {
        return "ObjectTracking [linking=" + linking + "]";
    }

}
