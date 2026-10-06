package com.ztune.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private static final int REQ_FOLDER = 101;
    private static final int BG = Color.parseColor("#0E1120");
    private static final int CARD = Color.parseColor("#1B2038");
    private static final int PINK = Color.parseColor("#FF5C7A");
    private static final int BLUE = Color.parseColor("#5C8DFF");
    private static final int TEXT = Color.parseColor("#F2F4FF");
    private static final int MUTED = Color.parseColor("#9AA3C7");

    private Uri treeUri;
    private TextView folderText;
    private TextView confPreview;
    private RadioButton ram8, ram12, dxvk1, dxvk2;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            buildUi();
        } catch (Throwable t) {
            TextView e = new TextView(this);
            e.setTextSize(12);
            e.setTextColor(Color.BLACK);
            e.setPadding(24, 48, 24, 24);
            e.setText(android.util.Log.getStackTraceString(t));
            ScrollView s = new ScrollView(this);
            s.setBackgroundColor(Color.WHITE);
            s.addView(e);
            setContentView(s);
        }
    }

    private void buildUi() {
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(36), dp(16), dp(32));
        sv.addView(root);

        root.addView(text("Z-Tune", 30, PINK, true));
        root.addView(text("ตัวช่วยตั้งค่า Dead Rising 3 บน GameHub", 14, MUTED, false));

        // การ์ด 1: เลือกโฟลเดอร์
        LinearLayout c1 = card(root);
        c1.addView(text("1. เลือกโฟลเดอร์เกม", 18, TEXT, true));
        c1.addView(text("เลือกโฟลเดอร์ที่มีไฟล์ .exe ของ Dead Rising 3", 13, MUTED, false));
        folderText = text("ยังไม่ได้เลือก", 13, BLUE, false);
        c1.addView(folderText);
        c1.addView(button("เลือกโฟลเดอร์", BLUE, new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(i, REQ_FOLDER);
            }
        }));

        // การ์ด 2: dxvk.conf
        View.OnClickListener refresh = new View.OnClickListener() {
            public void onClick(View v) { confPreview.setText(buildConf()); }
        };
        LinearLayout c2 = card(root);
        c2.addView(text("2. ตั้งค่า DXVK", 18, TEXT, true));
        c2.addView(text("แรมของเครื่อง", 13, MUTED, false));
        RadioGroup g1 = new RadioGroup(this);
        ram8 = radio("แรม 6–8GB", refresh);
        ram12 = radio("แรม 12GB ขึ้นไป", refresh);
        g1.addView(ram8);
        g1.addView(ram12);
        c2.addView(g1);
        ram8.setChecked(true);
        c2.addView(text("DXVK ที่เลือกใน GameHub", 13, MUTED, false));
        RadioGroup g2 = new RadioGroup(this);
        dxvk1 = radio("dxvk-1.10.3-async", refresh);
        dxvk2 = radio("2.x gplasync", refresh);
        g2.addView(dxvk1);
        g2.addView(dxvk2);
        c2.addView(g2);
        dxvk1.setChecked(true);
        confPreview = text(buildConf(), 12, TEXT, false);
        confPreview.setTypeface(android.graphics.Typeface.MONOSPACE);
        c2.addView(confPreview);
        c2.addView(button("เขียน dxvk.conf", PINK, new View.OnClickListener() {
            public void onClick(View v) { saveConf(); }
        }));
        c2.addView(text("ไฟล์นี้ช่วยลดอาการกระตุกจากการคอมไพล์ shader เป็นหลัก ไม่ได้เพิ่ม fps โดยตรง ไฟล์เดิมจะถูกสำรองเป็น .bak", 12, MUTED, false));

        // การ์ด 3: เช็กลิสต์ GameHub
        LinearLayout c3 = card(root);
        c3.addView(text("3. ตั้งใน GameHub (สำคัญที่สุด)", 18, TEXT, true));
        c3.addView(text(
                "• Compatibility layer: proton11.0-arm64x\n"
              + "• CPU translator: Built-in\n"
              + "• Translation parameters: Stable\n"
              + "• เปิด Skip audio/video decoding ถ้าเมนู/คัตซีนกระตุก\n"
              + "• ล็อกเฟรมที่ 30\n"
              + "• ความละเอียดเรนเดอร์ประมาณ 720p\n"
              + "• VRAM limit ให้ตรงกับ dxvk.conf (1536MB สำหรับแรม 8GB)\n"
              + "• ปิดแอปอื่นก่อนเข้าเกม", 14, TEXT, false));

        // การ์ด 4: rendersettings (ทดลอง)
        LinearLayout c4 = card(root);
        c4.addView(text("4. rendersettings.ini (ทดลอง)", 18, TEXT, true));
        c4.addView(text("แก้เฉพาะบรรทัดเหล่านี้ในไฟล์เดิมที่ Documents\\My Games\\Dead Rising 3\\ ห้ามวางทับทั้งไฟล์ และสำรองไฟล์เดิมก่อน ยังไม่ยืนยันว่าได้ผลกับทุกเครื่อง", 12, MUTED, false));
        TextView ini = text(buildIni(), 12, TEXT, false);
        ini.setTypeface(android.graphics.Typeface.MONOSPACE);
        c4.addView(ini);
        c4.addView(button("คัดลอกบรรทัดเหล่านี้", BLUE, new View.OnClickListener() {
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("ini", buildIni()));
                toast("คัดลอกแล้ว");
            }
        }));

        root.addView(text("ผลลัพธ์ต่างกันตามเครื่อง ไม่การันตีว่าจะดีขึ้นเท่ากันทุกเครื่อง", 12, MUTED, false));
        setContentView(sv);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_FOLDER && res == RESULT_OK && data != null && data.getData() != null) {
            treeUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (Exception e) { }
            folderText.setText(treeUri.getLastPathSegment());
        }
    }

    private String buildConf() {
        boolean big = ram12 != null && ram12.isChecked();
        boolean v2 = dxvk2 != null && dxvk2.isChecked();
        StringBuilder sb = new StringBuilder();
        sb.append("dxvk.enableAsync = true\n");
        if (v2) sb.append("dxvk.gplAsyncCache = true\n");
        sb.append("dxvk.numAsyncThreads = 2\n");
        sb.append("dxvk.numCompilerThreads = 2\n");
        sb.append("dxgi.maxDeviceMemory = ").append(big ? 2048 : 1536).append("\n");
        sb.append("dxgi.maxSharedMemory = ").append(big ? 1536 : 1024).append("\n");
        sb.append("d3d11.maxTessFactor = 8\n");
        if (v2) sb.append("d3d11.relaxedBarriers = True\n");
        return sb.toString();
    }

    private String buildIni() {
        return "ScreenWidth = 1280\n"
             + "ScreenHeight = 720\n"
             + "WindowWidth = 1280\n"
             + "WindowHeight = 720\n"
             + "RefreshNumerator = 60000\n"
             + "RefreshDenominator = 1000\n"
             + "GAME_RESOLUTION = GAME_RESOLUTION_720P\n";
    }

    private void saveConf() {
        if (treeUri == null) { toast("เลือกโฟลเดอร์เกมก่อน"); return; }
        toast(writeFile("dxvk.conf", buildConf()) ? "เขียน dxvk.conf แล้ว" : "เขียนไม่สำเร็จ ลองเลือกโฟลเดอร์ใหม่");
    }

    private Uri findChild(String name) {
        String docId = DocumentsContract.getTreeDocumentId(treeUri);
        Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId);
        Cursor c = null;
        try {
            c = getContentResolver().query(children, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null);
            while (c != null && c.moveToNext()) {
                if (name.equals(c.getString(1))) {
                    return DocumentsContract.buildDocumentUriUsingTree(treeUri, c.getString(0));
                }
            }
        } catch (Exception e) {
        } finally {
            if (c != null) c.close();
        }
        return null;
    }

    private boolean writeFile(String name, String content) {
        try {
            String docId = DocumentsContract.getTreeDocumentId(treeUri);
            Uri dir = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId);
            Uri target = findChild(name);
            if (target != null && findChild(name + ".bak") == null) {
                try {
                    Uri renamed = DocumentsContract.renameDocument(getContentResolver(), target, name + ".bak");
                    if (renamed != null) target = null;
                } catch (Exception e) { }
            }
            if (target == null) {
                target = DocumentsContract.createDocument(getContentResolver(), dir,
                        "application/octet-stream", name);
            }
            if (target == null) return false;
            OutputStream os = getContentResolver().openOutputStream(target, "wt");
            if (os == null) return false;
            os.write(content.getBytes("UTF-8"));
            os.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- UI helpers ----------
    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(4), 0, dp(4));
        return t;
    }

    private LinearLayout card(LinearLayout parent) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable g = new GradientDrawable();
        g.setColor(CARD);
        g.setCornerRadius(dp(18));
        c.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(14), 0, 0);
        parent.addView(c, lp);
        return c;
    }

    private Button button(String label, int color, View.OnClickListener l) {
        Button bt = new Button(this);
        bt.setText(label);
        bt.setAllCaps(false);
        bt.setTextColor(Color.WHITE);
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(14));
        bt.setBackground(new RippleDrawable(ColorStateList.valueOf(0x66FFFFFF), g, null));
        bt.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        lp.setMargins(0, dp(10), 0, dp(4));
        bt.setLayoutParams(lp);
        return bt;
    }

    private RadioButton radio(String s, View.OnClickListener l) {
        RadioButton r = new RadioButton(this);
        r.setText(s);
        r.setTextColor(TEXT);
        r.setOnClickListener(l);
        return r;
    }
}
