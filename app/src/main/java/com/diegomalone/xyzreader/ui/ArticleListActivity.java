package com.diegomalone.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.diegomalone.xyzreader.R;
import com.diegomalone.xyzreader.data.ArticleLoader;
import com.diegomalone.xyzreader.data.ItemsContract;
import com.diegomalone.xyzreader.data.UpdaterService;
import com.diegomalone.xyzreader.utils.DateUtil;
import com.diegomalone.xyzreader.utils.SpacingItemDecorator;
import com.github.florent37.glidepalette.GlidePalette;

import java.util.Date;

import static com.diegomalone.xyzreader.utils.DateUtil.parseStringDate;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleListActivity.class.toString();

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setLogo(R.drawable.logo);

        setupRecyclerView();
        getLoaderManager().initLoader(0, null, this);
    }

    private void setupRecyclerView() {
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mRecyclerView = findViewById(R.id.recycler_view);
        int spacingMargin = (int) getResources().getDimension(R.dimen.article_list_margin);
        mRecyclerView.addItemDecoration(new SpacingItemDecorator(true, spacingMargin));
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.setLayoutManager(getLayoutManager());
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        return new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder viewHolder = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(viewHolder.getAdapterPosition()))));
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            mCursor.moveToPosition(position);

            String publishedDateString = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            Date publishedDate = parseStringDate(publishedDateString);

            String articleTitle = mCursor.getString(ArticleLoader.Query.TITLE);
            String articleAuthor = mCursor.getString(ArticleLoader.Query.AUTHOR);
            String articleDate = DateUtil.getSinceDate(publishedDate);

            holder.titleTextView.setText(articleTitle);
            holder.authorTextView.setText(articleAuthor);
            holder.publishedDateTextView.setText(articleDate);

            String imageUrl = mCursor.getString(ArticleLoader.Query.THUMB_URL);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                holder.thumbnailView.setVisibility(View.VISIBLE);

                Glide.with(ArticleListActivity.this)
                        .load(imageUrl)
                        .listener(GlidePalette.with(imageUrl)
                                .use(GlidePalette.Profile.MUTED)
                                .intoBackground(holder.container, GlidePalette.Swatch.RGB)
                                .intoTextColor(holder.titleTextView, GlidePalette.Swatch.TITLE_TEXT_COLOR)
                                .intoTextColor(holder.authorTextView, GlidePalette.Swatch.BODY_TEXT_COLOR)
                                .intoTextColor(holder.publishedDateTextView, GlidePalette.Swatch.BODY_TEXT_COLOR)
                        )
                        .into(holder.thumbnailView);
            } else {
                holder.thumbnailView.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView titleTextView;
        public TextView authorTextView;
        public TextView publishedDateTextView;
        public ViewGroup container;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            thumbnailView = view.findViewById(R.id.thumbnail);
            titleTextView = view.findViewById(R.id.article_title);
            authorTextView = view.findViewById(R.id.article_author);
            publishedDateTextView = view.findViewById(R.id.article_date);
        }
    }
}
