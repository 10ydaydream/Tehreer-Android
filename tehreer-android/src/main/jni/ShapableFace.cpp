/*
 * Copyright (C) 2021 Muhammad Tayyab Akram
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

extern "C" {
#include <ft2build.h>
#include FT_ADVANCES_H
#include FT_FREETYPE_H
#include FT_MULTIPLE_MASTERS_H
#include FT_TRUETYPE_TABLES_H
}

#include <mutex>

#include "FreeType.h"
#include "ShapableFace.h"

using namespace std;
using namespace Tehreer;

using FaceLock = lock_guard<RenderableFace>;

hb_font_funcs_t *ShapableFace::createFontFuncs()
{
    hb_font_funcs_t *funcs = hb_font_funcs_create();

    hb_font_funcs_set_nominal_glyph_func(funcs, [](hb_font_t *font, void *object,
                                                   hb_codepoint_t unicode,
                                                   hb_codepoint_t *glyph,
                                                   void *userData) -> hb_bool_t
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);

        RenderableFace &renderableFace = instance->renderableFace();
        FaceLock lock(renderableFace);
        FT_Face ftFace = renderableFace.ftFace();

        FT_UInt glyphID = FT_Get_Char_Index(ftFace, unicode);
        if (!glyphID) {
            return false;
        }

        *glyph = glyphID;
        return true;
    }, nullptr, nullptr);

    hb_font_funcs_set_nominal_glyphs_func(funcs, [](hb_font_t *font, void *object,
                                                    unsigned int count,
                                                    const hb_codepoint_t *firstUnicode,
                                                    unsigned int unicodeStride,
                                                    hb_codepoint_t *firstGlyph,
                                                    unsigned int glyphStride,
                                                    void *user_data) -> unsigned int
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);

        RenderableFace &renderableFace = instance->renderableFace();
        FaceLock lock(renderableFace);
        FT_Face ftFace = renderableFace.ftFace();

        unsigned int done;

        auto unicodePtr = reinterpret_cast<const uint8_t *>(firstUnicode);
        auto glyphPtr = reinterpret_cast<uint8_t *>(firstGlyph);

        for (done = 0; done < count; done++) {
            auto unicodeRef = reinterpret_cast<const hb_codepoint_t *>(unicodePtr);
            auto glyphRef = reinterpret_cast<hb_codepoint_t *>(glyphPtr);

            FT_UInt glyphID = FT_Get_Char_Index(ftFace, *unicodeRef);

            if (glyphID) {
                *glyphRef = glyphID;
            } else {
                break;
            }

            unicodePtr += unicodeStride;
            glyphPtr += glyphStride;
        }

        return done;
    }, nullptr, nullptr);

    hb_font_funcs_set_variation_glyph_func(funcs, [](hb_font_t *font, void *object,
                                                     hb_codepoint_t unicode,
                                                     hb_codepoint_t variationSelector,
                                                     hb_codepoint_t *glyph,
                                                     void *userData) -> hb_bool_t
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);

        RenderableFace &renderableFace = instance->renderableFace();
        FaceLock lock(renderableFace);
        FT_Face ftFace = renderableFace.ftFace();

        FT_UInt glyphID = FT_Face_GetCharVariantIndex(ftFace, unicode, variationSelector);
        if (!glyphID) {
            return false;
        }

        *glyph = glyphID;
        return true;
    }, nullptr, nullptr);

    hb_font_funcs_set_glyph_h_advance_func(funcs, [](hb_font_t *font, void *object,
                                                     hb_codepoint_t glyph,
                                                     void *userData) -> hb_position_t
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);

        RenderableFace &renderableFace = instance->renderableFace();
        FaceLock lock(renderableFace);
        FT_Face ftFace = renderableFace.ftFace();

        AdvanceCache &cache = instance->m_advanceCache;
        auto glyphID = static_cast<uint16_t>(glyph);
        int32_t glyphAdvance = 0;

        if (cache.get(glyphID, &glyphAdvance)) {
            return glyphAdvance;
        }

        FT_Fixed ftAdvance = 0;
        FT_Get_Advance(ftFace, glyphID, FT_LOAD_NO_SCALE, &ftAdvance);

        cache.put(glyphID, ftAdvance);

        return ftAdvance;
    }, nullptr, nullptr);

    hb_font_funcs_set_glyph_h_advances_func(funcs, [](hb_font_t *font, void *object,
                                                      unsigned int count,
                                                      const hb_codepoint_t *firstGlyph,
                                                      unsigned glyphStride,
                                                      hb_position_t *firstAdvance,
                                                      unsigned advanceStride,
                                                      void *user_data) -> void
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);
        AdvanceCache &cache = instance->m_advanceCache;

        RenderableFace &renderableFace = instance->renderableFace();
        FaceLock lock(renderableFace);
        FT_Face ftFace = renderableFace.ftFace();

        auto glyphPtr = reinterpret_cast<const uint8_t *>(firstGlyph);
        auto advancePtr = reinterpret_cast<uint8_t *>(firstAdvance);

        for (unsigned int i = 0; i < count; i++) {
            auto glyphRef = reinterpret_cast<const hb_codepoint_t *>(glyphPtr);
            auto advanceRef = reinterpret_cast<hb_position_t *>(advancePtr);

            auto glyphID = static_cast<uint16_t>(*glyphRef);
            int32_t glyphAdvance = 0;

            if (cache.get(glyphID, &glyphAdvance)) {
                *advanceRef = glyphAdvance;
            } else {
                FT_Fixed ftAdvance = 0;
                FT_Get_Advance(ftFace, glyphID, FT_LOAD_NO_SCALE, &ftAdvance);

                *advanceRef = ftAdvance;
                cache.put(glyphID, ftAdvance);
            }

            glyphPtr += glyphStride;
            advancePtr += advanceStride;
        }
    }, nullptr, nullptr);

    hb_font_funcs_make_immutable(funcs);

    return funcs;
}

hb_font_funcs_t *ShapableFace::defaultFontFuncs() {
    static hb_font_funcs_t *defaultFontFuncs = createFontFuncs();
    return defaultFontFuncs;
}

ShapableFace &ShapableFace::create(RenderableFace &renderableFace)
{
    auto instance = new ShapableFace(renderableFace);
    return *instance;
}

ShapableFace::ShapableFace(RenderableFace &renderableFace)
    : m_rootFace(nullptr)
    , m_renderableFace(renderableFace.retain())
    , m_retainCount(1)
{
    FT_Face ftFace = renderableFace.ftFace();
    auto faceIndex = static_cast<unsigned int>(ftFace->face_index);
    auto unitsPerEm = static_cast<unsigned int>(ftFace->units_per_EM);

    hb_face_t *hbFace = hb_face_create_for_tables([](hb_face_t *face, hb_tag_t tag,
                                                     void *object) -> hb_blob_t *
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);

        RenderableFace &renderableFace = instance->renderableFace();
        FaceLock lock(renderableFace);
        FT_Face ftFace = renderableFace.ftFace();

        FT_ULong length = 0;
        FT_Load_Sfnt_Table(ftFace, tag, 0, nullptr, &length);

        if (length == 0) {
            return nullptr;
        }

        void *memory = malloc(length);

        auto buffer = reinterpret_cast<FT_Byte *>(memory);
        FT_Load_Sfnt_Table(ftFace, tag, 0, buffer, nullptr);

        return hb_blob_create(reinterpret_cast<const char *>(memory), length,
                              HB_MEMORY_MODE_WRITABLE, nullptr, free);
    }, this, nullptr);

    hb_face_set_index(hbFace, faceIndex);
    hb_face_set_upem(hbFace, unitsPerEm);

    m_hbFont = hb_font_create(hbFace);
    hb_font_set_funcs(m_hbFont, defaultFontFuncs(), this, nullptr);

    hb_face_destroy(hbFace);

    setupCoordinates();
}

ShapableFace::ShapableFace(ShapableFace &parent, RenderableFace &renderableFace)
    : m_rootFace(nullptr)
    , m_renderableFace(renderableFace.retain())
    , m_retainCount(1)
{
    ShapableFace *rootFace = parent.m_rootFace ?: &parent;
    hb_font_t *rootFont = rootFace->hbFont();

    m_hbFont = hb_font_create_sub_font(rootFont);
    hb_font_set_funcs(m_hbFont, defaultFontFuncs(), this, nullptr);

    m_rootFace = &rootFace->retain();

    setupCoordinates();
}

void ShapableFace::setupCoordinates()
{
    const CoordArray *coordinates = m_renderableFace.coordinates();
    if (coordinates) {
        hb_font_set_var_coords_design(m_hbFont, coordinates->data(), coordinates->size());
    }
}

ShapableFace::~ShapableFace()
{
    hb_font_destroy(m_hbFont);
    m_renderableFace.release();

    if (m_rootFace) {
        m_rootFace->release();
    }
}

ShapableFace &ShapableFace::retain()
{
    m_retainCount++;
    return *this;
}

void ShapableFace::release()
{
    if (--m_retainCount == 0) {
        delete this;
    }
}

ShapableFace &ShapableFace::deriveVariation(RenderableFace &renderableFace)
{
    auto instance = new ShapableFace(*this, renderableFace);
    return *instance;
}
