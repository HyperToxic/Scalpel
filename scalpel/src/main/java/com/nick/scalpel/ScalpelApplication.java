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

import android.app.Application;
import android.support.annotation.CallSuper;

import com.nick.scalpel.config.Configuration;

public class ScalpelApplication extends Application {
    @Override
    @CallSuper
    public void onCreate() {
        super.onCreate();
        Scalpel.getDefault()
                .application(this)
                .config(Configuration.builder()
                        .autoFindIfNull(true)
                        .debug(true)
                        .logTag(getClass().getSimpleName())
                        .build());
    }
}
