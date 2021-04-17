package com.example.emmc_kotlin

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.*
import java.util.*


class CopyElfs(var ct: Context) {
    var TAG = "Ce_Debug:"
    var appFileDirectory: String

    //得到可执行文件的路径
    var executableFilePath: String
    var assetManager: AssetManager
    var resList: List<*>? = null
    val rootcmd = "sh"
    var cpuType: String
    private lateinit var dos: DataOutputStream
    private lateinit var br: BufferedReader
    private lateinit var err: BufferedReader
    var assetsFiles = arrayOf(
        "mmc"
    )
    fun resFileExist(filename: String): Boolean {
        val f = File("$executableFilePath/$filename")
        return if (f.exists()) true else false
    }

    fun copyFile(`in`: InputStream, out: OutputStream) {
        try {
            Log.d("callelf","!!!!!!!!执行到此")
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read/write asset file: ", e)
        }
    }

    private fun copyAssets(filename: String) {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        Log.d(TAG, "Attempting to copy this file: $filename")
        try {
            `in` = assetManager.open("$cpuType/$filename")
            val outFile = File(executableFilePath, filename)
            out = FileOutputStream(outFile)
            copyFile(`in`, out)
            `in`.close()
            `in` = null
            out.flush()
            out.close()
            out = null
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy asset file: $filename", e)
        }
        Log.d(TAG, "Copy success: $filename")
    }

    fun copyAll2Data() {
        var i: Int
        val folder = File(executableFilePath)
        if (!folder.exists()) {
            folder.mkdir()
        }
        i = 0
        while (i < assetsFiles.size) {
            if (!resFileExist(assetsFiles[i])) {
                copyAssets(assetsFiles[i])
                val execFile = File(executableFilePath + "/" + assetsFiles[i])
                execFile.setExecutable(true)
            }
            i++
        }
    }


    @Throws(InterruptedException::class)
    fun callElf(cmd: String): List<String> {
        var cmd = cmd
        var tmpText: String=""
        val mmc_elf = "$executableFilePath/mmc "
        cmd = mmc_elf + cmd
        val list: MutableList<String> = ArrayList()

        try {
            val p = Runtime.getRuntime().exec("sh") // 获取root
            dos = DataOutputStream(p.outputStream) // 写入流
            br = BufferedReader(InputStreamReader(p.inputStream)) // 输出缓存


            err = BufferedReader(InputStreamReader(p.errorStream)) // 错误流缓存
            if (err==null)
                err.close()
            dos.writeBytes(
                """
                    $cmd
                    
                    """.trimIndent()
            )
            Log.e("展示命令", "exe cmd: $cmd") //写
            dos.flush() //
            dos.writeBytes("exit\n")
            dos.flush()
            var i = 0
            var j = 0
            if(br!=null)
            {

                while ((br.readLine())?.also { tmpText = it } != null) {
                    //tmpText += "\n";
                    i++
                    Log.i("std", "std: $tmpText")
                    list.add(tmpText)
                }
                Log.i("callElf", "callElf num: $i")
            }
            if(err!=null)
            {
                //空指针异常的排除
                while ((err.readLine())?.also { tmpText = it } != null) {
                    tmpText += "\n"
                    j++
                    Log.i("stderr", "stderr: $tmpText")
                    list.add(tmpText)
                }
                Log.i("callElf", "err num: $j")
                // 等待shell子进程执行完成,返回0表示正常结束
                p.waitFor()
            }
            /*
            */
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            // 关闭流
            if (dos != null) {
                try {
                    dos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (err != null) {
                try {
                    err.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return list
    }
    init {
        appFileDirectory = ct.filesDir.path
        executableFilePath = "$appFileDirectory/executable"

        // cpuType = Build.SUPPORTED_ABIS[0];
        cpuType = Build.CPU_ABI
        Log.d(TAG, "CPU_ABI:$cpuType")
        assetManager = ct.assets
        try {
            resList = Arrays.asList(*ct.assets.list("$cpuType/"))
            Log.d(TAG, "get assets list:" + resList.toString())
        } catch (e: IOException) {
            Log.e(TAG, "Error list assets folder:", e)
        }
    }
}
