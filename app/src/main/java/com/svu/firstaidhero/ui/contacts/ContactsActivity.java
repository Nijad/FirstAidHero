package com.svu.firstaidhero.ui.contacts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.svu.firstaidhero.R;
import com.svu.firstaidhero.data.FirstAidRepository;
import com.svu.firstaidhero.model.EmergencyContact;
import com.svu.firstaidhero.ui.adapter.ContactAdapter;

import java.util.List;

/**
 * Activity responsible for displaying and managing emergency contacts.
 * Separates contacts into 'general' (non-deletable) and 'custom' (user-managed).
 * Handles CRUD operations for custom contacts and direct calling functionality.
 */
public class ContactsActivity extends AppCompatActivity implements ContactAdapter.Listener {

    // Request code for CALL_PHONE permission
    private static final int REQ_CALL = 100;

    private FirstAidRepository repo;
    private ContactAdapter generalAdapter, customAdapter;
    private TextView tvEmptyCustom;

    // Cache the phone number while waiting for the user to grant call permission
    private String pendingPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        repo = FirstAidRepository.getInstance(this);

        // Setup back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvGeneral = findViewById(R.id.rvGeneral);
        RecyclerView rvCustom = findViewById(R.id.rvCustom);
        tvEmptyCustom = findViewById(R.id.tvEmptyCustom);

        // Initialize adapters: general contacts are read-only (false), custom are editable (true)
        generalAdapter = new ContactAdapter(this, false);
        customAdapter = new ContactAdapter(this, true);

        rvGeneral.setLayoutManager(new LinearLayoutManager(this));
        rvGeneral.setAdapter(generalAdapter);

        rvCustom.setLayoutManager(new LinearLayoutManager(this));
        rvCustom.setAdapter(customAdapter);

        // Setup add contact button
        findViewById(R.id.btnAddContact).setOnClickListener(v -> showAddEditDialog(null));

        loadContacts();
    }

    /**
     * Fetches contacts from the database and updates the respective adapters.
     */
    private void loadContacts() {
        List<EmergencyContact> general = repo.contacts().getGeneralContacts();
        generalAdapter.setItems(general);

        List<EmergencyContact> custom = repo.contacts().getCustomContacts();
        customAdapter.setItems(custom);

        // Show empty state text if there are no custom contacts
        tvEmptyCustom.setVisibility(custom.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // ============ ContactAdapter.Listener Implementations ============

    @Override
    public void onCall(EmergencyContact c) {
        if (c.getPhone() == null || c.getPhone().isEmpty()) {
            Toast.makeText(this, "رقم غير صالح", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingPhone = c.getPhone();

        // Check for CALL_PHONE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            startCall(pendingPhone);
        } else {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL);
        }
    }

    @Override
    public void onEdit(EmergencyContact c) {
        showAddEditDialog(c);
    }

    @Override
    public void onDelete(EmergencyContact c) {
        // Show confirmation dialog before deleting
        new AlertDialog.Builder(this)
                .setTitle("حذف الرقم")
                .setMessage("هل تريد حذف " + c.getName() + "؟")
                .setPositiveButton("حذف", (d, w) -> {
                    repo.contacts().deleteCustom(c.getId());
                    loadContacts();
                    Toast.makeText(this, "تم الحذف", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    // ============ Add / Edit Dialog ============

    /**
     * Displays a dialog for adding a new contact or editing an existing one.
     *
     * @param existing The contact to edit, or null if adding a new contact.
     */
    private void showAddEditDialog(EmergencyContact existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextInputEditText etName = view.findViewById(R.id.etName);
        TextInputEditText etPhone = view.findViewById(R.id.etPhone);
        Spinner spinner = view.findViewById(R.id.spinnerCategory);

        // Define categories for the dropdown spinner
        String[] categories = {"طبيب", "عائلة", "أخرى"};
        String[] categoryValues = {"doctor", "family", "other"};
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(spinAdapter);

        boolean isEdit = existing != null;
        if (isEdit) {
            tvTitle.setText("تعديل الرقم");
            etName.setText(existing.getName());
            etPhone.setText(existing.getPhone());
            // Set the spinner to the correct category
            for (int i = 0; i < categoryValues.length; i++) {
                if (categoryValues[i].equals(existing.getCategory())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        } else {
            tvTitle.setText("إضافة رقم مخصص");
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("حفظ", null)
                .setNegativeButton("إلغاء", null)
                .create();

        // Override the positive button click to prevent dialog from closing on validation errors
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = text(etName);
                    String phone = text(etPhone);

                    // ===== Input Validation =====
                    if (name.isEmpty()) {
                        etName.setError("الاسم مطلوب");
                        etName.requestFocus();
                        return;
                    }
                    if (name.length() > 60) {
                        etName.setError("الاسم طويل جدًا (60 حرفًا كحد أقصى)");
                        return;
                    }
                    if (phone.isEmpty()) {
                        etPhone.setError("الرقم مطلوب");
                        etPhone.requestFocus();
                        return;
                    }
                    if (!isValidPhone(phone)) {
                        etPhone.setError("رقم غير صالح (أرقام ورموز + - فقط)");
                        return;
                    }

                    String cat = categoryValues[spinner.getSelectedItemPosition()];

                    try {
                        if (isEdit) {
                            // Update existing contact
                            existing.setName(name);
                            existing.setPhone(phone);
                            existing.setCategory(cat);
                            repo.contacts().update(existing);
                        } else {
                            // Insert new custom contact
                            EmergencyContact c = new EmergencyContact(
                                    0, name, phone, EmergencyContact.TYPE_CUSTOM, cat);
                            repo.contacts().insert(c);
                        }
                        loadContacts();
                        dialog.dismiss();
                    } catch (Exception e) {
                        Toast.makeText(this, "فشل الحفظ: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }));

        dialog.show();
    }

    /**
     * Validates the phone number format.
     * Allows numbers, spaces, and the '+' or '-' symbols only.
     *
     * @param phone The phone number string to validate.
     * @return true if valid, false otherwise.
     */
    private boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleaned = phone.trim();
        if (cleaned.length() < 3 || cleaned.length() > 20) return false;
        return cleaned.matches("[0-9+\\-\\s]+");
    }

    /**
     * Helper method to safely extract text from a TextInputEditText.
     *
     * @param et The input field.
     * @return The trimmed string, or an empty string if null.
     */
    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    // ============ Calling Logic ============

    /**
     * Initiates a phone call using ACTION_CALL.
     * If a SecurityException occurs, it falls back to ACTION_DIAL (opening the dialer).
     *
     * @param phone The phone number to call.
     */
    private void startCall(String phone) {
        Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        try {
            startActivity(i);
        } catch (SecurityException e) {
            // Fallback: Open the dialer instead of making a direct call
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
        }
    }

    /**
     * Handles the result of the runtime permission request.
     * If granted, makes the call. If denied, falls back to the dialer.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingPhone != null) startCall(pendingPhone);
            } else {
                // Permission denied: Use ACTION_DIAL as a safe fallback
                if (pendingPhone != null) {
                    startActivity(new Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:" + pendingPhone)));
                }
            }
            pendingPhone = null;
        }
    }
}