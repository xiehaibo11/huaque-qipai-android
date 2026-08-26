package com.nanbeiyule.game;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import androidx.exifinterface.media.ExifInterface;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

final class AvatarImageProcessor {
    record ProcessedAvatar(Bitmap bitmap, byte[] jpeg) {
        ProcessedAvatar {
            if (bitmap == null || jpeg == null || jpeg.length == 0) {
                throw new IllegalArgumentException("Processed avatar must contain bitmap and JPEG");
            }
            jpeg = jpeg.clone();
        }

        @Override
        public byte[] jpeg() {
            return jpeg.clone();
        }
    }

    private static final int OUTPUT_SIZE = 512;
    private static final long MAX_PIXELS = 25_000_000L;
    private static final int MAX_SOURCE_BYTES = 8 * 1024 * 1024;

    private final Context context;

    AvatarImageProcessor(Context context) {
        this.context = context.getApplicationContext();
    }

    ProcessedAvatar process(Uri uri) throws IOException {
        if (uri == null) {
            throw new IOException("未选择照片");
        }
        File source = File.createTempFile("avatar-source-", ".image", context.getCacheDir());
        try {
            copySource(uri, source);
            Bitmap decoded = decodeSampled(source);
            Bitmap oriented = orient(decoded, new ExifInterface(source.getAbsolutePath()));
            if (oriented != decoded) {
                decoded.recycle();
            }
            AvatarCropGeometry.Crop crop =
                    AvatarCropGeometry.centerSquare(
                            oriented.getWidth(), oriented.getHeight());
            Bitmap square =
                    Bitmap.createBitmap(
                            oriented,
                            crop.left(),
                            crop.top(),
                            crop.size(),
                            crop.size());
            if (square != oriented) {
                oriented.recycle();
            }
            Bitmap output =
                    Bitmap.createScaledBitmap(square, OUTPUT_SIZE, OUTPUT_SIZE, true);
            if (output != square) {
                square.recycle();
            }
            ByteArrayOutputStream encoded = new ByteArrayOutputStream();
            if (!output.compress(Bitmap.CompressFormat.JPEG, 90, encoded)) {
                output.recycle();
                throw new IOException("无法压缩头像");
            }
            return new ProcessedAvatar(output, encoded.toByteArray());
        } finally {
            source.delete();
        }
    }

    private void copySource(Uri uri, File target) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        try (InputStream input = resolver.openInputStream(uri);
                FileOutputStream output = new FileOutputStream(target)) {
            if (input == null) {
                throw new IOException("无法读取所选照片");
            }
            byte[] buffer = new byte[8192];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_SOURCE_BYTES) {
                    throw new IOException("照片不能超过 8 MiB");
                }
                output.write(buffer, 0, read);
            }
            output.getFD().sync();
        }
        if (target.length() <= 0) {
            throw new IOException("所选照片为空");
        }
    }

    private static Bitmap decodeSampled(File source) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(source.getAbsolutePath(), bounds);
        if (bounds.outWidth <= 0
                || bounds.outHeight <= 0
                || (long) bounds.outWidth * bounds.outHeight > MAX_PIXELS) {
            throw new IOException("照片尺寸不符合要求");
        }
        int sample = 1;
        while (bounds.outWidth / sample > 2048 || bounds.outHeight / sample > 2048) {
            sample *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap decoded = BitmapFactory.decodeFile(source.getAbsolutePath(), options);
        if (decoded == null) {
            throw new IOException("所选文件不是有效图片");
        }
        return decoded;
    }

    private static Bitmap orient(Bitmap source, ExifInterface exif) {
        int orientation =
                exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1.0f, 1.0f);
            case ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180.0f);
            case ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1.0f, -1.0f);
            case ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90.0f);
                matrix.postScale(-1.0f, 1.0f);
            }
            case ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90.0f);
            case ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90.0f);
                matrix.postScale(-1.0f, 1.0f);
            }
            case ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90.0f);
            default -> {
                return source;
            }
        }
        return Bitmap.createBitmap(
                source,
                0,
                0,
                source.getWidth(),
                source.getHeight(),
                matrix,
                true);
    }
}
