package com.luo123.androidlab

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat


class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)
        findPreference<Preference>("fuck")?.setOnPreferenceClickListener {
            //卸载应用
            val uninstallIntent =  Intent()
            uninstallIntent.action = Intent.ACTION_DELETE;
            uninstallIntent.data = Uri.parse("package:"+ context!!.packageName)
            context!!.startActivity(uninstallIntent)
            true
        }
        //值改变的监听器
        findPreference<ListPreference>("setting_network_type")?.setOnPreferenceChangeListener { preference, newValue ->
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("提示")
            alertDialog.setMessage("修改成功，重启应用后生效")
            alertDialog.setPositiveButton("确定",DialogInterface.OnClickListener { dialog, which ->
                //重启当前应用
                Process.killProcess(Process.myPid())
            })
            alertDialog.show()
            true
        }

    }


}