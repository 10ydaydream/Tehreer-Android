/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mta.tehreer.internal.collections

import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.internal.util.Preconditions.checkElementIndex
import com.mta.tehreer.internal.util.Preconditions.checkIndexRange

internal class JFloatArrayList(
    private val array: FloatArray,
    private val offset: Int,
    private val size: Int
) : FloatList() {
    override fun size(): Int {
        return size
    }

    override fun get(index: Int): Float {
        checkElementIndex(index, size)
        return array[index + offset]
    }

    override fun copyTo(array: FloatArray, atIndex: Int) {
        System.arraycopy(this.array, offset, array, atIndex, size)
    }

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        checkIndexRange(fromIndex, toIndex, size)
        return JFloatArrayList(array, offset + fromIndex, toIndex - fromIndex)
    }
}
