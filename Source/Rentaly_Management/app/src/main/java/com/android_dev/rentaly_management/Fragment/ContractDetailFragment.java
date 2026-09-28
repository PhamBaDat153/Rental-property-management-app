package com.android_dev.rentaly_management.Fragment;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.ProgressBar;
import android.app.DatePickerDialog;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.DTO.ContractTenant;
import com.android_dev.rentaly_management.DTO.ContractTenantRequest;
import com.android_dev.rentaly_management.DTO.UserTenant;
import com.android_dev.rentaly_management.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContractDetailFragment extends Fragment {
    private RentalContract contract;
    private final List<Room> rooms = new ArrayList<>();
    private View view;
    private static final int DOCUMENT_PICKER = 703;
    private Uri selectedDocument;
    private final List<ContractTenant> assignments = new ArrayList<>();
    private final List<UserTenant> tenants = new ArrayList<>();

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        view = inflater.inflate(R.layout.fragment_contract_detail, container, false);
        view.findViewById(R.id.contract_detail_back).setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        view.findViewById(R.id.contract_detail_edit).setOnClickListener(v -> editContract());
        view.findViewById(R.id.contract_detail_delete).setOnClickListener(v -> confirmDelete());
        view.findViewById(R.id.contract_detail_view_document).setOnClickListener(v -> openDocument());
        view.findViewById(R.id.contract_detail_choose_document).setOnClickListener(v -> chooseDocument());
        view.findViewById(R.id.contract_detail_start).setOnClickListener(v -> chooseDate(R.id.contract_detail_start));
        view.findViewById(R.id.contract_detail_end).setOnClickListener(v -> chooseDate(R.id.contract_detail_end));
        view.findViewById(R.id.contract_detail_signed).setOnClickListener(v -> chooseDate(R.id.contract_detail_signed));
        view.findViewById(R.id.contract_detail_terminated).setOnClickListener(v -> chooseDate(R.id.contract_detail_terminated));
        view.findViewById(R.id.contract_tenant_add).setOnClickListener(v -> showNewTenantForm());
        load(requireArguments().getString("contract_id"));
        return view;
    }
    private void showNewTenantForm(){
        List<UserTenant> available=new ArrayList<>();
        for(UserTenant t:tenants){boolean used=false;for(ContractTenant a:assignments)if(a.tenant_id!=null&&a.tenant_id.equals(t.getTenant_id()))used=true;if(!used&&t.getTenant_id()!=null)available.add(t);}
        if(available.isEmpty()){toast("Không còn người thuê phù hợp để thêm");return;}
        LinearLayout form=new LinearLayout(requireContext());form.setPadding(16,8,16,8);form.setOrientation(LinearLayout.VERTICAL);
        Spinner spinner=new Spinner(requireContext());List<String> labels=new ArrayList<>();for(UserTenant t:available)labels.add(t.displayName());spinner.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_spinner_dropdown_item,labels));form.addView(spinner);
        EditText moveIn=new EditText(requireContext());moveIn.setHint("Ngày vào ở");moveIn.setFocusable(false);moveIn.setOnClickListener(v->chooseTenantDate(moveIn));form.addView(moveIn);
        EditText moveOut=new EditText(requireContext());moveOut.setHint("Ngày rời đi");moveOut.setFocusable(false);moveOut.setOnClickListener(v->chooseTenantDate(moveOut));form.addView(moveOut);
        RadioGroup rep=new RadioGroup(requireContext());RadioButton yes=new RadioButton(requireContext());yes.setText("Người đại diện");RadioButton no=new RadioButton(requireContext());no.setText("Không đại diện");rep.addView(yes);rep.addView(no);no.setChecked(true);form.addView(rep);
        new AlertDialog.Builder(requireContext()).setTitle("Thêm người thuê").setView(form).setNegativeButton("Hủy",null).setPositiveButton("Thêm",(d,w)->{try{LocalDate in=LocalDate.parse(moveIn.getText().toString());String out=moveOut.getText().toString().trim();LocalDate outDate=out.isEmpty()?null:LocalDate.parse(out);ContractTenantRequest request=new ContractTenantRequest(contract.contract_id,available.get(spinner.getSelectedItemPosition()).getTenant_id(),yes.isChecked(),in,outDate);ApiClient.api.createContractTenant(request).enqueue(new Callback<ContractTenant>(){public void onResponse(Call<ContractTenant> c,Response<ContractTenant> r){if(r.isSuccessful()){toast("Đã thêm người thuê");loadTenants();}else toast("Không thể thêm người thuê");}public void onFailure(Call<ContractTenant> c,Throwable t){toast("Không thể kết nối đến máy chủ");}});}catch(Exception e){toast("Vui lòng chọn ngày vào ở hợp lệ");}}).show();
    }
    private void chooseTenantDate(EditText field){java.util.Calendar now=java.util.Calendar.getInstance();new DatePickerDialog(requireContext(),(dialog,year,month,day)->field.setText(String.format(java.util.Locale.ROOT,"%04d-%02d-%02d",year,month+1,day)),now.get(java.util.Calendar.YEAR),now.get(java.util.Calendar.MONTH),now.get(java.util.Calendar.DAY_OF_MONTH)).show();}
    private void load(String id) {
        ApiClient.api.contract(id).enqueue(new Callback<RentalContract>() {
            public void onResponse(Call<RentalContract> c, Response<RentalContract> r) { if (r.isSuccessful() && r.body()!=null) { contract=r.body(); bind(); loadRooms(); loadTenants(); } else toast("Không thể tải hợp đồng"); }
            public void onFailure(Call<RentalContract> c, Throwable t) { toast("Không thể kết nối đến máy chủ"); }
        });
    }
    private void loadRooms() { ApiClient.api.rooms().enqueue(new Callback<List<Room>>() { public void onResponse(Call<List<Room>> c, Response<List<Room>> r) { if(r.isSuccessful()&&r.body()!=null){rooms.addAll(r.body());bind();} } public void onFailure(Call<List<Room>> c,Throwable t){} }); }
    private void loadTenants() {
        ProgressBar bar=view.findViewById(R.id.contract_tenant_loading); bar.setVisibility(View.VISIBLE);
        ApiClient.api.contractTenants().enqueue(new Callback<List<ContractTenant>>() { public void onResponse(Call<List<ContractTenant>> c, Response<List<ContractTenant>> r) { if(r.isSuccessful()&&r.body()!=null){assignments.clear();for(ContractTenant a:r.body())if(contract.contract_id.equals(a.contract_id))assignments.add(a);loadTenantProfiles();}else tenantError(); } public void onFailure(Call<List<ContractTenant>> c,Throwable t){tenantError();} });
    }
    private void loadTenantProfiles(){ApiClient.api.managedUsers(null).enqueue(new Callback<List<UserTenant>>(){public void onResponse(Call<List<UserTenant>> c,Response<List<UserTenant>> r){view.findViewById(R.id.contract_tenant_loading).setVisibility(View.GONE);if(r.isSuccessful()&&r.body()!=null){tenants.clear();tenants.addAll(r.body());renderTenants();}else tenantError();}public void onFailure(Call<List<UserTenant>> c,Throwable t){tenantError();}});}
    private void tenantError(){if(view==null)return;view.findViewById(R.id.contract_tenant_loading).setVisibility(View.GONE);TextView state=view.findViewById(R.id.contract_tenant_state);state.setVisibility(View.VISIBLE);state.setText("Không thể tải danh sách người thuê");}
    private UserTenant tenant(UUID id){for(UserTenant t:tenants)if(id!=null&&id.equals(t.getTenant_id()))return t;return null;}
    private String tenantName(UUID id){UserTenant t=tenant(id);return t==null?"Chưa cập nhật người thuê":t.displayName();}
    private void renderTenants(){LinearLayout list=view.findViewById(R.id.contract_tenant_list);list.removeAllViews();TextView state=view.findViewById(R.id.contract_tenant_state);state.setVisibility(assignments.isEmpty()?View.VISIBLE:View.GONE);state.setText("Chưa có người thuê trong hợp đồng");for(ContractTenant a:assignments){LinearLayout row=new LinearLayout(requireContext());row.setOrientation(LinearLayout.VERTICAL);row.setPadding(12,12,12,12);row.setBackgroundResource(R.drawable.resource_item_background);TextView text=new TextView(requireContext());text.setText(tenantName(a.tenant_id)+"\n"+(Boolean.TRUE.equals(a.is_representative)?"Người đại diện":"Người thuê")+"\nVào ở: "+value(a.move_in_date)+" | Rời đi: "+value(a.move_out_date)+"\nTrạng thái: "+status(a.status));text.setTextColor(getResources().getColor(R.color.dark_navy));row.addView(text);LinearLayout actions=new LinearLayout(requireContext());Button edit=new Button(requireContext());edit.setText("Sửa");edit.setOnClickListener(v->showTenantForm(a));Button delete=new Button(requireContext());delete.setText("Xóa");delete.setOnClickListener(v->confirmTenantDelete(a));actions.addView(edit,new LinearLayout.LayoutParams(0,48,1));actions.addView(delete,new LinearLayout.LayoutParams(0,48,1));row.addView(actions);LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.setMargins(0,0,0,8);list.addView(row,params);}}
    private void showTenantForm(ContractTenant current){if(contract==null)return;List<UserTenant> available=new ArrayList<>();for(UserTenant t:tenants){boolean used=false;for(ContractTenant a:assignments)if(a.tenant_id!=null&&a.tenant_id.equals(t.getTenant_id())&&(current==null||!a.tenant_id.equals(current.tenant_id)))used=true;if(!used&&t.getTenant_id()!=null)available.add(t);}if(current==null&&available.isEmpty()){toast("Không còn người thuê phù hợp để thêm");return;}LinearLayout form=new LinearLayout(requireContext());form.setPadding(16,8,16,8);form.setOrientation(LinearLayout.VERTICAL);Spinner tenantSpinner=new Spinner(requireContext());List<String> labels=new ArrayList<>();for(UserTenant t:available)labels.add(t.displayName());if(current!=null){UserTenant selected=tenant(current.tenant_id);if(selected!=null){available.add(0,selected);labels.add(0,selected.displayName());}}tenantSpinner.setAdapter(new ArrayAdapter<>(requireContext(),android.R.layout.simple_spinner_dropdown_item,labels));tenantSpinner.setEnabled(current==null);form.addView(tenantSpinner);EditText moveIn=new EditText(requireContext());moveIn.setHint("Ngày vào ở yyyy-MM-dd");moveIn.setInputType(16);moveIn.setText(current==null?"":value(current.move_in_date));form.addView(moveIn);EditText moveOut=new EditText(requireContext());moveOut.setHint("Ngày rời đi yyyy-MM-dd");moveOut.setInputType(16);moveOut.setText(current==null?"":value(current.move_out_date));form.addView(moveOut);RadioGroup rep=new RadioGroup(requireContext());RadioButton yes=new RadioButton(requireContext());yes.setId(View.generateViewId());yes.setText("Người đại diện");RadioButton no=new RadioButton(requireContext());no.setId(View.generateViewId());no.setText("Không đại diện");rep.addView(yes);rep.addView(no);(Boolean.TRUE.equals(current==null?false:current.is_representative)?yes:no).setChecked(true);form.addView(rep);AlertDialog dialog=new AlertDialog.Builder(requireContext()).setTitle(current==null?"Thêm người thuê":"Sửa người thuê").setView(form).setNegativeButton("Hủy",null).setPositiveButton("Lưu",null).create();dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{LocalDate in=moveIn.getText().toString().trim().isEmpty()?null:LocalDate.parse(moveIn.getText().toString().trim());LocalDate out=moveOut.getText().toString().trim().isEmpty()?null:LocalDate.parse(moveOut.getText().toString().trim());if(in!=null&&out!=null&&out.isBefore(in))throw new IllegalArgumentException();UUID tenantId=current==null?available.get(tenantSpinner.getSelectedItemPosition()).getTenant_id():current.tenant_id;ContractTenantRequest req=new ContractTenantRequest(contract.contract_id,tenantId,rep.getCheckedRadioButtonId()==yes.getId(),in,out);Call<ContractTenant> call=current==null?ApiClient.api.createContractTenant(req):ApiClient.api.updateContractTenant(contract.contract_id.toString(),tenantId.toString(),req);call.enqueue(new Callback<ContractTenant>(){public void onResponse(Call<ContractTenant> c,Response<ContractTenant> r){if(r.isSuccessful()){dialog.dismiss();toast("Đã lưu người thuê");loadTenants();}else toast(r.code()==409?"Người thuê đã có trong hợp đồng":"Không thể lưu người thuê");}public void onFailure(Call<ContractTenant> c,Throwable t){toast("Không thể kết nối đến máy chủ");}});}catch(Exception e){toast("Vui lòng kiểm tra ngày vào ở và ngày rời đi");}}));dialog.show();}
    private void confirmTenantDelete(ContractTenant a){new AlertDialog.Builder(requireContext()).setTitle("Xóa người thuê?").setMessage("Người thuê sẽ bị gỡ khỏi hợp đồng này.").setNegativeButton("Hủy",null).setPositiveButton("Xóa",(d,w)->ApiClient.api.deleteContractTenant(a.contract_id.toString(),a.tenant_id.toString()).enqueue(new Callback<Void>(){public void onResponse(Call<Void> c,Response<Void> r){if(r.isSuccessful()){toast("Đã xóa người thuê");loadTenants();}else toast("Không thể xóa người thuê");}public void onFailure(Call<Void> c,Throwable t){toast("Không thể kết nối đến máy chủ");}})).show();}
    private void editContract() {
        if (contract == null || contract.contract_id == null) { toast("Hợp đồng chưa tải xong"); return; }
        try {
            EditText start = view.findViewById(R.id.contract_detail_start);
            EditText end = view.findViewById(R.id.contract_detail_end);
            EditText rent = view.findViewById(R.id.contract_detail_rent);
            EditText deposit = view.findViewById(R.id.contract_detail_deposit);
            EditText billing = view.findViewById(R.id.contract_detail_billing_day);
            EditText due = view.findViewById(R.id.contract_detail_due_days);
            EditText terms = view.findViewById(R.id.contract_detail_terms);
            EditText terminated = view.findViewById(R.id.contract_detail_terminated);
            java.time.LocalDate startDate = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startDate = LocalDate.parse(start.getText().toString().trim());
            }
            String endText = end.getText().toString().trim();
            LocalDate endDate = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                endDate = endText.isEmpty() || endText.equals("Chưa cập nhật") ? null : LocalDate.parse(endText);
            }
            java.math.BigDecimal rentValue = new java.math.BigDecimal(rent.getText().toString().trim());
            java.math.BigDecimal depositValue = new java.math.BigDecimal(deposit.getText().toString().trim());
            String billingText = billing.getText().toString().trim();
            Integer billingValue = billingText.isEmpty() || billingText.equals("Chưa cập nhật") ? null : Integer.valueOf(billingText);
            Integer dueValue = Integer.valueOf(due.getText().toString().trim());
            String terminatedText = terminated.getText().toString().trim();
            java.time.LocalDateTime terminatedAt = terminatedText.isEmpty() || terminatedText.equals("Chưa cập nhật")
                    ? null : java.time.LocalDate.parse(terminatedText).atStartOfDay();
            int statusId = ((RadioGroup) view.findViewById(R.id.contract_detail_status)).getCheckedRadioButtonId();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (statusId == -1 || rentValue.signum() < 0 || depositValue.signum() < 0 || dueValue < 0
                        || (billingValue != null && (billingValue < 1 || billingValue > 31))
                        || (endDate != null && endDate.isBefore(startDate))) throw new IllegalArgumentException();
            }
            com.android_dev.rentaly_management.DTO.RentalContractRequest request =
                    new com.android_dev.rentaly_management.DTO.RentalContractRequest(contract.room_id, startDate,
                            endDate, signedAt((EditText) view.findViewById(R.id.contract_detail_signed)), rentValue, depositValue, billingValue, dueValue,
                            statusId == R.id.contract_detail_status_active ? "ACTIVE" : "INACTIVE", optional(terms),
                            terminatedAt, optional(view.findViewById(R.id.contract_detail_lifecycle)));
            okhttp3.RequestBody body = okhttp3.RequestBody.create(ApiClient.gson.toJson(request), okhttp3.MediaType.parse("application/json"));
            view.findViewById(R.id.contract_detail_edit).setEnabled(false);
            ApiClient.api.updateContract(contract.contract_id.toString(), body, documentPart()).enqueue(new Callback<RentalContract>() {
                public void onResponse(Call<RentalContract> call, Response<RentalContract> response) {
                    view.findViewById(R.id.contract_detail_edit).setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) { contract = response.body(); bind(); toast("Đã cập nhật hợp đồng"); }
                    else toast("Không thể cập nhật hợp đồng");
                }
                public void onFailure(Call<RentalContract> call, Throwable t) { view.findViewById(R.id.contract_detail_edit).setEnabled(true); toast("Không thể kết nối đến máy chủ: " + t.getMessage()); }
            });
        } catch (Exception ignored) { toast("Vui lòng kiểm tra các trường hợp đồng"); }
    }
    private void bind() {
        if (contract == null || view == null) return;
        ((TextView)view.findViewById(R.id.contract_detail_room)).setText(ContractDisplay.roomCode(contract, rooms));
        ((RadioGroup) view.findViewById(R.id.contract_detail_status)).clearCheck();
        ((android.widget.RadioButton)view.findViewById("ACTIVE".equalsIgnoreCase(contract.status) ? R.id.contract_detail_status_active : R.id.contract_detail_status_inactive)).setChecked(true);
        ((EditText)view.findViewById(R.id.contract_detail_start)).setText(value(contract.start_date));
        ((EditText)view.findViewById(R.id.contract_detail_end)).setText(value(contract.end_date));
        ((EditText)view.findViewById(R.id.contract_detail_signed)).setText(dateValue(contract.signed_at));
        ((EditText)view.findViewById(R.id.contract_detail_rent)).setText(value(contract.rent_amount));
        ((EditText)view.findViewById(R.id.contract_detail_deposit)).setText(value(contract.deposit_required));
        ((EditText)view.findViewById(R.id.contract_detail_billing_day)).setText(value(contract.billing_day));
        ((EditText)view.findViewById(R.id.contract_detail_due_days)).setText(value(contract.payment_due_days));
        ((EditText)view.findViewById(R.id.contract_detail_terms)).setText(value(contract.terms));
        ((android.widget.EditText)view.findViewById(R.id.contract_detail_terminated)).setText(dateValue(contract.terminated_at));
        ((android.widget.EditText)view.findViewById(R.id.contract_detail_lifecycle)).setText(value(contract.termination_reason));
    }
    private void openDocument(){
        if(!hasDocument()){toast("Hợp đồng chưa có tài liệu PDF hoặc DOCX");return;}
        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(contract.document_url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { startActivity(Intent.createChooser(intent, "Mở tài liệu hợp đồng")); }
        catch(Exception ignored){ toast("Thiết bị không có ứng dụng hoặc trình duyệt để mở tài liệu"); }
    }
    private void downloadDocument(){
        if(!hasDocument()){toast("Hợp đồng chưa có tài liệu PDF hoặc DOCX");return;}
        try {
            DownloadManager.Request request=new DownloadManager.Request(Uri.parse(contract.document_url));
            request.setTitle("Tài liệu hợp đồng");
            request.setDescription("Đang tải tài liệu hợp đồng");
            request.setMimeType(documentMime());
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(requireContext(),"Download","hop-dong-"+contract.contract_id+"."+documentExtension());
            DownloadManager manager=(DownloadManager)requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
            if(manager == null) throw new IllegalStateException();
            manager.enqueue(request);
            toast("Đã bắt đầu tải tài liệu");
        } catch(Exception ignored) { toast("Không thể bắt đầu tải tài liệu"); }
    }
    private String documentMime(){return contract.document_url.toLowerCase(java.util.Locale.ROOT).contains(".docx")?"application/vnd.openxmlformats-officedocument.wordprocessingml.document":"application/pdf";}
    private String documentExtension(){return "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(documentMime())?"docx":"pdf";}
    private boolean hasDocument(){return contract!=null&&contract.document_url!=null&&!contract.document_url.trim().isEmpty();}
    private java.time.LocalDateTime signedAt(EditText field){String text=field.getText().toString().trim();return text.isEmpty()||text.equals("Chưa cập nhật")?null:java.time.LocalDate.parse(text.substring(0,10)).atStartOfDay();}
    private String optional(EditText field){String text=field.getText().toString().trim();return text.isEmpty()||text.equals("Chưa cập nhật")?null:text;}
    private void chooseDate(int id){java.util.Calendar now=java.util.Calendar.getInstance();new DatePickerDialog(requireContext(),(dialog,year,month,day)->((EditText)view.findViewById(id)).setText(String.format(java.util.Locale.ROOT,"%04d-%02d-%02d",year,month+1,day)),now.get(java.util.Calendar.YEAR),now.get(java.util.Calendar.MONTH),now.get(java.util.Calendar.DAY_OF_MONTH)).show();}
    private void chooseDocument(){Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.setType("*/*");intent.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/pdf","application/vnd.openxmlformats-officedocument.wordprocessingml.document"});intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);intent.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(intent,DOCUMENT_PICKER);}
    @Override public void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode==DOCUMENT_PICKER&&resultCode==Activity.RESULT_OK&&data!=null&&data.getData()!=null){selectedDocument=data.getData();toast("Đã chọn tài liệu mới");}}
    private okhttp3.MultipartBody.Part documentPart() throws Exception {if(selectedDocument==null)return null;String type=requireContext().getContentResolver().getType(selectedDocument);String name=selectedDocument.getLastPathSegment()==null?"":selectedDocument.getLastPathSegment().toLowerCase(java.util.Locale.ROOT);boolean pdf="application/pdf".equals(type)||name.endsWith(".pdf");boolean docx="application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(type)||name.endsWith(".docx");if(!pdf&&!docx)throw new IllegalArgumentException();java.io.InputStream input=requireContext().getContentResolver().openInputStream(selectedDocument);java.io.ByteArrayOutputStream output=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;while(input!=null&&(count=input.read(buffer))!=-1)output.write(buffer,0,count);if(input!=null)input.close();if(output.size()==0)throw new IllegalArgumentException();String mime=pdf?"application/pdf":"application/vnd.openxmlformats-officedocument.wordprocessingml.document";return okhttp3.MultipartBody.Part.createFormData("document","contract."+(pdf?"pdf":"docx"),okhttp3.RequestBody.create(output.toByteArray(),okhttp3.MediaType.parse(mime)));}
    private void confirmDelete(){if(contract==null)return;new AlertDialog.Builder(requireContext()).setTitle("Xóa hợp đồng?").setMessage("Thao tác này không thể hoàn tác.").setNegativeButton("Hủy",null).setPositiveButton("Xóa hợp đồng",(d,w)->ApiClient.api.deleteContract(contract.contract_id.toString()).enqueue(new Callback<Void>(){public void onResponse(Call<Void> c,Response<Void> r){if(r.isSuccessful())NavHostFragment.findNavController(ContractDetailFragment.this).navigateUp();else toast(r.code()==409?"Không thể xóa vì hợp đồng đang có người thuê hoặc hóa đơn":"Không thể xóa hợp đồng");}public void onFailure(Call<Void> c,Throwable t){toast("Không thể kết nối đến máy chủ");}})).show();}
     private String dateValue(Object o){String text=value(o);return text.length()>10?text.substring(0,10):text;} private String value(Object o){return o==null||o.toString().trim().isEmpty()?"Chưa cập nhật":o.toString();} private String status(String s){return "ACTIVE".equalsIgnoreCase(s)?"Đang hoạt động":"Không hoạt động";} private void toast(String s){Toast.makeText(requireContext(),s,Toast.LENGTH_LONG).show();}
}
