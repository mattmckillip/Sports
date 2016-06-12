package com.example.matt.movieWatchList.viewControllers.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.matt.movieWatchList.Models.POJO.Result;
import com.example.matt.movieWatchList.Models.Realm.JSONCast;
import com.example.matt.movieWatchList.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.realm.RealmList;

/**
 * Created by Matt on 6/11/2016.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private List<Result> movies;
    private Context context;

    public SearchAdapter(List<Result> movies, Context context) {
        this.movies = movies;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @Override
    public void onBindViewHolder(SearchViewHolder searchViewHolder, int i) {
        Result movie = movies.get(i);
        searchViewHolder.movieTitle.setText(movie.getTitle());
        Typeface type = Typeface.createFromAsset(context.getAssets(), "fonts/Lobster-Regular.ttf");
        searchViewHolder.movieTitle.setTypeface(type);
        searchViewHolder.movieDescription.setText(movie.getOverview());
        searchViewHolder.watchedLayout.setVisibility(View.GONE);
        searchViewHolder.watchListLayout.setVisibility(View.GONE);

        Picasso.with(context)
                .load("https://image.tmdb.org/t/p/w300//" + movie.getBackdropPath())
                //.placeholder(R.drawable.unkown_person)
                .error(R.drawable.generic_movie_background)
                .into(searchViewHolder.movieImage);
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.item_card, viewGroup, false);

        return new SearchViewHolder(itemView);
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {

        protected TextView movieTitle;
        protected TextView movieDescription;
        protected ImageView movieImage;
        protected RelativeLayout watchedLayout;
        protected RelativeLayout watchListLayout;

        public SearchViewHolder(View v) {
            super(v);

            movieTitle =  (TextView) v.findViewById(R.id.card_title);
            movieDescription = (TextView)  v.findViewById(R.id.card_text);
            movieImage = (ImageView)  v.findViewById(R.id.card_image);

            watchedLayout = (RelativeLayout)  v.findViewById(R.id.watched_layout);
            watchListLayout = (RelativeLayout)  v.findViewById(R.id.watch_list_layout);
        }
    }
}
