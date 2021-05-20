package com.sabiha.cardmaker.ui.mycards;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sabiha.cardmaker.BuildConfig;
import com.sabiha.cardmaker.CardDetailsActivity;
import com.sabiha.cardmaker.R;
import com.sabiha.cardmaker.firebase_model.MyCards;
import com.sabiha.cardmaker.ui.BaseFragment;
import com.sabiha.cardmaker.util.Constant;
import com.sabiha.cardmaker.viewholder.FeaturedCardsViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MyCardsFragment extends BaseFragment {
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    File file, picDir;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_cards, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("mycard").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        recyclerView = view.findViewById(R.id.rvProduct);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        FirebaseRecyclerOptions<MyCards> options =
                new FirebaseRecyclerOptions.Builder<MyCards>()
                        .setQuery(databaseReference, MyCards.class)
                        .build();

        FirebaseRecyclerAdapter<MyCards, FeaturedCardsViewHolder> adapter =
                new FirebaseRecyclerAdapter<MyCards, FeaturedCardsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FeaturedCardsViewHolder holder, int position, @NonNull final MyCards model) {

                        Picasso.get().load(model.getEditedcard()).into(holder.ivProductImage);


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String options[] = getResources().getStringArray(R.array.array_cart_options);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Options");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialogInterface, int i) {
                                        if (i == 0) {//edit
                                            Intent intent = new Intent(getActivity(), CardDetailsActivity.class);
                                            intent.putExtra(Constant.CID, model.getId());
                                            intent.putExtra(Constant.FRAGMENT, Constant.MY_CARDS_FRAGMENT);//0
                                            System.out.println("pid" + model.getId());
                                            startActivity(intent);
                                            showToast("clicked");
                                        }
                                        if (i == 1) {//remove
                                            showToast("remove click");
                                            DatabaseReference removeRef = FirebaseDatabase.getInstance().getReference("mycard")
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(model.getId());
                                            removeRef.removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                showToast("Item removed successfully");

                                                            }

                                                        }
                                                    });
                                        }
                                        if (i == 2) {//share
                                            showToast("share click");
                                            shareImage(model.getEditedcard());
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FeaturedCardsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.featured_card_item_layout, parent, false);
                        FeaturedCardsViewHolder holder = new FeaturedCardsViewHolder(view);
                        return holder;

                    }
                };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void shareImage(String editedcard) {
        baseActivity.startProgressDialog();
        Picasso.get().load(editedcard).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                picDir = null;
                picDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CardMaker");

                if (!picDir.exists()) {
                    picDir.mkdirs();
                    // Toast.makeText(context,"Done",Toast.LENGTH_SHORT).show();
                    showToast("Done");
                }
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
                        Intent i = new Intent();
                        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri photoURI = FileProvider.getUriForFile(getContext(),
                                BuildConfig.APPLICATION_ID + ".fileprovider", file);
                        i.setAction(Intent.ACTION_SEND);
                        i.setType("image/*");
                        i.putExtra(Intent.EXTRA_SUBJECT, "Sending Image of My Created Cards.");
                       // i.putExtra(Intent.EXTRA_TEXT, "PLEASE CHECK THE LINK TO DOWNLOAD THE APP \n https://drive.google.com/open?id=1P0mxx_nJgQpbubXDDb7iP49irJadIACX");
                        i.putExtra(Intent.EXTRA_STREAM, photoURI);
                        try {
                            getContext().startActivity(Intent.createChooser(i, "Share My Cards"));
                        } catch (ActivityNotFoundException e) {
                           // Toast.makeText(getContext(), "No App Available", Toast.LENGTH_SHORT).show();
                            showToast("No App Available");
                        }
                        //Toast.makeText(context, "Sharing", Toast.LENGTH_SHORT).show();
                        showToast("Sharing");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                baseActivity.stopProgressDialog();

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }
}