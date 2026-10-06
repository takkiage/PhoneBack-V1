package com.phoneback.v1

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var messageInput: EditText
    private lateinit var preview: ImageView

    private var currentCard: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 28)
        }

        val title = TextView(this).apply {
            text = "PhoneBack V1"
            textSize = 30f
        }

        root.addView(title)

        nameInput = createInput("Name")
        phoneInput = createInput("Alternative phone number")
        messageInput = createInput("Recovery message").apply {
            minLines = 3
        }

        root.addView(nameInput)
        root.addView(phoneInput)
        root.addView(messageInput)

        val generateButton = Button(this).apply {
            text = "Generate Recovery Card"
            setOnClickListener {
                currentCard = createCard()
                preview.setImageBitmap(currentCard)
            }
        }

        root.addView(generateButton)

        preview = ImageView(this).apply {
            adjustViewBounds = true
            setPadding(0, 20, 0, 20)
        }

        root.addView(
            preview,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val saveButton = Button(this).apply {
            text = "Save Image"
            setOnClickListener {
                saveCard()
            }
        }

        val shareButton = Button(this).apply {
            text = "Share"
            setOnClickListener {
                shareCard()
            }
        }

        buttons.addView(
            saveButton,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        buttons.addView(
            shareButton,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        root.addView(buttons)

        setContentView(root)
    }

    private fun createInput(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 17f
        }
    }

    private fun createCard(): Bitmap {

        val width = 1000
        val height = 1250

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
        }

        paint.textSize = 58f
        canvas.drawText(
            "PHONEBACK",
            55f,
            85f,
            paint
        )

        paint.textSize = 38f
        canvas.drawText(
            "Recovery Card",
            55f,
            140f,
            paint
        )

        paint.textSize = 32f

        canvas.drawText(
            "Name: ${nameInput.text}",
            55f,
            220f,
            paint
        )

        canvas.drawText(
            "Alternative: ${phoneInput.text}",
            55f,
            275f,
            paint
        )

        paint.textSize = 27f

        canvas.drawText(
            "Recovery message:",
            55f,
            345f,
            paint
        )

        var y = 390f

        messageInput.text
            .toString()
            .chunked(48)
            .take(7)
            .forEach { line ->

                canvas.drawText(
                    line,
                    55f,
                    y,
                    paint
                )

                y += 38f
            }

        val qrData =
            "PhoneBack|Name=${nameInput.text}|Alternative=${phoneInput.text}|Message=${messageInput.text}"

        val qrBitmap = QrCode.generate(
            qrData,
            360
        )

        canvas.drawBitmap(
            qrBitmap,
            null,
            Rect(550, 710, 910, 1070),
            paint
        )

        paint.textSize = 22f

        canvas.drawText(
            "Scan QR for recovery information",
            530f,
            1110f,
            paint
        )

        canvas.drawText(
            "PhoneBack V1 • Offline",
            55f,
            1190f,
            paint
        )

        return bitmap
    }

    private fun saveCard() {

        val bitmap =
            currentCard ?: createCard().also {
                currentCard = it
            }

        val values = ContentValues().apply {

            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "PhoneBack-Recovery-Card.png"
            )

            put(
                MediaStore.Images.Media.MIME_TYPE,
                "image/png"
            )

            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/PhoneBack"
            )
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        if (uri != null) {

            contentResolver
                .openOutputStream(uri)
                ?.use { output ->

                    bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        output
                    )
                }

            Toast.makeText(
                this,
                "Recovery card saved to Pictures/PhoneBack",
                Toast.LENGTH_LONG
            ).show()

        } else {

            Toast.makeText(
                this,
                "Could not save image",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun shareCard() {

        val bitmap =
            currentCard ?: createCard().also {
                currentCard = it
            }

        val uri = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "PhoneBack Recovery Card",
            "PhoneBack V1"
        )

        if (uri != null) {

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(
                    Intent.EXTRA_STREAM,
                    Uri.parse(uri)
                )
            }

            startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share recovery card"
                )
            )
        }
    }
}
