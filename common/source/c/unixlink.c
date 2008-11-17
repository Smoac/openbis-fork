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

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_hardlink
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

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_symlink
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

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_linkinfo(JNIEnv *env, jclass clss, jstring filename, jlongArray result)
{
    const char* pfilename;
	struct stat statbuf;
	jlong resultbuf[8];
    int retval;

    pfilename = (char *)(*env)->GetStringUTFChars(env, filename, NULL);
	retval = lstat(pfilename, &statbuf);
    (*env)->ReleaseStringUTFChars(env, filename, pfilename);
	if (retval < 0)
	{
		return -errno;
	} else
	{
		resultbuf[0] = statbuf.st_ino;
		resultbuf[1] = statbuf.st_nlink;
		if (S_ISLNK(statbuf.st_mode))
		{
        resultbuf[2] = SYMLINK;
		} else if (S_ISDIR(statbuf.st_mode))
		{
		    resultbuf[2] = DIRECTORY;
		} else if (S_ISREG(statbuf.st_mode))
		{
		    resultbuf[2] = REGULAR_FILE;
		} else
		{
		    resultbuf[2] = OTHER;
		}
		resultbuf[3] = statbuf.st_mode & 07777;
		resultbuf[4] = statbuf.st_size;
		resultbuf[5] = statbuf.st_uid;
		resultbuf[6] = statbuf.st_gid;
		resultbuf[7] = statbuf.st_mtime;
		(*env)->SetLongArrayRegion(env, result, 0, 8, resultbuf);
		return 0;
	}
}

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_readlink(JNIEnv *env, jclass clss, jstring linkname, jint linkvallen)
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

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_chmod(JNIEnv *env, jclass clss, jstring linkname, jshort mode)
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

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_chown(JNIEnv *env, jclass clss, jstring linkname, jint uid, jint gid)
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

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_getpwuid(JNIEnv *env, jclass clss, jint uid)
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

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_getgrgid(JNIEnv *env, jclass clss, jint gid)
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

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_getpwnam(JNIEnv *env, jclass clss, jstring user)
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

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_getgrnam(JNIEnv *env, jclass clss, jstring group)
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

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_strerrorErrno(JNIEnv *env, jclass clss)
{
    return (*env)->NewStringUTF(env, strerror(errno));
}

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_common_filesystem_FileLinkUtilities_strerror(JNIEnv *env, jclass clss, jint errnum)
{
    return (*env)->NewStringUTF(env, strerror(errnum < 0 ? -errnum : errnum));
}
