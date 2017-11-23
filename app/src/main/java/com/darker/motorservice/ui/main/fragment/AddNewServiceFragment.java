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
import com.darker.motorservice.utils.NetWorkUtils;
import com.darker.motorservice.utils.StringUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
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

                if (validateText()) return;

                if (StringUtils.isPhoneNumber(phoneNumber)) {
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
            alert("กรุณากรอกข้อมูลให้ครบ");
            return true;
        }
        return false;
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
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        String imgName = email.split("@")[0];
        String imgCover = imgName + "_cover.png";
        String imgPro = imgName + "_pro.png";
        String latlng = "14.8823047,102.0206782";
        String work = "วันจันทร์-ศุกร์ เวลา 08:00-12:00, 13:00-20:00 น.\nวันเสาร์-อาทิตย์ เวลา 08:00-12:00 น.\nปิดเทอม เวลา 09:00-17:00 น.";
        String s = getResources().getString(R.string.t1);
        String d = getResources().getString(R.string.t2);
        ServicesItem servicesItem = new ServicesItem(uid, name, position, email, phoneNumber, imgPro, imgCover, latlng, work, s, d);
        dbRef.child(SERVICE).child(uid).setValue(servicesItem);
        dbRef.child(STATUS).child(uid).setValue(false);
        uploadImg(imgCover, cover);
        uploadImg(imgPro, R.drawable.pro);
        clearEditText();
    }

    private void uploadImg(final String image, int draw) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), draw);
        StorageReference mountainsRef = FirebaseStorage.getInstance().getReference().child(image);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressBar.setVisibility(View.GONE);
                alert(image + " upload ไม่ได้!");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressBar.setVisibility(View.GONE);
                alert(image + " upload เรียบร้อย");
            }
        });
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
