package ch.systemsx.cisd.openbis.clc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import org.apache.lucene.document.Document;

import com.clcbio.api.base.attribute_custom.CustomAttribute;
import com.clcbio.api.base.persistence.PersistenceException;
import com.clcbio.api.base.persistence.PersistenceExceptionUnchecked;
import com.clcbio.api.base.persistence.RebuildIndexProcess;
import com.clcbio.api.base.persistence.accesscontrol.Authenticator;
import com.clcbio.api.base.persistence.accesscontrol.PersistencePermissions;
import com.clcbio.api.base.persistence.model.IndexStatus;
import com.clcbio.api.base.persistence.model.IteratorResultHandler;
import com.clcbio.api.base.persistence.model.PersistenceContainer;
import com.clcbio.api.base.persistence.model.PersistenceContainerResultHandler;
import com.clcbio.api.base.persistence.model.PersistenceElement;
import com.clcbio.api.base.persistence.model.PersistenceListener;
import com.clcbio.api.base.persistence.model.PersistenceModel;
import com.clcbio.api.base.persistence.model.PersistenceStructure;
import com.clcbio.api.base.persistence.model.PersistenceStructureResultHandler;
import com.clcbio.api.base.persistence.model.PersistenceStructureScore;
import com.clcbio.api.base.persistence.model.PersistenceTransaction;
import com.clcbio.api.base.persistence.model.RecycleBin;
import com.clcbio.api.base.persistence.model.VoidResultHandler;
import com.clcbio.api.base.process.Activity;
import com.clcbio.api.free.datatypes.ClcObject;
import com.clcbio.api.free.datatypes.lazyreference.ClcObjectHolder;
import com.clcbio.api.free.datatypes.lazyreference.LazyReference;
import com.clcbio.api.free.datatypes.project.FolderObject;
import com.clcbio.api.free.datatypes.project.LocationObject;
import com.clcbio.api.free.framework.searching.SearchExpression;

/**
 * Dummy openBIS persistence model.
 * 
 * @author anttil
 */
public class OpenBISPersistenceModel implements PersistenceModel
{
    @Override
    public int allocateObjectId(PersistenceElement arg0, Object arg1)
            throws IOException
    {
        System.err.println("1");
        return 0;
    }

    public static OpenBISPersistenceModel create()
    {
        System.err.println("1.5");
        return new OpenBISPersistenceModel();
    }

    @Override
    public int count()
    {
        System.err.println("2");
        return 0;
    }

    @Override
    public PersistenceContainer createContainerBefore(String arg0,
            PersistenceTransaction arg1, PersistenceStructure arg2)
            throws PersistenceException
    {
        System.err.println("3");
        return null;
    }

    @Override
    public void createContainerBefore(
            PersistenceContainerResultHandler arg0, String arg1,
            PersistenceTransaction arg2, PersistenceStructure arg3)
    {
        System.err.println("4");
    }

    @Override
    public File export(PersistenceStructure arg0, File arg1,
            PersistenceTransaction arg2) throws PersistenceException
    {
        System.err.println("5");
        return null;
    }

    @Override
    public void export(VoidResultHandler arg0, PersistenceElement arg1,
            File arg2, PersistenceTransaction arg3)
    {
        System.err.println("6");

    }

    @Override
    public Object fetchLazyObject(LazyReference<?> arg0)
            throws PersistenceExceptionUnchecked
    {
        System.err.println("7");
        return new Object();
    }

    @Override
    public Icon getOpenIcon()
    {
        System.err.println("8");
        return null;
    }

    @Override
    public PersistenceStructure insertBefore(PersistenceStructure arg0,
            PersistenceTransaction arg1, PersistenceStructure arg2)
            throws PersistenceException
    {
        System.err.println("9");
        return null;
    }

    @Override
    public void insertBefore(PersistenceStructureResultHandler arg0,
            PersistenceStructure arg1, PersistenceTransaction arg2,
            PersistenceStructure arg3)
    {
        System.err.println("10");

    }

    @Override
    public PersistenceStructure insertBefore(PersistenceStructure arg0,
            PersistenceTransaction arg1, PersistenceStructure arg2,
            Activity arg3) throws PersistenceException
    {
        System.err.println("11");
        return null;
    }

    @Override
    public void insertBefore(PersistenceStructureResultHandler arg0,
            PersistenceStructure arg1, PersistenceTransaction arg2,
            PersistenceStructure arg3, Activity arg4)
    {
        System.err.println("12");

    }

    @Override
    public PersistenceStructure insertFileBefore(File arg0,
            PersistenceTransaction arg1, PersistenceStructure arg2)
            throws PersistenceException
    {
        System.err.println("13");
        return null;
    }

    @Override
    public void insertFileBefore(
            PersistenceStructureResultHandler arg0, File arg1,
            PersistenceTransaction arg2, PersistenceStructure arg3)
    {
        System.err.println("14");

    }

    @Override
    public Iterator<PersistenceStructure> list()
            throws PersistenceException
    {
        System.err.println("15");
        return Collections.<PersistenceStructure> emptyList().iterator();
    }

    @Override
    public void list(IteratorResultHandler arg0)
    {
        System.err.println("16");

    }

    @Override
    public void moveBefore(PersistenceStructure arg0,
            PersistenceTransaction arg1, PersistenceStructure arg2)
            throws PersistenceException
    {
        System.err.println("17");

    }

    @Override
    public void moveBefore(VoidResultHandler arg0,
            PersistenceStructure arg1, PersistenceTransaction arg2,
            PersistenceStructure arg3)
    {
        System.err.println("18");

    }

    @Override
    public int rebuildIndex(Activity arg0, RebuildIndexProcess arg1)
            throws PersistenceException, InterruptedException
    {
        System.err.println("19");
        return 0;
    }

    @Override
    public void refresh(VoidResultHandler arg0)
    {
        System.err.println("20");

    }

    @Override
    public void remove(PersistenceStructure arg0,
            PersistenceTransaction arg1) throws PersistenceException
    {
        System.err.println("21");

    }

    @Override
    public void remove(VoidResultHandler arg0,
            PersistenceStructure arg1, PersistenceTransaction arg2)
    {
        System.err.println("22");

    }

    @Override
    public void renameChild(PersistenceStructure arg0, String arg1)
    {
        System.err.println("23");

    }

    @Override
    public Iterator<PersistenceStructure> search(SearchExpression arg0,
            int arg1, int arg2) throws PersistenceException
    {
        System.err.println("24");
        return new ArrayList<PersistenceStructure>().iterator();
    }

    @Override
    public Iterator<PersistenceStructure> search(String arg0, int arg1,
            int arg2) throws PersistenceException
    {
        System.err.println("25");
        return new ArrayList<PersistenceStructure>().iterator();
    }

    @Override
    public void search(IteratorResultHandler arg0,
            SearchExpression arg1, int arg2, int arg3)
    {
        System.err.println("26");

    }

    @Override
    public void search(IteratorResultHandler arg0, String arg1,
            int arg2, int arg3)
    {
        System.err.println("27");

    }

    @Override
    public void sort(PersistenceTransaction arg0)
            throws PersistenceException
    {
        System.err.println("28");

    }

    @Override
    public void sort(VoidResultHandler arg0, PersistenceTransaction arg1)
    {
        System.err.println("29");

    }

    @Override
    public void update(PersistenceElement arg0,
            PersistenceTransaction arg1) throws PersistenceException
    {
        System.err.println("30");

    }

    @Override
    public void update(VoidResultHandler arg0, PersistenceElement arg1,
            PersistenceTransaction arg2)
    {
        System.err.println("31");

    }

    @Override
    public void update(PersistenceElement arg0,
            PersistenceTransaction arg1, Activity arg2)
            throws PersistenceException
    {
        System.err.println("32");

    }

    @Override
    public void update(VoidResultHandler arg0, PersistenceElement arg1,
            PersistenceTransaction arg2, Activity arg3)
    {
        System.err.println("33");

    }

    @Override
    public void addListener(PersistenceListener arg0)
    {
        System.err.println("34");

    }

    @Override
    public Icon getIcon()
    {
        System.err.println("35");
        return new Icon()
            {

                @Override
                public int getIconHeight()
                {
                    return 10;
                }

                @Override
                public int getIconWidth()
                {
                    return 10;
                }

                @Override
                public void paintIcon(Component arg0, Graphics arg1,
                        int arg2, int arg3)
                {
                    arg0.setBackground(Color.BLACK);
                }

            };
    }

    @Override
    public ClcObject getObject()
    {
        System.err.println("36");
        return new FolderObject();
    }

    @Override
    public PersistenceContainer getParent()
    {
        System.err.println("37");
        return this.parent;
    }

    @Override
    public PersistenceModel getPersistenceModel()
    {
        System.err.println("38");
        return this;
    }

    @Override
    public Class<?> getType()
    {
        System.err.println("39");
        return LocationObject.class;
    }

    @Override
    public int getUsage()
    {
        System.err.println("40");
        return 0;
    }

    @Override
    public void removeListener(PersistenceListener arg0)
    {
        System.err.println("41");

    }

    private PersistenceContainer parent;

    @Override
    public void setParent(PersistenceContainer arg0)
    {
        this.parent = arg0;
        System.err.println("42");
    }

    @Override
    public String getId()
    {
        System.err.println("43");
        return "openBIS";
    }

    @Override
    public String getName()
    {
        System.err.println("44");
        return "openBIS";
    }

    @Override
    public void addLazyObject(byte[] arg0, String arg1, int arg2)
            throws PersistenceException
    {
        System.err.println("45");

    }

    @Override
    public void commitInsert(String arg0, Document arg1, int arg2)
            throws PersistenceException
    {
        System.err.println("46");

    }

    @Override
    public void commitUpdate(String arg0, Document arg1, int arg2)
            throws PersistenceException
    {
        System.err.println("47");

    }

    @Override
    public byte[] fetchLazyByteArray(String arg0, int arg1)
            throws PersistenceExceptionUnchecked
    {
        System.err.println("48");
        return null;
    }

    @Override
    public boolean startInsertBefore(ClcObjectHolder arg0,
            PersistenceStructure arg1) throws PersistenceException
    {
        System.err.println("49");
        return false;
    }

    @Override
    public void startUpdate(ClcObjectHolder arg0)
            throws PersistenceException
    {
        System.err.println("50");

    }

    @Override
    public void checkReadPermission() throws PersistenceException
    {
        System.err.println("51");

    }

    @Override
    public void checkWritePermission() throws PersistenceException
    {
        System.err.println("52");

    }

    @Override
    public Authenticator getAuthenticator()
    {
        System.err.println("53X");
        return null;
    }

    @Override
    public List<PersistencePermissions> getPermissions()
            throws PersistenceException
    {
        System.err.println("54");
        return new ArrayList<PersistencePermissions>();
    }

    @Override
    public boolean permissionsSupport()
    {
        System.err.println("55");
        return false;
    }

    @Override
    public void setPermission(String arg0, boolean arg1, boolean arg2,
            boolean arg3) throws PersistenceException
    {
        System.err.println("56");

    }

    @Override
    public void addListener(PersistenceStructure arg0,
            PersistenceListener arg1)
    {
        System.err.println("57");

    }

    @Override
    public PersistenceTransaction beginTransaction()
    {
        System.err.println("58");
        return null;
    }

    @Override
    public boolean exists(String arg0)
    {
        System.err.println("59");
        return false;
    }

    @Override
    public PersistenceStructure fetch(String arg0)
            throws PersistenceException
    {
        System.err.println("60");
        return null;
    }

    @Override
    public CustomAttribute[] getAllAttributes()
            throws PersistenceException
    {
        System.err.println("61");
        return null;
    }

    @Override
    public Object[] getArgumentsToStaticCreate()
    {
        System.err.println("62");
        return new Object[0];
    }

    @Override
    public CustomAttribute getAttribute(String arg0)
            throws PersistenceException
    {
        System.err.println("63");
        return null;
    }

    @Override
    public IndexStatus getIndexStatus()
    {
        System.err.println("64");
        return null;
    }

    @Override
    public List<PersistenceListener> getListeners(
            PersistenceStructure arg0)
    {
        System.err.println("65");
        return new ArrayList<PersistenceListener>();
    }

    @Override
    public String getLongDescription()
    {
        System.err.println("66");
        return null;
    }

    @Override
    public RecycleBin getRecycleBin()
    {
        System.err.println("67");
        return null;
    }

    @Override
    public void insertAttribute(CustomAttribute arg0)
            throws PersistenceException
    {
        System.err.println("68");

    }

    @Override
    public boolean isActive()
    {
        System.err.println("69");
        return true;
    }

    @Override
    public void removeAttribute(CustomAttribute arg0)
            throws PersistenceException
    {
        System.err.println("70");

    }

    @Override
    public void removeListener(PersistenceStructure arg0,
            PersistenceListener arg1)
    {
        System.err.println("71");

    }

    @Override
    public Iterator<PersistenceStructureScore> searchWithScore(
            String arg0, int arg1, int arg2)
            throws PersistenceException
    {
        System.err.println("72");
        return new ArrayList<PersistenceStructureScore>().iterator();
    }

    @Override
    public void updateActive()
    {
        System.err.println("73");
    }

    @Override
    public int hashCode()
    {
        System.err.println("74");
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        System.err.println("74");
        if (o == this)
        {
            System.err.println("74: this");
            return true;
        }
        if (o == null || !(o instanceof PersistenceModel))
        {
            System.err.println("74: fail");
            return false;
        }
        System.err.println("74: compare");
        PersistenceModel other = (PersistenceModel) o;
        return getId().equals(other.getId());
    }

}
