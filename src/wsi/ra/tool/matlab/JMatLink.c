/*****************************************************************************
*                           JMatLink                                         *
******************************************************************************
* (c) 1999-2001 Stefan Mueller  (email: stefan@held-mueller.de)              *
******************************************************************************
* 19.01.1999  beginning (reuse of HelloWorld example, Java Tutorial)  (0.01) *
* 06.02.1999  separation from gui                                     (0.02) *
* 27.03.1999  engGetScalar, engGetVector, engGetArray                 (0.03) *
*             engGetScalar already working                                   *
* 01.04.1999  engGetArray: hints from bwymans@home.com                       *
* 02.05.1999  engGetArray is working now                              (0.04) *
* 20.05.1999  begin of engPutArray                                           *
* 13.08.1999  renamed all native methods to ...NATIVE                 (0.05) *
* 30.08.1999  beginning to include some failure checks                (0.06) *
* 31.08.1999  increased BUFSIZE to account for larger outputs         (0.07) *
* 11.09.1999  get char arrays from workspace                          (0.08) *
* 10.10.1999  engPutArray[][] completed                               (0.10) *
* 04/07/2001  restart work on JMatLink                                (0.11) *
* 05/24/2001  engOpenSingleUse                                        (0.11) *
*             Introduce pool of engine pointers                              *
*             Removed engPutArray, engPutArray[], always use engPutArray[][] *
* 07/31/01    Code Cleanup                                                   *
* 08/11/01    Moved to new version numer                              (1.00) *
******************************************************************************/


/* **** ToDo ****
*  complex number support
*
*/


#include <jni.h>
#include "JMatLink.h"
#include <stdio.h>


#include <stdlib.h>
#include <string.h>
#include "engine.h"

#define V5_COMPAT
#define  BUFSIZE 2560

    mxArray *T      = NULL;
    mxArray *result = NULL;
    char buffer[BUFSIZE];
    double *TP;
    mxArray  *arrayP;
    jdouble  scalar;
    int      engOpenMarkerI = 0;    // Remember the engine that was opened with
                                   //   engOpen-command 

    #define enginePMax 10          // Maximum number of engines opened at the 
                                   //   same time.
    Engine *engineP[ enginePMax ]; // Array of pointers to engines
                                   // Element 0 is reserved

    jboolean debugB = JNI_FALSE;       // No debug messages from start
   

/******************************************************************************/
int getUnusedEnginePointer()
{
    /* Find unsed entry in array of pointers to engine */
    /* j=0 is reserved!!                               */

    int j;

    for (j=1; j<enginePMax; j++) 
    {
        if ( engineP[j] == NULL )
        {
            if (debugB) printf("engOpen %i", j);
            return j;                // Unused entry in array
        }
        else
        {

        }
    }

    if (debugB) printf("engOpen: getUnusedEnginePointer no more pointers available");

    return 0; // all entries in use
}

void delEnginePointer(int p)
{

    if (    ( p < 1           )
         || ( p >= enginePMax )
       )
    {
        return; // pointer out of allowed region
    }

    // free entry in array
    engineP[p] = 0;

}


JNIEXPORT void JNICALL Java_wsi_ra_tool_matlab_JMatLink_setDebugNATIVE
            (JNIEnv *env, jobject obj, jboolean d)
{
    if (d) debugB = JNI_TRUE;
    else   debugB = JNI_FALSE;
     
}


/********************************************************************************/
 
/********************************************************************************/
/********************    int   engOpenNATIVE( startcmd )    *********************/
/********************************************************************************/

JNIEXPORT jint JNICALL Java_wsi_ra_tool_matlab_JMatLink_engOpenNATIVE__Ljava_lang_String_2
                                 (JNIEnv *env, jobject obj, jstring startCmdS_JNI)
{
    int         engineI;   
    const char *openS; 

    if (engOpenMarkerI != 0) return engOpenMarkerI; // engOpen already used before
                                        // return handle valid connection

    openS = (*env)->GetStringUTFChars(env, startCmdS_JNI, 0);

    /* find unused entry in engine array */
    engineI = getUnusedEnginePointer();

    if (engineI==0) return 0;  // no more pointers available

    if (!(engineP[engineI] = engOpen(openS)) )
    {
        if (debugB) fprintf(stderr, "\nCan't start MATLAB engine\n");
        (*env)->ReleaseStringUTFChars(env, startCmdS_JNI, openS); // free memory
        delEnginePointer(engineI);
        return 0;
    }

    engOpenMarkerI = engineI; // Remember engine that was opened with engOpen()    

    (*env)->ReleaseStringUTFChars(env, startCmdS_JNI, openS); // free memory

    return engineI;
}

/********************************************************************************/
/****************    int   engOpenSingleUseNATIVE( startcmd )    ****************/
/********************************************************************************/
JNIEXPORT jint JNICALL Java_wsi_ra_tool_matlab_JMatLink_engOpenSingleUseNATIVE 
                               (JNIEnv *env, jobject obj, jstring startCmdS_JNI)
{
    const char *openS     = (*env)->GetStringUTFChars(env, startCmdS_JNI, 0);
    int         retStatus = 0;
    int         engineI;   

    /* find unused entry in engine array */
    engineI = getUnusedEnginePointer();

    if (engineI==0) return 0;  // no more pointers avaiblable

    if (!(engineP[engineI] = engOpenSingleUse(openS, NULL, &retStatus)))
    {
        if (debugB) fprintf(stderr, "\nCan't start MATLAB engine\n");
        (*env)->ReleaseStringUTFChars(env, startCmdS_JNI, openS); // free memory

        delEnginePointer(engineI);
        return 0;
    }

    (*env)->ReleaseStringUTFChars(env, startCmdS_JNI, openS); // free memory

    return engineI;
}


/********************************************************************************/
/****************        int  engCloseNATIVE( int epI )          ****************/
/********************************************************************************/
JNIEXPORT jint JNICALL Java_wsi_ra_tool_matlab_JMatLink_engCloseNATIVE__I  
                                           (JNIEnv *env, jobject obj, jint engine)
{
    int retValI;

    // Check if engine pointer is within allowed region
    if (( engine < 1 ) || ( engine >= enginePMax ))
    {
        return 0; // Pointer is out of allowed region
    }


    if ( engineP[ engine ] != NULL )
    {
        retValI = engClose(engineP[ engine ]);
        delEnginePointer( engine );
        if (engine == engOpenMarkerI)
        {
            // This engine was opened with engOpen() before
            engOpenMarkerI = 0;
        }
        if (debugB) printf("\n engClose \n");
    }
    else
    {
        return 0;
    }
}



/********************************************************************************/
/***********   int  engEvalStringNATIVE( int epI, String evalS )    *************/
/********************************************************************************/
JNIEXPORT jint JNICALL Java_wsi_ra_tool_matlab_JMatLink_engEvalStringNATIVE__ILjava_lang_String_2
                        (JNIEnv *env, jobject obj, jint engine, jstring evalS_JNI)
{
    int  retValI = 0;
    const char *evalS = (*env)->GetStringUTFChars(env, evalS_JNI, 0);


    // Check if engine pointer is within allowed region
    if (( engine < 1 ) || ( engine >= enginePMax ))
    {
        return 0; // Pointer is out of allowed region
    }
  
    retValI = engEvalString(engineP[ engine ], evalS);

    //printf("evalString %i",OpenB);

    (*env)->ReleaseStringUTFChars(env, evalS_JNI, evalS); // free memory

    return retValI;
}



/********************************************************************************/
// public native void   engOutputBufferNATIVE(int  epI, int buflenI );
/********************************************************************************/
JNIEXPORT jstring JNICALL Java_wsi_ra_tool_matlab_JMatLink_engOutputBufferNATIVE  
                             (JNIEnv *env, jobject obj,  jint engine, jint buflen)
{
  // !!!!! buflen not implemented yet

    // Check if engine pointer is within allowed region
    if (( engine < 1 ) || ( engine >= enginePMax ))
    {
        return 0; // Pointer is out of allowed region
    }

   engOutputBuffer(engineP[ engine ], buffer, BUFSIZE);

   if (debugB) printf("JMatLink %s", buffer);
   return (*env)->NewStringUTF(env, buffer);
}


/********************************************************************************/
// public native engPutArrayNATIVE(int epI, String nameS, double[][] array );
/********************************************************************************/
JNIEXPORT void JNICALL Java_wsi_ra_tool_matlab_JMatLink_engPutArrayNATIVE
   (JNIEnv *env, jobject obj, jint engine, jstring arrayS_JNI, jobjectArray valueDD_JNI)
{

    int i, j;

    const char *arrayS;    
    int   rowCount;
    jobject colPtr;
    int   colCount;
    double *tPtrR;  
    jdouble *arrayElements;

    // Check if engine pointer is within allowed region
    if (( engine < 1 ) || ( engine >= enginePMax ))
    {
        return; // Pointer is out of allowed region
    }


    arrayS    = (*env)->GetStringUTFChars(env, arrayS_JNI, 0);
    rowCount        = (*env)->GetArrayLength(env, valueDD_JNI);
    colPtr        = (*env)->GetObjectArrayElement(env, valueDD_JNI, 0);
    colCount        = (*env)->GetArrayLength(env, colPtr);
    //jboolean *isCopy;

    //double *arrayElements = (*env)->GetDoubleArrayElements(env, colPtr, isCopy);

    if (debugB) printf("engPutArray [][] %s %i %i\n", arrayS, rowCount, colCount);

    T = mxCreateDoubleMatrix(rowCount, colCount, mxREAL);
    mxSetName(T, arrayS);
    //printf("matrix created and name set\n");
    tPtrR = mxGetPr(T);

    for (i=0; i<rowCount; i++) {
    //printf("row %i\n",i);
        colPtr = (*env)->GetObjectArrayElement(env, valueDD_JNI, i);
        //printf("got colPtr %i\n",i);
        arrayElements = (*env)->GetDoubleArrayElements(env, colPtr, 0);
        //printf("got array Elements\n");
        for (j=0; j<colCount; j++) {
            //printf("col %i\n",j);
            tPtrR[i + j*rowCount] = arrayElements[j];
        }
    } // rows

    //printf("elements copied\n");

    engPutArray( engineP[ engine ], T );  /* send array to MATLAB */

    mxDestroyArray(T);

    (*env)->ReleaseStringUTFChars(env, arrayS_JNI, arrayS); // free memory
}



/********************************************************************************/
// public native double      engGetScalarNATIVE(int epI, String nameS);
/********************************************************************************/
JNIEXPORT jdouble JNICALL Java_wsi_ra_tool_matlab_JMatLink_engGetScalarNATIVE
                      (JNIEnv *env, jobject obj, jint engine, jstring scalarS_JNI)
{


    const char *scalarS= (*env)->GetStringUTFChars(env, scalarS_JNI, 0);

    // Check if engine pointer is within allowed region
    if (( engine < 1 ) || ( engine >= enginePMax ))
    {
        return 0.0; // Pointer is out of allowed region
    }

    
    if (debugB) printf("native engGetScalar %s \n",scalarS);

    //if (OpenB==0) return 0.0; // engine is not open   

    arrayP = engGetArray( engineP[ engine ], scalarS);

    if (arrayP == NULL) {
        printf("Could not get scalar from MATLAB workspace.\n");
        (*env)->ReleaseStringUTFChars(env, scalarS_JNI, scalarS); // free memory
        return 0.0;
    }
    else {
        scalar = mxGetScalar(arrayP);
    }
 	
    mxDestroyArray(arrayP);                                   // free memory
    (*env)->ReleaseStringUTFChars(env, scalarS_JNI, scalarS); // free memory

    return scalar;
}


/********************************************************************************/
/**********   double[][]  engGetArrayNATIVE( int epI, String nameS )    *********/
/********************************************************************************/ 
JNIEXPORT jobjectArray JNICALL Java_wsi_ra_tool_matlab_JMatLink_engGetArrayNATIVE
                       (JNIEnv *env, jobject obj, jint engine, jstring arrayS_JNI)
{
    // NOTE: in java there are only 1-dimensional (array[]) arrays.
    //       higher dimensional arrays are arrays of arrays.
    jarray rowA;
    jclass rowClass;

    jarray columnA;
    int in,im;
    int m = 0;
    int n = 0;
  
    jdouble *rowElements;
    double *TP;

    // convert array name to c-string
    const char *arrayS = (*env)->GetStringUTFChars(env, arrayS_JNI, 0);

    // Check if engine pointer is within allowed region
    //if (( engine < 1 ) || ( engine >= enginePMax ))
    //{
    //    return NULL; // Pointer is out of allowed region
    //}


    if (engineP[ engine ] != NULL)
    {

        if (debugB) printf("native engGetArray %s \n",arrayS);

        arrayP = engGetArray( engineP[ engine ] , arrayS);
        if (arrayP == NULL) 
        {
            printf("Could not get array %s from MATLAB workspace.\n", arrayS);
            (*env)->ReleaseStringUTFChars(env, arrayS_JNI, arrayS); // free memory
            return NULL;
        }

        m  = mxGetM( arrayP );   // rows 
        n  = mxGetN( arrayP );   // columns 
        TP = mxGetPr( arrayP );  // get pointer to values

 
      /* create an array of double and get its class  */
      rowA        = (*env)->NewDoubleArray( env, n);              // row vector
      rowClass    = (*env)->GetObjectClass( env, rowA);           // row class

      /* create an array of object with the rowClass as the
         the default element */
      columnA = (*env)->NewObjectArray( env, m, rowClass, NULL);  // column vector


    for (im=0; im<m; im++) 
    {
        rowA        = (*env)->NewDoubleArray( env, n);              // row vector
        rowClass    = (*env)->GetObjectClass( env, rowA);           // row class
        rowElements = (*env)->GetDoubleArrayElements(env, rowA, 0); // row elements

        for (in=0; in<n; in++) 
        {
            rowElements[in] = TP[in*m + im];  
        }

        (*env)->SetObjectArrayElement(env, columnA, im, rowA);

        (*env)->ReleaseDoubleArrayElements(env, rowA, rowElements, 0);
        }
    } // engineP[ ]

    mxDestroyArray(arrayP);                                 // free memory
    (*env)->ReleaseStringUTFChars(env, arrayS_JNI, arrayS); // free memory


    // are the following two line ok? return NULL ?!?!?
    if (engineP[ engine ] != NULL) return columnA;
    else                           return NULL;
}

/********************************************************************************/
// public String[] engGetCharArrayNATIVE(String name)
/********************************************************************************/
JNIEXPORT jobjectArray JNICALL Java_wsi_ra_tool_matlab_JMatLink_engGetCharArrayNATIVE
                    (JNIEnv *env, jobject jthis, jint engine, jstring arrayS_JNI)
{


    // Check if engine pointer is within allowed region
    //if (( engine < 1 ) || ( engine >= enginePMax ))
    //{
    //    return NULL; // Pointer is out of allowed region
    //}

   const char *arrayS = (*env)->GetStringUTFChars(env, arrayS_JNI, 0);


   if (debugB)  printf("native engGetArray %s \n",arrayS);

   arrayP = engGetArray( engineP[ engine ], arrayS);
   if (arrayP == NULL) {
       printf("Could not get array %s from MATLAB workspace.\n", arrayS);
       (*env)->ReleaseStringUTFChars(env, arrayS_JNI, arrayS); // free memory
       return NULL;
   }
   if (mxIsChar(arrayP) != 0) {
       printf("The array %s is not of type char.\n", arrayS);
       (*env)->ReleaseStringUTFChars(env, arrayS_JNI, arrayS); // free memory
       return NULL;
   }


   mxDestroyArray(arrayP);                                 // free memory
   (*env)->ReleaseStringUTFChars(env, arrayS_JNI, arrayS); // free memory
} // end engGetCharArrayNATIVE



