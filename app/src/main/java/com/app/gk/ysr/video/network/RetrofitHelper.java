package com.app.gk.ysr.video.network;

import com.app.gk.ysr.video.BilibiliApp;
import com.app.gk.ysr.video.utils.CommonUtil;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by xiongzheng on 2018/11/16.
 */

public class RetrofitHelper {
    private static OkHttpClient mOkHttpClient;

    static {
        initOkHttpClient();
    }

    /*
    public static LiveService getLiveAPI(){
        return createApi(LiveService.class, ApiCons);
    }

    public static BiliAppService getBiliAppAPI(){
        return createApi(BiliAppService.class, ApiCo);
    }*/

    /*
     * 根据传入的baseUri, 和api创建retrofit
     */
    private static <T> T createApi(Class<T> clazz, String baseUrl){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(mOkHttpClient)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(clazz);
    }

    /*
     * 初始化OKHttpClient,设置缓存,设置超时时间,设置打印日志,设置UA拦截器
     */
    private static void initOkHttpClient(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        if(mOkHttpClient == null){
            synchronized (RetrofitHelper.class){
                if(mOkHttpClient == null) {
                   //设置Http缓存
                    /*Cache cache = new Cache(new File());
                    mOkHttpClient = new OkHttpClient().Builder()
                            .cache(cache)
                            .add*/
                }
            }
        }
    }

   /*
    * 添加UA拦截器, B站请求API需要加上UA才能正常使用
    */
   private static class UserAgentInterceptro implements Interceptor{

       @Override
       public Response intercept(Chain chain) throws IOException {
           Request originalRequest = chain.request();
           Request requestWithUserAgent = originalRequest.newBuilder()
                    .removeHeader("User-Agent")
                    .addHeader("User-Agent", ApiCon)
                    .build();
           return chain.proceed(requestWithUserAgent);
       }
   }


   /*
    * 为okhttp添加缓存,这里考虑到服务器不支持缓存时,从而让okhttp支持缓存
    */
    private static class CacheInterceptor implements Interceptor{

       @Override
       public Response intercept(Chain chain) throws IOException {
            //有网络时 设置缓存超过时间1个小时
           int maxAge = 60 * 60;
           //无网络是,设置超过1天
           int maxStale = 60 * 60 * 24;
           Request request = chain.request();
           if(CommonUtil.isNetworkAvailable(BilibiliApp.getInstance())){
               //有网络时只从网络获取
                request = request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
           }else {
                //无网络时只从缓存中读取
               request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
           }
           Response response = chain.proceed(request);
           if(CommonUtil.isNetworkAvailable(BilibiliApp.getInstance())){
                response = response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
           }
           else {
                response = response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
           }

           return response;
       }
   }


}


