package org.telegraam.addon.util;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Map;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ExifTools.kt */
@Metadata(d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0019\u0012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\u0002\u0010\u0005J\u0015\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0003J\u001f\u0010\t\u001a\u00020\u00002\u0014\b\u0002\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u0003HÆ\u0001J\u0013\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\u0001HÖ\u0003J\t\u0010\r\u001a\u00020\u000eHÖ\u0001J\t\u0010\u000f\u001a\u00020\u0004HÖ\u0001R\u001d\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u0003¢\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007¨\u0006\u0010"}, d2 = {"Lorg/telegraam/addon/util/CameraSignature;", "", "tags", "", "", "(Ljava/util/Map;)V", "getTags", "()Ljava/util/Map;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes7.dex */
public final /* data */ class CameraSignature {
    private final Map<String, String> tags;

    /* JADX WARN: Multi-variable type inference failed */
    public static /* synthetic */ CameraSignature copy$default(CameraSignature cameraSignature, Map map, int i, Object obj) {
        if ((i & 1) != 0) {
            map = cameraSignature.tags;
        }
        return cameraSignature.copy(map);
    }

    public final Map<String, String> component1() {
        return this.tags;
    }

    public final CameraSignature copy(Map<String, String> tags) {
        Intrinsics.checkNotNullParameter(tags, "tags");
        return new CameraSignature(tags);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return (other instanceof CameraSignature) && Intrinsics.areEqual(this.tags, ((CameraSignature) other).tags);
    }

    public int hashCode() {
        return this.tags.hashCode();
    }

    public String toString() {
        return "CameraSignature(tags=" + this.tags + ")";
    }

    public CameraSignature(Map<String, String> tags) {
        Intrinsics.checkNotNullParameter(tags, "tags");
        this.tags = tags;
    }

    public final Map<String, String> getTags() {
        return this.tags;
    }
}
