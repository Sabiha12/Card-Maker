package com.sabiha.cardmaker;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sabiha.cardmaker.databinding.ActivityCardDetailsBinding;
import com.sabiha.cardmaker.firebase_model.FeaturedCards;
import com.sabiha.cardmaker.firebase_model.MyCards;
import com.sabiha.cardmaker.util.Constant;
import com.sabiha.cardmaker.util.FileCompressor;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

public class CardDetailsActivity extends BaseActivity {
    ActivityCardDetailsBinding activityCardDetailsBinding;
    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    BottomSheetDialog bottomSheetDialog;
    TextView tvName, tvDesignation, tvSkill1, tvSkill2, tvSkill3, tvMobile, tvEmail;
    ImageView ivCard;
    Button btnSave, btnShare;
    String cardID, cardTemp, name, designation, skill1, skill2, skill3, mobile, email, emailPattern, mobilePattern, strCurrentPhotoPath;

    FileCompressor fileCompressor;
    File mPhotoFile, fileImage, file;
    RelativeLayout rlCard;
    ConstraintLayout clCard;
    Bitmap bitmap;
    Uri _uriPhoto;
    File picDir;
    private UploadTask uploadTask;
    private StorageReference mStorageRef;
    FirebaseDatabase firebaseDatabase;
    private String photoStringLink;
    int fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCardDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_card_details);

        cardID = getIntent().getStringExtra(Constant.CID);
        fragment = getIntent().getIntExtra(Constant.FRAGMENT, 1); // 0 = MyCardsFragment, 1 =FeaturedCardsFragment
        System.out.println("prId" + cardID);
        //user_id = sharedPrefStore.getString(Constant.UID);
        getCardDetails(cardID);
        mAuth = FirebaseAuth.getInstance();

        init();
    }

    private void init() {
        fileCompressor = new FileCompressor(this);

        mStorageRef = FirebaseStorage.getInstance().getReference("editcard");
        setSupportActionBar(activityCardDetailsBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activityCardDetailsBinding.btnPreview.setOnClickListener(this);
        // activityCardDetailsBinding.btnShare.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPreview:
                if (formValidation()) {
                    showBottomSheetPreview();
                }
                //addToCartList();
                break;
//            case R.id.btnShare:
//                gotoConfirmOrderActivity();
//                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void showBottomSheetPreview() {
        bottomSheetDialog = new BottomSheetDialog(this, R.style.bottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_preview_card, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        ivCard = view.findViewById(R.id.ivCard);
        tvName = view.findViewById(R.id.tvName);
        tvDesignation = view.findViewById(R.id.tvDesignation);
        tvSkill1 = view.findViewById(R.id.tvSkill1);
        tvSkill2 = view.findViewById(R.id.tvSkill2);
        tvSkill3 = view.findViewById(R.id.tvSkill3);
        tvMobile = view.findViewById(R.id.tvMobile);
        tvEmail = view.findViewById(R.id.tvEmail);

        btnSave = view.findViewById(R.id.btnSave);
        btnShare = view.findViewById(R.id.btnShare);
        clCard = view.findViewById(R.id.clCard);
        rlCard = view.findViewById(R.id.rlCard);

        btnSave.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Save Button Clicked");
                bottomSheetDialog.dismiss();
                //takeScreenShot();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkPermission()) {
                        takeScreenShot();
                    } else {
                        requestPermission();
                    }
                }

            }
        });
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Share Button Clicked");
                //takeScreenShot();
                bottomSheetDialog.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkPermission()) {
                        takeScreenShot();
                        if (file != null) {
                            shareImage(file);
                        }
                    } else {
                        requestPermission();
                    }
                }
            }
        });
        setData();
    }


    private void takeScreenShot() {
        clCard.setDrawingCacheEnabled(true);
        clCard.buildDrawingCache(true);
        bitmap = Bitmap.createBitmap(clCard.getDrawingCache());
        clCard.setDrawingCacheEnabled(false);

        try {
            if (bitmap != null) {
                saveScreenshoot(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void shareImage(File file) {

        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file);

        //No need to do mimeType work or ext

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Sending Image of My Created Cards.");
        //intent.putExtra(Intent.EXTRA_TEXT, "PLEASE CHECK THE LINK TO DOWNLOAD THE APP \n https://drive.google.com/open?id=1r1vXWMG49LHD_uYQhMEXUTy8eCfpPqjD");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/*");
        try {
            startActivity(Intent.createChooser(intent, "Share Image:"));
        } catch (ActivityNotFoundException e) {
            showToast("No app Available");
        }



      /*  Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(Intent.EXTRA_SUBJECT, "Sending Image of My Created Cards.");
        intent.putExtra(Intent.EXTRA_TEXT, "PLEASE CHECK THE LINK TO DOWNLOAD THE APP \n https://drive.google.com/open?id=1r1vXWMG49LHD_uYQhMEXUTy8eCfpPqjD");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Share My Cards"));
        } catch (ActivityNotFoundException e) {
            //Toast.makeText(mContext, "No App Available", Toast.LENGTH_SHORT).show();
            showToast("No app Available");
        }*/
    }

    private void saveScreenshoot(Bitmap bitmap) {
        //do your work
        picDir = null;
        picDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CardMaker");

        if (!picDir.exists()) {
            picDir.mkdirs();
            Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
        }


      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                //do your work
                picDir = null;
                picDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"CardMaker");

                if (!picDir.exists()){
                    picDir.mkdirs();
                    Toast.makeText(this,"Done",Toast.LENGTH_SHORT).show();
                }
            } else {
                requestPermission();
            }
        }*/

        ByteArrayOutputStream bao = null;
        file = null;
        try {
            bao = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
            String filename = picDir.getPath() + File.separator + System.currentTimeMillis() + ".jpg";
            file = new File(filename);
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bao.toByteArray());
            fos.close();

            if (file != null) {
                uploadFile(file);
                Toast.makeText(this, "Added to Mycard", Toast.LENGTH_SHORT).show();
                // Toast.makeText(mContext,System.currentTimeMillis()+".jpg",Toast.LENGTH_LONG).show();
            }

        } catch (
                Exception e) {
            e.printStackTrace();
        }
        //scanGallery(mContext,file.getAbsolutePath());
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(File file) {
        Uri uri = Uri.fromFile(file);
        if (uri != null && fragment == Constant.FEATURED_CARDS_FRAGMENT) {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(uri));
            uploadTask = fileReference.putFile(uri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();
                        showToast("Successfully uploaded");
                        if (downloadUri != null) {
                            photoStringLink = downloadUri.toString(); //YOU WILL GET THE DOWNLOAD URL HERE !!!!
                            System.out.println("onComplete" + photoStringLink);

                            if (photoStringLink != null) {
                                String key = FirebaseDatabase.getInstance().getReference("mycard")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().getKey();

                                DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference("mycard")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key);
                                HashMap<String, Object> result = new HashMap<>();
                                result.put("id", key);
                                result.put("editedcard", photoStringLink);
                                result.put("cardtemp", cardTemp);
                                result.put("name", name);
                                result.put("designation", designation);
                                result.put("skill1", skill1);
                                result.put("skill2", skill2);
                                result.put("skill3", skill3);
                                result.put("mobile", mobile);
                                result.put("email", email);

                                tempRef.updateChildren(result);

                               /* HashMap<String, Object> result1 = new HashMap<>();
                                result1.put("id", key);
                                tempRef.updateChildren(result1);*/
                            }
                        }
                    } else {
                        // Handle failures
                        // ...
                        showToast("Upload failed");
                    }
                }
            });
        } else if (uri != null && fragment == Constant.MY_CARDS_FRAGMENT) {

            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(uri));
            uploadTask = fileReference.putFile(uri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();
                        showToast("Successfully uploaded");
                        if (downloadUri != null) {
                            photoStringLink = downloadUri.toString(); //YOU WILL GET THE DOWNLOAD URL HERE !!!!
                            System.out.println("onComplete" + photoStringLink);

                            if (photoStringLink != null) {

                                String key = FirebaseDatabase.getInstance().getReference("mycard")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push().getKey();

                                DatabaseReference tempRef = FirebaseDatabase.getInstance().getReference("mycard")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key);
                                HashMap<String, Object> result = new HashMap<>();
                                result.put("id", key);
                                result.put("editedcard", photoStringLink);
                                result.put("cardtemp", cardTemp);
                                result.put("name", name);
                                result.put("designation", designation);
                                result.put("skill1", skill1);
                                result.put("skill2", skill2);
                                result.put("skill3", skill3);
                                result.put("mobile", mobile);
                                result.put("email", email);

                                tempRef.updateChildren(result);

                               /* HashMap<String, Object> result1 = new HashMap<>();
                                result1.put("id", key);
                                tempRef.updateChildren(result1);*/
                            }
                        }
                        DatabaseReference removeRef = FirebaseDatabase.getInstance().getReference("mycard")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(cardID);
                        removeRef.removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            showToast("Item removed successfully");

                                        }

                                    }
                                });
                    } else {
                        // Handle failures
                        // ...
                        showToast("Upload failed");
                    }
                }
            });

        }
    }

    protected void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    protected boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }


   /* private void takeScreenShot() {

        String now = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            String mPath = storageDir + "/" + now + ".jpg";
            clCard.setDrawingCacheEnabled(true);
            clCard.buildDrawingCache(true);
            bitmap = Bitmap.createBitmap(clCard.getDrawingCache());
            clCard.setDrawingCacheEnabled(false);


            FileOutputStream outputStream = new FileOutputStream(mPath);
            int quality = 100;
            file = new File(String.valueOf(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)));
            outputStream.flush();
            outputStream.close();

        } catch (Throwable e) {

            e.printStackTrace();
        }
    }*/

    private void setData() {
        Picasso.get().load(cardTemp).into(ivCard);
        System.out.println("cardTemp" + cardTemp);
        tvName.setText(name);
        tvDesignation.setText(designation);
        tvSkill1.setText(skill1);
        tvSkill2.setText(skill2);
        tvMobile.setText(mobile);
        tvEmail.setText(email);
    }

    private boolean formValidation() {
        email = activityCardDetailsBinding.etEmail.getText().toString();
        name = activityCardDetailsBinding.etName.getText().toString();
        designation = activityCardDetailsBinding.etDesignation.getText().toString();
        skill1 = activityCardDetailsBinding.etSkill1.getText().toString();
        skill2 = activityCardDetailsBinding.etSkill2.getText().toString();
        skill3 = activityCardDetailsBinding.etSkill3.getText().toString();
        mobile = activityCardDetailsBinding.etMobile.getText().toString();

        emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"; //accept ex: abc@gmail.com
        mobilePattern = "^(?:\\+?88)?01[13-9]\\d{8}$"; //accept +880********* or 0********* @[13-9] means 1 or 3 or 4 or 5 or 6 or 7 or 8 or 9

        if (activityCardDetailsBinding.etName.getText().toString().equals("")) {
            activityCardDetailsBinding.etName.setError(getString(R.string.name_required));
            activityCardDetailsBinding.etName.requestFocus();
            return false;
        }
        if (activityCardDetailsBinding.etDesignation.getText().toString().equals("")) {
            activityCardDetailsBinding.etDesignation.setError(getString(R.string.designation_required));
            activityCardDetailsBinding.etDesignation.requestFocus();
            return false;
        }
        if (activityCardDetailsBinding.etSkill1.getText().toString().equals("")) {
            activityCardDetailsBinding.etSkill1.setError(getString(R.string.skill1_required));
            activityCardDetailsBinding.etSkill1.requestFocus();
            return false;
        }
        if (activityCardDetailsBinding.etSkill2.getText().toString().equals("")) {
            activityCardDetailsBinding.etSkill2.setError(getString(R.string.skill2_required));
            activityCardDetailsBinding.etSkill2.requestFocus();
            return false;
        }
        if (activityCardDetailsBinding.etSkill3.getText().toString().equals("")) {
            activityCardDetailsBinding.etSkill3.setError(getString(R.string.skill3_required));
            activityCardDetailsBinding.etSkill3.requestFocus();
            return false;
        }
        if (activityCardDetailsBinding.etMobile.getText().toString().equals("")) {
            activityCardDetailsBinding.etMobile.setError(getString(R.string.mobile_number_required));
            activityCardDetailsBinding.etMobile.requestFocus();
            return false;
        }
        if (!mobile.matches(mobilePattern) && mobile.length() > 0) {
            activityCardDetailsBinding.etMobile.setError(getString(R.string.mobile_invalid));
            activityCardDetailsBinding.etMobile.requestFocus();
            return false;
        }

        if (activityCardDetailsBinding.etEmail.getText().toString().equals("")) {
            activityCardDetailsBinding.etEmail.setError(getString(R.string.email_required));
            activityCardDetailsBinding.etEmail.requestFocus();
            return false;
        }
        if (!email.matches(emailPattern) && email.length() > 0) {
            activityCardDetailsBinding.etEmail.setError(getString(R.string.email_invalid));
            activityCardDetailsBinding.etEmail.requestFocus();
            return false;
        }
        return true;
    }



    private void getCardDetails(String cardID) {
        if (fragment == Constant.FEATURED_CARDS_FRAGMENT) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("featuredcards");

            databaseReference.child(cardID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        FeaturedCards featuredCards = snapshot.getValue(FeaturedCards.class);
                        cardTemp = featuredCards.getImage();
                        System.out.println("cardTempDetailsFeature" + cardTemp);
                        Picasso.get().load(featuredCards.getImage()).into(activityCardDetailsBinding.ivCard);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("mycard").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            databaseReference.child(cardID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        MyCards myCards = snapshot.getValue(MyCards.class);
               /*     productPrice = String.valueOf(featuredCards.getPrice());
                    pName = featuredCards.getName();
                    activityCardDetailsBinding.tvProductName.setText(pName);
                    activityCardDetailsBinding.tvProductDesc.setText(getResources().getString(R.string.product_description_dot) + " " + products.getDescription());
                    activityCardDetailsBinding.tvProductPrice.setText(productPrice + "TK");*/

                        cardTemp = myCards.getCardtemp();
                        Picasso.get().load(myCards.getCardtemp()).into(activityCardDetailsBinding.ivCard);
                        activityCardDetailsBinding.etName.setText(myCards.getName());
                        activityCardDetailsBinding.etDesignation.setText(myCards.getDesignation());
                        activityCardDetailsBinding.etSkill1.setText(myCards.getSkill1());
                        activityCardDetailsBinding.etSkill2.setText(myCards.getSkill2());
                        activityCardDetailsBinding.etSkill3.setText(myCards.getSkill3());
                        activityCardDetailsBinding.etMobile.setText(myCards.getMobile());
                        activityCardDetailsBinding.etEmail.setText(myCards.getEmail());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
}