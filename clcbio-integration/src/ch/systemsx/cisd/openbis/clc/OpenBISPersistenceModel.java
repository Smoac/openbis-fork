package ch.systemsx.cisd.openbis.clc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.clcbio.api.base.attribute_custom.CustomAttribute;
import com.clcbio.api.base.persistence.PersistenceException;
import com.clcbio.api.base.persistence.model.IndexStatus;
import com.clcbio.api.base.persistence.model.PersistenceListener;
import com.clcbio.api.base.persistence.model.PersistenceModel;
import com.clcbio.api.base.persistence.model.PersistenceStructure;
import com.clcbio.api.base.persistence.model.PersistenceStructureScore;
import com.clcbio.api.base.persistence.model.PersistenceTransaction;
import com.clcbio.api.base.persistence.model.RecycleBin;
import com.clcbio.api.free.datatypes.ClcObject;
import com.clcbio.api.free.datatypes.project.FolderObject;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.OpenbisServiceFacade;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;

/**
 * Dummy openBIS persistence model.
 * 
 * @author anttil
 */
public class OpenBISPersistenceModel extends BasePersistenceContainer implements PersistenceModel
{

    private IOpenbisServiceFacade openbis;

    public OpenBISPersistenceModel(String name)
    {
        super(name, null, null);
        this.id = UUID.randomUUID().toString();
        this.openbis = OpenbisServiceFacade.tryCreate("selenium", "password", "http://localhost:10000", 1000000L);
    }

    public static OpenBISPersistenceModel create(String name)
    {
        System.err.println("OpenBISPersistenceModel: static create with name " + name);
        return new OpenBISPersistenceModel(name);
    }

    @Override
    public PersistenceModel getPersistenceModel()
    {
        return this;
    }

    @Override
    public Iterator<PersistenceStructure> list() throws PersistenceException
    {
        System.err.println(id + ": list()");
        return getSpacesFromOpenBIS().iterator();
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (o == null || !(o instanceof PersistenceModel))
        {
            return false;
        }
        PersistenceModel other = (PersistenceModel) o;
        return getId().equals(other.getId());
    }

    @Override
    public void addListener(PersistenceStructure arg0, PersistenceListener arg1)
    {
        System.err.println(id + ": addListener(" + arg0 + ", " + arg1 + ")");
    }

    @Override
    public PersistenceTransaction beginTransaction()
    {
        System.err.println(id + ": beginTransaction()");
        return null;
    }

    @Override
    public boolean exists(String arg0)
    {
        System.err.println(id + ": exists(" + arg0 + ")");
        return true;
    }

    @Override
    public PersistenceStructure fetch(String childId) throws PersistenceException
    {
        System.err.println(id + ": fetch(" + childId + ")");
        for (PersistenceStructure child : getSpacesFromOpenBIS())
        {
            if (child.getId().equals(childId))
            {
                return child;
            }
        }
        throw new PersistenceException("Could not find " + childId);
    }

    @Override
    public CustomAttribute[] getAllAttributes() throws PersistenceException
    {
        System.err.println(id + ": getAllAttributes()");
        return new CustomAttribute[0];
    }

    @Override
    public Object[] getArgumentsToStaticCreate()
    {
        System.err.println(id + ": getArgumentsToStaticCreate()");
        return new Object[] { "openBIS" };
    }

    @Override
    public CustomAttribute getAttribute(String arg0) throws PersistenceException
    {
        System.err.println(id + ": getAttribute(" + arg0 + ")");
        return null;
    }

    @Override
    public IndexStatus getIndexStatus()
    {
        System.err.println(id + ": getIndexStatus()");
        return IndexStatus.FINISHED;
    }

    @Override
    public List<PersistenceListener> getListeners(PersistenceStructure arg0)
    {
        System.err.println(id + ": getListeners(" + arg0 + ")");
        return new ArrayList<PersistenceListener>();
    }

    @Override
    public String getLongDescription()
    {
        System.err.println(id + ": getLongDescrption()");
        return "long description";
    }

    @Override
    public RecycleBin getRecycleBin()
    {
        System.err.println(id + ": getRecycleBin()");
        return null;
    }

    @Override
    public void insertAttribute(CustomAttribute arg0) throws PersistenceException
    {
        System.err.println(id + ": insertAttribute(" + arg0 + ")");
    }

    @Override
    public boolean isActive()
    {
        System.err.println(id + ": isActive()");
        return true;
    }

    @Override
    public void removeAttribute(CustomAttribute arg0) throws PersistenceException
    {
        System.err.println(id + ": removeAttribute(" + arg0 + ")");
    }

    @Override
    public void removeListener(PersistenceStructure arg0, PersistenceListener arg1)
    {
        System.err.println(id + ": removeListener(" + arg0 + ", " + arg1 + ")");
    }

    @Override
    public Iterator<PersistenceStructureScore> searchWithScore(String arg0, int arg1, int arg2) throws PersistenceException
    {
        System.err.println(id + ": searchWithScore(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
        return null;
    }

    @Override
    public void updateActive()
    {
        System.err.println(id + ": updateActive()");
    }

    @Override
    public ClcObject getObject()
    {
        System.err.println(id + ": getObject()");
        return new FolderObject(this);
    }

    @Override
    public Class<?> getType()
    {
        System.err.println(id + ": getType()");
        return FolderObject.class;
    }

    private List<PersistenceStructure> getSpacesFromOpenBIS()
    {
        List<PersistenceStructure> spaces = new ArrayList<PersistenceStructure>();
        for (SpaceWithProjectsAndRoleAssignments a : openbis.getSpacesWithProjects())
        {
            spaces.add(new Folder(a.getCode(), this, this));
        }
        return spaces;
    }
}
