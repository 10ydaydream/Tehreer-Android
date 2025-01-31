/*
 * Copyright (C) 2017-2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.graphics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import java.util.Iterator;
import java.util.List;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * A <code>TypeFamily</code> object represents a collection of typefaces that relate to each other.
 */
public class TypeFamily {
    private final @NonNull String familyName;
    private final @NonNull @Size(min = 1) List<Typeface> typefaces;

    /**
     * Constructs a type family object.
     *
     * @param familyName The name of family.
     * @param typefaces The list of typefaces belonging to family.
     */
    public TypeFamily(@NonNull String familyName, @NonNull @Size(min = 1) List<Typeface> typefaces) {
        checkNotNull(familyName, "familyName");
        checkNotNull(typefaces, "typefaces");
        checkArgument(!typefaces.isEmpty(), "Typefaces list cannot be empty");

        this.familyName = familyName;
        this.typefaces = typefaces;
    }

    /**
     * Returns the name of this family.
     *
     * @return The name of this family.
     */
    public @NonNull String getFamilyName() {
        return familyName;
    }

    /**
     * Returns the list of typefaces belonging to this family.
     *
     * @return The list of typefaces belonging to this family.
     */
    public @NonNull List<Typeface> getTypefaces() {
        return typefaces;
    }

    private static int widthGap(@NonNull TypeWidth desired, @NonNull TypeWidth candidate) {
        return Math.abs(desired.ordinal() - candidate.ordinal());
    }

    private static int slopeGap(@NonNull TypeSlope desired, @NonNull TypeSlope candidate) {
        int[] gaps = {
            // "If the value is `normal`, normal faces are checked first, then oblique faces, then
            // italic faces."
            /*   PLAIN: */ 0, 2, 1,

            // "If the value is `italic`, italic faces are checked first, then oblique, then normal
            // faces."
            /*  ITALIC: */ 2, 0, 1,

            // "If the value is `oblique`, oblique faces are checked first, then italic faces and
            // then normal faces."
            /* OBLIQUE: */ 2, 1, 0,
        };
        int row = desired.ordinal();
        int column = candidate.ordinal();

        return gaps[(row * 3) + column];
    }

    private static int weightGap(@NonNull TypeWeight desired, @NonNull TypeWeight candidate) {
        int[] gaps = {
            // "If the desired weight is less than 400, weights below the desired weight are checked
            // in descending order followed by weights above the desired weight in ascending order
            // until a match is found."
            /* 100: */ 0, 1, 2, 3, 4, 5, 6, 7, 8,
            /* 200: */ 1, 0, 2, 3, 4, 5, 6, 7, 8,
            /* 300: */ 2, 1, 0, 3, 4, 5, 6, 7, 8,

            // "If the desired weight is 400, 500 is checked first and then the rule for desired
            // weights less than 400 is used."
            /* 400: */ 4, 3, 2, 0, 1, 5, 6, 7, 8,

            // "If the desired weight is 500, 400 is checked first and then the rule for desired
            // weights less than 400 is used."
            /* 500: */ 4, 3, 2, 1, 0, 5, 6, 7, 8,

            // "If the desired weight is greater than 500, weights above the desired weight are
            // checked in ascending order followed by weights below the desired weight in descending
            // order until a match is found."
            /* 600: */ 8, 7, 6, 5, 4, 0, 1, 2, 3,
            /* 700: */ 8, 7, 6, 5, 4, 3, 0, 1, 2,
            /* 800: */ 8, 7, 6, 5, 4, 3, 2, 0, 1,
            /* 900: */ 8, 7, 6, 5, 4, 3, 2, 1, 0,
        };
        int row = desired.ordinal();
        int column = candidate.ordinal();

        return gaps[(row * 9) + column];
    }

    /**
     * Returns a typeface best matching the specified style.
     *
     * @param typeWidth The typographic width of desired typeface.
     * @param typeWeight The typographic weight of desired typeface.
     * @param typeSlope The typographic slope of desired typeface.
     * @return A typeface best matching the specified style.
     */
    public @NonNull Typeface getTypefaceByStyle(@NonNull TypeWidth typeWidth, @NonNull TypeWeight typeWeight, @NonNull TypeSlope typeSlope) {
        checkNotNull(typeWidth, "typeWidth");
        checkNotNull(typeWeight, "typeWeight");
        checkNotNull(typeSlope, "typeSlope");

        // BASED ON CSS FONT MATCHING ALGORITHM.
        Iterator<Typeface> iterator = typefaces.iterator();
        Typeface candidate = iterator.next();

        while (iterator.hasNext()) {
            Typeface current = iterator.next();

            int widthGap = widthGap(typeWidth, current.getWidth())
                         - widthGap(typeWidth, candidate.getWidth());
            if (widthGap > 0) {
                continue;
            }

            int slopeGap = slopeGap(typeSlope, current.getSlope())
                         - slopeGap(typeSlope, candidate.getSlope());
            if (slopeGap > 0) {
                continue;
            }

            int weightGap = weightGap(typeWeight, current.getWeight())
                          - weightGap(typeWeight, candidate.getWeight());
            if (weightGap > 0) {
                continue;
            }

            candidate = current;
        }

        return candidate;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this != obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            TypeFamily other = (TypeFamily) obj;
            if (!familyName.equals(other.familyName)
                    || !typefaces.equals(other.typefaces)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = familyName.hashCode();
        result = 31 * result + typefaces.hashCode();

        return result;
    }

    @Override
    public @NonNull String toString() {
        return "TypeFamily{familyName=" + familyName
                + ", typefaces=" + typefaces
                + "}";
    }
}
