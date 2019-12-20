package com.example.apt_test;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class EventBus {

    private Map<Object,List<SubscribleMethod>> cacheMap;
    private Handler mHandler;
    private static volatile EventBus instance;
    private EventBus(){
        cacheMap = new HashMap<>();
        mHandler = new Handler();
    }

    public static EventBus getDefault() {
        if (instance==null){
            synchronized (EventBus.class){
                if (instance==null){
                    instance = new EventBus();
                }
            }
        }
        return instance;
    }

    public void register(Object obj) {
        List<SubscribleMethod> list = cacheMap.get(obj);
        if (list==null){
            list = findSubcribeMethods(obj);
            cacheMap.put(obj,list);
        }
    }

    public void unreister(Object obj){
        cacheMap.remove(obj);
    }

    private List<SubscribleMethod> findSubcribeMethods(Object obj) {
        List<SubscribleMethod> list = new ArrayList<>();
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        while (clazz!=null) {
            //找父类的时候，需要先判断一下是否是系统级别的父类
            String name = clazz.getName();
            if (name.startsWith("java.")||name.startsWith("javax.")||name.startsWith("android.")){
                break;
            }
            for (Method method : methods) {
                //找到带有subcribe的注解方法
                Subscribe subscribe = method.getAnnotation(Subscribe.class);
                if (subscribe == null) {
                    continue;
                }
                //判断带有subscribe注解方法中的参数类型
                Class<?>[] types = method.getParameterTypes();
                if (types.length != 1) {
                    Log.e("EventBus", "eventbus only accept one parameter");
                }
                ThreadMode threadMode = subscribe.threadMode();
                SubscribleMethod subscribleMethod = new SubscribleMethod(method, threadMode, types[0]);
                list.add(subscribleMethod);
            }
            clazz = clazz.getSuperclass();
        }
        return list;
    }

    public void post(final Object type){
        //直接循环map里的方法，找到对应的，然后回调
        Set<Object> set = cacheMap.keySet();
        Iterator<Object> iterator = set.iterator();
        while (iterator.hasNext()){
            final Object object = iterator.next();
            List<SubscribleMethod> list = cacheMap.get(object);
            for (final SubscribleMethod subscribleMethod : list) {
                //if条件前面的对象 对象所对应的类信息是不是if条件后面的对象所对应的类信息的父类或接口
                if (subscribleMethod.getType().isAssignableFrom(type.getClass())){
                    switch (subscribleMethod.getmThreadMode()){
                        case MAIN:
                            //从主线程切换到主线程
                            if (Looper.myLooper()==Looper.getMainLooper()){
                                invoke(subscribleMethod,object,type);
                            }else{
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(subscribleMethod,object,type);
                                    }
                                });
                            }
                            //从子线程切换到主线程
                            break;
                        case BACKGROUND:
                            //从主线程切换到子线程
                            if (Looper.myLooper()==Looper.getMainLooper()){
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        invoke(subscribleMethod,object,type);
                                    }
                                }).start();
                            }else{
                                //从子线程切换到子线程
                                invoke(subscribleMethod,object,type);
                            }
                            break;
                    }
                }
            }
        }
    }

    private void invoke(SubscribleMethod subscribleMethod, Object object, Object type) {
        Method method = subscribleMethod.getmMethod();
        try {
            method.invoke(object,type);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
