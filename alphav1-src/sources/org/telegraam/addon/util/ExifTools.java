package org.telegraam.addon.util;

import android.content.ContentResolver;
import android.location.Location;
import android.net.Uri;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.exifinterface.media.ExifInterface;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;

/* compiled from: ExifTools.kt */
@Metadata(d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\bÆ\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002J\u0010\u0010\u000b\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u0016\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J4\u0010\u0014\u001a\u00020\b2\u0006\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u00132\b\u0010\u0018\u001a\u0004\u0018\u00010\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\b\u0002\u0010\u001c\u001a\u00020\u001bJ\u0018\u0010\u001d\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u001e\u001a\u00020\u001bH\u0002J\u0018\u0010\u001f\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002R\u0016\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004¢\u0006\u0004\n\u0002\u0010\u0006¨\u0006 "}, d2 = {"Lorg/telegraam/addon/util/ExifTools;", "", "()V", "COPY_TAGS", "", "", "[Ljava/lang/String;", "clearDates", "", "exif", "Landroidx/exifinterface/media/ExifInterface;", "clearGeo", "open", "Ljava/io/InputStream;", "cr", "Landroid/content/ContentResolver;", "uri", "Landroid/net/Uri;", "readSignature", "Lorg/telegraam/addon/util/CameraSignature;", "rewriteExif", "file", "Ljava/io/File;", "signature", "loc", "Landroid/location/Location;", "nowMs", "", "offsetMs", "setDateTime", "tsMs", "setGeo", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes7.dex */
public final class ExifTools {
    public static final ExifTools INSTANCE = new ExifTools();
    private static final String[] COPY_TAGS = {ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL, ExifInterface.TAG_SOFTWARE, ExifInterface.TAG_LENS_MAKE, ExifInterface.TAG_LENS_MODEL, ExifInterface.TAG_LENS_SPECIFICATION, ExifInterface.TAG_FOCAL_LENGTH, ExifInterface.TAG_F_NUMBER, ExifInterface.TAG_EXPOSURE_TIME, ExifInterface.TAG_ISO_SPEED_RATINGS, ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, ExifInterface.TAG_WHITE_BALANCE, ExifInterface.TAG_METERING_MODE, ExifInterface.TAG_FLASH, ExifInterface.TAG_EXPOSURE_PROGRAM, ExifInterface.TAG_SCENE_CAPTURE_TYPE, ExifInterface.TAG_COLOR_SPACE, ExifInterface.TAG_ORIENTATION};

    private ExifTools() {
    }

    public final CameraSignature readSignature(ContentResolver cr, Uri uri) {
        Intrinsics.checkNotNullParameter(cr, "cr");
        Intrinsics.checkNotNullParameter(uri, "uri");
        LinkedHashMap map = new LinkedHashMap();
        InputStream open = open(cr, uri);
        try {
            InputStream ins = open;
            ExifInterface exif = new ExifInterface(ins);
            for (String t : COPY_TAGS) {
                String it = exif.getAttribute(t);
                if (it != null) {
                    Intrinsics.checkNotNull(it);
                    map.put(t, it);
                }
            }
            Unit unit = Unit.INSTANCE;
            CloseableKt.closeFinally(open, null);
            return new CameraSignature(map);
        } finally {
        }
    }

    public final void rewriteExif(File file, CameraSignature signature, Location loc, long nowMs, long offsetMs) {
        Map $this$forEach$iv;
        Intrinsics.checkNotNullParameter(file, "file");
        ExifInterface exif = new ExifInterface(file.getAbsolutePath());
        clearGeo(exif);
        clearDates(exif);
        if (signature != null && ($this$forEach$iv = signature.getTags()) != null) {
            for (Map.Entry element$iv : $this$forEach$iv.entrySet()) {
                String k = element$iv.getKey();
                String v = element$iv.getValue();
                exif.setAttribute(k, v);
            }
        }
        long ts = nowMs + offsetMs;
        setDateTime(exif, ts);
        if (loc != null) {
            INSTANCE.setGeo(exif, loc);
        }
        exif.saveAttributes();
    }

    private final InputStream open(ContentResolver cr, Uri uri) {
        InputStream openInputStream = cr.openInputStream(uri);
        if (openInputStream != null) {
            return openInputStream;
        }
        throw new IllegalArgumentException("Cannot open " + uri);
    }

    private final void clearGeo(ExifInterface exif) {
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, null);
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null);
        exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null);
        exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null);
        exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, null);
        exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, null);
        exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null);
        exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null);
        exif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, null);
    }

    private final void clearDates(ExifInterface exif) {
        exif.setAttribute(ExifInterface.TAG_DATETIME, null);
        exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, null);
        exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, null);
        exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME, null);
        exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIGINAL, null);
        exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME_DIGITIZED, null);
        exif.setAttribute(ExifInterface.TAG_OFFSET_TIME, null);
        exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL, null);
        exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_DIGITIZED, null);
    }

    private final void setDateTime(ExifInterface exif, long tsMs) {
        Calendar local = Calendar.getInstance();
        local.setTimeInMillis(tsMs);
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(tsMs);
        SimpleDateFormat fmtLocal = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US);
        SimpleDateFormat fmtUtcDate = new SimpleDateFormat("yyyy:MM:dd", Locale.US);
        fmtUtcDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat $this$setDateTime_u24lambda_u247 = new SimpleDateFormat("HH:mm:ss", Locale.US);
        $this$setDateTime_u24lambda_u247.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat fmtSub = new SimpleDateFormat("SSS", Locale.US);
        String localStr = fmtLocal.format(local.getTime());
        String sub = fmtSub.format(local.getTime());
        exif.setAttribute(ExifInterface.TAG_DATETIME, localStr);
        exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, localStr);
        exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, localStr);
        exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME, sub);
        exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIGINAL, sub);
        exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME_DIGITIZED, sub);
        TimeZone tz = local.getTimeZone();
        int offMin = tz.getOffset(tsMs) / 60000;
        String sign = offMin >= 0 ? "+" : "-";
        int hh = Math.abs(offMin) / 60;
        int mm = Math.abs(offMin) % 60;
        StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
        String offStr = String.format(Locale.US, "%s%02d:%02d", Arrays.copyOf(new Object[]{sign, Integer.valueOf(hh), Integer.valueOf(mm)}, 3));
        Intrinsics.checkNotNullExpressionValue(offStr, "format(...)");
        exif.setAttribute(ExifInterface.TAG_OFFSET_TIME, offStr);
        exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL, offStr);
        exif.setAttribute(ExifInterface.TAG_OFFSET_TIME_DIGITIZED, offStr);
        exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, fmtUtcDate.format(utc.getTime()));
        exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, $this$setDateTime_u24lambda_u247.format(utc.getTime()));
    }

    private final void setGeo(ExifInterface exif, Location loc) {
        String rational;
        exif.setLatLong(loc.getLatitude(), loc.getLongitude());
        if (loc.hasAltitude()) {
            double alt = loc.getAltitude();
            if ((Double.isInfinite(alt) || Double.isNaN(alt)) ? false : true) {
                double meters = Math.abs(alt);
                long numerator = (long) (1000 * meters);
                rational = numerator + "/1000";
            } else {
                rational = "0/1";
            }
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, rational);
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, alt >= 0.0d ? "0" : "1");
        }
        exif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, "NETWORK");
    }
}
