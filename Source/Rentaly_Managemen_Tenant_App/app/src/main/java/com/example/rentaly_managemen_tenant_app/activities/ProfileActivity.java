package com.example.rentaly_managemen_tenant_app.activities;

import android.content.Intent;
import android.text.InputType;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentaly_managemen_tenant_app.R;
import com.example.rentaly_managemen_tenant_app.models.UserProfileUpdateRequest;
import com.example.rentaly_managemen_tenant_app.models.UserTenant;
import com.example.rentaly_managemen_tenant_app.network.ApiCallback;
import com.example.rentaly_managemen_tenant_app.repository.UserRepository;

public class ProfileActivity extends TenantBaseActivity {
    private final UserRepository repository = new UserRepository();
    private UserTenant currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createScreen("Hồ sơ người thuê");
        loadProfile();
    }

    private void loadProfile() {
        showEmpty("Đang tải hồ sơ...");
        repository.getProfile(session.getUserId(), new ApiCallback<UserTenant>() {
            @Override
            public void onSuccess(UserTenant user) {
                currentProfile = user;
                renderProfile();
            }

            @Override
            public void onError(String error) {
                showFailure(error);
                addButton("Đăng xuất", v -> logout());
            }
        });
    }

    private void renderProfile() {
        content.removeAllViews();
        showLoaded();
        addSection(value(currentProfile.full_name));
        addButton("Chỉnh sửa thông tin", v -> renderEditor());

        addSection("Tài khoản");
        addLine("Tên đăng nhập: " + value(currentProfile.user_name));
        addLine("Trạng thái: " + value(currentProfile.status));
        addButton("Đăng xuất", v -> logout());

        addSection("Thông tin cá nhân");
        addLine("Điện thoại: " + value(currentProfile.phone));
        addLine("Email: " + value(currentProfile.email));
        addLine("Giới tính: " + displayGender(currentProfile.gender));
        addLine("Ngày sinh: " + value(currentProfile.date_of_birth));

        addSection("Giấy tờ tùy thân");
        addLine("Loại giấy tờ: " + displayIdentity(currentProfile.identityType));
        addLine("Số giấy tờ: " + value(currentProfile.identity_number));
        addLine("Ngày/nơi cấp: " + value(currentProfile.identity_issued_date)
                + " / " + value(currentProfile.identity_issued_place));

        addSection("Địa chỉ và liên hệ");
        addLine("Địa chỉ thường trú: " + value(currentProfile.permanent_address));
        addLine("Liên hệ khẩn cấp: " + value(currentProfile.emergency_contact_name)
                + " — " + value(currentProfile.emergency_contact_phone));
        if (currentProfile.additional_note != null && !currentProfile.additional_note.trim().isEmpty()) {
            addLine("Ghi chú: " + currentProfile.additional_note);
        }
    }

    private void logout() {
        session.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void renderEditor() {
        content.removeAllViews();
        showLoaded();
        addSection("Chỉnh sửa thông tin cá nhân");

        EditText fullName = field("Họ và tên", currentProfile.full_name, InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        EditText phone = field("Số điện thoại", currentProfile.phone, InputType.TYPE_CLASS_PHONE);
        EditText email = field("Email", currentProfile.email, InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        Spinner gender = spinner(new String[]{"Chọn giới tính", "Nam", "Nữ", "Khác"},
                genderPosition(currentProfile.gender));
        EditText dateOfBirth = field("Ngày sinh (YYYY-MM-DD)", currentProfile.date_of_birth,
                InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);

        addSection("Giấy tờ tùy thân");
        Spinner identityType = spinner(new String[]{"Chọn loại giấy tờ", "CCCD/CMND", "Hộ chiếu", "Khác"},
                identityPosition(currentProfile.identityType));
        EditText identityNumber = field("Số giấy tờ", currentProfile.identity_number,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        EditText issuedDate = field("Ngày cấp (YYYY-MM-DD)", currentProfile.identity_issued_date,
                InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        EditText issuedPlace = field("Nơi cấp", currentProfile.identity_issued_place, InputType.TYPE_CLASS_TEXT);

        addSection("Địa chỉ và liên hệ khẩn cấp");
        EditText address = field("Địa chỉ thường trú", currentProfile.permanent_address, InputType.TYPE_CLASS_TEXT);
        EditText emergencyName = field("Tên người liên hệ khẩn cấp", currentProfile.emergency_contact_name,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        EditText emergencyPhone = field("Số điện thoại liên hệ khẩn cấp", currentProfile.emergency_contact_phone,
                InputType.TYPE_CLASS_PHONE);
        EditText note = field("Ghi chú thêm", currentProfile.additional_note,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        note.setMinLines(2);

        addPrimaryButton("Lưu thay đổi", v -> saveProfile(fullName, phone, email, gender, dateOfBirth,
                identityType, identityNumber, issuedDate, issuedPlace, address, emergencyName,
                emergencyPhone, note));
        TextView cancel = text("Hủy chỉnh sửa", 14, true);
        cancel.setTextColor(getColor(R.color.secondary));
        cancel.setGravity(Gravity.CENTER);
        cancel.setPadding(0, dp(10), 0, dp(10));
        cancel.setOnClickListener(v -> renderProfile());
        content.addView(cancel, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private EditText field(String hint, String value, int inputType) {
        EditText input = new EditText(this);
        styleInput(input, hint, 1);
        input.setInputType(inputType);
        if (value != null) input.setText(value);
        return input;
    }

    private Spinner spinner(String[] options, int selectedPosition) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selectedPosition);
        styleSpinner(spinner);
        return spinner;
    }

    private void saveProfile(EditText fullName, EditText phone, EditText email, Spinner gender,
                             EditText dateOfBirth, Spinner identityType, EditText identityNumber,
                             EditText issuedDate, EditText issuedPlace, EditText address,
                             EditText emergencyName, EditText emergencyPhone, EditText note) {
        String birth = input(dateOfBirth);
        String issued = input(issuedDate);
        if (!validDate(birth) || !validDate(issued)) {
            showFailure("Ngày sinh và ngày cấp cần đúng định dạng YYYY-MM-DD.");
            return;
        }
        if (input(phone).length() > 20 || input(emergencyPhone).length() > 20) {
            showFailure("Số điện thoại không được dài quá 20 ký tự.");
            return;
        }
        String emailValue = input(email);
        if (!emailValue.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            showFailure("Địa chỉ email chưa đúng định dạng.");
            return;
        }

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.full_name = input(fullName);
        request.phone = input(phone);
        request.email = emailValue;
        request.gender = genderValue(gender.getSelectedItemPosition());
        request.date_of_birth = birth;
        request.identityType = identityValue(identityType.getSelectedItemPosition());
        request.identity_number = input(identityNumber);
        request.identity_issued_date = issued;
        request.identity_issued_place = input(issuedPlace);
        request.permanent_address = input(address);
        request.emergency_contact_name = input(emergencyName);
        request.emergency_contact_phone = input(emergencyPhone);
        request.additional_note = input(note);

        if (request.full_name.isEmpty()) {
            showFailure("Vui lòng nhập họ và tên.");
            return;
        }
        showEmpty("Đang lưu thay đổi...");
        repository.updateProfile(session.getUserId(), request, new ApiCallback<UserTenant>() {
            @Override
            public void onSuccess(UserTenant user) {
                currentProfile = user;
                Toast.makeText(ProfileActivity.this, "Đã cập nhật hồ sơ.", Toast.LENGTH_SHORT).show();
                renderProfile();
            }

            @Override
            public void onError(String error) {
                showFailure(error);
                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String input(EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private boolean validDate(String value) {
        if (value.isEmpty()) return true;
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        try {
            java.time.LocalDate.parse(value);
            return true;
        } catch (java.time.format.DateTimeParseException exception) {
            return false;
        }
    }

    private int genderPosition(String value) {
        if ("MALE".equalsIgnoreCase(value)) return 1;
        if ("FEMALE".equalsIgnoreCase(value)) return 2;
        if ("OTHER".equalsIgnoreCase(value)) return 3;
        return 0;
    }

    private String genderValue(int position) {
        if (position == 1) return "MALE";
        if (position == 2) return "FEMALE";
        if (position == 3) return "OTHER";
        return "";
    }

    private int identityPosition(String value) {
        if ("ID".equalsIgnoreCase(value)) return 1;
        if ("PASSPORT".equalsIgnoreCase(value)) return 2;
        if ("OTHER".equalsIgnoreCase(value)) return 3;
        return 0;
    }

    private String identityValue(int position) {
        if (position == 1) return "ID";
        if (position == 2) return "PASSPORT";
        if (position == 3) return "OTHER";
        return "";
    }

    private String displayGender(String value) {
        if ("MALE".equalsIgnoreCase(value)) return "Nam";
        if ("FEMALE".equalsIgnoreCase(value)) return "Nữ";
        if ("OTHER".equalsIgnoreCase(value)) return "Khác";
        return "—";
    }

    private String displayIdentity(String value) {
        if ("ID".equalsIgnoreCase(value)) return "CCCD/CMND";
        if ("PASSPORT".equalsIgnoreCase(value)) return "Hộ chiếu";
        if ("OTHER".equalsIgnoreCase(value)) return "Khác";
        return "—";
    }
}
