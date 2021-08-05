package com.example.social_media_app.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content_Type:application/json",
            "Authorization:key=AAAA4Z1pQx8:APA91bH59I5TjvPgzLFWjiR1lxj5U-JS52rUgV1dfph2CLnVNoOQCS6-1lPX-HluxrapGa4BlYKbxlIA3qFeThzXhyKe0zrMMFMYdkBB2ZUn7UJnkoKeXaKNd2hPnhzwLzvjEzFjPdlU"

    })
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);

}
