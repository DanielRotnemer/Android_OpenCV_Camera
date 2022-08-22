package com.moviera.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;

import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities
{
    public static String ServerIpAdderss = "192.168.43.188";

    public static int UserColor = 0xFF04694b;
    public static int PageColor = 0xFF053a5e;
    public static int ChannelColor = 0xFFce413c;

    public static String TypeUser = "USER";
    public static String TypePage = "PAGE";
    public static String TypeChannel = "CHANNEL";

    public static String ValidateEmailLoginDetails(String email, String password)
    {
        if (password.replace(" ", "").equals("") || email.replace(" ", "").equals("")) {
            return "Enter your details to log in.";
        }
        if (!ValidEmail(email)) {
            return "Enter your email address.";
        }
        Pattern p = Pattern.compile("[A-Z]");
        Matcher m = p.matcher(password);
        if (!m.find()) { return "Password must contain letters A-Z."; }
        p = Pattern.compile("[a-z]");
        m = p.matcher(password);
        if (!m.find()) { return "Password must contain characters a-z."; }
        p = Pattern.compile("[0-9]");
        m = p.matcher(password);
        if (!m.find()) { return "Password must contain numbers 0-9."; }
        if (password.length() < 10) { return "Password must consist of at least 10 characters."; }
        if (password.length() > 30) { return "Please enter a shorter password."; }
        String specialChars = "`~!@#$%^&*()_-+={}[]|\\:;\"'/?><.*";
        for (char l : password.toCharArray())
        {
            if (specialChars.indexOf(l) != -1) {
                return "";
            }
        }
        return "Password must contain at least one special character.";
    }

    public static Boolean ValidEmail(String email)
    {
        boolean match = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        return match;
    }

    public static Boolean ValidUsername(String username)
    {
        return true;
    }

    public static String GetIPAddress(boolean useIPv4)
    {
        try
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces)
            {
                List<InetAddress> addresses = Collections.list(intf.getInetAddresses());
                for (InetAddress address : addresses)
                {
                    if (!address.isLoopbackAddress())
                    {
                        String hostAddress = address.getHostAddress();
                        boolean isIPv4 = hostAddress.indexOf(':') < 0;
                        if (useIPv4)
                        {
                            if (isIPv4)
                                return hostAddress;
                        }
                        else
                        {
                            if (!isIPv4) {
                                int delimeter = hostAddress.indexOf('%');
                                return delimeter < 0 ? hostAddress.toUpperCase() : hostAddress.substring(0, delimeter).toUpperCase();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex) { }
        return "";
    }

    public static String AdjustNumber(String number)
    {
        if (number.indexOf(".") >= 0)
        {
            char order = number.charAt(number.length() - 1);
            String first = number.substring(0, number.indexOf('.'));
            String last = number.substring(number.indexOf('.') + 1);
            last = "." + last.charAt(0);
            if (last == ".0") { last = ""; }
            number = first + last + order;
        }
        if (number.length() == 4 && number.indexOf(".") == -1 && number.indexOf("K") == -1
                && number.indexOf("M") == -1 && number.indexOf("B") == -1 && number.indexOf("T") == -1)
        {
            number = number.charAt(0) + "," + number.substring(1);
        }
        return number;
    }

    public static String RoundNumber(Long number)
    {
        int thousand = 1000, tenThousand = 10000, hundredThousand = 100000,
                million = 1000000, hundredMillion = 100000000, billion = 1000000000;
        long hundredBillion = 100000000000L, trillion = 1000000000000L, hundredTrillion = 100000000000000L,
                quadrillion = 1000000000000000L;
        if (number < tenThousand) {
            return number.toString();
        }
        if (number >= tenThousand && number < hundredThousand) {
            return new Float((float)number / thousand) + "K";
        }
        if (number >= hundredThousand && number < million) {
            return new Long(number / thousand) + "K";
        }
        if (number >= million && number < hundredMillion) {
            return new Float((float)number / million) + "M";
        }
        if (number >= hundredMillion && number < billion) {
            return new Long(number / million) + "M";
        }
        if (number >= billion && number < hundredBillion) {
            return new Float((float)number / billion) + "B";
        }
        if (number >= hundredBillion && number < trillion) {
            return new Long(number / billion) + "B";
        }
        if (number >= trillion && number < hundredTrillion) {
            return new Float((float)number / trillion) + "T";
        }
        if (number >= hundredTrillion && number < quadrillion) {
            return new Long(number / trillion) + "T";
        }
        else { return number.toString(); }
    }

    public static float PixelsToDp(Context context, int dp)
    {
        float pixels = dp * context.getResources().getDisplayMetrics().density;
        return pixels;
    }

    public static String GetScreenSize(Activity activity)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return width + "|" + height;
    }

    public static int GetTimeZoneOffset()
    {
        Date dateTime = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String localString = dateFormatter.format(dateTime);

        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcString = dateFormatter.format(dateTime);

        TimeZone timezone = TimeZone.getDefault();
        Date now = new Date();
        int timezoneOffset = timezone.getOffset(now.getTime()) / -1000;
        timezoneOffset /= 60;

        try {
            Date local = dateFormatter.parse(localString);
            Date utc = dateFormatter.parse(utcString);
            timezoneOffset = (int)((utc.getTime() - local.getTime()) / 1000);
            timezoneOffset /= 60;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timezoneOffset;
    }

    public static Bitmap FlipBitmap(Bitmap source, boolean xFlip, boolean yFlip)
    {
        Matrix matrix = new Matrix();
        matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, source.getWidth() / 2f, source.getHeight() / 2f);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap RotateBitmap(Bitmap source, int degrees)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(), source.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    public static Bitmap AdjustBitmapResolution(Bitmap source)
    {
        Bitmap adjustedBitmap = Bitmap.createBitmap(source.getHeight(), source.getWidth(), Bitmap.Config.RGB_565);
        Color color;
        for (int x = 0; x < source.getWidth(); x++)
        {
            for (int y = 0; y < source.getHeight(); y++)
            {
                int pixel = source.getPixel(x, y);
                adjustedBitmap.setPixel(source.getHeight() - 1 - y, x, Color.rgb(Color.red(pixel),
                        Color.green(pixel), Color.blue(pixel)));
            }
        }
        return adjustedBitmap;
    }

    public static boolean FrontCameraAvailable()
    {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++)
        {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return true;
            }
        }
        return false;
    }

    public static Bitmap GetBitmapImageFromURL(String url)
    {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
            return bitmap;
        }
        catch (IOException ex) {
            return null;
        }
    }

    public static String GetVideoTimeFromSeconds(Long seconds)
    {
        String time = "";
        Long hours = (seconds / 3600);
        String zero = hours < 10 ? "0" : (hours == 0 ? "00" : "");
        time += zero + hours + ":";
        seconds -= (hours * 3600);

        Long minutes = (seconds / 60);
        zero = minutes < 10 ? "0" : (minutes == 0 ? "00" : "");
        time += zero + minutes + ":";
        seconds -= (minutes * 60);

        zero = seconds < 10 ? "0" : (seconds == 0 ? "00" : "");
        time += zero + seconds + ":";
        return time;
    }

    public static String GetUserLanguage() {
        return Locale.getDefault().getDisplayLanguage();
    }

    public static boolean HasPermissions(Context context, String[] permissions)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null)
        {
            for (String permission : permissions)
            {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Bitmap OverlayBitmap(Bitmap main, Bitmap overlay)
    {
        Bitmap resultBitmap = Bitmap.createBitmap(main.getWidth(), main.getHeight(), main.getConfig());
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(main, new Matrix(), null);

        Bitmap newOverlay = Bitmap.createScaledBitmap(overlay, main.getWidth(), main.getHeight(), false);

        canvas.drawBitmap(newOverlay, new Matrix(), null);
        Log.d("FlashModes", "Main size: " + main.getWidth() + ", " + main.getHeight() +
                "Overlay size: " + newOverlay.getWidth() + ", " + newOverlay.getHeight());
        return resultBitmap;
    }
}
