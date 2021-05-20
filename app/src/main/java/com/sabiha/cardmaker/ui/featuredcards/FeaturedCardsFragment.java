package com.sabiha.cardmaker.ui.featuredcards;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sabiha.cardmaker.CardDetailsActivity;
import com.sabiha.cardmaker.R;
import com.sabiha.cardmaker.firebase_model.FeaturedCards;
import com.sabiha.cardmaker.ui.BaseFragment;
import com.sabiha.cardmaker.util.Constant;
import com.sabiha.cardmaker.viewholder.FeaturedCardsViewHolder;
import com.squareup.picasso.Picasso;

public class FeaturedCardsFragment extends BaseFragment {
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_featured_cards, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("featuredcards");

        recyclerView = view.findViewById(R.id.rvProduct);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        FirebaseRecyclerOptions<FeaturedCards> options =
                new FirebaseRecyclerOptions.Builder<FeaturedCards>()
                        .setQuery(databaseReference, FeaturedCards.class)
                        .build();

        FirebaseRecyclerAdapter<FeaturedCards, FeaturedCardsViewHolder> adapter =
                new FirebaseRecyclerAdapter<FeaturedCards, FeaturedCardsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FeaturedCardsViewHolder holder, int position, @NonNull final FeaturedCards model) {

                        Picasso.get().load(model.getImage()).into(holder.ivCardBackground);


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getActivity(), CardDetailsActivity.class);
                                intent.putExtra(Constant.CID, model.getPid());
                                intent.putExtra(Constant.FRAGMENT, Constant.FEATURED_CARDS_FRAGMENT);//1
                                System.out.println("pid" + model.getPid());
                                startActivity(intent);
                                showToast("clicked");
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
}