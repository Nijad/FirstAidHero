package com.svu.firstaidhero.ui.treated;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import java.util.Calendar;
import java.util.UUID;

import android.graphics.Bitmap;
import android.util.Log;

public class AddTreatedCaseActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "extra_treated_id";

    private FirstAidRepository repo;

    private ImageView ivPhoto;
    private MaterialButton btnPickPhoto, btnRemovePhoto, btnSave, btnCancel;
    private TextInputEditText etDescription, etDate, etProcedures;
    private TextView tvTitle;

    private int editingId = -1;      // -1 = إضافة جديدة
    private String currentPhotoPath; // مسار الصورة الحالية

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri cameraImageUri;
    private ActivityResultLauncher<String> requestCameraPermission;

    // منتقي الصور الحديث
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    String saved = copyImageToInternalStorage(uri);
                    if (saved != null) {
                        currentPhotoPath = saved;
                        ivPhoto.setImageBitmap(BitmapFactory.decodeFile(saved));
                        btnRemovePhoto.setVisibility(View.VISIBLE);
                        btnPickPhoto.setText(R.string.change_image);
                    } else {
                        Toast.makeText(this, "فشل حفظ الصورة", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_treated_case);

        repo = FirstAidRepository.getInstance(this);

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

        // منتقي التاريخ
        etDate.setOnClickListener(v -> showDatePicker());

        // إعداد الكاميرا
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && cameraImageUri != null) {
                currentPhotoPath = cameraImageUri.getPath(); // المسار الفعلي
                ivPhoto.setImageURI(cameraImageUri);
                btnRemovePhoto.setVisibility(View.VISIBLE);
                btnPickPhoto.setText(R.string.change_image);
            }
        });

        requestCameraPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) openCamera();
            else Toast.makeText(this, "يجب السماح بالوصول للكاميرا", Toast.LENGTH_SHORT).show();
        });

        // عند الضغط على زر إضافة صورة، نعرض خيار (المعرض أم الكاميرا)
        btnPickPhoto.setOnClickListener(v -> {
            String[] options = {"الكاميرا", "معرض الصور"};
            new android.app.AlertDialog.Builder(this)
                    .setTitle("اختر مصدر الصورة")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                openCamera();
                            } else {
                                requestCameraPermission.launch(Manifest.permission.CAMERA);
                            }
                        } else {
                            pickImage.launch("image/*");
                        }
                    }).show();
        });

        // اختيار الصورة
        btnPickPhoto.setOnClickListener(v -> pickImage.launch("image/*"));

        // إزالة الصورة
        btnRemovePhoto.setOnClickListener(v -> {
            currentPhotoPath = null;
            ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
            btnPickPhoto.setText(R.string.add_image);
            btnRemovePhoto.setVisibility(View.GONE);
        });

        // حفظ
        btnSave.setOnClickListener(v -> save());

        // إلغاء
        btnCancel.setOnClickListener(v -> finish());

        // إذا كان تعديلًا
        editingId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (editingId != -1) {
            loadExisting(editingId);
        }
    }

    private void openCamera() {
        try {
            File dir = new File(getFilesDir(), "treated_photos");
            if (!dir.exists()) dir.mkdirs();
            File photoFile = new File(dir, "cam_" + UUID.randomUUID() + ".jpg");
            cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".file-provider", photoFile);
            takePictureLauncher.launch(cameraImageUri);
        } catch (Exception e) {
            Toast.makeText(this, "فشل فتح الكاميرا", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && resultCode == RESULT_OK) {
            // الصورة التقطت، سنحتاج للبحث عنها في المجلد
            File dir = new File(getFilesDir(), "treated_photos");
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                // نأخذ أحدث ملف
                File latest = files[0];
                for (File f : files) { if (f.lastModified() > latest.lastModified()) latest = f; }
                currentPhotoPath = latest.getAbsolutePath();
                ivPhoto.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath));
                btnRemovePhoto.setVisibility(View.VISIBLE);
                btnPickPhoto.setText(R.string.change_image);
            }
        }
    }

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
            String date = String.format("%04d-%02d-%02d", y, m + 1, d);
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
            etDescription.setError("الوصف طويل جدًا (1000 حرف كحد أقصى)");
            return;
        }
        if (proc.isEmpty()) {
            etProcedures.setError(getString(R.string.procedures_required));
            etProcedures.requestFocus();
            return;
        }
        if (proc.length() > 2000) {
            etProcedures.setError("الإجراءات طويلة جدًا (2000 حرف كحد أقصى)");
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
            Toast.makeText(this, "فشل الحفظ: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean isValidDate(String date) {
        if (date == null || date.length() != 10) return false;
        try {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            sdf.setLenient(false);
            sdf.parse(date);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    /**
     * نسخ الصورة المختارة مع ضغطها لتجنّب OOM.
     */
    private String copyImageToInternalStorage(Uri src) {
        try {
            File dir = new File(getFilesDir(), "treated_photos");
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e("AddTreated", "Cannot create photos dir");
                return null;
            }
            File dst = new File(dir, "img_" + UUID.randomUUID() + ".jpg");

            // 1) فك أبعاد الصورة أولًا (بدون تحميلها كاملة)
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            try (InputStream boundsIn = getContentResolver().openInputStream(src)) {
                if (boundsIn == null) return null;
                BitmapFactory.decodeStream(boundsIn, null, opts);
            }

            // 2) حساب نسبة التصغير (بحد أقصى 1024 بكسل)
            int maxDim = 1024;
            int sample = 1;
            while (opts.outWidth / sample > maxDim || opts.outHeight / sample > maxDim) {
                sample *= 2;
            }

            // 3) تحميل الصورة مع التصغير
            BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
            decodeOpts.inSampleSize = sample;
            Bitmap bitmap;
            try (InputStream in = getContentResolver().openInputStream(src)) {
                if (in == null) return null;
                bitmap = BitmapFactory.decodeStream(in, null, decodeOpts);
            }
            if (bitmap == null) {
                Log.e("AddTreated", "Failed to decode bitmap");
                return null;
            }

            // 4) ضغط وحفظ
            try (OutputStream out = new FileOutputStream(dst)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            }
            bitmap.recycle();

            return dst.getAbsolutePath();
        } catch (Exception e) {
            Log.e("AddTreated", "copyImage failed", e);
            return null;
        }
    }
}