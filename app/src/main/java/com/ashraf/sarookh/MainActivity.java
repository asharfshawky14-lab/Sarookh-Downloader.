
package com.ashraf.sarookh;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.*;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.Locale;

import dev.ffmpegkit_maintained.ytdlp.YtDlp;
import dev.ffmpegkit_maintained.ytdlp.YtDlpRequest;

public class MainActivity extends Activity {

    private final int BG = Color.rgb(10, 16, 32);
    private final int PANEL = Color.rgb(22, 31, 51);
    private final int BLUE = Color.rgb(48, 126, 255);
    private final int WHITE = Color.rgb(245, 247, 255);
    private final int MUTED = Color.rgb(164, 178, 202);
    private final int GREEN = Color.rgb(52, 211, 153);

    private LinearLayout root, content;
    private EditText urlInput;
    private Spinner quality, mode;
    private ProgressBar progressBar;
    private TextView progressText, status;
    private Button downloadButton;

    private boolean ready = false;
    private boolean downloading = false;

    private final Handler handler =
        new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setContentView(root);

        buildScreen();

        new Thread(() -> {
            try {
                YtDlp.init(getApplicationContext());
                ready = true;
                handler.post(() ->
                    setStatus("محرك التحميل جاهز ✓", GREEN));
            } catch (Exception e) {
                handler.post(() ->
                    setStatus("فشل تجهيز المحرك", Color.RED));
            }
        }).start();
    }

    private int dp(float n) {
        return (int) (n *
            getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return l;
    }

    private TextView text(String s, int size, int color,
                          boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT,
            bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private LinearLayout card() {
        LinearLayout c = column();
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        c.setBackgroundColor(PANEL);
        return c;
    }

    private Button button(String title, int color) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextColor(WHITE);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setMinHeight(dp(50));
        b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(color));
        return b;
    }

    private void gap(LinearLayout l, int h) {
        View v = new View(this);
        l.addView(v, new LinearLayout.LayoutParams(1, dp(h)));
    }

    private void buildScreen() {
        root.removeAllViews();

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(16), dp(18), dp(16));

        header.addView(text("🚀", 30, WHITE, true));

        LinearLayout brand = column();
        brand.addView(text("صاروخ التحميل", 21, WHITE, true));
        brand.addView(text("حمّل ملفاتك بسهولة", 12, MUTED, false));

        LinearLayout.LayoutParams bp =
            new LinearLayout.LayoutParams(0, -2, 1);
        bp.setMarginStart(dp(12));
        header.addView(brand, bp);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        content = column();
        content.setPadding(dp(18), dp(10), dp(18), dp(24));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        content.addView(text("أهلاً بيك 👋", 25, WHITE, true));
        gap(content, 5);
        content.addView(text(
            "الصق رابط الفيديو وابدأ التنزيل",
            14, MUTED, false));
        gap(content, 18);

        LinearLayout c = card();

        c.addView(text("رابط الفيديو", 16, WHITE, true));
        gap(c, 10);

        urlInput = new EditText(this);
        urlInput.setHint("https://...");
        urlInput.setTextColor(WHITE);
        urlInput.setHintTextColor(MUTED);
        urlInput.setTextSize(14);
        urlInput.setPadding(dp(12), dp(12), dp(12), dp(12));
        urlInput.setMinLines(2);
        urlInput.setMaxLines(3);
        urlInput.setGravity(Gravity.TOP | Gravity.RIGHT);
        urlInput.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(BLUE));
        c.addView(urlInput,
            new LinearLayout.LayoutParams(-1, dp(85)));

        gap(c, 10);

        Button paste = button("📋 لصق الرابط",
            Color.rgb(43, 58, 82));
        paste.setOnClickListener(v -> pasteLink());
        c.addView(paste);

        gap(c, 16);
        c.addView(text("جودة الفيديو", 15, WHITE, true));
        gap(c, 5);

        quality = new Spinner(this);
        quality.setAdapter(new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_dropdown_item,
            new String[] {
                "أفضل جودة متاحة",
                "حتى 1080p",
                "حتى 720p",
                "حتى 480p",
                "حتى 360p"
            }));
        c.addView(quality);

        gap(c, 14);
        c.addView(text("نوع التنزيل", 15, WHITE, true));
        gap(c, 5);

        mode = new Spinner(this);
        mode.setAdapter(new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_dropdown_item,
            new String[] { "فيديو", "صوت فقط" }));
        c.addView(mode);

        gap(c, 16);

        downloadButton = button("⬇ ابدأ التحميل", BLUE);
        downloadButton.setOnClickListener(v -> startDownload());
        c.addView(downloadButton);

        gap(c, 14);
        progressBar = new ProgressBar(this, null,
            android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        c.addView(progressBar,
            new LinearLayout.LayoutParams(-1, dp(8)));

        gap(c, 8);
        progressText = text("0%", 13, WHITE, true);
        c.addView(progressText);

        gap(c, 8);
        status = text("جاري تجهيز المحرك...", 12, MUTED, false);
        c.addView(status);

        content.addView(c);

        gap(content, 16);
        addFeature("🎬", "تنزيل الفيديو",
            "اختر حد الجودة المناسب.");
        addFeature("🎧", "تنزيل الصوت",
            "يحفظ المسار الصوتي المتاح دون تحويل مضمون إلى MP3.");
        addFeature("📁", "حفظ الملفات",
            "يتم الحفظ في مجلد خاص بالتطبيق.");

        gap(content, 12);
        content.addView(text(
            "قد لا تعمل بعض الروابط إذا كان الموقع يمنع التنزيل "
                + "أو يتطلب تسجيل الدخول.",
            12, MUTED, false));

        LinearLayout footer = new LinearLayout(this);
        footer.setPadding(dp(12), dp(10), dp(12), dp(10));
        footer.setBackgroundColor(PANEL);

        Button home = button("🚀 صاروخ التحميل", BLUE);
        home.setOnClickListener(v ->
            Toast.makeText(this, "أنت في الصفحة الرئيسية",
                Toast.LENGTH_SHORT).show());

        footer.addView(home,
            new LinearLayout.LayoutParams(-1, dp(48)));
        root.addView(footer);
    }

    private void addFeature(String icon, String title, String desc) {
        LinearLayout c = card();
        c.addView(text(icon + "  " + title, 16, WHITE, true));
        gap(c, 5);
        c.addView(text(desc, 12, MUTED, false));
        gap(content, 9);
        content.addView(c);
    }

    private void pasteLink() {
        try {
            ClipboardManager cm = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

            if (cm != null && cm.hasPrimaryClip()) {
                ClipData clip = cm.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    urlInput.setText(
                        clip.getItemAt(0).coerceToText(this));
                    return;
                }
            }
            toast("الحافظة فارغة");
        } catch (Exception e) {
            toast("تعذر لصق الرابط");
        }
    }

    private void startDownload() {
        if (downloading) {
            toast("يوجد تنزيل جارٍ بالفعل");
            return;
        }

        if (!ready) {
            toast("انتظر حتى يصبح محرك التحميل جاهزًا");
            return;
        }

        String url = urlInput.getText().toString().trim();
        Uri uri = Uri.parse(url);

        if (uri.getHost() == null ||
            !("https".equalsIgnoreCase(uri.getScheme()) ||
              "http".equalsIgnoreCase(uri.getScheme()))) {
            toast("أدخل رابط HTTP أو HTTPS صحيحًا");
            return;
        }

        File dir = getExternalFilesDir("downloads");
        if (dir == null || (!dir.exists() && !dir.mkdirs())) {
            toast("تعذر تجهيز مجلد التنزيلات");
            return;
        }

        String output = new File(dir,
            "%(title).150B.%(ext)s").getAbsolutePath();

        YtDlpRequest request = new YtDlpRequest(url)
            .setOutputTemplate(output);

        boolean audioOnly = mode.getSelectedItemPosition() == 1;

        if (audioOnly) {
            request.addOption("-f", "bestaudio/best");
        } else {
            String format;
            switch (quality.getSelectedItemPosition()) {
                case 1:
                    format = "best[height<=1080]/best";
                    break;
                case 2:
                    format = "best[height<=720]/best";
                    break;
                case 3:
                    format = "best[height<=480]/best";
                    break;
                case 4:
                    format = "best[height<=360]/best";
                    break;
                default:
                    format = "best";
            }
            request.addOption("-f", format);
        }

        request.addOption("--no-playlist");
        request.addOption("--newline");

        downloading = true;
        downloadButton.setEnabled(false);
        downloadButton.setText("⏳ جاري التنزيل");
        progressBar.setProgress(0);
        progressText.setText("0%");
        setStatus("بدأ تجهيز التنزيل...", BLUE);

        try {
            YtDlp.executeAsync(request, (p, eta, line) -> {
                handler.post(() -> {
                    int percent = Math.max(0, Math.min(100, p));
                    progressBar.setProgress(percent);
                    progressText.setText(String.format(
                        Locale.US, "%d%%", percent));

                    if (line != null && !line.trim().isEmpty()) {
                        setStatus(line, MUTED);
                    }
                });
            });

            urlInput.clearFocus();

        } catch (Exception e) {
            finishDownload("تعذر بدء التنزيل", Color.RED);
        }
    }

    private void finishDownload(String message, int color) {
        downloading = false;
        downloadButton.setEnabled(true);
        downloadButton.setText("⬇ ابدأ التحميل");
        setStatus(message, color);
    }

    private void setStatus(String message, int color) {
        if (status != null) {
            status.setText(message);
            status.setTextColor(color);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
