package com.svu.firstaidhero.ui.treated;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.data.FirstAidRepository;
import com.svu.firstaidhero.model.FirstAidCase;
import com.svu.firstaidhero.model.TreatedCase;
import com.svu.firstaidhero.util.DateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Activity responsible for adding a new treated case or editing an existing one.
 * Handles data validation, database insertion/update, date picking,
 * image attachment via camera or gallery, and linking to a predefined FirstAidCase.
 */
public class AddTreatedCaseActivity extends AppCompatActivity {

    private static final String TAG = "AddTreatedCase";
    public static final String EXTRA_ID = "extra_treated_id";

    private FirstAidRepository repo;

    // UI Components
    private ImageView ivPhoto;
    private MaterialButton btnPickPhoto, btnRemovePhoto;
    private TextInputEditText etDescription, etDate, etProcedures;
    private TextView tvTitle;
    private MaterialAutoCompleteTextView spinnerCaseType;

    private int editingId = -1;
    private String currentPhotoPath;
    private File cameraPhotoFile;

    // Variables for Case Selection
    private List<FirstAidCase> allCasesList = new ArrayList<>();
    private Integer selectedCaseId = null; // null means "Unlinked / Other"

    // ====== Activity Result Launchers ======

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

    private final ActivityResultLauncher<String> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "يجب السماح بالوصول للكاميرا لالتقاط الصور", Toast.LENGTH_LONG).show();
                }
            });

    // ====== Lifecycle ======

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_treated_case);

        repo = FirstAidRepository.getInstance(this);

        // Bind UI components
        ivPhoto = findViewById(R.id.ivPhoto);
        btnPickPhoto = findViewById(R.id.btnPickPhoto);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        etProcedures = findViewById(R.id.etProcedures);
        tvTitle = findViewById(R.id.tvTitle);
        spinnerCaseType = findViewById(R.id.spinnerCaseType);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        etDate.setText(DateUtils.today());
        etDate.setOnClickListener(v -> showDatePicker());

        // Setup Case Type Spinner
        setupCaseSpinner();

        // Setup image picker button
        btnPickPhoto.setOnClickListener(v -> {
            String[] options = {"التقاط صورة بالكاميرا", "اختيار من المعرض"};
            new AlertDialog.Builder(this)
                    .setTitle("إضافة صورة")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
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

        // Remove image button
        btnRemovePhoto.setOnClickListener(v -> {
            currentPhotoPath = null;
            ivPhoto.setImageTintList(android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary)));
            ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
            btnPickPhoto.setText(R.string.add_image);
            btnRemovePhoto.setVisibility(View.GONE);
        });

        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> finish());

        // Check for Edit Mode
        editingId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (editingId != -1) {
            loadExisting(editingId);
        }
    }

    /**
     * Populates the AutoCompleteTextView with predefined cases from the database.
     */
    private void setupCaseSpinner() {
        allCasesList = repo.cases().getAllCases();
        List<String> caseNames = new ArrayList<>();
        caseNames.add("غير محدد (حالة أخرى)"); // Default option

        for (FirstAidCase c : allCasesList) {
            caseNames.add(c.getTitle());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, caseNames);
        spinnerCaseType.setAdapter(adapter);

        // Handle selection: Map the spinner position back to the Case ID
        spinnerCaseType.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedCaseId = null; // Unlinked
            } else {
                selectedCaseId = allCasesList.get(position - 1).getId();
            }
        });

        spinnerCaseType.setOnClickListener(v -> spinnerCaseType.showDropDown());

        spinnerCaseType.setText("غير محدد (حالة أخرى)", false);
    }

    // ====== Helper Methods ======

    private void loadExisting(int id) {
        TreatedCase t = repo.treated().getById(id);
        if (t == null) { finish(); return; }

        tvTitle.setText(R.string.edit_treated_title);
        etDescription.setText(t.getDescription());
        etDate.setText(t.getDate());
        etProcedures.setText(t.getProcedures());

        // Restore the linked case if exists
        if (t.getCaseId() != null) {
            selectedCaseId = t.getCaseId();
            for (int i = 0; i < allCasesList.size(); i++) {
                if (allCasesList.get(i).getId() == selectedCaseId) {
                    spinnerCaseType.setText(allCasesList.get(i).getTitle(), false);
                    break;
                }
            }
        } else {
            spinnerCaseType.setText("غير محدد (حالة أخرى)", false);
        }

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

        // Validation
        if (desc.isEmpty()) {
            etDescription.setError(getString(R.string.description_required));
            etDescription.requestFocus();
            return;
        }
        if (proc.isEmpty()) {
            etProcedures.setError(getString(R.string.procedures_required));
            etProcedures.requestFocus();
            return;
        }
        if (!isValidDate(date)) {
            etDate.setError("تاريخ غير صالح");
            return;
        }

        try {
            if (editingId == -1) {
                // Pass selectedCaseId (can be null)
                TreatedCase t = new TreatedCase(0, selectedCaseId, desc, date, currentPhotoPath, proc);
                repo.treated().insert(t);
            } else {
                TreatedCase t = new TreatedCase(editingId, selectedCaseId, desc, date, currentPhotoPath, proc);
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

    private void openCamera() {
        try {
            File dir = new File(getFilesDir(), "treated_photos");
            if (!dir.exists() && !dir.mkdirs()) return;

            cameraPhotoFile = new File(dir, "cam_" + UUID.randomUUID() + ".jpg");
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cameraPhotoFile);
            takePictureLauncher.launch(uri);

        } catch (Exception e) {
            Log.e(TAG, "Error opening camera", e);
            Toast.makeText(this, "فشل فتح الكاميرا", Toast.LENGTH_SHORT).show();
        }
    }

    private String copyImageToInternalStorage(Uri src) {
        try {
            File dir = new File(getFilesDir(), "treated_photos");
            if (!dir.exists() && !dir.mkdirs()) return null;
            File dst = new File(dir, "img_" + UUID.randomUUID() + ".jpg");

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            try (InputStream boundsIn = getContentResolver().openInputStream(src)) {
                if (boundsIn == null) return null;
                BitmapFactory.decodeStream(boundsIn, null, opts);
            }

            int maxDim = 1024;
            int sample = 1;
            while (opts.outWidth / sample > maxDim || opts.outHeight / sample > maxDim) {
                sample *= 2;
            }

            BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
            decodeOpts.inSampleSize = sample;
            Bitmap bitmap;
            try (InputStream in = getContentResolver().openInputStream(src)) {
                if (in == null) return null;
                bitmap = BitmapFactory.decodeStream(in, null, decodeOpts);
            }
            if (bitmap == null) return null;

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