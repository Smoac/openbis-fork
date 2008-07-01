#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <jni.h>

JNIEXPORT jint JNICALL Java_ch_systemsx_cisd_common_utilities_FileLinkUtilities_hardlink
  (JNIEnv *env, jclass clss, jstring filename, jstring linktarget)
{
    const char* pfilename;
    const char* plinktarget;
    jboolean isCopy;
    int retval;

    pfilename = (char *)(*env)->GetStringUTFChars(env, filename, &isCopy);
    plinktarget = (char *)(*env)->GetStringUTFChars(env, linktarget, &isCopy);

    retval = link(pfilename, plinktarget);
    if (retval < 0)
    {
        retval = errno;
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
    jboolean isCopy;
    int retval;

    pfilename = (char *)(*env)->GetStringUTFChars(env, filename, &isCopy);
    plinktarget = (char *)(*env)->GetStringUTFChars(env, linktarget, &isCopy);

    retval = symlink(pfilename, plinktarget);
    if (retval < 0)    { 
        retval = errno; 
    }

    (*env)->ReleaseStringUTFChars(env, filename, pfilename);
    (*env)->ReleaseStringUTFChars(env, linktarget, plinktarget);

   return retval;
}

JNIEXPORT jstring JNICALL Java_ch_systemsx_cisd_common_utilities_FileLinkUtilities_strerror(JNIEnv *env, jclass clss, jint errnum)
{
    return (*env)->NewStringUTF(env, strerror(errnum));
}
