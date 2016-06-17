/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.matt.movieWatchList.viewControllers.fragments.shows;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.matt.movieWatchList.Models.Realm.JSONShow;
import com.example.matt.movieWatchList.MyApplication;
import com.example.matt.movieWatchList.R;
import com.example.matt.movieWatchList.viewControllers.activities.shows.TVShowWatchListDetailActivity;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Provides UI for the view with Cards.
 */
public class TVShowWatchListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);

        boolean isWatched;

        if (getArguments().getInt("watched") == 1) {
            isWatched = true;
        }
        else {
            isWatched = false;
        }

        ContentAdapter cardAdapter = new ContentAdapter((MyApplication) getActivity().getApplication(), getActivity(), isWatched);
        recyclerView.setAdapter(cardAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return  recyclerView;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(final LayoutInflater inflater, ViewGroup parent, final RealmResults<JSONShow> movieList, final Realm uiRealm, final ContentAdapter adapter, final boolean isWatched) {
                super(inflater.inflate(R.layout.watch_list_card, parent, false));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        JSONShow movie = movieList.get(getAdapterPosition());
                        Intent intent = new Intent(context, TVShowWatchListDetailActivity.class);
                        Log.d("ViewHodler()",movie.getName());
                        Log.d("ViewHodler()",Integer.toString(movie.getId()));

                        intent.putExtra("showID", movie.getId());
                        context.startActivity(intent);
                    }
                });

                // Adding Snackbar to Action Button inside card

                Button watchButton = (Button)itemView.findViewById(R.id.watch_button);
                if (isWatched) {
                    watchButton.setVisibility(View.GONE);
                }
                else {
                    watchButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            JSONShow movie = movieList.get(getAdapterPosition());
                            uiRealm.beginTransaction();
                            //JSONMovie movieToAdd = uiRealm.createObject(movie);
                            movie.setWatched(true);
                            movie.setOnWatchList(false);
                            uiRealm.commitTransaction();

                            adapter.notifyDataSetChanged();

                            Snackbar.make(v, "Watched!",
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
                }


                // Adding Snackbar to Action Button inside card
                Button removeButton = (Button)itemView.findViewById(R.id.remove_button);
                removeButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        JSONShow show = movieList.get(getAdapterPosition());

                        uiRealm.beginTransaction();
                        //JSONMovie movieToAdd = uiRealm.createObject(movie);
                        RealmResults<JSONShow> result1 = uiRealm.where(JSONShow.class)
                                .equalTo("name", show.getName())
                                .findAll();
                        result1.clear();
                        uiRealm.commitTransaction();
                        adapter.notifyDataSetChanged();

                        Snackbar.make(v, "Removed from your shows",
                                Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        }

        /**
         * Adapter to display recycler view.
         */
        public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
            // Set numbers of Card in RecyclerView.
            private Realm uiRealm;
            private RealmResults<JSONShow> showList;
            private Activity activity;
            private boolean isWatched;

            public ContentAdapter(MyApplication app, Activity activity, boolean isWatched) {
                uiRealm = app.getUiRealm();
                this.isWatched = isWatched;

                // Build the query looking at all users:
                RealmQuery<JSONShow> query = uiRealm.where(JSONShow.class);

                // Execute the query:
                if (isWatched){
                    RealmResults<JSONShow> shows = query.equalTo("isWatched", true).findAll();
                    this.activity = activity;
                    showList = shows;
                } else {
                    RealmResults<JSONShow> shows = query.findAll();
                    this.activity = activity;
                    showList = shows;
                }
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(parent.getContext()), parent, showList, uiRealm, this, isWatched);
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                TextView title = (TextView) holder.itemView.findViewById(R.id.card_title);
                TextView genre = (TextView) holder.itemView.findViewById(R.id.card_text);
                ImageView coverArt = (ImageView) holder.itemView.findViewById(R.id.card_image);

                Bitmap bmp;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                if (showList.get(position).getBackdropBitmap() != null) {
                    bmp = BitmapFactory.decodeByteArray(showList.get(position).getBackdropBitmap(), 0, showList.get(position).getBackdropBitmap().length, options);
                    coverArt.setImageBitmap(bmp);
                }

                title.setText(showList.get(position).getName());

                title.setText(showList.get(position).getName());
            Typeface type = Typeface.createFromAsset(this.activity.getAssets(),"fonts/Lobster-Regular.ttf");
            title.setTypeface(type);

            genre.setText(showList.get(position).getOverview());
        }

        @Override
        public int getItemCount() {
            return showList.size();
        }
    }
}
