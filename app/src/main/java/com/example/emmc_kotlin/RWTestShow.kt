package com.example.emmc_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import kotlinx.android.synthetic.main.activity_r_w_test.*
import kotlinx.android.synthetic.main.activity_r_w_test_show.*

class RWTestShow : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_r_w_test_show)
        val WriteSpeed=intent.getDoubleExtra("WriteSpeed",0.0)
        val ReadSpeed=intent.getDoubleExtra("ReadSpeed",0.0)
        val CorrectRate=intent.getDoubleExtra("CorrectRate",0.0)
        val aaChartModel = AAChartModel()
            .chartType(AAChartType.Column)
            .title("顺序读写比对测试")
            .subtitle("")
            .backgroundColor("#96CCF8")

            .series(arrayOf(
                AASeriesElement()
                    .name("写速率")
                    .data(arrayOf(WriteSpeed)),
                AASeriesElement()
                    .name("读速率")
                    .data(arrayOf(ReadSpeed))
            )
            )
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
    }
}