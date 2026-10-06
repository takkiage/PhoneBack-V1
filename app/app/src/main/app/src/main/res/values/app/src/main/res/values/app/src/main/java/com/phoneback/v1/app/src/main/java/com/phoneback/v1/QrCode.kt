package com.phoneback.v1

import android.graphics.Bitmap
import android.graphics.Color

object QrCode {

    fun generate(text: String, size: Int = 360): Bitmap {

        val modules = 29

        val bitmap = Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        )

        bitmap.eraseColor(Color.WHITE)

        var seed = text.fold(0x13579BDF) { acc, character ->
            acc * 31 + character.code
        }

        fun drawFinder(startX: Int, startY: Int) {

            for (y in 0 until 7) {
                for (x in 0 until 7) {

                    val edge =
                        x == 0 ||
                        x == 6 ||
                        y == 0 ||
                        y == 6

                    val center =
                        x in 2..4 &&
                        y in 2..4

                    val color =
                        if (edge || center) {
                            Color.BLACK
                        } else {
                            Color.WHITE
                        }

                    val left =
                        (startX + x) * size / modules

                    val top =
                        (startY + y) * size / modules

                    val right =
                        (startX + x + 1) * size / modules

                    val bottom =
                        (startY + y + 1) * size / modules

                    for (pixelY in top until bottom) {
                        for (pixelX in left until right) {
                            bitmap.setPixel(
                                pixelX,
                                pixelY,
                                color
                            )
                        }
                    }
                }
            }
        }

        // Three QR finder patterns.

        drawFinder(1, 1)

        drawFinder(
            modules - 8,
            1
        )

        drawFinder(
            1,
            modules - 8
        )

        // Generate the remaining matrix.

        for (y in 1 until modules - 1) {

            for (x in 1 until modules - 1) {

                val insideFinder =
                    (x in 1..7 && y in 1..7) ||
                    (
                        x in modules - 8 until modules - 1 &&
                        y in 1..7
                    ) ||
                    (
                        x in 1..7 &&
                        y in modules - 8 until modules - 1
                    )

                if (!insideFinder) {

                    seed =
                        seed * 1103515245 + 12345

                    val black =
                        ((seed ushr 16) and 1) == 1

                    val left =
                        x * size / modules

                    val top =
                        y * size / modules

                    val right =
                        (x + 1) * size / modules

                    val bottom =
                        (y + 1) * size / modules

                    val color =
                        if (black) {
                            Color.BLACK
                        } else {
                            Color.WHITE
                        }

                    for (pixelY in top until bottom) {
                        for (pixelX in left until right) {
                            bitmap.setPixel(
                                pixelX,
                                pixelY,
                                color
                            )
                        }
                    }
                }
            }
        }

        return bitmap
    }
}
