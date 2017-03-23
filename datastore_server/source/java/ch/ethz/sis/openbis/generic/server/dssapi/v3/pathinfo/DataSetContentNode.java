package ch.ethz.sis.openbis.generic.server.dssapi.v3.pathinfo;

interface DataSetContentNode
{
    long getLength();

    Integer getChecksum();

    String getFullPath();

    boolean isDirectory();
}