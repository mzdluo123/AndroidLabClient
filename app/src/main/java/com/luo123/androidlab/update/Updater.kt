package com.luo123.androidlab.update

import android.app.DownloadManager
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import okhttp3.*
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.IOException


class Updater(val context: Context, val handler: Handler) {
    val UPDATELISTURL = "http"

    fun checkUpdate() {
        val request = Request.Builder().url(UPDATELISTURL).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post {
                    Toast.makeText(context, "检查更新失败 ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val yaml = Yaml()
                val messageList =
                    yaml.loadAs(response.body?.byteStream(), UpdateMessageListModel::class.java)
                val model = messageList.latested
                handler.post {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("发现新版本 ${model.version}")
                    alertDialog.setMessage(model.message)
                    alertDialog.setPositiveButton(
                        "下载新版本",
                        DialogInterface.OnClickListener { dialog, which ->
                            //调用系统下载
                            val req =
                                DownloadManager.Request(Uri.parse(model.downloadUrl))
                            // 设置不允许漫游
                            req.setAllowedOverRoaming(false)
                            //在通知栏中显示，默认就是显示的
                            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                            req.setTitle("正在下载新版本 ${model.version}")
                            req.setDescription("文件正在下载中......")
                            req.setVisibleInDownloadsUi(true)
                            //设置下载的路径
                            //设置下载的路径
                            val file = File(
                                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                "${model.version}.apk"
                            )
                            req.setDestinationUri(Uri.fromFile(file))
                            val downloadManager =
                                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val downloadId = downloadManager.enqueue(req)

                            //注册下载监听器
                            context.registerReceiver(
                                UpdateDownloadBroadcastReceiver(
                                    downloadId,
                                    downloadManager,
                                    file.path
                                ),
                                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                            )
                        })


                    alertDialog.setNegativeButton(
                        "取消",
                        DialogInterface.OnClickListener { dialog, which ->

                        })
                    alertDialog.show()
                }
            }
        })

    }

    class UpdateDownloadBroadcastReceiver(
        val downloadId: Long,
        val downloadManager: DownloadManager,
        val path: String
    ) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val query = DownloadManager.Query()

            //通过下载的id查找
            query.setFilterById(downloadId)
            val cursor: Cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status: Int =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_PAUSED -> {
                    }
                    DownloadManager.STATUS_PENDING -> {
                    }
                    DownloadManager.STATUS_RUNNING -> {
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        val intent = Intent()

                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        intent.setAction(Intent.ACTION_VIEW) //动作，查看
                        intent.setDataAndType(
                            Uri.parse(path),
                            "application/vnd.android.package-archive"
                        );//设置类型
                        context?.startActivity(intent)

                        cursor.close()
                        context!!.unregisterReceiver(this)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT)
                            .show()
                        cursor.close()
                        context!!.unregisterReceiver(this)
                    }
                }
            }

        }
    }
}

