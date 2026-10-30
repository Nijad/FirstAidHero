package com.svu.firstaidhero.ui.treated;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.data.FirstAidRepository;
import com.svu.firstaidhero.model.TreatedCase;
import com.svu.firstaidhero.util.DateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class AddTreatedCaseActivity extends AppCompatActivity {

    private static final String TAG = "AddTreatedCase";
    public static final String EXTRA_ID = "extra_treated_id";

    private FirstAidRepository repo;

    private ImageView ivPhoto;
    private MaterialButton btnPickPhoto, btnRemovePhoto, btnSave, btnCancel;
    private TextInputEditText etDescription, etDate, etProcedures;
    private TextView tvTitle;

    private int editingId = -1;      // -1 = إضافة جديدة
    private String currentPhotoPath; // مسار الصورة الحالية
    private File cameraPhotoFile;    // ملف الكاميرا المؤقت

    // ====== مشغلات النتائج (Activity Result Launchers) ======

    // 1. اختيار صورة من المعرض
    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    String saved = copyImageToInternalStorage(uri);
                    if (saved != null) {
                        currentPhotoPath = saved;
                        ivPhoto.setImageTintList(null);
                        ivPhoto.setImageBitmap(BitmapFactory.decodeFile(saved));
                        btnRemovePhoto.setVisibility(View.VISIBLE);
                        btnPickPhoto.setText(R.string.change_image);
                    } else {
                        Toast.makeText(this, "فشل حفظ الصورة", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // 2. التقاط صورة بالكاميرا
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraPhotoFile != null && cameraPhotoFile.exists()) {
                    currentPhotoPath = cameraPhotoFile.getAbsolutePath();
                    ivPhoto.setImageTintList(null);
                    ivPhoto.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
                    btnRemovePhoto.setVisibility(View.VISIBLE);
                    btnPickPhoto.setText(R.string.change_image);
                } else {
                    Toast.makeText(this, "لم يتم التقاط الصورة", Toast.LENGTH_SHORT).show();
                }
            });

    // 3. طلب إذن الكاميرا
    private final ActivityResultLauncher<String> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "يجب السماح بالوصول للكاميرا لالتقاط الصور", Toast.LENGTH_LONG).show();
                }
            });

    // ====== دورة حياة النشاط (Lifecycle) ======

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_treated_case);

        repo = FirstAidRepository.getInstance(this);

        // ربط العناصر
        ivPhoto = findViewById(R.id.ivPhoto);
        btnPickPhoto = findViewById(R.id.btnPickPhoto);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        etProcedures = findViewById(R.id.etProcedures);
        tvTitle = findViewById(R.id.tvTitle);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // التاريخ الافتراضي = اليوم
        etDate.setText(DateUtils.today());

        // إعداد منتقي التاريخ
        etDate.setOnClickListener(v -> showDatePicker());

        // إعداد زر إضافة الصورة (يظهر خيار الكاميرا أو المعرض)
        btnPickPhoto.setOnClickListener(v -> {
            String[] options = {"التقاط صورة بالكاميرا", "اختيار من المعرض"};
            new AlertDialog.Builder(this)
                    .setTitle("إضافة صورة")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // التحقق من إذن الكاميرا
                            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                openCamera();
                            } else {
                                requestCameraPermission.launch(android.Manifest.permission.CAMERA);
                            }
                        } else {
                            pickImage.launch("image/*");
                        }
                    }).show();
        });

        // إزالة الصورة
        btnRemovePhoto.setOnClickListener(v -> {
            currentPhotoPath = null;
            // إعادة تلوين الأيقونة الرمادية
            ivPhoto.setImageTintList(android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary)));
            ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
            btnPickPhoto.setText(R.string.add_image);
            btnRemovePhoto.setVisibility(View.GONE);
        });

        // حفظ وإلغاء
        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> finish());

        // التحقق من وضع التعديل
        editingId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (editingId != -1) {
            loadExisting(editingId);
        }
    }

    // ====== الوظائف المساعدة ======

    private void loadExisting(int id) {
        TreatedCase t = repo.treated().getById(id);
        if (t == null) { finish(); return; }

        tvTitle.setText(R.string.edit_treated_title);
        etDescription.setText(t.getDescription());
        etDate.setText(t.getDate());
        etProcedures.setText(t.getProcedures());

        if (t.getPhotoPath() != null && new File(t.getPhotoPath()).exists()) {
            currentPhotoPath = t.getPhotoPath();
            ivPhoto.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
            btnPickPhoto.setText(R.string.change_image);
            btnRemovePhoto.setVisibility(View.VISIBLE);
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            etDate.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String desc = text(etDescription);
        String date = text(etDate);
        String proc = text(etProcedures);

        // ===== Validation =====
        if (desc.isEmpty()) {
            etDescription.setError(getString(R.string.description_required));
            etDescription.requestFocus();
            return;
        }
        if (desc.length() > 1000) {
            etDescription.setError("الوصف طويل جداً (1000 حرف كحد أقصى)");
            return;
        }
        if (proc.isEmpty()) {
            etProcedures.setError(getString(R.string.procedures_required));
            etProcedures.requestFocus();
            return;
        }
        if (proc.length() > 2000) {
            etProcedures.setError("الإجراءات طويلة جداً (2000 حرف كحد أقصى)");
            return;
        }
        if (!isValidDate(date)) {
            etDate.setError("تاريخ غير صالح");
            return;
        }

        try {
            if (editingId == -1) {
                TreatedCase t = new TreatedCase(0, null, desc, date, currentPhotoPath, proc);
                repo.treated().insert(t);
            } else {
                TreatedCase t = new TreatedCase(editingId, null, desc, date, currentPhotoPath, proc);
                repo.treated().update(t);
            }
            Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Save failed", e);
            Toast.makeText(this, "فشل الحفظ: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private boolean isValidDate(String date) {
        if (date == null || date.length() != 10) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ====== إدارة الصور (الكاميرا والمعرض) ======

    private void openCamera() {
        try {
            File dir = new File(getFilesDir(), "treated_photos");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e(TAG, "Failed to create directory");
                    return;
                }
            }

            cameraPhotoFile = new File(dir, "cam_" + UUID.randomUUID() + ".jpg");

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    cameraPhotoFile
            );

            takePictureLauncher.launch(uri);

        } catch (Exception e) {
            Log.e(TAG, "Error opening camera", e);
            Toast.makeText(this, "فشل فتح الكاميرا", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * نسخ الصورة المختارة من المعرض مع ضغطها لتجنّب OOM.
     */
    private String copyImageToInternalStorage(Uri src) {
        try {
            File dir = new File(getFilesDir(), "treated_photos");
            if (!dir.exists() && !dir.mkdirs()) return null;
            File dst = new File(dir, "img_" + UUID.randomUUID() + ".jpg");

            // 1. قراءة الأبعاد
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            try (InputStream boundsIn = getContentResolver().openInputStream(src)) {
                if (boundsIn == null) return null;
                BitmapFactory.decodeStream(boundsIn, null, opts);
            }

            // 2. حساب التصغير
            int maxDim = 1024;
            int sample = 1;
            while (opts.outWidth / sample > maxDim || opts.outHeight / sample > maxDim) {
                sample *= 2;
            }

            // 3. تحميل الصورة
            BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
            decodeOpts.inSampleSize = sample;
            Bitmap bitmap;
            try (InputStream in = getContentResolver().openInputStream(src)) {
                if (in == null) return null;
                bitmap = BitmapFactory.decodeStream(in, null, decodeOpts);
            }
            if (bitmap == null) return null;

            // 4. ضغط وحفظ
            try (OutputStream out = new FileOutputStream(dst)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            }
            bitmap.recycle();

            return dst.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "copyImage failed", e);
            return null;
        }
    }
}