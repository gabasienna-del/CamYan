package org.telegraam.addon.ui;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.io.CloseableKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt;
import org.telegraam.addon.R;
import org.telegraam.addon.license.LicenseManager;
import org.telegraam.addon.security.ApkSignatureVerifier;
import org.telegraam.addon.util.CameraSignature;
import org.telegraam.addon.util.ExifTools;
import org.telegraam.addon.util.IOKt;

/* compiled from: MainActivity.kt */
@Metadata(d1 = {"\u0000\u0086\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 =2\u00020\u0001:\u0001=B\u0005¢\u0006\u0002\u0010\u0002J\b\u0010\u001b\u001a\u00020\u001cH\u0002J\b\u0010\u001d\u001a\u00020\u001cH\u0002J\n\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0002J\u0018\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020%H\u0002J\n\u0010&\u001a\u0004\u0018\u00010'H\u0002J\"\u0010(\u001a\u00020\u001c2\u0006\u0010)\u001a\u00020\u00142\u0006\u0010*\u001a\u00020\u00142\b\u0010+\u001a\u0004\u0018\u00010,H\u0014J\u0012\u0010-\u001a\u00020\u001c2\b\u0010.\u001a\u0004\u0018\u00010/H\u0014J\b\u00100\u001a\u00020\u001cH\u0014J\u0010\u00101\u001a\u00020\u001c2\u0006\u00102\u001a\u00020'H\u0002J\u0010\u00103\u001a\u00020\u001c2\u0006\u00104\u001a\u000205H\u0002J\u0010\u00106\u001a\u00020\u001c2\u0006\u00107\u001a\u000205H\u0002J\u0010\u00108\u001a\u00020\u001c2\u0006\u00109\u001a\u00020:H\u0002J\b\u0010;\u001a\u00020\u001cH\u0002J\b\u0010<\u001a\u00020\u001cH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.¢\u0006\u0002\n\u0000R\u0016\u0010\u0010\u001a\n \u0012*\u0004\u0018\u00010\u00110\u0011X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0014X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0014X\u0082D¢\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u000fX\u0082.¢\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u000fX\u0082.¢\u0006\u0002\n\u0000¨\u0006>"}, d2 = {"Lorg/telegraam/addon/ui/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "btnActivate", "Landroid/widget/Button;", "btnHeartbeat", "btnPick", "btnPickRef", "btnStart", "btnStop", "edtToken", "Landroid/widget/EditText;", "gson", "Lcom/google/gson/Gson;", "info", "Landroid/widget/TextView;", "net", "Ljava/util/concurrent/ExecutorService;", "kotlin.jvm.PlatformType", "pickReq", "", "pickReqRef", "preview", "Landroid/widget/ImageView;", "reqLocPerm", "txtLicense", "txtRef", "activateByToken", "", "ensureLocationPermission", "getLastBestLocation", "Landroid/location/Location;", "getName", "", "cr", "Landroid/content/ContentResolver;", "uri", "Landroid/net/Uri;", "loadSignature", "Lorg/telegraam/addon/util/CameraSignature;", "onActivityResult", "requestCode", "resultCode", "data", "Landroid/content/Intent;", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "saveSignature", "sig", "setEnabled", MainActivity.KEY_ENABLED, "", "setLicenseUiBusy", "busy", "showPreview", "file", "Ljava/io/File;", "updateLicenseUi", "updateRefUi", "Companion", "app_debug"}, k = 1, mv = {1, 9, 0}, xi = ConstraintLayout.LayoutParams.Table.LAYOUT_CONSTRAINT_VERTICAL_CHAINSTYLE)
/* loaded from: classes4.dex */
public final class MainActivity extends AppCompatActivity {
    public static final String ACTION_LH_STATE_CHANGED = "org.telegraam.addon.ACTION_STATE_CHANGED";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_SIGNATURE = "camera_signature_json";
    private static final String PREFS_EXIF = "exif_ref";
    private static final String PREFS_SWITCH = "lasthope";
    private Button btnActivate;
    private Button btnHeartbeat;
    private Button btnPick;
    private Button btnPickRef;
    private Button btnStart;
    private Button btnStop;
    private EditText edtToken;
    private TextView info;
    private ImageView preview;
    private TextView txtLicense;
    private TextView txtRef;
    private final int pickReqRef = 1000;
    private final int pickReq = 1001;
    private final int reqLocPerm = 900;
    private final ExecutorService net = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isDebug = (getApplicationInfo().flags & 2) != 0;
        if (!isDebug) {
            boolean valid = ApkSignatureVerifier.INSTANCE.isSignatureValid(this);
            if (!valid) {
                Toast.makeText(this, "Неверная подпись приложения", 1).show();
                finishAffinity();
                return;
            }
        }
        setContentView(R.layout.activity_main);
        View findViewById = findViewById(R.id.edtToken);
        Intrinsics.checkNotNullExpressionValue(findViewById, "findViewById(...)");
        this.edtToken = (EditText) findViewById;
        View findViewById2 = findViewById(R.id.btnActivate);
        Intrinsics.checkNotNullExpressionValue(findViewById2, "findViewById(...)");
        this.btnActivate = (Button) findViewById2;
        View findViewById3 = findViewById(R.id.txtLicense);
        Intrinsics.checkNotNullExpressionValue(findViewById3, "findViewById(...)");
        this.txtLicense = (TextView) findViewById3;
        View findViewById4 = findViewById(R.id.btnHeartbeat);
        Intrinsics.checkNotNullExpressionValue(findViewById4, "findViewById(...)");
        this.btnHeartbeat = (Button) findViewById4;
        View findViewById5 = findViewById(R.id.btnPickRef);
        Intrinsics.checkNotNullExpressionValue(findViewById5, "findViewById(...)");
        this.btnPickRef = (Button) findViewById5;
        View findViewById6 = findViewById(R.id.txtRef);
        Intrinsics.checkNotNullExpressionValue(findViewById6, "findViewById(...)");
        this.txtRef = (TextView) findViewById6;
        View findViewById7 = findViewById(R.id.btnPick);
        Intrinsics.checkNotNullExpressionValue(findViewById7, "findViewById(...)");
        this.btnPick = (Button) findViewById7;
        View findViewById8 = findViewById(R.id.btnStart);
        Intrinsics.checkNotNullExpressionValue(findViewById8, "findViewById(...)");
        this.btnStart = (Button) findViewById8;
        View findViewById9 = findViewById(R.id.btnStop);
        Intrinsics.checkNotNullExpressionValue(findViewById9, "findViewById(...)");
        this.btnStop = (Button) findViewById9;
        View findViewById10 = findViewById(R.id.preview);
        Intrinsics.checkNotNullExpressionValue(findViewById10, "findViewById(...)");
        this.preview = (ImageView) findViewById10;
        View findViewById11 = findViewById(R.id.info);
        Intrinsics.checkNotNullExpressionValue(findViewById11, "findViewById(...)");
        this.info = (TextView) findViewById11;
        Button button = this.btnActivate;
        Button button2 = null;
        if (button == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnActivate");
            button = null;
        }
        button.setOnClickListener(new View.OnClickListener() { // from class: org.telegraam.addon.ui.MainActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$0(MainActivity.this, view);
            }
        });
        Button button3 = this.btnHeartbeat;
        if (button3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnHeartbeat");
            button3 = null;
        }
        button3.setEnabled(false);
        Button button4 = this.btnHeartbeat;
        if (button4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnHeartbeat");
            button4 = null;
        }
        button4.setOnClickListener(new View.OnClickListener() { // from class: org.telegraam.addon.ui.MainActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$1(MainActivity.this, view);
            }
        });
        Button button5 = this.btnPickRef;
        if (button5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnPickRef");
            button5 = null;
        }
        button5.setOnClickListener(new View.OnClickListener() { // from class: org.telegraam.addon.ui.MainActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$3(MainActivity.this, view);
            }
        });
        Button button6 = this.btnPick;
        if (button6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnPick");
            button6 = null;
        }
        button6.setOnClickListener(new View.OnClickListener() { // from class: org.telegraam.addon.ui.MainActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$5(MainActivity.this, view);
            }
        });
        Button button7 = this.btnStart;
        if (button7 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnStart");
            button7 = null;
        }
        button7.setOnClickListener(new View.OnClickListener() { // from class: org.telegraam.addon.ui.MainActivity$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$8(MainActivity.this, view);
            }
        });
        Button button8 = this.btnStop;
        if (button8 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnStop");
        } else {
            button2 = button8;
        }
        button2.setOnClickListener(new View.OnClickListener() { // from class: org.telegraam.addon.ui.MainActivity$$ExternalSyntheticLambda6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.onCreate$lambda$9(MainActivity.this, view);
            }
        });
        File f = new File(getFilesDir(), "fake.jpg");
        if (f.exists()) {
            showPreview(f);
        }
        updateLicenseUi();
        updateRefUi();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$0(MainActivity this$0, View it) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        this$0.activateByToken();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$1(MainActivity this$0, View it) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        Toast.makeText(this$0, "Heartbeat отключён", 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$3(MainActivity this$0, View it) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        if (!LicenseManager.INSTANCE.isLicensed(this$0)) {
            Toast.makeText(this$0, "Нужно активировать лицензию", 0).show();
            return;
        }
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("image/*");
        this$0.startActivityForResult(intent, this$0.pickReqRef);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$5(MainActivity this$0, View it) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        if (!LicenseManager.INSTANCE.isLicensed(this$0)) {
            Toast.makeText(this$0, "Нужно активировать лицензию", 0).show();
            return;
        }
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("image/*");
        this$0.startActivityForResult(intent, this$0.pickReq);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$8(final MainActivity this$0, View it) {
        String obj;
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        EditText editText = this$0.edtToken;
        String str = null;
        if (editText == null) {
            Intrinsics.throwUninitializedPropertyAccessException("edtToken");
            editText = null;
        }
        Editable text = editText.getText();
        if (text != null && (obj = text.toString()) != null) {
            str = StringsKt.trim((CharSequence) obj).toString();
        }
        if (str == null) {
            str = "";
        }
        final String token = str;
        if (token.length() == 0) {
            Toast.makeText(this$0, "Введите токен для активации", 0).show();
        } else {
            this$0.setLicenseUiBusy(true);
            this$0.net.execute(new Runnable() { // from class: org.telegraam.addon.ui.MainActivity$$ExternalSyntheticLambda8
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.onCreate$lambda$8$lambda$7(MainActivity.this, token);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$8$lambda$7(final MainActivity this$0, String token) {
        final boolean ok;
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        Intrinsics.checkNotNullParameter(token, "$token");
        try {
            ok = LicenseManager.INSTANCE.init(this$0, token);
        } catch (Throwable th) {
            ok = false;
        }
        this$0.runOnUiThread(new Runnable() { // from class: org.telegraam.addon.ui.MainActivity$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.onCreate$lambda$8$lambda$7$lambda$6(MainActivity.this, ok);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$8$lambda$7$lambda$6(MainActivity this$0, boolean $ok) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        this$0.setLicenseUiBusy(false);
        if ($ok && LicenseManager.INSTANCE.isLicensed(this$0)) {
            Toast.makeText(this$0, "Лицензия активирована", 0).show();
            this$0.updateLicenseUi();
            this$0.setEnabled(true);
            TextView textView = this$0.info;
            if (textView == null) {
                Intrinsics.throwUninitializedPropertyAccessException("info");
                textView = null;
            }
            textView.setText("Подмена: ВКЛ");
            return;
        }
        this$0.setEnabled(false);
        Toast.makeText(this$0, "Неверный токен / сервер недоступен", 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void onCreate$lambda$9(MainActivity this$0, View it) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        this$0.setEnabled(false);
        TextView textView = this$0.info;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("info");
            textView = null;
        }
        textView.setText("Подмена: ВЫКЛ");
    }

    private final void activateByToken() {
        String obj;
        EditText editText = this.edtToken;
        String str = null;
        if (editText == null) {
            Intrinsics.throwUninitializedPropertyAccessException("edtToken");
            editText = null;
        }
        Editable text = editText.getText();
        if (text != null && (obj = text.toString()) != null) {
            str = StringsKt.trim((CharSequence) obj).toString();
        }
        if (str == null) {
            str = "";
        }
        final String token = str;
        if (token.length() == 0) {
            Toast.makeText(this, "Введите токен", 0).show();
        } else {
            setLicenseUiBusy(true);
            this.net.execute(new Runnable() { // from class: org.telegraam.addon.ui.MainActivity$$ExternalSyntheticLambda9
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.activateByToken$lambda$11(MainActivity.this, token);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void activateByToken$lambda$11(final MainActivity this$0, String token) {
        final boolean ok;
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        Intrinsics.checkNotNullParameter(token, "$token");
        try {
            ok = LicenseManager.INSTANCE.init(this$0, token);
        } catch (Throwable th) {
            ok = false;
        }
        this$0.runOnUiThread(new Runnable() { // from class: org.telegraam.addon.ui.MainActivity$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.activateByToken$lambda$11$lambda$10(MainActivity.this, ok);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void activateByToken$lambda$11$lambda$10(MainActivity this$0, boolean $ok) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        this$0.setLicenseUiBusy(false);
        if ($ok && LicenseManager.INSTANCE.isLicensed(this$0)) {
            Toast.makeText(this$0, "Лицензия активирована", 0).show();
            this$0.updateLicenseUi();
        } else {
            Toast.makeText(this$0, "Неверный токен / сервер недоступен", 1).show();
            this$0.setEnabled(false);
        }
    }

    private final void updateLicenseUi() {
        boolean hasToken = LicenseManager.INSTANCE.isLicensed(this);
        SharedPreferences prefs = getSharedPreferences("license", 0);
        long lastSeen = prefs.getLong("last_seen", 0L);
        String string = prefs.getString("token", "");
        String currentToken = string != null ? string : "";
        EditText editText = this.edtToken;
        TextView textView = null;
        if (editText == null) {
            Intrinsics.throwUninitializedPropertyAccessException("edtToken");
            editText = null;
        }
        Editable text = editText.getText();
        if (!Intrinsics.areEqual(text != null ? text.toString() : null, currentToken)) {
            if (currentToken.length() > 0) {
                EditText editText2 = this.edtToken;
                if (editText2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("edtToken");
                    editText2 = null;
                }
                editText2.setText(currentToken);
                EditText editText3 = this.edtToken;
                if (editText3 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("edtToken");
                    editText3 = null;
                }
                EditText editText4 = this.edtToken;
                if (editText4 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("edtToken");
                    editText4 = null;
                }
                Editable text2 = editText4.getText();
                editText3.setSelection(text2 != null ? text2.length() : 0);
            }
        }
        EditText editText5 = this.edtToken;
        if (editText5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("edtToken");
            editText5 = null;
        }
        editText5.setEnabled(true);
        Button button = this.btnActivate;
        if (button == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnActivate");
            button = null;
        }
        button.setEnabled(true);
        Button button2 = this.btnPickRef;
        if (button2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnPickRef");
            button2 = null;
        }
        button2.setEnabled(hasToken);
        Button button3 = this.btnPick;
        if (button3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnPick");
            button3 = null;
        }
        button3.setEnabled(hasToken);
        Button button4 = this.btnStart;
        if (button4 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnStart");
            button4 = null;
        }
        button4.setEnabled(true);
        Button button5 = this.btnStop;
        if (button5 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnStop");
            button5 = null;
        }
        button5.setEnabled(hasToken);
        Button button6 = this.btnHeartbeat;
        if (button6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnHeartbeat");
            button6 = null;
        }
        button6.setEnabled(false);
        TextView textView2 = this.txtLicense;
        if (textView2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("txtLicense");
        } else {
            textView = textView2;
        }
        StringBuilder $this$updateLicenseUi_u24lambda_u2412 = new StringBuilder();
        $this$updateLicenseUi_u24lambda_u2412.append(hasToken ? "Лицензия: активирована" : "Лицензия: нет");
        if (lastSeen > 0) {
            $this$updateLicenseUi_u24lambda_u2412.append("\nПоследняя проверка: ").append(DateFormat.format("yyyy-MM-dd HH:mm", lastSeen));
        }
        String sb = $this$updateLicenseUi_u24lambda_u2412.toString();
        Intrinsics.checkNotNullExpressionValue(sb, "toString(...)");
        textView.setText(sb);
    }

    private final void setLicenseUiBusy(boolean busy) {
        Button button = this.btnActivate;
        EditText editText = null;
        if (button == null) {
            Intrinsics.throwUninitializedPropertyAccessException("btnActivate");
            button = null;
        }
        button.setEnabled(!busy);
        EditText editText2 = this.edtToken;
        if (editText2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("edtToken");
        } else {
            editText = editText2;
        }
        editText.setEnabled(!busy);
    }

    private final void setEnabled(boolean enabled) {
        if (enabled && !LicenseManager.INSTANCE.isLicensed(this)) {
            Toast.makeText(this, "Нет активной лицензии", 0).show();
        } else {
            getSharedPreferences(PREFS_SWITCH, 0).edit().putBoolean(KEY_ENABLED, enabled).apply();
            sendBroadcast(new Intent(ACTION_LH_STATE_CHANGED));
        }
    }

    private final CameraSignature loadSignature() {
        SharedPreferences p = getSharedPreferences(PREFS_EXIF, 0);
        String json = p.getString(KEY_SIGNATURE, null);
        if (json == null) {
            return null;
        }
        try {
            return (CameraSignature) this.gson.fromJson(json, CameraSignature.class);
        } catch (Throwable th) {
            return null;
        }
    }

    private final void saveSignature(CameraSignature sig) {
        getSharedPreferences(PREFS_EXIF, 0).edit().putString(KEY_SIGNATURE, this.gson.toJson(sig)).apply();
        updateRefUi();
    }

    private final void updateRefUi() {
        CameraSignature s = loadSignature();
        TextView textView = this.txtRef;
        if (textView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("txtRef");
            textView = null;
        }
        textView.setText(s == null ? "Эталон камеры: не задан" : "Эталон камеры: загружен");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Type inference failed for: r0v7, types: [java.lang.Throwable] */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            return;
        }
        TextView textView = null;
        CameraSignature cameraSignature = null;
        Uri data2 = data != null ? data.getData() : null;
        if (data2 == null) {
            return;
        }
        Uri uri = data2;
        getContentResolver().takePersistableUriPermission(uri, 1);
        if (requestCode == this.pickReqRef) {
            try {
                ExifTools exifTools = ExifTools.INSTANCE;
                ContentResolver contentResolver = getContentResolver();
                Intrinsics.checkNotNullExpressionValue(contentResolver, "getContentResolver(...)");
                cameraSignature = exifTools.readSignature(contentResolver, uri);
            } catch (Throwable th) {
                Toast.makeText(this, "Не удалось прочитать EXIF эталона", 0).show();
            }
            CameraSignature cameraSignature2 = cameraSignature;
            if (cameraSignature2 != null) {
                saveSignature(cameraSignature2);
                Toast.makeText(this, "Эталон камеры сохранён", 0).show();
                return;
            }
            return;
        }
        if (requestCode == this.pickReq) {
            File file = new File(getFilesDir(), "fake.jpg");
            ContentResolver contentResolver2 = getContentResolver();
            Intrinsics.checkNotNullExpressionValue(contentResolver2, "getContentResolver(...)");
            IOKt.copyUriToFile(contentResolver2, uri, file);
            ensureLocationPermission();
            ExifTools.INSTANCE.rewriteExif(file, loadSignature(), getLastBestLocation(), System.currentTimeMillis(), 5000L);
            showPreview(file);
            TextView textView2 = this.info;
            if (textView2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("info");
            } else {
                textView = textView2;
            }
            ContentResolver contentResolver3 = getContentResolver();
            Intrinsics.checkNotNullExpressionValue(contentResolver3, "getContentResolver(...)");
            textView.setText("Фото выбрано: " + getName(contentResolver3, uri));
        }
    }

    private final void showPreview(File file) {
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        ImageView imageView = this.preview;
        if (imageView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("preview");
            imageView = null;
        }
        imageView.setImageBitmap(bmp);
    }

    private final String getName(ContentResolver cr, Uri uri) {
        Cursor query = cr.query(uri, new String[]{"_display_name"}, null, null, null);
        String str = null;
        if (query != null) {
            Cursor cursor = query;
            try {
                Cursor c = cursor;
                String string = c.moveToFirst() ? c.getString(0) : "image";
                CloseableKt.closeFinally(cursor, null);
                str = string;
            } catch (Throwable th) {
                try {
                    throw th;
                } catch (Throwable th2) {
                    CloseableKt.closeFinally(cursor, th);
                    throw th2;
                }
            }
        }
        return str == null ? "image" : str;
    }

    private final void ensureLocationPermission() {
        int fine = ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION");
        int coarse = ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION");
        if (fine != 0 && coarse != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"}, this.reqLocPerm);
        }
    }

    private final Location getLastBestLocation() {
        Object m181constructorimpl;
        Object m181constructorimpl2;
        Object systemService = getSystemService("location");
        Intrinsics.checkNotNull(systemService, "null cannot be cast to non-null type android.location.LocationManager");
        LocationManager lm = (LocationManager) systemService;
        boolean hasFine = ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0;
        boolean hasCoarse = ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0;
        if (!hasFine && !hasCoarse) {
            return null;
        }
        try {
            Result.Companion companion = Result.INSTANCE;
            MainActivity mainActivity = this;
            m181constructorimpl = Result.m181constructorimpl(lm.getLastKnownLocation("gps"));
        } catch (Throwable th) {
            Result.Companion companion2 = Result.INSTANCE;
            m181constructorimpl = Result.m181constructorimpl(ResultKt.createFailure(th));
        }
        if (Result.m187isFailureimpl(m181constructorimpl)) {
            m181constructorimpl = null;
        }
        Location gps = (Location) m181constructorimpl;
        try {
            Result.Companion companion3 = Result.INSTANCE;
            MainActivity mainActivity2 = this;
            m181constructorimpl2 = Result.m181constructorimpl(lm.getLastKnownLocation("network"));
        } catch (Throwable th2) {
            Result.Companion companion4 = Result.INSTANCE;
            m181constructorimpl2 = Result.m181constructorimpl(ResultKt.createFailure(th2));
        }
        Location net = (Location) (Result.m187isFailureimpl(m181constructorimpl2) ? null : m181constructorimpl2);
        return (gps == null || net == null ? gps != null : gps.getTime() >= net.getTime()) ? gps : net;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        this.net.shutdown();
    }
}
