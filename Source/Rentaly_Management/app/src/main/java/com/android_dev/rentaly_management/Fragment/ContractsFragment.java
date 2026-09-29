package com.android_dev.rentaly_management.Fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android_dev.rentaly_management.Apis.ApiClient;
import com.android_dev.rentaly_management.DTO.RentalContract;
import com.android_dev.rentaly_management.DTO.RentalContractRequest;
import com.android_dev.rentaly_management.DTO.Room;
import com.android_dev.rentaly_management.R;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContractsFragment extends Fragment {
    private static final int PDF_PICKER = 702;
    private final List<RentalContract> contracts = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private final List<RentalContract> visible = new ArrayList<>();
    private ArrayAdapter<RentalContract> adapter;
    private EditText search;
    private ProgressBar loading;
    private TextView state;
    private Uri selectedPdf;
    private String pendingEditId;
    private boolean loadFailed;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View view = inflater.inflate(R.layout.fragment_contracts, container, false);
        search = view.findViewById(R.id.contract_search); loading = view.findViewById(R.id.contract_loading); state = view.findViewById(R.id.contract_state);
        ListView list = view.findViewById(R.id.contract_list);
        adapter = new ArrayAdapter<RentalContract>(requireContext(), R.layout.item_contract, R.id.contract_primary, visible) {
            @NonNull @Override public View getView(int position, View convert, @NonNull ViewGroup parent) {
                View row = super.getView(position, convert, parent); RentalContract c = visible.get(position);
                ((TextView) row.findViewById(R.id.contract_primary)).setText("Phòng: " + ContractDisplay.roomCode(c, rooms));
                ((TextView) row.findViewById(R.id.contract_secondary)).setText("Thời hạn: " + value(c.start_date) + " - " + value(c.end_date));
                ((TextView) row.findViewById(R.id.contract_meta)).setText("Giá thuê: " + value(c.rent_amount) + " | " + status(c.status));
                return row;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((p, row, position, id) -> { Bundle args = new Bundle(); args.putString("contract_id", visible.get(position).contract_id.toString()); NavHostFragment.findNavController(this).navigate(R.id.contractDetailFragment, args); });
        view.findViewById(R.id.contract_add).setOnClickListener(v -> showForm(null));
        search.addTextChangedListener(new TextWatcher() { public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int b,int c){ render(); } public void afterTextChanged(Editable e){} });
        pendingEditId = getArguments() == null ? null : getArguments().getString("edit_contract_id");
        load(); return view;
    }

    private void load() {
        loading.setVisibility(View.VISIBLE); state.setVisibility(View.GONE); loadFailed = false; contracts.clear(); rooms.clear();
        final boolean[] done = {false, false}; final boolean[] failed = {false};
        ApiClient.api.contracts().enqueue(new Callback<List<RentalContract>>() {
            public void onResponse(Call<List<RentalContract>> c, Response<List<RentalContract>> r) { if (r.isSuccessful() && r.body()!=null) contracts.addAll(r.body()); else failed[0]=true; done[0]=true; finishLoad(done, failed); }
            public void onFailure(Call<List<RentalContract>> c, Throwable t) { failed[0]=true; done[0]=true; finishLoad(done, failed); }
        });
        ApiClient.api.rooms().enqueue(new Callback<List<Room>>() {
            public void onResponse(Call<List<Room>> c, Response<List<Room>> r) { if (r.isSuccessful() && r.body()!=null) rooms.addAll(r.body()); else failed[0]=true; done[1]=true; finishLoad(done, failed); }
            public void onFailure(Call<List<Room>> c, Throwable t) { failed[0]=true; done[1]=true; finishLoad(done, failed); }
        });
    }
    private void finishLoad(boolean[] done, boolean[] failed) { if (!done[0] || !done[1]) return; loading.setVisibility(View.GONE); loadFailed = failed[0]; if (loadFailed) { state.setVisibility(View.VISIBLE); state.setText("Không thể tải dữ liệu hợp đồng hoặc phòng. Chạm để thử lại."); state.setOnClickListener(v -> load()); } render(); if (pendingEditId != null) { for (RentalContract item : contracts) if (item.contract_id != null && pendingEditId.equals(item.contract_id.toString())) { pendingEditId = null; showForm(item); break; } } }
    private void render() { if (adapter==null) return; String q=search.getText().toString(); visible.clear(); for (RentalContract c:contracts) if (ContractDisplay.matchesRoomCode(c, rooms, q)) visible.add(c); adapter.notifyDataSetChanged(); if (!loading.isShown() && !loadFailed) { state.setVisibility(visible.isEmpty()?View.VISIBLE:View.GONE); if(visible.isEmpty()) { state.setText("Chưa có hợp đồng phù hợp"); state.setOnClickListener(null); } } }

    private void showForm(RentalContract current) {
        if (rooms.isEmpty()) { toast("Chưa tải được danh sách phòng để chọn"); return; }
        selectedPdf = null; View form = getLayoutInflater().inflate(R.layout.dialog_create_rental_contract, null);
        Spinner room = form.findViewById(R.id.contract_room); EditText start=form.findViewById(R.id.contract_start), end=form.findViewById(R.id.contract_end), rent=form.findViewById(R.id.contract_rent), deposit=form.findViewById(R.id.contract_deposit), billing=form.findViewById(R.id.contract_billing_day), due=form.findViewById(R.id.contract_due_days), terms=form.findViewById(R.id.contract_terms); RadioGroup status=form.findViewById(R.id.contract_status); TextView pdf=form.findViewById(R.id.contract_pdf_status);
        List<String> labels=new ArrayList<>(); for(Room r:rooms) labels.add(value(r.room_code)); room.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels));
        start.setText(current==null?"":value(current.start_date)); end.setText(current==null?"":value(current.end_date)); rent.setText(current==null?"":value(current.rent_amount)); deposit.setText(current==null?"":value(current.deposit_required)); billing.setText(current==null?"":value(current.billing_day)); due.setText(current==null?"0":value(current.payment_due_days)); terms.setText(current==null?"":value(current.terms)); ((RadioButton)form.findViewById("ACTIVE".equals(current==null?"INACTIVE":current.status)?R.id.contract_status_active:R.id.contract_status_inactive)).setChecked(true);
        if(current!=null) for(int i=0;i<rooms.size();i++) if(rooms.get(i).room_id.equals(current.room_id)) room.setSelection(i);
        start.setOnClickListener(v->date(start)); end.setOnClickListener(v->date(end)); form.findViewById(R.id.contract_choose_pdf).setOnClickListener(v->{ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("*/*"); i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/pdf","application/vnd.openxmlformats-officedocument.wordprocessingml.document"}); i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false); i.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(i,PDF_PICKER); });
        AlertDialog dialog=new AlertDialog.Builder(requireContext()).setTitle(current==null?"Thêm hợp đồng":"Sửa hợp đồng").setView(form).setNegativeButton("Hủy",null).setPositiveButton("Lưu hợp đồng",null).create();
         dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{ try { int index=room.getSelectedItemPosition(); LocalDate s=LocalDate.parse(start.getText().toString().trim()); LocalDate e=end.getText().toString().trim().isEmpty()?null:LocalDate.parse(end.getText().toString().trim()); BigDecimal ra=new BigDecimal(rent.getText().toString().trim()), de=new BigDecimal(deposit.getText().toString().trim()); Integer bd=billing.getText().toString().trim().isEmpty()?null:Integer.valueOf(billing.getText().toString().trim()); int pd=Integer.parseInt(due.getText().toString().trim()); if(index<0||!ContractValidation.valid(s,e,ra,de,bd,pd)||status.getCheckedRadioButtonId()==-1) throw new IllegalArgumentException(); String st=status.getCheckedRadioButtonId()==R.id.contract_status_active?"ACTIVE":"INACTIVE"; RentalContractRequest req=new RentalContractRequest(rooms.get(index).room_id,s,e,null,ra,de,bd,pd,st,optional(terms),null,null); RequestBody body=RequestBody.create(ApiClient.gson.toJson(req),MediaType.parse("application/json")); dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); Call<RentalContract> call=current==null?ApiClient.api.createContract(body,documentPart()):ApiClient.api.updateContract(current.contract_id.toString(),body,documentPart()); call.enqueue(new Callback<RentalContract>(){ public void onResponse(Call<RentalContract> c,Response<RentalContract> r){ if(r.isSuccessful()){dialog.dismiss();toast(current==null?"Đã thêm hợp đồng":"Đã cập nhật hợp đồng");load();}else{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);toast(error(r,"Không thể lưu hợp đồng"));}} public void onFailure(Call<RentalContract> c,Throwable t){dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);toast("Không thể kết nối đến máy chủ: "+t.getMessage());} }); }catch(Exception ex){dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);toast(ex.getMessage()==null?"Vui lòng kiểm tra ngày, giá trị và thông tin bắt buộc":ex.getMessage());} })); dialog.show();
    }
    private MultipartBody.Part documentPart() throws Exception { if(selectedPdf==null)return null; String type=requireContext().getContentResolver().getType(selectedPdf); String name=selectedPdf.getLastPathSegment()==null?"":selectedPdf.getLastPathSegment().toLowerCase(Locale.ROOT); boolean pdf="application/pdf".equals(type)||name.endsWith(".pdf"); boolean docx="application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(type)||name.endsWith(".docx"); if(!pdf&&!docx) throw new IllegalArgumentException("PDF or DOCX required"); byte[] bytes=read(selectedPdf); if(bytes.length==0) throw new IllegalArgumentException("Empty document"); String mediaType=pdf?"application/pdf":"application/vnd.openxmlformats-officedocument.wordprocessingml.document"; String extension=pdf?"pdf":"docx"; return MultipartBody.Part.createFormData("document","contract."+extension,RequestBody.create(bytes,MediaType.parse(mediaType))); }
    private byte[] read(Uri uri)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();java.io.InputStream in=requireContext().getContentResolver().openInputStream(uri);byte[] b=new byte[8192];int n;while(in!=null&&(n=in.read(b))!=-1)out.write(b,0,n);if(in!=null)in.close();return out.toByteArray();}
    @Override public void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==PDF_PICKER&&result==android.app.Activity.RESULT_OK&&data!=null&&data.getData()!=null){selectedPdf=data.getData();toast("Đã chọn file PDF hoặc DOCX");}}
    private void date(EditText target){java.util.Calendar c=java.util.Calendar.getInstance();new DatePickerDialog(requireContext(),(v,y,m,d)->target.setText(String.format(Locale.ROOT,"%04d-%02d-%02d",y,m+1,d)),c.get(1),c.get(2),c.get(5)).show();}
    private String optional(EditText e){String s=e.getText().toString().trim();return s.isEmpty()?null:s;} private String value(Object o){return o==null?"":o.toString();} private String status(String s){return "ACTIVE".equalsIgnoreCase(s)?"Đang hoạt động":"Không hoạt động";} private void toast(String s){Toast.makeText(requireContext(),s,Toast.LENGTH_LONG).show();}
    private String error(Response<?> r,String fallback){try{if(r.errorBody()!=null){String m=new org.json.JSONObject(r.errorBody().string()).optString("message");if(!m.isEmpty())return m;}}catch(Exception ignored){}return fallback;}
}
