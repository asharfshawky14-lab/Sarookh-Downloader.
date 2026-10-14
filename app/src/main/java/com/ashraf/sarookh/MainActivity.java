package com.ashraf.sarookh;

import android.app.Activity;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends Activity {

private EditText urlInput;
private TextView statusText;

private int dp(float value) {
    return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
}

private TextView makeText(String text, int size, int color) {
    TextView view = new TextView(this);
    view.setText(text);
    view.setTextSize(size);
    view.setTextColor(color);
    view.setGravity(Gravity.CENTER);
    view.setPadding(dp(8), dp(8), dp(8), dp(8));
    return view;
}

private Button makeButton(String text) {
    Button button = new Button(this);
    button.setText(text);
    button.setTextSize(16);
    button.setAllCaps(false);
    button.setTextColor(Color.WHITE);
    button.setBackgroundTintList(
        android.content.res.ColorStateList.valueOf(Color.rgb(35, 117, 235))
    );
    return button;
}

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getWindow().setStatusBarColor(Color.rgb(12, 19, 35));
    getWindow().setNavigationBarColor(Color.rgb(12, 19, 35));

    ScrollView scroll = new ScrollView(this);
    scroll.setFillViewport(true);
    scroll.setBackgroundColor(Color.rgb(12, 19, 35));

    LinearLayout page = new LinearLayout(this);
    page.setOrientation(LinearLayout.VERTICAL);
    page.setGravity(Gravity.CENTER_HORIZONTAL);
    page.setPadding(dp(22), dp(24), dp(22), dp(24));
    scroll.addView(page);

    TextView logo = makeText("🚀", 54, Color.WHITE);
    page.addView(logo, new LinearLayout.LayoutParams(
        -1, dp(85)
    ));

    TextView title = makeText(
        "صاروخ التحميل", 28, Color.WHITE
    );
    title.setTypeface(null, Typeface.BOLD);
    page.addView(title);

    TextView subtitle = makeText(
        "حمّل ملفاتك مباشرة إلى هاتفك",
        15, Color.LTGRAY
    );
    page.addView(subtitle);

    TextView label = makeText(
        "ضع رابط الملف هنا",
        16, Color.WHITE
    );
    label.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
    label.setPadding(0, dp(22), 0, dp(8));
    page.addView(label);

    urlInput = new EditText(this);
    urlInput.setSingleLine(false);
    urlInput.setMinLines(2);
    urlInput.setMaxLines(3);
    urlInput.setTextSize(15);
    urlInput.setTextColor(Color.WHITE);
    urlInput.setHintTextColor(Color.LTGRAY);
    urlInput.setHint("https://example.com/file.mp4");
    urlInput.setPadding(dp(14), dp(12), dp(14), dp(12));
    urlInput.setGravity(Gravity.TOP | Gravity.LEFT);
    urlInput.setBackgroundTintList(
        android.content.res.ColorStateList.valueOf(
            Color.rgb(90, 120, 165)
        )
    );

    page.addView(urlInput, new LinearLayout.LayoutParams(
        -1, dp(105)
    ));

    Button pasteButton = makeButton("📋 لصق الرابط");
    LinearLayout.LayoutParams pasteParams =
        new LinearLayout.LayoutParams(-1, dp(52));
    pasteParams.topMargin = dp(12);
    page.addView(pasteButton, pasteParams);

    pasteButton.setOnClickListener(v -> {
        ClipboardManager clipboard =
            (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).coerceToText(this);
                if (text != null) {
                    urlInput.setText(text.toString());
                    urlInput.setSelection(urlInput.length());
                }
            }
        } else {
            Toast.makeText(this, "الحافظة فارغة", Toast.LENGTH_SHORT).show();
        }
    });

    Button downloadButton = makeButton("⬇️ ابدأ التحميل");
    LinearLayout.LayoutParams downloadParams =
        new LinearLayout.LayoutParams(-1, dp(56));
    downloadParams.topMargin = dp(12);
    page.addView(downloadButton, downloadParams);

    statusText = makeText(
        "جاهز للتحميل", 14, Color.rgb(120, 220, 170)
    );
    statusText.setPadding(dp(8), dp(18), dp(8), dp(18));
    page.addView(statusText);

    TextView note = makeText(
        "ملاحظة: هذه النسخة تدعم روابط الملفات المباشرة فقط، "
        + "وليست روابط صفحات الفيديو من مواقع التواصل.",
        13, Color.LTGRAY
    );
    page.addView(note);

    downloadButton.setOnClickListener(v -> startDownload());

    setContentView(scroll);
}

private void startDownload() {
    String link = urlInput.getText().toString().trim();

    if (link.isEmpty()) {
        urlInput.setError("اكتب رابط الملف أولاً");
        return;
    }

    Uri uri;

    try {
        uri = Uri.parse(link);
    } catch (Exception e) {
        urlInput.setError("الرابط غير صحيح");
        return;
    }

    String scheme = uri.getScheme();

    if (scheme == null ||
        !(scheme.equalsIgnoreCase("https") ||
          scheme.equalsIgnoreCase("http")) ||
        uri.getHost() == null) {
        urlInput.setError("استخدم رابط HTTP أو HTTPS صحيح");
        return;
    }

    try {
        InputMethodManager imm =
            (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
        }

        String filename = uri.getLastPathSegment();

        if (filename == null || filename.trim().isEmpty()) {
            filename = "sarookh_download_" + System.currentTimeMillis();
        }

        try {
            filename = URLDecoder.decode(
                filename, StandardCharsets.UTF_8.name()
            );
        } catch (Exception ignored) {
        }

        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

        if (filename.isEmpty() || filename.equals(".") || filename.equals("..")) {
            filename = "sarookh_download_" + System.currentTimeMillis();
        }

        DownloadManager.Request request =
            new DownloadManager.Request(uri);

        request.setTitle(filename);
        request.setDescription("صاروخ التحميل 🚀");
        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        );
        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS, filename
        );

        DownloadManager manager =
            (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        if (manager == null) {
            statusText.setText("خدمة التحميل غير متاحة");
            return;
        }

        manager.enqueue(request);
        statusText.setText(
            "بدأ التحميل! تابع تقدمه من إشعارات الهاتف."
        );

        Toast.makeText(
            this,
            "تم إرسال الملف إلى مدير التحميلات",
            Toast.LENGTH_LONG
        ).show();

    } catch (Exception e) {
        statusText.setText("تعذر بدء التحميل. راجع الرابط وحاول مجددًا.");
        Toast.makeText(
            this, "حدث خطأ أثناء بدء التحميل",
            Toast.LENGTH_LONG
        ).show();
    }
}

}
