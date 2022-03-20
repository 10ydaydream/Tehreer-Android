/*
 * Copyright (C) 2022 Muhammad Tayyab Akram
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

package com.mta.tehreer.unicode;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CodePointTest {
    @Test
    public void testGetBidiClass() {
        assertEquals(CodePoint.getBidiClass('a'), BidiClass.LEFT_TO_RIGHT);
        assertEquals(CodePoint.getBidiClass('ا'), BidiClass.ARABIC_LETTER);
        assertEquals(CodePoint.getBidiClass('1'), BidiClass.EUROPEAN_NUMBER);
        assertEquals(CodePoint.getBidiClass(' '), BidiClass.WHITE_SPACE);
    }

    @Test
    public void testGetGeneralCategory() {
        assertEquals(CodePoint.getGeneralCategory('A'), GeneralCategory.UPPERCASE_LETTER);
        assertEquals(CodePoint.getGeneralCategory('a'), GeneralCategory.LOWERCASE_LETTER);
        assertEquals(CodePoint.getGeneralCategory('1'), GeneralCategory.DECIMAL_NUMBER);
        assertEquals(CodePoint.getGeneralCategory(' '), GeneralCategory.SPACE_SEPARATOR);
    }

    @Test
    public void testGetScript() {
        assertEquals(CodePoint.getScript(' '), Script.COMMON);
        assertEquals(CodePoint.getScript('ا'), Script.ARABIC);
        assertEquals(CodePoint.getScript('a'), Script.LATIN);
    }

    @Test
    public void testGetMirror() {
        assertEquals(CodePoint.getMirror('('), ')');
        assertEquals(CodePoint.getMirror(')'), '(');
    }
}
