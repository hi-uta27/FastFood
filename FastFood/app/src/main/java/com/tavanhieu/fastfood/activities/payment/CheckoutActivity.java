package com.tavanhieu.fastfood.activities.payment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.tavanhieu.fastfood.R;
import com.tavanhieu.fastfood.adapters.CartAdapter;
import com.tavanhieu.fastfood.my_class.BuyProduct;
import com.tavanhieu.fastfood.my_class.MyDate;
import com.tavanhieu.fastfood.my_class.Payment;
import com.tavanhieu.fastfood.my_class.User;
import com.tavanhieu.fastfood.view_model.MyViewModel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private EditText edtUserName, edtPhoneNumber, edtAddress;
    private TextView txtEdit, txtItemTotal, txtShipping, txtTotalPayment;
    private RecyclerView rcvDanhSach;
    private CartAdapter adapter;
    private Button btnPayment;

    private MyViewModel myViewModel;
    private Payment payment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        anhXa();
        myOnClick();
        loadData();
    }

    private void myOnClick() {
        btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payment();
            }
        });

        txtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDeliveryAddress();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateDeliveryAddress() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(R.layout.activity_dialog_delivery_address)
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                })
                .setPositiveButton("Ok", (dialog, which) -> {
                    EditText name, phone, address;
                    //??nh x???:
                    name = ((Dialog) dialog).findViewById(R.id.edt_user_name);
                    phone = ((Dialog) dialog).findViewById(R.id.edt_phone_number);
                    address = ((Dialog) dialog).findViewById(R.id.edt_address);
                    //Set d??? li???u:
                    edtUserName.setText(name.getText().toString().trim());
                    edtAddress.setText(address.getText().toString().trim());
                    edtPhoneNumber.setText("(+84) " + phone.getText().toString().trim());
                })
                .show();
    }

    @SuppressLint("SimpleDateFormat")
    private void payment() {
        if (payment != null && payment.getArrBuyProduct().size() != 0) {
            //c???p nh???t th??ng tin ng?????i ?????t h??ng:
            payment.setUser(new User(payment.getUser().getUid(),
                    edtUserName.getText().toString().trim(),
                    edtAddress.getText().toString().trim(),
                    edtPhoneNumber.getText().toString().trim()));
            //C???p nh???t s???n ph???m mua l??n firebase:
            FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("TablePayment")
                    .child(payment.getUser().getUid())
                    .child(payment.getId())
                    .setValue(payment);
            //X??a b??? danh s??ch s???n ph???m trong gi???:
            myViewModel.deleteTable(this);
            Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Don't have product to payment!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData() {

        //Load th??ng tin ng?????i d??ng:
        SharedPreferences sharedPreferences = getSharedPreferences("InformationUser", MODE_PRIVATE);
        edtUserName.setText(sharedPreferences.getString("UserName", null));
//        edtPhoneNumber.setText(sharedPreferences.getString("PhoneNumber", null));
        edtAddress.setText(sharedPreferences.getString("Address", null));

        //ViewModel: l???ng nghe thay ?????i d??? li???u tr??n database v?? c???p nh???t s??? thay ?????i l??n giao di???n
        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        myViewModel.getListForLiveData(this).observe(this, new androidx.lifecycle.Observer<List<BuyProduct>>() {
            @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
            @Override
            public void onChanged(List<BuyProduct> buyProducts) { //h??m l???ng nghe s??? thay ?????i c???a d??? li???u
                adapter = new CartAdapter(buyProducts);
                rcvDanhSach.setAdapter(adapter);

                //hi???n th??? t???ng ti???n c???a c??c s???n ph???m:
                float sum = 0;
                for (BuyProduct item : buyProducts) {
                    sum += item.getPrice() * item.getNumber();
                }

                //T??nh t???ng ti???n, gi?? ship,...
                //N???u danh s??ch s???n ph???m mua b??? x??a h???t -> th?? s??? set v??? $0.00
                float ship = 0;
                if (buyProducts.size() > 0) {
                    ship = sum * 0.05f;
                    DecimalFormat df = new DecimalFormat("0.00");
                    txtShipping.setText("$" + df.format(ship));
                    txtItemTotal.setText("$" + df.format(sum));
                    txtTotalPayment.setText("$" + df.format(sum + ship));
                } else {
                    txtShipping.setText("$0.00");
                    txtItemTotal.setText("$0.00");
                    txtTotalPayment.setText("$0.00");
                }

                //T???o d??? li???u cho payment ????? add l??n firebase:
                User user = new User(
                        sharedPreferences.getString("Uid", null),
                        sharedPreferences.getString("UserName", null),
                        sharedPreferences.getString("Email", null),
                        sharedPreferences.getString("DateOfBirth", null),
                        sharedPreferences.getString("Address", null),
                        null);
                Calendar c = Calendar.getInstance();
                payment = new Payment(
                        user,
                        buyProducts,
                        sum,
                        ship,
                        sum + ship,
                        "ToPay",
                        new MyDate(
                                c.get(Calendar.DAY_OF_MONTH),
                                c.get(Calendar.MONTH) + 1,
                                c.get(Calendar.YEAR),
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                c.get(Calendar.SECOND)
                        ));
            }
        });
    }

    private void anhXa() {
        edtPhoneNumber = findViewById(R.id.txt_phone_number);
        edtUserName = findViewById(R.id.txt_user_name);
        edtAddress = findViewById(R.id.txt_address);
        txtTotalPayment = findViewById(R.id.txt_total_payment);
        txtItemTotal = findViewById(R.id.txt_item_total);
        txtShipping = findViewById(R.id.txt_shipping);
        txtEdit = findViewById(R.id.txt_edit);
        btnPayment = findViewById(R.id.btn_payment);
        rcvDanhSach = findViewById(R.id.rcv_list_checkout);
    }
}