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

package com.nick.scalpel.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.nick.scalpel.config.Configuration;
import com.nick.scalpel.core.utils.Preconditions;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

@TargetApi(value = Build.VERSION_CODES.M)
public class AutoRequestPermissionWirer extends AbsClassWirer {

    public AutoRequestPermissionWirer(Configuration configuration) {
        super(configuration);
    }

    @Override
    public Class<? extends Annotation> annotationClass() {
        return AutoRequirePermission.class;
    }

    @Override
    public void wire(Object obj) {

        Preconditions.checkState(obj instanceof Activity);

        Activity activity = (Activity) obj;

        Preconditions.checkState(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);

        AutoRequirePermission autoRequirePermission = activity.getClass().getAnnotation(AutoRequirePermission.class);
        int code = autoRequirePermission.requestCode();

        String[] scope = autoRequirePermission.permissions();
        if (scope.length == 0) {
            String[] declaredPerms = getPkgInfo(activity).requestedPermissions;
            String[] required = extractUnGranted(activity, declaredPerms);
            if (required == null) return;
            activity.requestPermissions(required, code);
        } else {
            String[] required = extractUnGranted(activity, scope);
            activity.requestPermissions(required, code);
        }
    }

    private String[] extractUnGranted(Activity activity, String[] declaredPerms) {
        if (declaredPerms == null || declaredPerms.length == 0) return null;
        PackageManager packageManager = activity.getPackageManager();
        List<String> requestList = new ArrayList<>(declaredPerms.length);
        for (String info : declaredPerms) {
            int code = packageManager.checkPermission(info, activity.getPackageName());
            if (code == PackageManager.PERMISSION_GRANTED) continue;
            Log.v(logTag, "Will request perm:" + info + ", current code:" + code);
            requestList.add(info);
        }
        String[] out = new String[requestList.size()];
        for (int i = 0; i < requestList.size(); i++) {
            out[i] = requestList.get(i);
        }
        return out;
    }

    private PackageInfo getPkgInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
