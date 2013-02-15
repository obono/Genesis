/*
 * Copyright (C) 2013 OBN-soft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.obnsoft.chred;

import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Color;

public class ColData {

    public static final int MAX_PALS = 16;
    public static final int COLS_PER_PAL = 16;

    private static final byte[] HEADER1 =
            {'P', 'X', '0', '1', 0x0C, 0x02, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00};
    private static final byte[] HEADER2 =
            {'P', 'E', 'T', 'C', '0', '1', '0', '0', 'R', 'C', 'O', 'L'};

    private int[] mColor = new int[COLS_PER_PAL * MAX_PALS];

    public int getColor(int pal, int c) {
        if (pal < 0 || pal >= MAX_PALS || c < 0 || c >= COLS_PER_PAL) return Color.TRANSPARENT;
        return mColor[pal << 4 | c];
    }

    public void setColor(int pal, int c, int val) {
        if (pal < 0 || pal >= MAX_PALS || c < 0 || c >= COLS_PER_PAL) return;
        mColor[pal << 4 | c] = val | 0xFF000000;
    }

    public boolean loadFromStream(InputStream in) {
        byte[] data = new byte[HEADER2.length + mColor.length * 2];
        if (Utils.loadFromStreamCommon(in, HEADER1, data)) {
            int offset = HEADER2.length;
            for (int i = 0; i < mColor.length; i++) {
                int val = (data[offset + i * 2] | data[offset + i * 2 + 1] << 8) & 0x7FFF;
                mColor[i] = Color.rgb(
                        (val & 0x1f) * 8, (val >> 5 & 0x1f) * 8, (val >> 10 & 0x1f) * 8);
            }
            return true;
        }
        return false;
    }

    public boolean saveToStream(OutputStream out, String strName) {
        byte[] data = new byte[HEADER2.length + mColor.length * 2];
        System.arraycopy(HEADER2, 0, data, 0, HEADER2.length);
        int offset = HEADER2.length;
        for (int i = 0; i < mColor.length; i++) {
            int val = Color.red(mColor[i]) >> 3 |
                    (Color.green(mColor[i]) & 0xF8) << 2 |
                    (Color.blue(mColor[i]) & 0xF8) << 7;
            data[offset + i * 2]     = (byte) (val & 0xFF);
            data[offset + i * 2 + 1] = (byte) (val >> 8 & 0xFF);
        }
        return Utils.saveToStreamCommon(out, strName, HEADER1, data);
    }

}