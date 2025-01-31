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
package com.mta.tehreer.internal.sfnt

internal interface SfntTable {
    fun readBytes(offset: Int, count: Int): ByteArray

    fun readInt8(offset: Int): Byte
    fun readUInt8(offset: Int): Short

    fun readInt16(offset: Int): Short
    fun readInt32(offset: Int): Int

    fun readUInt16(offset: Int): Int
    fun readUInt32(offset: Int): Long

    fun readInt64(offset: Int): Long

    fun readFixed(offset: Int): Float {
        return readInt32(offset) / 65536.0f
    }

    fun readOffset32(offset: Int): Int {
        return (readUInt32(offset) and (-0x80000000).inv()).toInt()
    }

    fun subTable(offset: Int): SfntTable {
        return SubTable(this, offset)
    }
}
