package com.koksheng.procleanservices.Remote;

import com.koksheng.procleanservices.Model.MyResponse;
import com.koksheng.procleanservices.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(

            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA1Vy82eA:APA91bHAfDhxeCTtvkTPglskkHdG9q1Um5lzUPPj9JBcoeg4N4wGbX-hGtECwQCMDGyxLh_AL0jB-JuEBvLdLCTg5-iyC_JndLQxSn_9q4DsXkiMazMnK9vGE7Tpq1VFaw7QooAMp5bk"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
