/*
 * Copyright 2008 ETH Zuerich, CISD
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
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <pwd.h>
#include <grp.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <jni.h>

/* Types of links. Keep in sync with Java enum. */
#define REGULAR_FILE 0
#define DIRECTORY 1
#define SYMLINK 2
#define OTHER 3

#ifndef __STAT
#define __STAT stat
#endif

#ifndef __LSTAT
#define __LSTAT lstat
#endif

/* Global references. */
jclass stringClass;
jclass passwordClass;
jmethodID passwordConstructorID;
jclass groupClass;
jmethodID groupConstructorID;
jclass statClass;
jmethodID statConstructorID;

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_init
  (JNIEnv *env, jclass clss)
{
    stringClass = (*env)->FindClass(env, "java/lang/String");
    if (stringClass == NULL) /* Really shouldn't happen, will throw NoClassDefFoundError. */
    {
        return -1;
    }
    stringClass = (*env)->NewGlobalRef(env, stringClass);
    passwordClass = (*env)->FindClass(env, "ch/systemsx/cisd/base/unix/Unix$Password");
    if (passwordClass == NULL) /* Really shouldn't happen, will throw NoClassDefFoundError. */
    {
        return -1;
    }
    passwordClass = (*env)->NewGlobalRef(env, passwordClass);
    passwordConstructorID = (*env)->GetMethodID(env, passwordClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (passwordConstructorID == NULL) /* Really shouldn't happen, will throw NoSuchMethodError. */
    {
        return -1;
    }
    groupClass = (*env)->FindClass(env, "ch/systemsx/cisd/base/unix/Unix$Group");
    if (groupClass == NULL) /* Really shouldn't happen, will throw NoClassDefFoundError. */
    {
        return -1;
    }
    groupClass = (*env)->NewGlobalRef(env, groupClass);
    groupConstructorID = (*env)->GetMethodID(env, groupClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;I[Ljava/lang/String;)V");
    if (groupConstructorID == NULL) /* Really shouldn't happen, will throw NoSuchMethodError. */
    {
        return -1;
    }
    statClass = (*env)->FindClass(env, "ch/systemsx/cisd/base/unix/Unix$Stat");
    if (statClass == NULL) /* Really shouldn't happen, will throw NoClassDefFoundError. */
    {
        return -1;
    }
    statClass = (*env)->NewGlobalRef(env, statClass);
    statConstructorID = (*env)->GetMethodID(env, statClass, "<init>", "(JJSBIIIJJJJJI)V");
    if (groupConstructorID == NULL) /* Really shouldn't happen, will throw NoSuchMethodError. */
    {
        return -1;
    }
    return 0;
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_link
  (JNIEnv *env, jclass clss, jstring filename, jstring linktarget)
{
    const char* pfilename;
    const char* plinktarget;
    int retval;

    pfilename = (char *)(*env)->GetStringUTFChars(env, filename, NULL);
    plinktarget = (char *)(*env)->GetStringUTFChars(env, linktarget, NULL);

    retval = link(pfilename, plinktarget);
    if (retval < 0)
    {
        retval = -errno;
    }

    (*env)->ReleaseStringUTFChars(env, filename, pfilename);
    (*env)->ReleaseStringUTFChars(env, linktarget, plinktarget);

   return retval;
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_symlink
  (JNIEnv *env, jclass clss, jstring filename, jstring linktarget)
{
    const char* pfilename;
    const char* plinktarget;
    int retval;

    pfilename = (char *)(*env)->GetStringUTFChars(env, filename, NULL);
    plinktarget = (char *)(*env)->GetStringUTFChars(env, linktarget, NULL);

    retval = symlink(pfilename, plinktarget);
    if (retval < 0)    { 
        retval = -errno; 
    }

    (*env)->ReleaseStringUTFChars(env, filename, pfilename);
    (*env)->ReleaseStringUTFChars(env, linktarget, plinktarget);

   return retval;
}

/* Function pointer for stat function calls. */
typedef int (*stat_func_ptr)(const char *path, struct __STAT *buf);

jobject call_stat(JNIEnv *env, jclass clss, jstring filename, stat_func_ptr statf)
{
    const char* pfilename;
    struct __STAT s;
    jobject result;
    int retval;
    jbyte type;

    pfilename = (char *) (*env)->GetStringUTFChars(env, filename, NULL);
    retval = statf(pfilename, &s);
    (*env)->ReleaseStringUTFChars(env, filename, pfilename);
    if (retval < 0)
    {
        return NULL;
    } else
    {
        if (S_ISLNK(s.st_mode))
        {
            type = SYMLINK;
        } else if (S_ISDIR(s.st_mode))
        {
            type = DIRECTORY;
        } else if (S_ISREG(s.st_mode))
        {
            type = REGULAR_FILE;
        } else
        {
            type = OTHER;
        }
        result = (*env)->NewObject(env, statClass, statConstructorID, (jlong) s.st_dev, (jlong) s.st_ino, 
                   (jshort) (s.st_mode & 07777), (jbyte) type, (jint) s.st_nlink, 
                   (jint) s.st_uid, (jint) s.st_gid, (jlong) s.st_atime, (jlong) s.st_mtime, 
                   (jlong) s.st_ctime, (jlong) s.st_size, (jlong) s.st_blocks, (jint) s.st_blksize);
        return result;
    }
}

JNIEXPORT jobject JNICALL Java_ch_systemsx_cisd_base_unix_Unix_stat(JNIEnv *env, jclass clss, jstring filename)
{
    return call_stat(env, clss, filename, &__STAT);
}

JNIEXPORT jobject JNICALL Java_ch_systemsx_cisd_base_unix_Unix_lstat(JNIEnv *env, jclass clss, jstring filename)
{
    return call_stat(env, clss, filename, &__LSTAT);
}

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_base_unix_Unix_readlink(JNIEnv *env, jclass clss, jstring linkname, jint linkvallen)
{
    const char* plinkname;
    char plinkvalue[linkvallen + 1];
    int retval;
	
    plinkname = (char *)(*env)->GetStringUTFChars(env, linkname, NULL);
    retval = readlink(plinkname, plinkvalue, linkvallen);
    (*env)->ReleaseStringUTFChars(env, linkname, plinkname);
    if (retval < 0)
    {
		    return NULL;
    } else
    {
        plinkvalue[linkvallen] = '\0';
        return (*env)->NewStringUTF(env, plinkvalue);
    }
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_chmod(JNIEnv *env, jclass clss, jstring linkname, jshort mode)
{
    const char* plinkname;
    int retval;
	
    plinkname = (char *)(*env)->GetStringUTFChars(env, linkname, NULL);
    retval = chmod(plinkname, mode);
    (*env)->ReleaseStringUTFChars(env, linkname, plinkname);
    if (retval < 0)
    {
		    return -errno;
    } else
    {
        return 0;
    }
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_chown(JNIEnv *env, jclass clss, jstring linkname, jint uid, jint gid)
{
    const char* plinkname;
    int retval;
	
    plinkname = (char *)(*env)->GetStringUTFChars(env, linkname, NULL);
    retval = chown(plinkname, uid, gid);
    (*env)->ReleaseStringUTFChars(env, linkname, plinkname);
    if (retval < 0)
    {
		    return -errno;
    } else
    {
        return 0;
    }
}

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getuser(JNIEnv *env, jclass clss, jint uid)
{
    struct passwd *pw;
	
    pw = getpwuid(uid);
    if (pw == NULL)
    {
		    return NULL;
    } else
    {
        return (*env)->NewStringUTF(env, pw->pw_name);
    }
}

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getgroup(JNIEnv *env, jclass clss, jint gid)
{
    struct group *gp;
	
    gp = getgrgid(gid);
    if (gp == NULL)
    {
		    return NULL;
    } else
    {
        return (*env)->NewStringUTF(env, gp->gr_name);
    }
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getuid__(JNIEnv *env, jclass clss)
{
    return getuid();
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_geteuid(JNIEnv *env, jclass clss)
{
    return geteuid();
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getuid__Ljava_lang_String_2(JNIEnv *env, jclass clss, jstring user)
{
    const char* puser;
    struct passwd *pw;
	
    puser = (char *)(*env)->GetStringUTFChars(env, user, NULL);
    pw = getpwnam(puser);
    (*env)->ReleaseStringUTFChars(env, user, puser);
    if (pw == NULL)
    {
		    return -errno;
    } else
    {
        return pw->pw_uid;
    }
}

JNIEXPORT jobject JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getpwnam(JNIEnv *env, jclass clss, jstring user)
{
    const char* puser;
    struct passwd *pw;
    jstring passwd;
    jstring fullname;
    jstring homedir;
    jstring shell;
    jobject result;
	
    puser = (char *)(*env)->GetStringUTFChars(env, user, NULL);
    pw = getpwnam(puser);
    (*env)->ReleaseStringUTFChars(env, user, puser);
    if (pw == NULL)
    {
		    return NULL;
    } else
    {
        passwd = (*env)->NewStringUTF(env, pw->pw_passwd);
        fullname = (*env)->NewStringUTF(env, pw->pw_gecos);
        homedir = (*env)->NewStringUTF(env, pw->pw_dir);
        shell = (*env)->NewStringUTF(env, pw->pw_shell);
        result = (*env)->NewObject(env, passwordClass, passwordConstructorID, user, passwd, pw->pw_uid, pw->pw_gid, fullname, homedir, shell);
        return result;
    }
}

JNIEXPORT jobject JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getpwuid(JNIEnv *env, jclass clss, jint uid)
{
    struct passwd *pw;
    jstring user;
    jstring passwd;
    jstring fullname;
    jstring homedir;
    jstring shell;
    jobject result;
	
    pw = getpwuid(uid);
    if (pw == NULL)
    {
		    return NULL;
    } else
    {
        user = (*env)->NewStringUTF(env, pw->pw_name);
        passwd = (*env)->NewStringUTF(env, pw->pw_passwd);
        fullname = (*env)->NewStringUTF(env, pw->pw_gecos);
        homedir = (*env)->NewStringUTF(env, pw->pw_dir);
        shell = (*env)->NewStringUTF(env, pw->pw_shell);
        result = (*env)->NewObject(env, passwordClass, passwordConstructorID, user, passwd, pw->pw_uid, pw->pw_gid, fullname, homedir, shell);
        return result;
    }
}

JNIEXPORT jobjectArray JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getgrnam(JNIEnv *env, jclass clss, jstring group)
{
    const char* pgroup;
    struct group *gr;
    jstring grouppwd;
    jobject result;
    jobjectArray members;
    int i;
	
    pgroup = (char *)(*env)->GetStringUTFChars(env, group, NULL);
    gr = getgrnam(pgroup);
    (*env)->ReleaseStringUTFChars(env, group, pgroup);
    if (gr == NULL)
    {
		    return NULL;
    } else
    {
        grouppwd = (*env)->NewStringUTF(env, gr->gr_passwd);
        /* Count group members. */
        for (i = 0; (gr->gr_mem)[i] != NULL; ++i);
        members = (*env)->NewObjectArray(env, i, stringClass, NULL);
        for (i = 0; (gr->gr_mem)[i] != NULL; ++i)
        {
            (*env)->SetObjectArrayElement(env, members, i, (*env)->NewStringUTF(env, (gr->gr_mem)[i]));
        }
        result = (*env)->NewObject(env, groupClass, groupConstructorID, group, grouppwd, gr->gr_gid, members); 
        return result;
    }
}

JNIEXPORT jobjectArray JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getgrgid(JNIEnv *env, jclass clss, jint gid)
{
    struct group *gr;
    jstring group;
    jstring grouppwd;
    jobject result;
    jobjectArray members;
    int i;
	
    gr = getgrgid(gid);
    if (gr == NULL)
    {
		    return NULL;
    } else
    {
        group = (*env)->NewStringUTF(env, gr->gr_name);
        grouppwd = (*env)->NewStringUTF(env, gr->gr_passwd);
        /* Count group members. */
        for (i = 0; (gr->gr_mem)[i] != NULL; ++i);
        members = (*env)->NewObjectArray(env, i, stringClass, NULL);
        for (i = 0; (gr->gr_mem)[i] != NULL; ++i)
        {
            (*env)->SetObjectArrayElement(env, members, i, (*env)->NewStringUTF(env, (gr->gr_mem)[i]));
        }
        result = (*env)->NewObject(env, groupClass, groupConstructorID, group, grouppwd, gr->gr_gid, members); 
        return result;
    }
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getgid__(JNIEnv *env, jclass clss)
{
    return getgid();
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getegid(JNIEnv *env, jclass clss)
{
    return getegid();
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getgid__Ljava_lang_String_2(JNIEnv *env, jclass clss, jstring group)
{
    const char* pgroup;
    struct group *gr;
	
    pgroup = (char *)(*env)->GetStringUTFChars(env, group, NULL);
    gr = getgrnam(pgroup);
    (*env)->ReleaseStringUTFChars(env, group, pgroup);
    if (gr == NULL)
    {
		    return -errno;
    } else
    {
        return gr->gr_gid;
    }
}

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_base_unix_Unix_getpid(JNIEnv *env, jclass clss)
{
    return getpid();
}

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_base_unix_Unix_strerror__I(JNIEnv *env, jclass clss, jint errnum)
{
    return (*env)->NewStringUTF(env, strerror(errnum < 0 ? -errnum : errnum));
}

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_base_unix_Unix_strerror__(JNIEnv *env, jclass clss)
{
    return (*env)->NewStringUTF(env, strerror(errno));
}
