package com.example.emmc_kotlin

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_old_test.*

class OldTest : AppCompatActivity() ,View.OnClickListener{
    private val item1= arrayOf("单选1","单选2","单选3")
    private val Param1="1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_old_test)
    }
    override fun onClick(v:View?){
        when(v?.id){
            R.id.param1->{
                val builder=AlertDialog.Builder(this).setTitle("选择参数1：").setSingleChoiceItems(item1,0,
                    { dialog, which ->  Toast.makeText(this,"你选择了《"+item1[which]+"》",Toast.LENGTH_SHORT).show()})
                builder.apply {
                    setPositiveButton("确定"){dialog,which->}
                    setNegativeButton("取消"){dialog,which->}
                    show()
                }

            }
            R.id.startTest->{
                val intent=Intent(this,OldTestShow::class.java)

                intent.putExtra("param1",Param1)
                intent.putExtra("param2",param3.toString())
                startActivity(intent)
            }
        }
    }
}