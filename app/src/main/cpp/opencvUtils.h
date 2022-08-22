#pragma once

#include <opencv2/core.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <opencv2/imgproc.hpp>
#include <android/bitmap.h>
#include <GLES2/gl2.h>
#include <EGL/egl.h>
#include <string>

using namespace cv;
using namespace std;

void myFlip(Mat src);
void myBlur(Mat src, float sigma);
std::string myFunc();
bool toGray(Mat img, Mat& gray);