package com.luo123.androidlab.update

import android.app.DownloadManager
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.luo123.androidlab.MainActivity
import okhttp3.*
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.IOException


class Updater(val context: Context, val handler: Handler) {
    val UPDATELISTURL = "https://github.com/mzdluo123/AndroidLabClient/raw/master/update.yaml"

    fun needUpdate(code: Int): Boolean {
        val packageManager = context.packageManager
        val nowCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(context.packageName, 0).longVersionCode
        } else {
            packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
        }
        return nowCode < code
    }


    fun checkUpdate(force:Boolean) {
        if (!force && !PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("auto_check_update",true)){
            return
        }
        val request = Request.Builder().url(UPDATELISTURL).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post {
                    Toast.makeText(context, "检查更新失败 ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val yaml = Yaml()
                val messageList =
                    yaml.loadAs(response.body?.byteStream(), UpdateMessageListModel::class.java)
                val model = messageList.latest
                if (!needUpdate(messageList.latestVersionCode)){
                    if (force){
                        Toast.makeText(context,"你当前使用的是最新版本",Toast.LENGTH_SHORT).show()
                    }
                    return
                }
                handler.post {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.setTitle("发现新版本 ${model.version}")
                    alertDialog.setMessage(model.message)
                    alertDialog.setCancelable(false)
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
                        if (context != null) {
                            installApk(context,path)
                        }
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

        private fun installApk(context: Context, downloadApk: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            //版本在7.0以上是不能直接通过uri访问的
            //版本在7.0以上是不能直接通过uri访问的
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                val file = File(downloadApk)
                // 由于没有在Activity环境下启动Activity,设置下面的标签
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                //参数1:上下文, 参数2:Provider主机地址 和配置文件中保持一致,参数3:共享的文件
                val apkUri =
                    FileProvider.getUriForFile(context, context.packageName, file)
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            } else {
                intent.setDataAndType(
                    Uri.fromFile(File(downloadApk)),
                    "application/vnd.android.package-archive"
                )
            }
            context.startActivity(intent)
        }
    }
}

