/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nick.scalpel;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.nick.scalpel.config.Configuration;
import com.nick.scalpel.core.AutoBindWirer;
import com.nick.scalpel.core.AutoFoundWirer;
import com.nick.scalpel.core.AutoRecycleWirer;
import com.nick.scalpel.core.AutoRegisterWirer;
import com.nick.scalpel.core.AutoRequestFullScreenWirer;
import com.nick.scalpel.core.AutoRequestPermissionWirer;
import com.nick.scalpel.core.ClassWirer;
import com.nick.scalpel.core.FieldWirer;
import com.nick.scalpel.core.HandlerSupplier;
import com.nick.scalpel.core.LifeCycleManager;
import com.nick.scalpel.core.OnClickWirer;
import com.nick.scalpel.core.OnTouchWirer;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Api class for Scalpel project.
 * To get the instance of Scalpel you can use {@link #getDefault()}
 * or create an instance manually.
 */
public class Scalpel implements LifeCycleManager, HandlerSupplier {

    private static Scalpel ourScalpel;

    private final Set<FieldWirer> mFieldWirers;
    private final Set<ClassWirer> mClassWirers;

    private Application mApp;
    private Handler mHandler;

    private String mLogTag;

    public Scalpel() {
        mFieldWirers = new HashSet<>();
        mClassWirers = new HashSet<>();
        mHandler = new Handler();
    }

    /**
     * Get the default shared single instance of {@link Scalpel}
     *
     * @return The default instance of {@link Scalpel}
     */
    public synchronized static Scalpel getDefault() {
        if (ourScalpel == null) ourScalpel = new Scalpel();
        return ourScalpel;
    }

    /**
     * Assign the application to {@link Scalpel}
     *
     * @param application Application instance of this app.
     * @return The instance of {@link Scalpel}
     * @see Application
     */
    public Scalpel application(Application application) {
        this.mApp = application;
        return this;
    }

    public Scalpel config(Configuration configuration) {
        Configuration usingConfig = configuration == null ? Configuration.DEFAULT : configuration;
        mLogTag = usingConfig.getLogTag();
        AutoFoundWirer autoFoundWirer = new AutoFoundWirer(usingConfig);
        mFieldWirers.add(autoFoundWirer);
        mFieldWirers.add(new OnClickWirer(autoFoundWirer, usingConfig));
        mFieldWirers.add(new OnTouchWirer(autoFoundWirer, usingConfig));
        mFieldWirers.add(new AutoBindWirer(usingConfig, this));
        mFieldWirers.add(new AutoRegisterWirer(usingConfig, this));
        mFieldWirers.add(new AutoRecycleWirer(usingConfig, this));

        mClassWirers.add(new AutoRequestPermissionWirer(usingConfig));
        mClassWirers.add(new AutoRequestFullScreenWirer(usingConfig, this));
        return this;
    }

    public void wire(Activity activity) {
        wireClz(activity);
        Class clz = activity.getClass();
        for (Field field : clz.getDeclaredFields()) {
            for (FieldWirer wirer : mFieldWirers) {
                if (field.isAnnotationPresent(wirer.annotationClass())) {
                    wirer.wire(activity, field);
                }
            }
        }
    }

    public void wire(Service service) {
        wireClz(service);
        Class clz = service.getClass();
        for (Field field : clz.getDeclaredFields()) {
            for (FieldWirer wirer : mFieldWirers) {
                if (field.isAnnotationPresent(wirer.annotationClass())) {
                    wirer.wire(service, field);
                }
            }
        }
    }

    public void wire(Fragment fragment) {
        wireClz(fragment);
        Class clz = fragment.getClass();
        for (Field field : clz.getDeclaredFields()) {
            for (FieldWirer wirer : mFieldWirers) {
                if (field.isAnnotationPresent(wirer.annotationClass())) {
                    wirer.wire(fragment, field);
                }
            }
        }
    }

    public void wire(Context context, Object target) {
        wireClz(target);
        Class clz = target.getClass();
        for (Field field : clz.getDeclaredFields()) {
            for (FieldWirer wirer : mFieldWirers) {
                if (field.isAnnotationPresent(wirer.annotationClass())) {
                    wirer.wire(context, target, field);
                    break;
                }
            }
        }
    }

    public void wire(View rootView, Object target) {
        wireClz(target);
        Class clz = target.getClass();
        for (Field field : clz.getDeclaredFields()) {
            for (FieldWirer wirer : mFieldWirers) {
                if (field.isAnnotationPresent(wirer.annotationClass())) {
                    wirer.wire(rootView, target, field);
                    break;
                }
            }
        }
    }

    private void wireClz(Object o) {
        Class clz = o.getClass();
        for (ClassWirer clzWirer : mClassWirers) {
            if (clz.isAnnotationPresent(clzWirer.annotationClass())) {
                clzWirer.wire(o);
            }
        }
    }

    @Override
    public boolean registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        if (mApp != null) {
            mApp.registerActivityLifecycleCallbacks(callback);
            return true;
        }
        Log.e(mLogTag, "Application not specify, call Scalpel.application() to set one!");
        return false;
    }

    @Override
    public boolean unRegisterActivityLifecycleCallbacks(final Application.ActivityLifecycleCallbacks callback) {
        // Post this task instead of directly call to avoid List modify errors.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mApp.unregisterActivityLifecycleCallbacks(callback);
            }
        });
        return true;
    }

    @NonNull
    @Override
    public Handler getHandler() {
        return mHandler;
    }
}
