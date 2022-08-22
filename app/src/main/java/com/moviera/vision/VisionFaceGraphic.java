package com.moviera.vision;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

import com.google.android.gms.vision.face.Contour;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.moviera.R;

public class VisionFaceGraphic extends VisionGraphicOverlay.VisionGraphic
{
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] =
            {
                    Color.BLUE,
                    Color.CYAN,
                    Color.GREEN,
                    Color.MAGENTA,
                    Color.RED,
                    Color.WHITE,
                    Color.YELLOW
            };

    private VisionFilter filter;

    private static int mCurrentColorIndex = 0;

    private Paint mBoxPaint, filterPaintFill, filterPaintStroke;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    public VisionFaceGraphic(VisionGraphicOverlay overlay, VisionFilter filter)
    {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        this.filter = filter;

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        filterPaintFill = new Paint();
        filterPaintFill.setColor(filter.getFillColor());
        filterPaintFill.setStyle(Paint.Style.FILL);

        filterPaintStroke = new Paint();
        filterPaintStroke.setColor(filter.getStrokeColor());
        filterPaintStroke.setStyle(Paint.Style.STROKE);
        filterPaintStroke.setStrokeWidth(filter.getStrokeWidth());
    }

    public void setId(int id) {
        mFaceId = id;
    }

    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas)
    {
        Face face = mFace;
        if (face == null)
            return;

        // DRAW BOUNDING BOX
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);
        //filterPaintFill.setShader(new LinearGradient(0, 0, 0, face.getHeight(), Color.BLACK, Color.WHITE, Shader.TileMode.REPEAT));

        // DRAW CONTOUR EFFECTS
        if (face.getContours().size() > 0 || face.getLandmarks().size() > 0)
        {
            // DOT CONTOURS
            if (filter.getFilter() == VisionFilter.VISION_FILTER_DOTCONTOURS)
            {
                for (Contour contour : face.getContours())
                {
                    for (PointF point : contour.getPositions())
                    {
                        float pointX = translateX(point.x);
                        float pointY = translateY(point.y);
                        canvas.drawCircle(pointX, pointY, 3, mBoxPaint);
                    }
                }
            }

            // LINE CONTOURS
            if (filter.getFilter() == VisionFilter.VISION_FILTER_LINECONTOURS)
            {
                for (Contour contour : face.getContours())
                {
                    for (int i = 0; i < contour.getPositions().length; i++)
                    {
                        int next = i < contour.getPositions().length - 1 ? i + 1 : -1;
                        PointF currentPoint = contour.getPositions()[i];
                        if (next != -1)
                        {
                            float pointCurrentX = translateX(currentPoint.x);
                            float pointCurrentY = translateY(currentPoint.y);
                            PointF nextPoint = contour.getPositions()[next];
                            float pointNextX = translateX(nextPoint.x);
                            float pointNextY = translateY(nextPoint.y);
                            canvas.drawCircle(pointCurrentX, pointCurrentY, 3, mBoxPaint);
                            canvas.drawLine(pointCurrentX + 1.5f, pointCurrentY + 1.5f, pointNextX, pointNextY, mBoxPaint);
                        }
                        else
                        {
                            float pointX = translateX(currentPoint.x);
                            float pointY = translateY(currentPoint.y);
                            canvas.drawCircle(pointX, pointY, 3, mBoxPaint);
                        }
                    }
                }
            }

            // LIPSTICK
            if (filter.getFilter() == VisionFilter.VISION_FILTER_LIPSTICK)
            {
                PointF[] upperLipTop = {}, upperLipBottom = {}, lowerLipTop = {},
                        lowerLipBottom = {};
                for (Contour contour : face.getContours())
                {
                    if (contour.getType() == Contour.UPPER_LIP_TOP)
                        upperLipTop = contour.getPositions();
                    if (contour.getType() == Contour.UPPER_LIP_BOTTOM)
                        upperLipBottom = contour.getPositions();
                    if (contour.getType() == Contour.LOWER_LIP_TOP)
                        lowerLipTop = contour.getPositions();
                    if (contour.getType() == Contour.LOWER_LIP_BOTTOM)
                        lowerLipBottom = contour.getPositions();
                }

                PointF[] lowerLip = new PointF[lowerLipTop.length + lowerLipBottom.length];
                for (int i = 0; i < lowerLipTop.length; i++) {
                    lowerLip[i] = lowerLipTop[i];
                }
                for (int i = 0; i < lowerLipBottom.length; i++) {
                    lowerLip[lowerLipTop.length + i] = lowerLipBottom[lowerLipBottom.length - 1 - i];
                }

                Path lowerLipPath = new Path();
                lowerLipPath.moveTo(translateX(lowerLip[0].x), translateY(lowerLip[0].y));
                for (int i = 0; i < lowerLip.length; i++)
                {
                    PointF point = lowerLip[i];
                    lowerLipPath.lineTo(translateX(point.x), translateY(point.y));
                }
                lowerLipPath.lineTo(translateX(lowerLip[0].x), translateY(lowerLip[0].y));
                lowerLipPath.close();
                if (filter.getFill())
                    canvas.drawPath(lowerLipPath, filterPaintFill);
                if (filter.getStroke())
                    canvas.drawPath(lowerLipPath, filterPaintStroke);

                PointF[] upperLip = new PointF[upperLipTop.length + upperLipBottom.length + 4];
                upperLip[0] = lowerLipBottom[lowerLipBottom.length - 1];
                for (int i = 0; i < upperLipTop.length; i++) {
                    upperLip[i + 1] = upperLipTop[i];
                }
                upperLip[upperLipTop.length + 1] = lowerLipBottom[0];
                upperLip[upperLipTop.length + 2] = lowerLipTop[0];
                for (int i = 0; i < upperLipBottom.length; i++) {
                    upperLip[upperLipTop.length + i + 3] = upperLipBottom[upperLipBottom.length - 1 - i];
                }
                upperLip[upperLip.length - 1] = lowerLipTop[lowerLipTop.length - 1];

                Path upperLipPath = new Path();
                upperLipPath.moveTo(translateX(upperLip[0].x), translateY(upperLip[0].y));
                for (int i = 0; i < upperLip.length; i++)
                {
                    PointF point = upperLip[i];
                    upperLipPath.lineTo(translateX(point.x), translateY(point.y));
                }
                upperLipPath.lineTo(translateX(upperLip[0].x), translateY(upperLip[0].y));
                upperLipPath.close();
                if (filter.getFill())
                    canvas.drawPath(upperLipPath, filterPaintFill);
                if (filter.getStroke())
                    canvas.drawPath(upperLipPath, filterPaintStroke);
            }

            // LETTERBOX
            if (filter.getFilter() == VisionFilter.VISION_FILTER_LETTERBOX)
            {
                float angelX = face.getEulerX(), angelY = face.getEulerY(), angelZ = face.getEulerZ();
                for (Landmark landmark : face.getLandmarks())
                {
                    //if (landmark.getType() == Landmark.LEFT_EYE)

                    float landmarkX = translateX(landmark.getPosition().x);
                    float landmarkY = translateY(landmark.getPosition().y);
                    canvas.drawCircle(landmarkX, landmarkY, 3, mBoxPaint);
                }

                // x: turn top down, y: turn left right, z: tilted face

                float letterBoxTop = top;
                RectF letterBox = new RectF(left, top, right, top + 100);

                canvas.save();
                canvas.rotate(angelZ, canvas.getWidth() / 2, canvas.getHeight() / 2);
                canvas.drawRect(letterBox, filterPaintFill);
                canvas.restore();

                Log.d("Face", "X: " + angelX + ", Y: " + angelY + ", Z: " + angelZ);
            }

            // COLOREDEYES
            if (filter.getFilter() == VisionFilter.VISION_FILTER_COLOREDEYES)
            {
                /*PointF[] leftEye = {}, rightEye = {};
                for (Contour contour : face.getContours())
                {
                    if (contour.getType() == Contour.LEFT_EYE)
                        leftEye = contour.getPositions();
                    if (contour.getType() == Contour.RIGHT_EYE)
                        rightEye = contour.getPositions();
                }

                Path leftEyePath = new Path();
                for (int i = 0; i < leftEye.length; i++) {
                    leftEyePath.lineTo(translateX(leftEye[i].x), translateY(leftEye[i].y));
                }
                leftEyePath.close();

                if (filter.getFill())
                    canvas.drawPath(leftEyePath, filterPaintFill);
                if (filter.getStroke())
                    canvas.drawPath(leftEyePath, filterPaintStroke);

                Path rightEyePath = new Path();
                for (int i = 0; i < rightEye.length; i++) {
                    rightEyePath.lineTo(translateX(rightEye[i].x), translateY(rightEye[i].y));
                }
                rightEyePath.close();

                if (filter.getFill())
                    canvas.drawPath(rightEyePath, filterPaintFill);
                if (filter.getStroke())
                    canvas.drawPath(rightEyePath, filterPaintStroke);*/
            }
        }


        /*
        if (face.getContours().size() > 0)
        {
            PointF[] faceContour = {}, leftEyebrowTop = {}, leftEyebrowBottom = {}, rightEyebrowTop = {},
                     rightEyebrowBottom = {}, leftEye = {}, rightEye = {}, upperLipTop = {}, upperLipBottom = {},
                     lowerLipTop = {}, lowerLipBottom = {}, noseBridge = {}, noseBottom = {}, leftCheek = {}, rightCheek = {};

            for (Contour contour : face.getContours())
            {
                if (contour.getType() == Contour.FACE)
                    faceContour = contour.getPositions();
                if (contour.getType() == Contour.LEFT_EYEBROW_TOP)
                    leftEyebrowTop = contour.getPositions();
                if (contour.getType() == Contour.LEFT_EYEBROW_BOTTOM)
                    leftEyebrowBottom = contour.getPositions();
                if (contour.getType() == Contour.RIGHT_EYEBROW_TOP)
                    rightEyebrowTop = contour.getPositions();
                if (contour.getType() == Contour.RIGHT_EYEBROW_BOTTOM)
                    rightEyebrowBottom = contour.getPositions();
                if (contour.getType() == Contour.LEFT_EYE)
                    leftEye = contour.getPositions();
                if (contour.getType() == Contour.RIGHT_EYE)
                    rightEye = contour.getPositions();
                if (contour.getType() == Contour.UPPER_LIP_TOP)
                    upperLipTop = contour.getPositions();
                if (contour.getType() == Contour.UPPER_LIP_BOTTOM)
                    upperLipBottom = contour.getPositions();
                if (contour.getType() == Contour.LOWER_LIP_TOP)
                    lowerLipTop = contour.getPositions();
                if (contour.getType() == Contour.LOWER_LIP_BOTTOM)
                    lowerLipBottom = contour.getPositions();
                if (contour.getType() == Contour.NOSE_BRIDGE)
                    noseBridge = contour.getPositions();
                if (contour.getType() == Contour.NOSE_BOTTOM)
                    noseBottom = contour.getPositions();
                if (contour.getType() == Contour.LEFT_CHEEK)
                    leftCheek = contour.getPositions();
                if (contour.getType() == Contour.RIGHT_CHEEK)
                    rightCheek = contour.getPositions();
            }

            for (Contour contour : face.getContours())
            {
                for (int i = 0; i < contour.getPositions().length; i++)
                {
                    int next = i < contour.getPositions().length - 1 ? i + 1 : -1;
                    PointF point = contour.getPositions()[i];
                    if (next != -1)
                    {
                        float pointX = translateX(point.x);
                        float pointY = translateY(point.y);
                        PointF nextPoint = contour.getPositions()[next];
                        float pointNextX = translateX(nextPoint.x);
                        float pointNextY = translateY(nextPoint.y);
                        canvas.drawCircle(pointX, pointY, 3, mBoxPaint);
                        canvas.drawLine(pointX + 1.5f, pointY + 1.5f, pointNextX, pointNextY, mBoxPaint);
                    }
                    else
                    {
                        float pointX = translateX(point.x);
                        float pointY = translateY(point.y);
                        canvas.drawCircle(pointX, pointY, 3, mBoxPaint);
                    }
                }
            }

            PointF lowerLipTopEnd = lowerLipTop[lowerLipTop.length - 1];
            PointF lowerLipBottomEnd = lowerLipBottom[lowerLipBottom.length - 1];

            float lowerLipTopEndX = translateX(lowerLipTopEnd.x);
            float lowerLipTopEndY = translateY(lowerLipTopEnd.y);
            float lowerLipBottomEndX = translateX(lowerLipBottomEnd.x);
            float lowerLipBottomEndY = translateY(lowerLipBottomEnd.y);

            canvas.drawLine(lowerLipTopEndX + 1.5f, lowerLipTopEndY + 1.5f, lowerLipBottomEndX + 1.5f, lowerLipBottomEndY + 1.5f, mBoxPaint);

            PointF lowerLipTopStart = lowerLipTop[0];
            PointF lowerLipBottomStart = lowerLipBottom[0];

            float lowerLipTopStartX = translateX(lowerLipTopStart.x);
            float lowerLipTopStartY = translateY(lowerLipTopStart.y);
            float lowerLipBottomStartX = translateX(lowerLipBottomStart.x);
            float lowerLipBottomStartY = translateY(lowerLipBottomStart.y);

            canvas.drawLine(lowerLipTopStartX + 1.5f, lowerLipTopStartY + 1.5f, lowerLipBottomStartX + 1.5f, lowerLipBottomStartY + 1.5f, mBoxPaint);

            Path path = new Path();
            path.moveTo(translateX(lowerLipBottom[0].x), translateY(lowerLipBottom[0].y));
            for (int i = 0; i < lowerLipBottom.length; i++) {
                PointF point = lowerLipBottom[i];
                path.lineTo(translateX(point.x), translateY(point.y));
            }
            for (int i = lowerLipTop.length - 1; i >= 0; i--) {
                PointF point = lowerLipTop[i];
                path.lineTo(translateX(point.x), translateY(point.y));
            }
            path.lineTo(translateX(lowerLipBottom[0].x), translateY(lowerLipBottom[0].y));
            path.close();
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setColor(0x7755037a);
            canvas.drawPath(path, p);
        }

         */


    }
}
