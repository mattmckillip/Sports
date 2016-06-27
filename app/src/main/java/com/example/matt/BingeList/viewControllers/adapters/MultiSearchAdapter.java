package com.example.matt.bingeList.viewControllers.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.matt.bingeList.models.Cast;
import com.example.matt.bingeList.models.Credits;
import com.example.matt.bingeList.models.Crew;
import com.example.matt.bingeList.models.MultiSearchResult;
import com.example.matt.bingeList.models.movies.Movie;
import com.example.matt.bingeList.models.shows.TVShow;
import com.example.matt.bingeList.R;
import com.example.matt.bingeList.uitls.API.MovieAPI;
import com.example.matt.bingeList.viewControllers.activities.movies.MovieBrowseDetailActivity;
import com.example.matt.bingeList.viewControllers.activities.shows.TVShowBrowseDetailActivity;
import com.mikepenz.iconics.view.IconicsButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MultiSearchAdapter extends RecyclerView.Adapter<MultiSearchAdapter.SearchViewHolder> {
    private static final String MOVIE_TYPE = "movie";
    private static final String SHOW_TYPE = "tv";

    private List<MultiSearchResult> mMultiSearchResults;
    private Context mContext;
    private Realm mUIrealm;

    public MultiSearchAdapter(List<MultiSearchResult> results, Context context, Realm uiRealm) {
        mMultiSearchResults = results;
        mContext = context;
        mUIrealm = uiRealm;
    }

    @Override
    public int getItemCount() {
        return mMultiSearchResults.size();
    }

    @Override
    public void onBindViewHolder(SearchViewHolder searchViewHolder, int i) {
        MultiSearchResult result = mMultiSearchResults.get(i);

        searchViewHolder.progressSpinner.setVisibility(View.GONE);
        searchViewHolder.watchedLayout.setVisibility(View.GONE);
        searchViewHolder.watchListLayout.setVisibility(View.GONE);

        if (result.getMediaType().equals(MOVIE_TYPE)) {
            searchViewHolder.mediaTitle.setText(result.getTitle());
            searchViewHolder.actionButton.setText("{gmd_add_to_queue} add to watchlist");

            if (mUIrealm.where(Movie.class).equalTo("id", result.getId()).equalTo("isWatched", true).findAll().size() == 1) {
                searchViewHolder.watchedLayout.setVisibility(View.VISIBLE);
                searchViewHolder.actionButton.setVisibility(View.GONE);
            } else if (mUIrealm.where(Movie.class).equalTo("id", result.getId()).equalTo("onWatchList", true).findAll().size() == 1) {
                searchViewHolder.watchedLayout.setVisibility(View.VISIBLE);
                searchViewHolder.actionButton.setVisibility(View.GONE);
            }
        } else if (result.getMediaType().equals(SHOW_TYPE)) {
            searchViewHolder.mediaTitle.setText(result.getName());
            searchViewHolder.actionButton.setText("{gmd_add_to_queue} add to your shows");

            if (mUIrealm.where(TVShow.class).equalTo("id", result.getId()).equalTo("isWatched", true).findAll().size() == 1) {
                searchViewHolder.watchedLayout.setVisibility(View.VISIBLE);
                searchViewHolder.actionButton.setVisibility(View.GONE);
            } else if (mUIrealm.where(TVShow.class).equalTo("id", result.getId()).equalTo("onWatchList", true).findAll().size() == 1) {
                searchViewHolder.watchedLayout.setVisibility(View.VISIBLE);
                searchViewHolder.actionButton.setVisibility(View.GONE);
            }
        }

        if (result.getOverview() != null) {
            searchViewHolder.mediaDescription.setText(result.getOverview().toString());
        }

        Picasso.with(mContext)
                .load("https://image.tmdb.org/t/p/w342/" + result.getBackdropPath())
                .error(R.drawable.generic_movie_background)
                .into(searchViewHolder.mediaImage);
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.item_more_options_card, viewGroup, false);

        return new SearchViewHolder(itemView);
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        private Movie movie;

        @BindView(R.id.card_title)
        TextView mediaTitle;

        @BindView(R.id.card_text)
        TextView mediaDescription;

        @BindView(R.id.card_image)
        ImageView mediaImage;

        @BindView(R.id.action_button)
        IconicsButton actionButton;

        @BindView(R.id.watched_layout)
        RelativeLayout watchedLayout;

        @BindView(R.id.watch_list_layout)
        RelativeLayout watchListLayout;


        @BindView(R.id.progress_spinner)
        ProgressBar progressSpinner;

        public SearchViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    MultiSearchResult result = mMultiSearchResults.get(getAdapterPosition());

                    if (result.getMediaType().equals(MOVIE_TYPE)) {
                        Intent intent = new Intent(context, MovieBrowseDetailActivity.class);
                        intent.putExtra("movieId", result.getId());
                        context.startActivity(intent);
                    } else if (result.getMediaType().equals(SHOW_TYPE)) {
                        Log.d("SHOWID", Integer.toString(result.getId()));
                        Intent intent = new Intent(context, TVShowBrowseDetailActivity.class);
                        intent.putExtra("showID", result.getId());
                        intent.putExtra("showName", result.getName());
                        context.startActivity(intent);
                    } else {
                        Log.d("CLICK", "ERROR");
                    }
                }
            });

            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    progressSpinner.setVisibility(View.VISIBLE);
                    MultiSearchResult result = mMultiSearchResults.get(getAdapterPosition());

                    if (result.getMediaType().equals(MOVIE_TYPE)) {
                        final int movieID = result.getId();

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://api.themoviedb.org/3/movie/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        MovieAPI service = retrofit.create(MovieAPI.class);
                        Call<Movie> call = service.getMovie(Integer.toString(movieID));

                        call.enqueue(new Callback<Movie>() {
                            @Override
                            public void onResponse(Call<Movie> call, Response<Movie> response) {
                                Log.d("getMovie()", "Callback Success");
                                movie = response.body();
                                movie.setBackdropPath("https://image.tmdb.org/t/p/w780/" + movie.getBackdropPath());

                                Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl("http://api.themoviedb.org/3/movie/")
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build();

                                MovieAPI service = retrofit.create(MovieAPI.class);
                                Call<Credits> creditsCall = service.getCredits(Integer.toString(movieID));

                                creditsCall.enqueue(new Callback<Credits>() {
                                    @Override
                                    public void onResponse(Call<Credits> call, Response<Credits> response) {
                                        Log.d("GetCredits()", "Callback Success");
                                        List<Cast> cast = response.body().getCast();
                                        List<Crew> crew = response.body().getCrew();

                                        RealmList<Cast> realmCast = new RealmList<>();
                                        /*for (Cast castMember : cast) {
                                            realmCast.add(castMember.convertToRealm());
                                        }*/

                                        RealmList<Cast> realmCrew = new RealmList<>();
                                        /*for (Crew crewMember : crew) {
                                            realmCrew.add(crewMember.convertToRealm());
                                        }*/

                                        Target target = new Target() {
                                            @Override
                                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                                movie.setBackdropBitmap(stream.toByteArray());

                                                mUIrealm.beginTransaction();
                                                movie.setOnWatchList(true);
                                                mUIrealm.copyToRealm(movie);
                                                mUIrealm.commitTransaction();

                                                watchListLayout.setVisibility(View.VISIBLE);
                                                progressSpinner.setVisibility(View.GONE);

                                                Snackbar.make(v, "Added to watchlist!",
                                                        Snackbar.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void onBitmapFailed(Drawable errorDrawable) {
                                            }

                                            @Override
                                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                            }
                                        };

                                        Picasso.with(mContext)
                                                .load(movie.getBackdropPath())
                                                .into(target);

                                        /*realmMovie.setCrew(realmCrew);
                                        realmMovie.setCast(realmCast);*/
                                    }

                                    @Override
                                    public void onFailure(Call<Credits> call, Throwable t) {
                                        Log.d("GetCredits()", "Callback Failure");
                                    }
                                });
                                //TODOgenre
                            }

                            @Override
                            public void onFailure(Call<Movie> call, Throwable t) {
                                Log.d("getMovie()", "Callback Failure");
                            }
                        });
                    }
                }
            });
        }
    }
}
