package com.example.carbonfootprintcalculation.dashboard

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.carbonfootprintcalculation.R
import java.io.File
import android.view.View
import androidx.core.content.FileProvider
import android.content.Intent
import android.net.Uri
import android.util.Log

class PdfViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        val downloadButton = findViewById<Button>(R.id.downloadButton)

        val pdfPath = intent.getStringExtra("PDF_PATH")

        if (!pdfPath.isNullOrEmpty()) {
            val file = File(pdfPath)
            if (file.exists()) {
                try {
                    // Open PDF using default PDF viewer
                    openPdf(file)

                    // Enable download/share button
                    downloadButton.visibility = View.VISIBLE
                    downloadButton.setOnClickListener {
                        sharePdf(file)
                    }
                } catch (e: Exception) {
                    Log.e("PdfViewerActivity", "Error opening PDF: ${e.message}")
                    Toast.makeText(this, "Failed to open PDF", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Log.e("PdfViewerActivity", "PDF file not found: $pdfPath")
                Toast.makeText(this, "PDF file not found!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Log.e("PdfViewerActivity", "No PDF path provided")
            Toast.makeText(this, "No PDF path provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun openPdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sharePdf(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share PDF"))
        } catch (e: Exception) {
            Log.e("PdfViewerActivity", "Error sharing PDF: ${e.message}")
            Toast.makeText(this, "Error sharing PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
