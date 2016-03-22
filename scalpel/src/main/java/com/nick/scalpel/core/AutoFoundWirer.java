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

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.View;

import com.nick.scalpel.config.Configuration;
import com.nick.scalpel.core.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class AutoFoundWirer extends AbsFieldWirer {

    public AutoFoundWirer(Configuration configuration) {
        super(configuration);
    }

    @Override
    public Class<? extends Annotation> annotationClass() {
        return AutoFound.class;
    }

    @Override
    public void wire(Activity activity, Field field) {
        WireParam param = getParam(activity, field);
        if (param == null) return;
        switch (param.type) {
            case View:
                wire(activity.getWindow().getDecorView(), activity, field);
                break;
            case String:
            case Color:
            case Integer:
            case Bool:
            case StringArray:
            case IntArray:
            case PM:
            case Account:
            case Alarm:
            case AM:
            case WM:
            case NM:
            case TM:
            case SP:
                wire(activity.getApplicationContext(), activity, field);
                break;
        }
    }

    @Override
    public void wire(Fragment fragment, Field field) {
        WireParam param = getParam(fragment, field);
        if (param == null) return;
        switch (param.type) {
            case View:
                wire(fragment.getView(), fragment, field);
                break;
            case String:
            case Color:
            case Integer:
            case Bool:
            case StringArray:
            case IntArray:
            case PM:
            case Account:
            case Alarm:
            case AM:
            case WM:
            case NM:
            case TM:
            case SP:
                wire(fragment.getActivity(), fragment, field);
                break;
        }
    }

    @Override
    public void wire(Service service, Field field) {
        wire(service.getApplicationContext(), service, field);
    }

    private void wireFromContext(Context context, AutoFoundType type, int idRes, Resources.Theme theme, Field field, Object forWho) {
        Resources resources = context.getResources();
        switch (type) {
            case String:
                ReflectionUtils.setField(field, forWho, resources.getString(idRes));
                break;
            case Color:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ReflectionUtils.setField(field, forWho, resources.getColor(idRes, theme));
                } else {
                    ReflectionUtils.setField(field, forWho, resources.getColor(idRes));
                }
                break;
            case Integer:
                ReflectionUtils.setField(field, forWho, resources.getInteger(idRes));
                break;
            case Bool:
                ReflectionUtils.setField(field, forWho, resources.getBoolean(idRes));
                break;
            case StringArray:
                ReflectionUtils.setField(field, forWho, resources.getStringArray(idRes));
                break;
            case IntArray:
                ReflectionUtils.setField(field, forWho, resources.getIntArray(idRes));
                break;
            case PM:
                ReflectionUtils.setField(field, forWho, context.getSystemService(Context.POWER_SERVICE));
                break;
            case Account:
                ReflectionUtils.setField(field, forWho, context.getSystemService(Context.ACCOUNT_SERVICE));
                break;
            case Alarm:
                ReflectionUtils.setField(field, forWho, context.getSystemService(Context.ALARM_SERVICE));
                break;
            case AM:
                ReflectionUtils.setField(field, forWho, context.getSystemService(Context.ACTIVITY_SERVICE));
                break;
            case WM:
                ReflectionUtils.setField(field, forWho, context.getSystemService(Context.WINDOW_SERVICE));
                break;
            case NM:
                ReflectionUtils.setField(field, forWho, context.getSystemService(Context.NOTIFICATION_SERVICE));
                break;
            case TM:
                ReflectionUtils.setField(field, forWho, context.getSystemService(Context.TELEPHONY_SERVICE));
                break;
            case TCM:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ReflectionUtils.setField(field, forWho, context.getSystemService(Context.TELECOM_SERVICE));
                }
                break;
            case SP:
                ReflectionUtils.setField(field, forWho, PreferenceManager.getDefaultSharedPreferences(context));
                break;
        }
    }

    private WireParam getParam(Object object, Field field) {
        ReflectionUtils.makeAccessible(field);

        Object fieldObject = ReflectionUtils.getField(field, object);

        boolean isBaseType = false;

        if (fieldObject != null)
            isBaseType = ReflectionUtils.isBaseDataType(fieldObject.getClass());
        if (fieldObject != null && !isBaseType) return null;

        AutoFound found = field.getAnnotation(AutoFound.class);
        AutoFoundType type = found.type();

        if (type == AutoFoundType.Auto) {
            type = autoDetermineType(field.getType());
            if (type == null) return null;
            return new WireParam(type, found.id());
        } else if (isTypeOf(field.getType(), type.targetClass))
            return new WireParam(type, found.id());
        return null;
    }

    private AutoFoundType autoDetermineType(Class clz) {
        AutoFoundType[] all = AutoFoundType.values();
        int matchCnt = 0;
        AutoFoundType found = null;
        for (AutoFoundType t : all) {
            if (isTypeOf(clz, t.targetClass)) {
                matchCnt++;
                if (matchCnt > 1) return null;
                found = t;
            }
        }
        return found;
    }

    @Override
    public void wire(Context context, Object object, Field field) {
        WireParam param = getParam(object, field);
        if (param == null) return;
        wireFromContext(context, param.type, param.idRes, null, field, object);
    }

    @Override
    public void wire(View root, Object object, Field field) {
        WireParam param = getParam(object, field);
        if (param == null) return;
        switch (param.type) {
            case View:
                ReflectionUtils.setField(field, object, root.findViewById(param.idRes));
                break;
            default:
                wire(root.getContext(), object, field);
        }
    }

    private boolean isTypeOf(Class clz, Class target) {
        if (clz == target) return true;
        Class sup = clz.getSuperclass();
        return sup != null && isTypeOf(sup, target);
    }

    class WireParam {
        AutoFoundType type;
        int idRes;

        public WireParam(AutoFoundType type, int idRes) {
            this.type = type;
            this.idRes = idRes;
        }

        @Override
        public String toString() {
            return "WireParam{" +
                    "type=" + type +
                    ", idRes=" + idRes +
                    '}';
        }
    }
}
