#include <jni.h>
#include <string>

extern "C"
JNIEXPORT void JNICALL
Java_cn_muse_facetrack_MainActivity_loadSample(JNIEnv *env, jobject instance,
                                               jstring detectSample_) {
    const char *detectSample = env->GetStringUTFChars(detectSample_, 0);

    // TODO

    env->ReleaseStringUTFChars(detectSample_, detectSample);
}