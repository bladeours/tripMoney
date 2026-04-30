package cc.n0th1ng.tripmoney.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun saveCsv(context: Context, fileName: String, content: String): File {
    val file = File(context.cacheDir, "$fileName.csv")
    file.writeText(content)
    return file
}

fun shareCsv(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, "Share CSV")
    )
}