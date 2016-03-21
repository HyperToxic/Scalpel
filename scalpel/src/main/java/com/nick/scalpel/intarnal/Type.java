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

package com.nick.scalpel.intarnal;

public enum Type {

    Auto(null),
    View(android.view.View.class),
    Color(int.class),
    String(String.class),
    Bool(boolean.class),
    Integer(int.class),
    StringArray(String[].class),
    IntArray(int[].class);

    Class targetClass;

    Type(Class targetClass) {
        this.targetClass = targetClass;
    }
}
