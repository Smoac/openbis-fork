#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <jni.h>

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_utilities_FileLinkUtilities_hardlink
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

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_utilities_FileLinkUtilities_symlink
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

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_utilities_FileLinkUtilities_linkinfo(JNIEnv *env, jclass clss, jstring filename, jintArray result)
{
    const char* pfilename;
	struct stat statbuf;
	jint resultbuf[4];
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
		resultbuf[2] = S_ISLNK(statbuf.st_mode);
		resultbuf[3] = statbuf.st_size;
		(*env)->SetIntArrayRegion(env, result, 0, 4, resultbuf);
		return 0;
	}
}

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_common_utilities_FileLinkUtilities_readlink(JNIEnv *env, jclass clss, jstring linkname, jint linkvallen)
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

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_common_utilities_FileLinkUtilities_strerror(JNIEnv *env, jclass clss, jint errnum)
{
    return (*env)->NewStringUTF(env, strerror(errnum < 0 ? -errnum : errnum));
}
