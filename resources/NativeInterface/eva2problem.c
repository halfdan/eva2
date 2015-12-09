#include "eva2_problems_NativeProblem.h"

JNIEXPORT jdoubleArray JNICALL Java_eva2_problems_NativeProblem_evaluate
  (JNIEnv *env, jobject obj, jdoubleArray array) {
  int i;
  double result[1] = {0.0};
  jdoubleArray resultArray = (*env)->NewDoubleArray(env, 1);
  if (resultArray == NULL) {
    return NULL; /* out of memory :( */
  }
  jsize len = (*env)->GetArrayLength(env, array);
  jdouble *body = (*env)->GetDoubleArrayElements(env, array, 0);

  // Calculate sum(x_i^2)
  for(i=0; i<len; i++) {
    result[0] += body[i] * body[i];
  }

  // Release input array
  (*env)->ReleaseDoubleArrayElements(env, array, body, 0);
  // Set result
  (*env)->SetDoubleArrayRegion(env, resultArray, 0, 1, result);
  return resultArray;
}

JNIEXPORT jstring JNICALL Java_eva2_problems_NativeProblem_getName
  (JNIEnv *env, jobject obj) {
  return (*env)->NewStringUTF(env, "Native Sphere");
}
