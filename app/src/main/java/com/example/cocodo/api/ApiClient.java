package com.example.cocodo.api;

import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;

import java.util.List;

public class ApiClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/";

    public interface ApiInterface {

        @GET("posts")
        Call<List<Post>> getPosts();

        @GET("posts/{id}")
        Call<Post> getPost(@Path("id") int postId);

        @POST("posts")
        @FormUrlEncoded
        Call<Post> createPost(
                @Field("userId") int userId,
                @Field("title") String title,
                @Field("body") String body);
    }

    private static Retrofit retrofit = null;

    public static Retrofit getApiClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiInterface getApiInterface() {
        return getApiClient().create(ApiInterface.class);
    }

    public static class Post {
        @SerializedName("userId")
        private int userId;
        @SerializedName("id")
        private int id;
        @SerializedName("title")
        private String title;
        @SerializedName("body")
        private String body;

        public int getUserId() {
            return userId;
        }
        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }
        public void setBody(String body) {
            this.body = body;
        }
    }
}