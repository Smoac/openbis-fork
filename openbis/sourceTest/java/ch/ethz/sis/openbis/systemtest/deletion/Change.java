package ch.ethz.sis.openbis.systemtest.deletion;

import java.util.Date;

class Change implements Comparable<Change>
{
    public final Date time;

    public final String value;

    public final boolean isRemoval;

    public Change(Date time, String value, boolean isRemoval)
    {
        this.time = time;
        this.value = value;
        this.isRemoval = isRemoval;
    }

    @Override
    public int compareTo(Change other)
    {
        int cmp = this.time.compareTo(other.time);
        if (cmp == 0)
        {
            if (this.isRemoval == other.isRemoval)
            {
                return 0;
            } else if (this.isRemoval)
            {
                return -1;
            } else
            {
                return 1;
            }
        } else
        {
            return cmp;
        }
    }

    @Override
    public String toString()
    {
        return value + " (" + time + ", removal " + isRemoval + ")";
    }
}