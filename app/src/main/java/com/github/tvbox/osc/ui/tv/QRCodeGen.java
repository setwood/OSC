package com.github.tvbox.osc.ui.tv;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.HashMap;

public class QRCodeGen {
    public static Bitmap generateBitmap(String str, int i, int i2, int i3) {
        QRCodeWriter qRCodeWriter = new QRCodeWriter();
        HashMap hashMap = new HashMap();
        hashMap.put(EncodeHintType.CHARACTER_SET, "utf-8");
        EncodeHintType encodeHintType = EncodeHintType.MARGIN;
        hashMap.put(encodeHintType, i3 + "");
        try {
            BitMatrix encode = qRCodeWriter.encode(str, BarcodeFormat.QR_CODE, i, i2, hashMap);
            int[] iArr = new int[(i * i2)];
            for (int i4 = 0; i4 < i2; i4++) {
                for (int i5 = 0; i5 < i; i5++) {
                    if (encode.get(i5, i4)) {
                        iArr[(i4 * i) + i5] = 0;
                    } else {
                        iArr[(i4 * i) + i5] = -1;
                    }
                }
            }
            return Bitmap.createBitmap(iArr, 0, i, i, i2, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap generateBitmap(String str, int i, int i2) {
        return generateBitmap(str, i, i2, 0);
    }
}
