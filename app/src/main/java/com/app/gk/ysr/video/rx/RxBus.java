package com.app.gk.ysr.video.rx;


import java.util.Observable;

import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;


/**
 * Created by xiongzheng on 2018/11/16.
 * Subject同时充当了Observer和Observable的角色,Subject是非线程安全的
 * 要避免该问题,需要将Subject转换为一个SerializedSubject
 * 上述RxBus类把线程非安全的PublishSubject包装成线程安全的Subject
 *
 * PublishSubject只会把在订阅发生的时间点之后来自原始Observable的数据发射给观察者
 *
 * ofType操作符只发送指定类型的数据,其内部就是filter+cast
 */

public class RxBus {
    private static volatile  RxBus mInstance;
    private final Subject<Object, Object> bus;

    private RxBus(){
        bus = new SerializedSubject<>(PublishSubject.create()) ;
    }

    /*
     * 单例模式RxBus
     */
    public static RxBus getInstance(){
        RxBus rxBus = mInstance;
        if(mInstance == null){
            synchronized (RxBus.class){
               rxBus = mInstance;
                if(mInstance == null){
                    rxBus = new RxBus();
                    mInstance = rxBus;
                }
            }
        }
        return rxBus;
    }

    /*
     * 发送消息
     */
    public void post(Object obj){
        bus.onNext(obj);
    }

    /*
     * 接收消息
     */
    public <T> rx.Observable<T> toObserverable(Class<T> eventType){
        return bus.ofType(eventType);
    }

}
