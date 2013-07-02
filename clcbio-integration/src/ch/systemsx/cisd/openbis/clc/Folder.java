/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.clc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import org.apache.lucene.document.Document;

import com.clcbio.api.base.persistence.PersistenceException;
import com.clcbio.api.base.persistence.PersistenceExceptionUnchecked;
import com.clcbio.api.base.persistence.RebuildIndexProcess;
import com.clcbio.api.base.persistence.accesscontrol.Authenticator;
import com.clcbio.api.base.persistence.accesscontrol.PersistencePermissions;
import com.clcbio.api.base.persistence.model.IteratorResultHandler;
import com.clcbio.api.base.persistence.model.PersistenceContainer;
import com.clcbio.api.base.persistence.model.PersistenceContainerResultHandler;
import com.clcbio.api.base.persistence.model.PersistenceElement;
import com.clcbio.api.base.persistence.model.PersistenceModel;
import com.clcbio.api.base.persistence.model.PersistenceStructure;
import com.clcbio.api.base.persistence.model.PersistenceStructureResultHandler;
import com.clcbio.api.base.persistence.model.PersistenceTransaction;
import com.clcbio.api.base.persistence.model.VoidResultHandler;
import com.clcbio.api.base.process.Activity;
import com.clcbio.api.free.datatypes.ClcObject;
import com.clcbio.api.free.datatypes.lazyreference.ClcObjectHolder;
import com.clcbio.api.free.datatypes.lazyreference.LazyReference;
import com.clcbio.api.free.datatypes.project.FileObject;
import com.clcbio.api.free.datatypes.project.FolderObject;
import com.clcbio.api.free.framework.searching.SearchExpression;

/**
 * @author anttil
 */
public class Folder extends BasePersistenceStructure implements PersistenceContainer
{

    private ContentProvider content;

    public Folder(String name, PersistenceContainer parent, PersistenceModel model, ContentProvider content)
    {
        super(name, parent, model);
        this.content = content;
    }

    @Override
    public void addLazyObject(byte[] arg0, String arg1, int arg2) throws PersistenceException
    {
        System.err.println(id + ": addLazyObject(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
    }

    @Override
    public void commitInsert(String arg0, Document arg1, int arg2) throws PersistenceException
    {
        System.err.println(id + ": commitInsert(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
    }

    @Override
    public void commitUpdate(String arg0, Document arg1, int arg2) throws PersistenceException
    {
        System.err.println(id + ": commitUpdate(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
    }

    @Override
    public byte[] fetchLazyByteArray(String arg0, int arg1) throws PersistenceExceptionUnchecked
    {
        System.err.println(id + ": fetchLazyByteArray(" + arg0 + ", " + arg1 + ")");
        return new byte[0];
    }

    @Override
    public boolean startInsertBefore(ClcObjectHolder arg0, PersistenceStructure arg1) throws PersistenceException
    {
        System.err.println(id + ": startInsertBefore(" + arg0 + ", " + arg1 + ")");
        return false;
    }

    @Override
    public void startUpdate(ClcObjectHolder arg0) throws PersistenceException
    {
        System.err.println(id + ": startUpdate(" + arg0 + ")");
    }

    @Override
    public void checkReadPermission() throws PersistenceException
    {
        System.err.println(id + ": checkReadPermission()");
    }

    @Override
    public void checkWritePermission() throws PersistenceException
    {
        System.err.println(id + ": checkWritePermission()");
    }

    @Override
    public Authenticator getAuthenticator()
    {
        System.err.println(id + ": getAuthenticator()");
        return new OpenBISAuthenticator(id);
    }

    @Override
    public List<PersistencePermissions> getPermissions() throws PersistenceException
    {
        System.err.println(id + ": getPermissions()");
        return new ArrayList<PersistencePermissions>();
    }

    @Override
    public boolean permissionsSupport()
    {
        System.err.println(id + ": permissionsSupport()");
        return true;
    }

    @Override
    public void setPermission(String arg0, boolean arg1, boolean arg2, boolean arg3) throws PersistenceException
    {
        System.err.println(id + ": setPermission(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ")");
    }

    @Override
    public int allocateObjectId(PersistenceElement arg0, Object arg1) throws IOException
    {
        System.err.println(id + ": allocateObjectId(" + arg0 + ", " + arg1 + ")");
        return 0;
    }

    @Override
    public int count()
    {
        System.err.println(id + ": count()");
        return 0;
    }

    @Override
    public PersistenceContainer createContainerBefore(String arg0, PersistenceTransaction arg1, PersistenceStructure arg2)
            throws PersistenceException
    {
        System.err.println(id + ": createContainerBefore(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
        throw new PersistenceException("creation of containers not implemented");
    }

    @Override
    public void createContainerBefore(PersistenceContainerResultHandler arg0, String arg1, PersistenceTransaction arg2, PersistenceStructure arg3)
    {
        System.err.println(id + ": createContainerBefore(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ")");
    }

    @Override
    public File export(PersistenceStructure arg0, File arg1, PersistenceTransaction arg2) throws PersistenceException
    {
        System.err.println(id + ": export(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
        return ((FileObject) arg0).getFile();
    }

    @Override
    public void export(VoidResultHandler arg0, PersistenceElement arg1, File arg2, PersistenceTransaction arg3)
    {
        System.err.println(id + ": export(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ")");
    }

    @Override
    public Object fetchLazyObject(LazyReference<?> arg0) throws PersistenceExceptionUnchecked
    {
        System.err.println(id + ": fetchLazyObject(" + arg0 + ")");
        throw new PersistenceExceptionUnchecked("fetchLazyObject not implemented");
    }

    @Override
    public Icon getIcon()
    {
        System.err.println(id + ": getOpenIcon()");
        return new FolderObject().getIcon();
    }

    @Override
    public Icon getOpenIcon()
    {
        System.err.println(id + ": getOpenIcon()");
        return new FolderObject().getIcon();
    }

    @Override
    public PersistenceStructure insertBefore(PersistenceStructure arg0, PersistenceTransaction arg1, PersistenceStructure arg2)
            throws PersistenceException
    {
        System.err.println(id + ": insertBefore(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
        throw new PersistenceException("insertBefore not implemented");
    }

    @Override
    public void insertBefore(PersistenceStructureResultHandler arg0, PersistenceStructure arg1, PersistenceTransaction arg2, PersistenceStructure arg3)
    {
        System.err.println(id + ": insertBefore(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ")");
    }

    @Override
    public PersistenceStructure insertBefore(PersistenceStructure arg0, PersistenceTransaction arg1, PersistenceStructure arg2, Activity arg3)
            throws PersistenceException
    {
        System.err.println(id + ": insertBefore(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ")");
        throw new PersistenceException("insertBefore not implemented");
    }

    @Override
    public void insertBefore(PersistenceStructureResultHandler arg0, PersistenceStructure arg1, PersistenceTransaction arg2,
            PersistenceStructure arg3, Activity arg4)
    {
        System.err.println(id + ": insertBefore(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ", " + arg4 + ")");
    }

    @Override
    public PersistenceStructure insertFileBefore(File arg0, PersistenceTransaction arg1, PersistenceStructure arg2) throws PersistenceException
    {
        System.err.println(id + ": insertFileBefore(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
        throw new PersistenceException("insertFileBefore not implemented");
    }

    @Override
    public void insertFileBefore(PersistenceStructureResultHandler arg0, File arg1, PersistenceTransaction arg2, PersistenceStructure arg3)
    {
        System.err.println(id + ": insertFileBefore(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ")");
    }

    @Override
    public Iterator<PersistenceStructure> list() throws PersistenceException
    {
        System.err.println(id + ": list()");
        return content.getContent(this, model).iterator();
    }

    @Override
    public void list(IteratorResultHandler arg0)
    {
        System.err.println(id + ": list(" + arg0 + ")");
    }

    @Override
    public void moveBefore(PersistenceStructure arg0, PersistenceTransaction arg1, PersistenceStructure arg2) throws PersistenceException
    {
        System.err.println(id + ": moveBefore(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
    }

    @Override
    public void moveBefore(VoidResultHandler arg0, PersistenceStructure arg1, PersistenceTransaction arg2, PersistenceStructure arg3)
    {
        System.err.println(id + ": moveBefore(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ")");
    }

    @Override
    public int rebuildIndex(Activity arg0, RebuildIndexProcess arg1) throws PersistenceException, InterruptedException
    {
        System.err.println(id + ": rebuildIndex(" + arg0 + ", " + arg1 + ")");
        return 0;
    }

    @Override
    public void refresh(VoidResultHandler arg0)
    {
        System.err.println(id + ": refresh(" + arg0 + ")");
    }

    @Override
    public void remove(PersistenceStructure arg0, PersistenceTransaction arg1) throws PersistenceException
    {
        System.err.println(id + ": remove(" + arg0 + ", " + arg1 + ")");
    }

    @Override
    public void remove(VoidResultHandler arg0, PersistenceStructure arg1, PersistenceTransaction arg2)
    {
        System.err.println(id + ": remove(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
    }

    @Override
    public void renameChild(PersistenceStructure arg0, String arg1)
    {
        System.err.println(id + ": renameChild(" + arg0 + ", " + arg1 + ")");
    }

    @Override
    public Iterator<PersistenceStructure> search(SearchExpression arg0, int arg1, int arg2) throws PersistenceException
    {
        System.err.println(id + ": search(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
        return new ArrayList<PersistenceStructure>().iterator();
    }

    @Override
    public Iterator<PersistenceStructure> search(String arg0, int arg1, int arg2) throws PersistenceException
    {
        System.err.println(id + ": search(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
        return new ArrayList<PersistenceStructure>().iterator();
    }

    @Override
    public void search(IteratorResultHandler arg0, SearchExpression arg1, int arg2, int arg3)
    {
        System.err.println(id + ": search(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ")");
    }

    @Override
    public void search(IteratorResultHandler arg0, String arg1, int arg2, int arg3)
    {
        System.err.println(id + ": search(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ")");
    }

    @Override
    public void sort(PersistenceTransaction arg0) throws PersistenceException
    {
        System.err.println(id + ": sort(" + arg0 + ")");
    }

    @Override
    public void sort(VoidResultHandler arg0, PersistenceTransaction arg1)
    {
        System.err.println(id + ": sort(" + arg0 + ", " + arg1 + ")");
    }

    @Override
    public void update(PersistenceElement arg0, PersistenceTransaction arg1) throws PersistenceException
    {
        System.err.println(id + ": update(" + arg0 + ", " + arg1 + ")");
    }

    @Override
    public void update(VoidResultHandler arg0, PersistenceElement arg1, PersistenceTransaction arg2)
    {
        System.err.println(id + ": update(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
    }

    @Override
    public void update(PersistenceElement arg0, PersistenceTransaction arg1, Activity arg2) throws PersistenceException
    {
        System.err.println(id + ": update(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
    }

    @Override
    public void update(VoidResultHandler arg0, PersistenceElement arg1, PersistenceTransaction arg2, Activity arg3)
    {
        System.err.println(id + ": update(" + arg0 + ", " + arg1 + ", " + arg2 + ", " + arg3 + ")");
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
        System.err.println(id + ": Folder.getType()");
        return FolderObject.class;
    }

}
