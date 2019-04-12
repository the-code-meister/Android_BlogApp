package com.example.yohan.blogapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GitPosts extends AppCompatActivity implements RecentPostAdapter.onItemClicked {

    private Toolbar mToolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView GitrecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<RecentModel> list;
    private RecentPostAdapter recentPostAdapter;
    ProgressDialog progressDialog1;

    private String GitBaseURL = "https://readhub.lk/wp-json/wp/v2/";
    public static final String RENDER_CONTENT = "RENDER";
    public  static final String title = "render";
    int cacheSize = 20 * 1024 * 1024; // 10 MB
    Cache cache;

    OkHttpClient okHttpClient;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_posts);

        mToolbar = findViewById(R.id.GitPost_app_bar);
        swipeRefreshLayout = findViewById(R.id.GitSwipe);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("ReadHub - Git ");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GitPosts.this,MainActivity.class);
                startActivity(i);
            }
        });

        GitrecyclerView = findViewById(R.id.Git_recycleview);

        progressDialog1 = new ProgressDialog(GitPosts.this);
        progressDialog1.setTitle("Git Posts");
        progressDialog1.setMessage("Loading");
        progressDialog1.show();



        cache = new Cache(getCacheDir(), cacheSize);

        okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .build();

        linearLayoutManager = new LinearLayoutManager(GitPosts.this,LinearLayoutManager.VERTICAL,false);
        GitrecyclerView.setLayoutManager(linearLayoutManager);

        list = new ArrayList<>();

        recentPostAdapter = new RecentPostAdapter(list,this);

        new GetGitJson().execute();
        GitrecyclerView.setAdapter(recentPostAdapter);

        recentPostAdapter.SetOnItemClickListener(GitPosts.this);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetGitJson().execute();
            }
        });
    }

    public class GetGitJson extends AsyncTask<Void,Void,Void> {

        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            progressDialog = new ProgressDialog(GitPosts.this);
//            progressDialog.setTitle("Git Post");
//            progressDialog.setMessage("Loading");
//            progressDialog.show();



        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
         //   progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GitBaseURL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RetrofitArrayAPI retrofitArrayAPI = retrofit. create(RetrofitArrayAPI.class);

            Call<List<WPJavaPost>> call = retrofitArrayAPI.getGitPost();


            call.enqueue(new Callback<List<WPJavaPost>>() {
                @Override
                public void onResponse(Call<List<WPJavaPost>> call, Response<List<WPJavaPost>> response) {
                   // Toast.makeText(GitPosts.this,"done",Toast.LENGTH_LONG).show();


                    swipeRefreshLayout.setRefreshing(false);
                    progressDialog1.dismiss();
                    for (int i =0;i<response.body().size(); i++){

                        String temdetails = response.body().get(i).getDate();
                        String titile = response.body().get(i).getTitle().getRendered().toString();
                        titile = titile.replace("&#8211;","");
                        titile = titile.replace("&#x200d;","");
                        titile = titile.replace("&#8230;","");
                        titile = titile.replace("&amp;","");
                        titile = titile.replace("&#8220;","");
                        titile = titile.replace("&#8221;","");
                        String render = response.body().get(i).getContent().getRendered();
                        /// render = render.replace("--aspect-ratio","aspect-ratio");

                        // String profileUrl = response.body().get(i).getLinks().getAuthor().get(0).getHref();

                        if(response.body().get(i).getBetterFeaturedImage().getMediaDetails().getSizes().getThumbnail().getSourceUrl() != null){
                            url =response.body().get(i).getBetterFeaturedImage().getMediaDetails().getSizes().getThumbnail().getSourceUrl();
                        }else if(response.body().get(i).getBetterFeaturedImage().getMediaDetails().getSizes().getVmagazinePostSliderLg().getSourceUrl() != null){
                            url =response.body().get(i).getBetterFeaturedImage().getMediaDetails().getSizes().getVmagazinePostSliderLg().getSourceUrl();
                        }else if(response.body().get(i).getBetterFeaturedImage().getMediaDetails().getSizes().getVmagazineLargeCategory().getSourceUrl() != null){
                            url = response.body().get(i).getBetterFeaturedImage().getMediaDetails().getSizes().getVmagazineLargeCategory().getSourceUrl();
                        }else {
                            url = response.body().get(i).getBetterFeaturedImage().getSourceUrl();
                        }

                        list.add(new RecentModel( titile,
                                temdetails,
                                url,render,RecentModel.IMAGE_TYPE,response.body().get(i).getEmbedded().getAuthor().get(0).getName()));

                    }

                    recentPostAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<List<WPJavaPost>> call, Throwable t) {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            progressDialog.dismiss();
        }
    }


    @Override
    public void OnItemClick(int index) {
        Intent i = new Intent(this,RecentPostView.class);
        RecentModel model = list.get(index);
        i.putExtra(RENDER_CONTENT,model.render);
        // i.putExtra(title,model.title);
        startActivity(i);
    }

    private void updateUI(){
        Intent startIntent = new Intent(GitPosts.this,Login.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout){
            openDialog();
        }
        return true;
    }

    public void openDialog(){

        logoutDialog logoutdialog = new logoutDialog();
        logoutdialog.show(getSupportFragmentManager(),"Logoutdialog");

    }
}
