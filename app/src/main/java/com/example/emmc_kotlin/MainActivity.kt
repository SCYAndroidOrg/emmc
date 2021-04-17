package com.example.emmc_kotlin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.leon.lfilepickerlibrary.LFilePicker
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.lang.StringBuilder

object FileUtil {

    /**
     * 创建文件
     * @param filePath 文件路径(不要以/结尾)
     * @param fileName 文件名称（包含后缀,如：ReadMe.txt）
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createTxtFile(filePath: String, fileName: String): Boolean {
        var flag = false
        val filename = File("$filePath/$fileName")
        if (!filename.exists()) {
            filename.createNewFile()
            flag = true
        }
        return flag
    }

    /**
     * 写文件
     *
     * @param content 文件内容
     * @param filePath 文件路径(不要以/结尾)
     * @param fileName 文件名称（包含后缀,如：ReadMe.txt）
     * 新内容
     * @throws IOException
     */
    fun writeTxtFile(content: String, filePath: String, fileName: String, append: Boolean): Boolean {
        var flag: Boolean = true
        val thisFile = File("$filePath/$fileName")
        try {
            if (!thisFile.parentFile.exists()) {
                thisFile.parentFile.mkdirs()
            }
            val fw = FileWriter("$filePath/$fileName", append)
            fw.write(content)
            fw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return flag
    }
    fun createNum(length:Int):String{
        val builder=StringBuilder()
        for(i in 1..length){
            val n=(1..10).random()
            builder.append("$n")
        }
        return builder.toString()
    }

    /**
     * 读TXT文件内容
     * @param filePath 文件路径(不要以 / 结尾)
     * @param fileName 文件名称（包含后缀,如：ReadMe.txt）
     * @return
     */
    @Throws(Exception::class)
    fun readTxtFile(filePath: String, fileName: String): String? {
        var result: String? = ""
        val fileName = File("$filePath/$fileName")
        var fileReader: FileReader? = null
        var bufferedReader: BufferedReader? = null
        try {
            fileReader = FileReader(fileName)
            bufferedReader = BufferedReader(fileReader)
            try {
                var read: String? = null
                while ({ read = bufferedReader.readLine();read }() != null) {
                    result = result + read + "\r\n"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close()
            }
            if (fileReader != null) {
                fileReader.close()
            }
        }
        println("读取出来的文件内容是：\r\n$result")
        return result
    }

}
class MainActivity : AppCompatActivity() {
    private var buffer= ArrayList<String>()
    private var mmcblkpath = ""
    private var filepath = ""
    private var model:Int=1
    // 初始化
    private lateinit var ce: CopyElfs
    private var IsSuccess = false
    var REQUESTCODE_FROM_ACTIVITY :Int =1000
    override fun onCreate(savedInstanceState: Bundle?) {

        // 初始化
        val service = FileUtil
        val pathName = "data/data/com.example.emmc_kotlin"
        val fileName = "test.txt"
        val content = "我现在在上班" +
                "比较忙的时候别来打扰我"
        service.createTxtFile(pathName, fileName)
        service.writeTxtFile(content, pathName, fileName, false)
        val str=service.readTxtFile(pathName,fileName)
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show()

        ce = CopyElfs(baseContext)
        ce.copyAll2Data()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ce = CopyElfs(getBaseContext())
        fw_version
        help.setOnClickListener {
            var intent= Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }
        read.setOnClickListener {
            var intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }
        chose_bin.setOnClickListener {
            LFilePicker()
                    .withActivity(this@MainActivity)
                    .withRequestCode(REQUESTCODE_FROM_ACTIVITY)
                    .withStartPath("/sdcard/") //指定初始显示路径
                    .withIsGreater(false) //过滤文件大小 小于指定大小的文件
                    .withFileSize((500 * 1024).toLong()) //指定文件大小为500K
                    .withFileFilter(arrayOf(".bin"))
                    .withMutilyMode(false)
                    .start()
        }
    }
    fun myclick(v: View) {
        when (v.id) {
            R.id.chose_emmc -> EmmcOnclick()
            R.id.update -> UpdateOnclick()
            R.id.rwtest->{
                var intent=Intent(this,RWTest::class.java)
                ShowMessage(mmcblkpath)
                intent.putExtra("mmcblkpath",mmcblkpath)
                startActivity(intent)
            }
            R.id.oldtest->{
                var intent=Intent(this,OldTest::class.java)
                intent.putExtra("mmcblkpath",mmcblkpath)
                startActivity(intent)
            }
        }
        // startActivity(intent);
    }
    private fun EmmcOnclick() {
        // 获取 mmcblk 命令，初始化可读写的devices

        try {
           buffer = ce.callElf("return") as ArrayList<String>
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        // 显示对话框
        showEmmcDialog()
    }

    /*
    * 选择emmc对话框显示
    * */
    private fun showEmmcDialog() {
        val devices: Array<String?> = list_to_array()

        // 会话框设置
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("选择emmc块")

        // 有无获取到设备
        if (devices.size == 0) {
            dialog.setMessage("未找到emmc设备")
        } else dialog.setItems(devices) { dialog, which ->
            mmcblkpath = devices[which].toString()
            ShowMessage(mmcblkpath)
            ShowEmmcblkFw(mmcblkpath)
        }
        // show()方法显示对话框
        dialog.show()
    }

    /*
     * 点击 更新 事件：
     * 首先判断emmc和filepath路径是否为空，即是否有选择emmc和文件
     * 然后执行mmc.elf的ffu将输出流存储到List<String>类型的buffer中，
     * 将buffer转换为String，并检测输出中是否有Success来判断是否更新成功
     * */
    private fun UpdateOnclick() { //更新按钮
        Log.d("callelf", "!!!!!!!!执行到此")
        if (filepath.isEmpty() || mmcblkpath.isEmpty()) { //这个path要修改
            ShowMessage("文件或emmc芯片未选择")
            return
        }
       IsSuccess = false // 状态初始化
        try {
            buffer = ce.callElf("ffu $filepath $mmcblkpath") as ArrayList<String>
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        showUpdateDialog()
    }
    /*
    * 更新结果显示对话框
    * */
    private fun showUpdateDialog() {
        val dialog = AlertDialog.Builder(this)
        dialog.setIcon(R.drawable.update)
        dialog.setTitle("更新固件")
        val updateOutString = DealUpdateString()
        if (IsSuccess) {
            dialog.setMessage("更新成功！！！")
            ShowEmmcblkFw(mmcblkpath) // 刷新当前emmc的版本显示文本框
        } else {
            dialog.setMessage("更新失败！！！\n$updateOutString")
        }
        // 点击确定
        dialog.setPositiveButton("确定") { dialog, which ->
            // tv_fw.setText(null);
            // MainActivity.this.finish();
        }
        dialog.show()
    }

    /*
    * 在TextView中显示选择的mmcblk的版本号
    * 通过extcsd read mmcblk的输出流的fw version输出来截取版本号
    * */
    private fun ShowEmmcblkFw(mmcblk: String) { //读取版本信息//
        // 获取 mmcblk 版本号: fw version
        var fw = String()
        try {
            buffer = ce.callElf("extcsd read $mmcblkpath") as ArrayList<String>
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        for (i in buffer.indices) {
            if (buffer.get(i).contains("fw version"))
                fw = buffer.get(i).toString()
        }
        buffer.clear()//清空队列
        if (!fw.isEmpty()) {
            fw = "$mmcblkpath $fw"
            fw_version.setText(fw)
        } else {
            fw_version.setText("读取" + mmcblkpath + "版本号失败")
        }
    }

    private fun ShowMessage(str: String) {
        Toast.makeText(this@MainActivity, str, Toast.LENGTH_LONG).show()
    }

    /*
    * 将list类型转换为String[]
    * */
    //筛选符合的mmc文件
    private fun list_to_array(): Array<String?> {
        val list: MutableList<String> = java.util.ArrayList()
        for (i in buffer.indices) {
            if (buffer.get(i).startsWith("/dev/block")) list.add(buffer.get(i))
        }
        val array = arrayOfNulls<String>(list.size)
        for (i in list.indices) {
            array[i] = list[i]
        }
        buffer.clear()
        return array
    }

    /** 处理更新事件的输出流，并判断是否更新成功
    * 将mmc.elf执行ffu功能的输出流（已存入到buffer中），由list<String>转换到String类型
    * 并通过字符串string是否包含Success来判断是否更新成功
    * */
    private fun DealUpdateString(): String { //处理更新信息
        var str = String()
        for (i in buffer.indices) {
            str = buffer.get(i).toString() + "\n"
            if (str.contains("Success")) IsSuccess = true
        }
        buffer.clear()
        return str
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_FROM_ACTIVITY) {
                //If it is a file selection mode, you need to get the path collection of all the files selected
                //List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);//Constant.RESULT_INFO == "paths"
                val list: List<String>? = data?.getStringArrayListExtra("paths")
                Toast.makeText(applicationContext, "selected " + list!!.size, Toast.LENGTH_SHORT)
                    .show()
                //If it is a folder selection mode, you need to get the folder path of your choice
                val path = data?.getStringExtra("path")
                Toast.makeText(applicationContext, "The selected path is:$path", Toast.LENGTH_SHORT)
                    .show()
                bin_file.text = "$path"
            }
        }
    }

}