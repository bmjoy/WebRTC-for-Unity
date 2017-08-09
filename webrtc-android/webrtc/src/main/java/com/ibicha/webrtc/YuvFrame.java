package com.ibicha.webrtc;

// Some work based on http://stackoverflow.com/a/12702836 by rics (http://stackoverflow.com/users/21047/rics)

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

// TODO: project should include io.pristine:libjingle from Maven
import org.webrtc.VideoRenderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class YuvFrame {
    private static final String TAG = YuvFrame.class.getSimpleName();
    public int width;
    public int height;
    public int[] yuvStrides;
    public byte[] yPlane;
    public byte[] uPlane;
    public byte[] vPlane;
    public int rotationDegree;
    public long timestamp;

    private final Object planeLock = new Object();

    public static final int PROCESSING_NONE = 0x00;
    public static final int PROCESSING_CROP_TO_SQUARE = 0x01;

    // Constants for indexing I420Frame information, for readability.
    private static final int I420_Y = 0;
    private static final int I420_V = 1;
    private static final int I420_U = 2;


    /**
     * Creates a YuvFrame from the provided I420Frame. Does no processing, and uses the current time as a timestamp.
     *
     * @param i420Frame Source I420Frame.
     */
    @SuppressWarnings("unused")
    public YuvFrame(final VideoRenderer.I420Frame i420Frame) {
        fromI420Frame(i420Frame, PROCESSING_NONE, System.nanoTime());
    }


    /**
     * Creates a YuvFrame from the provided I420Frame. Does any processing indicated, and uses the current time as a timestamp.
     *
     * @param i420Frame       Source I420Frame.
     * @param processingFlags Processing flags, YuvFrame.PROCESSING_NONE for no processing.
     */
    @SuppressWarnings("unused")
    public YuvFrame(final VideoRenderer.I420Frame i420Frame, final int processingFlags) {
        fromI420Frame(i420Frame, processingFlags, System.nanoTime());
    }


    /**
     * Creates a YuvFrame from the provided I420Frame. Does any processing indicated, and uses the given timestamp.
     *
     * @param i420Frame       Source I420Frame.
     * @param processingFlags Processing flags, YuvFrame.PROCESSING_NONE for no processing.
     * @param timestamp       The timestamp to give the frame.
     */
    public YuvFrame(final VideoRenderer.I420Frame i420Frame, final int processingFlags, final long timestamp) {
        fromI420Frame(i420Frame, processingFlags, timestamp);
    }


    /**
     * Replaces the data in this YuvFrame with the data from the provided frame. Will create new byte arrays to hold pixel data if necessary,
     * or will reuse existing arrays if they're already the correct size.
     *
     * @param i420Frame       Source I420Frame.
     * @param processingFlags Processing flags, YuvFrame.PROCESSING_NONE for no processing.
     * @param timestamp       The timestamp to give the frame.
     */
    public void fromI420Frame(final VideoRenderer.I420Frame i420Frame, final int processingFlags, final long timestamp) {
        synchronized (planeLock) {
            try {
                // Save timestamp
                this.timestamp = timestamp;

                // TODO: Check to see if i420Frame.yuvFrame is actually true?  Need to find out what the alternative would be.

                // Copy YUV stride information
                // TODO: There is probably a case where strides makes a difference, so far we haven't run across it.
                yuvStrides = new int[i420Frame.yuvStrides.length];
                System.arraycopy(i420Frame.yuvStrides, 0, yuvStrides, 0, i420Frame.yuvStrides.length);

                // Copy rotation information
                rotationDegree = i420Frame.rotationDegree;  // Just save rotation info for now, doing actual rotation can wait until per-pixel processing.

                // Copy the pixel data, processing as requested.
                if (PROCESSING_CROP_TO_SQUARE == (processingFlags & PROCESSING_CROP_TO_SQUARE)) {
                    copyPlanesCropped(i420Frame);
                } else {
                    copyPlanes(i420Frame);
                }
            } catch (Throwable t) {
                dispose();
            }
        }
    }


    public void dispose() {
        yPlane = null;
        vPlane = null;
        uPlane = null;
    }


    public boolean hasData() {
        return yPlane != null && vPlane != null && uPlane != null;
    }


    /**
     * Copy the Y, V, and U planes from the source I420Frame.
     * Sets width and height.
     *
     * @param i420Frame Source frame.
     */
    private void copyPlanes(final VideoRenderer.I420Frame i420Frame) {
        synchronized (planeLock) {
            // Copy the Y, V, and U ButeBuffers to their corresponding byte arrays.
            // Existing byte arrays are passed in for possible reuse.
            yPlane = copyByteBuffer(yPlane, i420Frame.yuvPlanes[I420_Y]);
            vPlane = copyByteBuffer(vPlane, i420Frame.yuvPlanes[I420_V]);
            uPlane = copyByteBuffer(uPlane, i420Frame.yuvPlanes[I420_U]);

            // Set the width and height of the frame.
            width = i420Frame.width;
            height = i420Frame.height;
        }
    }


    /**
     * Copies the entire contents of a ByteBuffer into a byte array.
     * If the byte array exists, and is the correct size, it will be reused.
     * If the byte array is null, or isn't properly sized, a new byte array will be created.
     *
     * @param dst A byte array to copy the ByteBuffer contents to. Can be null.
     * @param src A ByteBuffer to copy data from.
     * @return A byte array containing the contents of the ByteBuffer. If the provided dst was non-null and the correct size,
     * it will be returned. If not, a new byte array will be created.
     */
    private byte[] copyByteBuffer(@Nullable byte[] dst, @NonNull final ByteBuffer src) {
        // Create a new byte array if necessary.
        byte[] out;
        if ((null == dst) || (dst.length != src.capacity())) {
            out = new byte[src.capacity()];
        } else {
            out = dst;
        }

        // Copy the ByteBuffer's contents to the byte array.
        src.get(out);

        return out;
    }


    /**
     * Copy the Y, V, and U planes from the source I420Frame, cropping them to square.
     * Sets width and height.
     *
     * @param i420Frame Source frame.
     */
    private void copyPlanesCropped(final VideoRenderer.I420Frame i420Frame) {
        synchronized (planeLock) {
            // Verify that the dimensions of the I420Frame are appropriate for cropping
            // If improper dimensions are found, default back to copying the entire frame.
            final int width = i420Frame.width;
            final int height = i420Frame.height;

            if (width > height) {
                // Calculate the size of the cropped portion of the the image
                // The cropped width must be divisible by 4, since it will be divided by 2 to crop the center of the frame,
                // and then divided by two again for processing the U and V planes, as each value there corresponds to
                // a 2x2 square of pixels. All of those measurements must be whole integers.
                final int cropWidth = width - height;
                if ((cropWidth % 4) == 0) {
                    // Create a row buffer for the crop method to use - the largest row width will be equal to the source frame's height (since we're cropping to square)
                    final byte[] row = new byte[height];  // TODO: Create a static row buffer, so this doesn't get created for every frame?

                    // Copy the Y plane.  Existing yPlane is passed in for possible reuse if it's the same size.
                    yPlane = cropByteBuffer(yPlane, i420Frame.yuvPlanes[I420_Y], width, height, row);

                    // Copy/crop the U and V planes. The U and V planes' width and height will be half that of the Y plane's.
                    // The same row buffer can be reused, since being oversize isn't an issue.
                    vPlane = cropByteBuffer(vPlane, i420Frame.yuvPlanes[I420_V], width / 2, height / 2, row);
                    uPlane = cropByteBuffer(uPlane, i420Frame.yuvPlanes[I420_U], width / 2, height / 2, row);

                    // Set size
                    // noinspection SuspiciousNameCombination (Shut up, Lint, I know what I'm doing.)
                    this.width = height;
                    this.height = height;
                } else {
                    copyPlanes(i420Frame);
                }
            } else {
                // Calculate the size of the cropped portion of the the image
                // The cropped height must be divisible by 4, since it will be divided by 2 to crop the center of the frame,
                // and then divided by two again for processing the U and V planes, as each value there corresponds to
                // a 2x2 square of pixels. All of those measurements must be whole integers.
                final int cropHeight = height - width;
                if ((cropHeight % 4) == 0) {
                    // Copy the Y plane. (No row buffer is needed if height >= width.)
                    yPlane = cropByteBuffer(yPlane, i420Frame.yuvPlanes[I420_Y], width, height, null);

                    // Copy/crop the U and V planes. The U and V planes' width and height will be half that of the Y plane's.
                    // The same row buffer can be reused, since being oversize isn't an issue.
                    vPlane = cropByteBuffer(vPlane, i420Frame.yuvPlanes[I420_V], width / 2, height / 2, null);
                    uPlane = cropByteBuffer(uPlane, i420Frame.yuvPlanes[I420_U], width / 2, height / 2, null);

                    // Set size
                    // noinspection SuspiciousNameCombination (Shut up, Lint, I know what I'm doing.)
                    this.height = width;
                    this.width = width;
                } else {
                    copyPlanes(i420Frame);
                }
            }
        }
    }


    /**
     * Copies the contents of a ByteBuffer into a byte array, cropping the center of the image to square.
     * If the byte array exists, and is the correct size, it will be reused.
     * If the byte array is null, or isn't properly sized, a new byte array will be created.
     *
     * @param dst       A byte array to copy the ByteBuffer contents to. Can be null.
     * @param src       A ByteBuffer to copy data from.
     * @param srcWidth  The width of the source frame.
     * @param srcHeight The height of ths source frame.
     * @param row       A byte array with a length equal to or greater than the cropped frame's width, for use as a buffer.
     *                  Can be null. If no row buffer is provided and one is needed, or the buffer is too short, an exception
     *                  will likely result.
     * @return A byte array containing the cropped contents of the ByteBuffer. If the provided dst was non-null and the correct size,
     * it will be returned. If not, a new byte array will be created.
     * @throws NullPointerException
     */
    private byte[] cropByteBuffer(@Nullable byte[] dst, @NonNull final ByteBuffer src, final int srcWidth, final int srcHeight, @Nullable final byte[] row)
            throws NullPointerException {
        // If the frame is wider than it is tall, copy the center of each row to trim off the left and right edges
        if (srcWidth > srcHeight) {
            // We'll need a row buffer, here. Throw an exception if we don't have one.
            if (null == row) {
                throw new NullPointerException("YuvFrame.cropByteBffer: Need row buffer array, and the array provided was null.");
            }

            // Create a new destination byte array if necessary.
            final int croppedSize = srcHeight * srcHeight;
            final byte[] out;
            if ((null == dst) || (dst.length != croppedSize)) {
                out = new byte[croppedSize];
            } else {
                out = dst;
            }

            // Calculate where on each row to start copying
            final int indent = (srcWidth - srcHeight) / 2;

            // Copy the ByteBuffer
            for (int i = 0; i < srcHeight; i++) {
                // Set the position of the ByteBuffer to the beginning of the current row,
                // adding the calculated indent to trim off the left side.
                src.position((i * srcWidth) + indent);

                // Copy the cropped row to the row buffer
                src.get(row, 0, srcHeight);

                // Copy the row buffer to the destination array
                System.arraycopy(row, 0, out, i * srcHeight, srcHeight);
            }

            return out;
        }
        // If the frame is taller than it is wide, copy the center of the image, cropping off the top and bottom edges.
        // NOTE: If the width and height are equal, this method should result in a straight copy of the source ByteBuffer,
        //       as the calculated row offset will be zero.
        else {
            // Create a new destination byte array if necessary.
            final int croppedSize = srcWidth * srcWidth;
            final byte[] out;
            if ((null == dst) || (dst.length != croppedSize)) {
                out = new byte[croppedSize];
            } else {
                out = dst;
            }

            // Calculate where to start reading
            final int start = ((srcHeight - srcWidth) / 2) * srcWidth;  // ((h-w)/2) is the number of rows to skip, multiply by w again to get the starting ByteBuffer position.

            // Copy the ByteBuffer
            // Since we need to take a sequential series of whole rows, only one copy is necessary
            src.position(start);
            src.get(out, 0, croppedSize);

            return out;
        }
    }


    static Bitmap cacheBitmap = null;
    static int[] argb = null;

    /**
     * Converts this YUV frame to an ARGB_8888 Bitmap. Applies stored rotation.
     * Remaning code based on http://stackoverflow.com/a/12702836 by rics (http://stackoverflow.com/users/21047/rics)
     *
     * @return A new Bitmap containing the converted frame.
     */
    public Bitmap getBitmap() {
        // Calculate the size of the frame
        final int size = width * height;

        if (argb == null || argb.length != size) {
            argb = new int[size];
        }
        // Allocate an array to hold the ARGB pixel data
        convertYuvToArgbRot0(argb);
        Log.d(TAG, "getBitmap: convertYuvToArgbRot0");
        // Create Bitmap from ARGB pixel data.

        if (cacheBitmap == null || cacheBitmap.getWidth() != width || cacheBitmap.getHeight() != height) {
            cacheBitmap = Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888);
            cacheBitmap.setPremultiplied(false);
        } else {
            cacheBitmap.copyPixelsFromBuffer(IntBuffer.wrap(argb));
        }
        if (true)
            return cacheBitmap;

        if (rotationDegree == 90 || rotationDegree == -270) {
            convertYuvToArgbRot90(argb);
            Log.d(TAG, "getBitmap: convertYuvToArgbRot90");
            // Create Bitmap from ARGB pixel data.
            // noinspection SuspiciousNameCombination (Rotating image swaps width/height, name mismatch is fine, Lint.)
            return Bitmap.createBitmap(argb, height, width, Bitmap.Config.ARGB_8888);
        } else if (rotationDegree == 180 || rotationDegree == -180) {
            convertYuvToArgbRot180(argb);
            Log.d(TAG, "getBitmap: convertYuvToArgbRot180");

            // Create Bitmap from ARGB pixel data.
            return Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888);
        } else if (rotationDegree == 270 || rotationDegree == -90) {
            convertYuvToArgbRot270(argb);
            Log.d(TAG, "getBitmap: convertYuvToArgbRot270");

            // Create Bitmap from ARGB pixel data.
            // noinspection SuspiciousNameCombination (Rotating image swaps width/height, name mismatch is fine, Lint.)
            return Bitmap.createBitmap(argb, height, width, Bitmap.Config.ARGB_8888);
        } else {
            convertYuvToArgbRot0(argb);
            Log.d(TAG, "getBitmap: convertYuvToArgbRot0");
            // Create Bitmap from ARGB pixel data.
            return Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888);
        }
    }

    public byte[] getARGBBuffer() {
        Bitmap b = getBitmap();
        int bytes = b.getByteCount();
        //or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
        //int bytes = b.getWidth()*b.getHeight()*4;

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
        b.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

        byte[] array = buffer.array(); //Get the underlying array containing the data.

        return array;
    }

    private void convertYuvToArgbRot0(final int[] outputArgb) {
        synchronized (planeLock) {
            // Calculate the size of the frame
            int size = width * height;

            // Each U/V cell is overlaid on a 2x2 block of Y cells.
            // Loop through the size of the U/V planes, and manage the 2x2 Y block on each iteration.
            int u, v;
            int y1, y2, y3, y4;
            int p1, p2, p3, p4;
            int rowOffset = 0;  // Y and RGB array position is offset by an extra row width each iteration, since they're handled as 2x2 sections.

            final int uvSize = size / 4;   // U/V plane is one quarter the total size of the frame.
            final int uvWidth = width / 2;  // U/V plane width is half the width of the frame.

            for (int i = 0; i < uvSize; i++) {
                // At the end of each row, increment the Y/RGB row offset by an extra frame width
                if (i != 0 && (i % (uvWidth)) == 0) {
                    rowOffset += width;
                }

                // Calculate the 2x2 grid indices
                p1 = rowOffset + (i * 2);
                p2 = p1 + 1;
                p3 = p1 + width;
                p4 = p3 + 1;

                // Get the U and V values from the source.
                u = uPlane[i] & 0xff;
                v = vPlane[i] & 0xff;
                u = u - 128;
                v = v - 128;

                // Get the Y values for the matching 2x2 pixel block
                y1 = yPlane[p1] & 0xff;
                y2 = yPlane[p2] & 0xff;
                y3 = yPlane[p3] & 0xff;
                y4 = yPlane[p4] & 0xff;

                // Convert each YUV pixel to RGB
                outputArgb[p1] = convertYuvToArgb(y1, u, v);
                outputArgb[p2] = convertYuvToArgb(y2, u, v);
                outputArgb[p3] = convertYuvToArgb(y3, u, v);
                outputArgb[p4] = convertYuvToArgb(y4, u, v);
            }
        }
    }


    private void convertYuvToArgbRot90(final int[] outputArgb) {
        synchronized (planeLock) {
            int u, v;
            int y1, y2, y3, y4;
            int p1, p2, p3, p4;
            int d1, d2, d3, d4;
            int uvIndex;

            final int uvWidth = width / 2;  // U/V plane width is half the width of the frame.
            final int uvHeight = height / 2;  // U/V plane height is half the height of the frame.

            int rotCol;
            int rotRow;

            // Each U/V cell is overlaid on a 2x2 block of Y cells.
            // Loop through the size of the U/V planes, and manage the 2x2 Y block on each iteration.
            for (int row = 0; row < uvHeight; row++) {
                // Calculate the column on the rotated image from the row on the source image
                rotCol = (uvHeight - 1) - row;

                for (int col = 0; col < uvWidth; col++) {
                    // Calculate the row on the rotated image from the column on the source image
                    rotRow = col;

                    // Calculate the 2x2 grid indices
                    p1 = (row * width * 2) + (col * 2);
                    p2 = p1 + 1;
                    p3 = p1 + width;
                    p4 = p3 + 1;

                    // Get the U and V values from the source.
                    uvIndex = (row * uvWidth) + col;
                    u = uPlane[uvIndex] & 0xff;
                    v = vPlane[uvIndex] & 0xff;
                    u = u - 128;
                    v = v - 128;

                    // Get the Y values for the matching 2x2 pixel block
                    y1 = yPlane[p1] & 0xff;
                    y2 = yPlane[p2] & 0xff;
                    y3 = yPlane[p3] & 0xff;
                    y4 = yPlane[p4] & 0xff;

                    // Calculate the destination 2x2 grid indices
                    d1 = (rotRow * height * 2) + (rotCol * 2) + 1;
                    d2 = d1 + height;
                    d3 = d1 - 1;
                    d4 = d3 + height;

                    // Convert each YUV pixel to RGB
                    outputArgb[d1] = convertYuvToArgb(y1, u, v);
                    outputArgb[d2] = convertYuvToArgb(y2, u, v);
                    outputArgb[d3] = convertYuvToArgb(y3, u, v);
                    outputArgb[d4] = convertYuvToArgb(y4, u, v);
                }
            }
        }
    }


    private void convertYuvToArgbRot180(final int[] outputArgb) {
        synchronized (planeLock) {
            // Calculate the size of the frame
            int size = width * height;

            // Each U/V cell is overlaid on a 2x2 block of Y cells.
            // Loop through the size of the U/V planes, and manage the 2x2 Y block on each iteration.
            int u, v;
            int y1, y2, y3, y4;
            int p1, p2, p3, p4;
            int rowOffset = 0;  // Y and RGB array position is offset by an extra row width each iteration, since they're handled as 2x2 sections.

            final int uvSize = size / 4;   // U/V plane is one quarter the total size of the frame.
            final int uvWidth = width / 2;  // U/V plane width is half the width of the frame.
            final int invertSize = size - 1;  // Store size - 1 so it doesn't have to be calculated 4x every iteration.

            for (int i = 0; i < uvSize; i++) {
                // At the end of each row, increment the Y/RGB row offset by an extra frame width
                if (i != 0 && (i % (uvWidth)) == 0) {
                    rowOffset += width;
                }

                // Calculate the 2x2 grid indices
                p1 = rowOffset + (i * 2);
                p2 = p1 + 1;
                p3 = p1 + width;
                p4 = p3 + 1;

                // Get the U and V values from the source.
                u = uPlane[i] & 0xff;
                v = vPlane[i] & 0xff;
                u = u - 128;
                v = v - 128;

                // Get the Y values for the matching 2x2 pixel block
                y1 = yPlane[p1] & 0xff;
                y2 = yPlane[p2] & 0xff;
                y3 = yPlane[p3] & 0xff;
                y4 = yPlane[p4] & 0xff;

                // Convert each YUV pixel to RGB
                outputArgb[invertSize - p1] = convertYuvToArgb(y1, u, v);
                outputArgb[invertSize - p2] = convertYuvToArgb(y2, u, v);
                outputArgb[invertSize - p3] = convertYuvToArgb(y3, u, v);
                outputArgb[invertSize - p4] = convertYuvToArgb(y4, u, v);
            }
        }
    }


    // TODO: This is just rot90 reversed - would probably be a little faster if it was actually rotating -90 instead. Realistically, who cares.
    private void convertYuvToArgbRot270(final int[] outputArgb) {
        synchronized (planeLock) {
            // Calculate the size of the frame
            int size = width * height;

            int u, v;
            int y1, y2, y3, y4;
            int p1, p2, p3, p4;
            int d1, d2, d3, d4;
            int uvIndex;

            final int uvWidth = width / 2;  // U/V plane width is half the width of the frame.
            final int uvHeight = height / 2;  // U/V plane height is half the height of the frame.
            final int invertSize = size - 1;  // Store size - 1 so it doesn't have to be calculated 4x every iteration.

            int rotCol;
            int rotRow;

            // Each U/V cell is overlaid on a 2x2 block of Y cells.
            // Loop through the size of the U/V planes, and manage the 2x2 Y block on each iteration.
            for (int row = 0; row < uvHeight; row++) {
                // Calculate the column on the rotated image from the row on the source image
                rotCol = (uvHeight - 1) - row;

                for (int col = 0; col < uvWidth; col++) {
                    // Calculate the row on the rotated image from the column on the source image
                    rotRow = col;

                    // Calculate the 2x2 grid indices
                    p1 = (row * width * 2) + (col * 2);
                    p2 = p1 + 1;
                    p3 = p1 + width;
                    p4 = p3 + 1;

                    // Get the U and V values from the source.
                    uvIndex = (row * uvWidth) + col;
                    u = uPlane[uvIndex] & 0xff;
                    v = vPlane[uvIndex] & 0xff;
                    u = u - 128;
                    v = v - 128;

                    // Get the Y values for the matching 2x2 pixel block
                    y1 = yPlane[p1] & 0xff;
                    y2 = yPlane[p2] & 0xff;
                    y3 = yPlane[p3] & 0xff;
                    y4 = yPlane[p4] & 0xff;

                    // Calculate the destination 2x2 grid indices
                    d1 = (rotRow * height * 2) + (rotCol * 2) + 1;
                    d2 = d1 + height;
                    d3 = d1 - 1;
                    d4 = d3 + height;

                    // Convert each YUV pixel to ARGB
                    outputArgb[invertSize - d1] = convertYuvToArgb(y1, u, v);
                    outputArgb[invertSize - d2] = convertYuvToArgb(y2, u, v);
                    outputArgb[invertSize - d3] = convertYuvToArgb(y3, u, v);
                    outputArgb[invertSize - d4] = convertYuvToArgb(y4, u, v);
                }
            }
        }
    }


    private int convertYuvToArgb(final int y, final int u, final int v) {
        int r, g, b;

        // Convert YUV to RGB
        r = y + (int) (1.402f * v);
        g = y - (int) (0.344f * u + 0.714f * v);
        b = y + (int) (1.772f * u);

        return Color.argb(255, r, g, b);
    }
}