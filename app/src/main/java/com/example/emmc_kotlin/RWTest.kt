package com.example.emmc_kotlin

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import kotlinx.android.synthetic.main.activity_old_test.*
import kotlinx.android.synthetic.main.activity_r_w_test.*

class RWTest : AppCompatActivity() ,View.OnClickListener{
    /*读写测试的act*/
    //测试模式
    private val TestModelArray= arrayOf("random","serial")
    //测试次数
    private val TestFrequencyArray= arrayOf("1","2","3")
    private var TestModel:String=""
    private var TestFrequency:Int=0
    private var WriteSpeed=0.0
    private var ReadSpeed=0.0
    private var CorrectRate=0.0
    private var Offset=0
    private var Length=0
    private var buffer= ArrayList<String>()
    private lateinit var ce: CopyElfs
    private var mmcblkpath:String=""
    private var IsSuccess=false
    private var Size:String=""
/*    private var model:Int=1
    private var filepath:String=""*/

    override fun onCreate(savedInstanceState: Bundle?) {
        ce = CopyElfs(baseContext)
        ce.copyAll2Data()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_r_w_test)
        mmcblkpath= intent.getStringExtra("mmcblkpath").toString()
        testinf.setText(mmcblkpath)
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?){
        when(v?.id){
            R.id.param1_rw->{
                val builder=
                    AlertDialog.Builder(this).setTitle("选择测试模式：").setSingleChoiceItems(TestModelArray,3
                    ) { _, which ->
                        run {

                            val item=TestModelArray[which]
                            TestModel=item
                            Toast.makeText(this, "你选择了" + TestModel + " 模式", Toast.LENGTH_SHORT).show()
                            ModelText.text = "你选择了 $TestModel 测试模式"
                        }
                    }
                builder.apply {
                    setPositiveButton("确定"){ _, _ ->}
                    setNegativeButton("取消"){ _, _ ->}
                    show()
                }

            }
            //选择测试次数
            R.id.param2_rw->{
                val builder=
                    AlertDialog.Builder(this).setTitle("选择测试的次数：").setSingleChoiceItems(TestFrequencyArray,5
                    ) { _, which -> run{
                        val item=TestFrequencyArray[which]
                        TestFrequency=item.toInt()
                        Toast.makeText(this, "你选择了" + TestFrequency + " 次测试次数", Toast.LENGTH_SHORT).show()
                        FrequencyText.setText("你选择了" + TestFrequency + " 次测试次数")
                    } }
                builder.apply {
                    setPositiveButton("确定"){ _, _ ->}
                    setNegativeButton("取消"){ _, _ ->}
                    show()
                }

            }
            R.id.startTest->{
                startRWTest()
                /*val intent= Intent(this,RWTestShow::class.java)
                intent.putExtra("WriteSpeed",WriteSpeed)
                intent.putExtra("ReadSpeed",ReadSpeed)
                intent.putExtra("CorrectRate",CorrectRate)

                startActivity(intent)*/
                //testinf.setText("WriteSpeed is $WriteSpeed\nReadSpeed is $ReadSpeed\nCorrectRate is $CorrectRate\noffset is$Offset\nlength is $Length\n$Size")
                testinf.setText(buffer[0]+"---\n")
                testinf.setText(buffer[1]+"---\n")
                testinf.setText(buffer[2]+"---\n")
                testinf.setText(buffer[3]+"---\n")
            }
        }
    }
    private fun startRWTest() {
        if (mmcblkpath.isEmpty()) { //这个path要修改
            Toast.makeText(this,"路径为空，不能测试",Toast.LENGTH_SHORT).show()
            return
        }
        IsSuccess = false // 状态初始化
        try {
            val test="do_test $mmcblkpath $TestModel $TestFrequency"
            Toast.makeText(this,test,Toast.LENGTH_SHORT).show()
            buffer = ce.callElf("do_test /dev/block/mmcblk1 random 1") as ArrayList<String>
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        for(i in buffer.indices){
            val item=buffer[i]
            Toast.makeText(this,item,Toast.LENGTH_SHORT).show()

            when(item.take(1)){
                "t"->{
                    Size=item
                }
                "o"->{
                    Offset=item.drop(8).toInt()//offset: num bytes
                }
                "l"->{
                    Length=item.drop(8).dropLast(6).toInt()//length: num bytes
                }
                "w"->{
                    WriteSpeed= (item.drop(9)).dropLast(4).toDouble()//writespeed: num bytes
                }
                "r"->{
                    ReadSpeed=(item.drop(8)).dropLast(4).toDouble()
                }
                "C"->{
                    CorrectRate=(item.drop(14)).toDouble()
                }
            }
        }
}}