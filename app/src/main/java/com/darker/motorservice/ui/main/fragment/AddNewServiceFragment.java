package com.darker.motorservice.ui.main.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.darker.motorservice.R;
import com.darker.motorservice.database.ServiceDatabase;
import com.darker.motorservice.model.ServicesItem;
import com.darker.motorservice.ui.main.callback.ImageUploadCallback;
import com.darker.motorservice.utils.ImageUtils;
import com.darker.motorservice.utils.NetWorkUtils;
import com.darker.motorservice.utils.StringUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static com.darker.motorservice.R.drawable.cover;
import static com.darker.motorservice.utils.Constant.NEW_PASSWORD;
import static com.darker.motorservice.utils.Constant.SERVICE;
import static com.darker.motorservice.utils.Constant.STATUS;

public class AddNewServiceFragment extends Fragment {
    private EditText edName, edPosition, edPhoneNumber, edEmail;
    private Button btnAddNewStore;
    private String name, position, phoneNumber, email;
    private ProgressBar progressBar;
    private List<ServicesItem> servicesItemList;

    public AddNewServiceFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_new_service, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindView(view);
        loadEmail();
        onBtnAddClicked();
    }

    private void bindView(View view) {
        edName = (EditText) view.findViewById(R.id.form_name);
        edPosition = (EditText) view.findViewById(R.id.form_pos);
        edPhoneNumber = (EditText) view.findViewById(R.id.form_tel);
        edEmail = (EditText) view.findViewById(R.id.form_email);
        btnAddNewStore = (Button) view.findViewById(R.id.btn_add);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
    }

    private void loadEmail() {
        ServiceDatabase serviceDatabase = new ServiceDatabase(getContext());
        servicesItemList = serviceDatabase.getAllSerivce();
    }

    private boolean hasEmail(String email) {
        for (ServicesItem s : servicesItemList) {
            if (s.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    private void onBtnAddClicked() {
        btnAddNewStore.setOnClickListener(onClickListener());
    }

    @NonNull
    private View.OnClickListener onClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddNewStore.setClickable(false);
                getTextEditText();

                if (validateText()){
                    alert("กรุณากรอกข้อมูลให้ครบ");
                    return;
                }

                if (!StringUtils.isPhoneNumber(phoneNumber)) {
                    alert("เบอร์โทรศัพท์ไม่ถูกต้อง!");
                    return;
                }

                if (!StringUtils.isEmail(email)) {
                    alert("กรุณากรอกอีเมลที่ถูกต้อง!");
                    return;
                }

                if (hasEmail(email)) {
                    alert("อีเมลนี้ถูกใช้แล้ว! โปรดใช้อีเมลอื่น");
                    return;
                }

                if (NetWorkUtils.disable(getContext())) {
                    alert("เครือข่ายมีปัญหา! ไม่สามารถเพิ่มร้านได้");
                    return;
                }

                addNewService();
            }
        };
    }

    private boolean validateText() {
        if (StringUtils.stringOk(name) ||
                StringUtils.stringOk(position) ||
                StringUtils.stringOk(phoneNumber) ||
                StringUtils.stringOk(email)) {
            return false;
        }
        return true;
    }

    private void getTextEditText() {
        name = edName.getText().toString();
        position = edPosition.getText().toString();
        phoneNumber = edPhoneNumber.getText().toString();
        email = edEmail.getText().toString();
    }

    private void addNewService() {
        progressBar.setVisibility(View.VISIBLE);
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, NEW_PASSWORD).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isComplete()) {
                    String newUid = task.getResult().getUser().getUid();
                    addNewDataService(newUid);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void addNewDataService(String uid) {
        ServicesItem servicesItem = setServiceItem(uid);
        setValueInFirebase(uid, servicesItem);
        uploadImageToStorage(servicesItem);
        clearEditText();
    }

    private void uploadImageToStorage(ServicesItem servicesItem) {
        uploadImage(servicesItem.getCover(), cover);
        uploadImage(servicesItem.getPhoto(), R.drawable.pro);
    }

    private void setValueInFirebase(String uid, ServicesItem servicesItem) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(SERVICE).child(uid).setValue(servicesItem);
        dbRef.child(STATUS).child(uid).setValue(false);
    }

    private ServicesItem setServiceItem(String uid){
        String imgName = email.split("@")[0];
        String imgCover = imgName + "_cover.png";
        String imgPro = imgName + "_pro.png";
        String latlng = getString(R.string.default_latlng);
        String work = getString(R.string.default_time_work);
        String servies = getString(R.string.default_services);
        String distribute = getString(R.string.default_distribute);

        ServicesItem servicesItem = new ServicesItem();
        servicesItem.setId(uid);
        servicesItem.setName(name);
        servicesItem.setPos(position);
        servicesItem.setEmail(email);
        servicesItem.setTel(phoneNumber);
        servicesItem.setPhoto(imgPro);
        servicesItem.setCover(imgCover);
        servicesItem.setLatlng(latlng);
        servicesItem.setWorkTime(work);
        servicesItem.setService(servies);
        servicesItem.setDistribute(distribute);

        return servicesItem;
    }

    private void uploadImage(final String image, int draw) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), draw);
        ImageUtils.uploadImage(image, bitmap, getImageUploadCallback());
    }

    @NonNull
    private ImageUploadCallback getImageUploadCallback() {
        return new ImageUploadCallback() {

            @Override
            public void onSuccess(String imageName, Bitmap bitmap) {
                progressBar.setVisibility(View.GONE);
                alert(imageName + " upload เรียบร้อย");
            }

            @Override
            public void onFailure(String imageName) {
                progressBar.setVisibility(View.GONE);
                alert(imageName + " upload ไม่ได้!");
            }
        };
    }

    private void alert(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        btnAddNewStore.setClickable(true);
    }

    private void clearEditText() {
        edName.setText("");
        edName.setText("");
        edPosition.setText("");
        edPhoneNumber.setText("");
        edEmail.setText("");
    }
}
