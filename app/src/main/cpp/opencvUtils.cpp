#include "opencvUtils.h"

void myFlip(Mat src) {
    flip(src, src, 0);
}

void myBlur(Mat src, float sigma) {
    GaussianBlur(src, src, Size(3, 3), sigma);
}

std::string myFunc() {
    return "MyFuncString";
}

bool toGray(Mat img, Mat& gray)
{
    cvtColor(img, gray, CV_RGBA2GRAY);
    if (gray.rows == img.rows && gray.cols == img.rows)
        return true;
    return false;
}