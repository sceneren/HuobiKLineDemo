package wiki.scene.huobiklinedemo

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.JsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.github.fujianlian.klinechart.DataHelper
import com.github.fujianlian.klinechart.KLineChartAdapter
import com.github.fujianlian.klinechart.KLineEntity
import com.github.fujianlian.klinechart.draw.Status
import com.github.fujianlian.klinechart.formatter.DateFormatter
import com.zhangke.websocket.WebSocketHandler
import kotlinx.android.synthetic.main.act_ikv_stock_chart.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import wiki.scene.huobiklinedemo.socket.entity.SubKLineInfo
import wiki.scene.huobiklinedemo.socket.event.MessageEvent
import wiki.scene.huobiklinedemo.socket.request.ReqInfo
import wiki.scene.huobiklinedemo.socket.request.SubInfo
import wiki.scene.huobiklinedemo.socket.request.UnsubInfo
import wiki.scene.huobiklinedemo.socket.response.BaseSocketResponseList
import java.util.*


class IkvStockChartDemo : AppCompatActivity() {

    private val datas: MutableList<KLineEntity> = mutableListOf()

    private val adapter by lazy { KLineChartAdapter() }

    private val subTexts: ArrayList<TextView> by lazy {
        arrayListOf(
            macdText,
            kdjText,
            rsiText,
            wrText
        )
    }

    // 主图指标下标
    private var mainIndex = 0

    // 副图指标下标
    private var subIndex = -1

    private val wsHandler by lazy {
        WebSocketHandler.getDefault()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_ikv_stock_chart)

        initView()
        initListener()

        kLineChartView.justShowLoading()
        getAllData()

    }

    private fun initView() {
        kLineChartView.adapter = adapter
        kLineChartView.dateTimeFormatter = DateFormatter()
        kLineChartView.setGridRows(4)
        kLineChartView.setGridColumns(4)
    }

    private fun initListener() {
        kLineChartView.setRefreshListener {
            kLineChartView.postDelayed({
                kLineChartView.refreshEnd()
            },1000)
        }

        maText.setOnClickListener {
            if (mainIndex != 0) {
                kLineChartView.hideSelectData()
                mainIndex = 0
                maText.setTextColor(Color.parseColor("#eeb350"))
                bollText.setTextColor(Color.WHITE)
                kLineChartView.changeMainDrawType(Status.MA)
            }
        }
        bollText.setOnClickListener {
            if (mainIndex != 1) {
                kLineChartView.hideSelectData()
                mainIndex = 1
                bollText.setTextColor(Color.parseColor("#eeb350"))
                maText.setTextColor(Color.WHITE)
                kLineChartView.changeMainDrawType(Status.BOLL)
            }
        }
        mainHide.setOnClickListener {
            if (mainIndex != -1) {
                kLineChartView.hideSelectData()
                mainIndex = -1
                bollText.setTextColor(Color.WHITE)
                maText.setTextColor(Color.WHITE)
                kLineChartView.changeMainDrawType(Status.NONE)
            }
        }
        for ((index, text) in subTexts.withIndex()) {
            text.setOnClickListener {
                if (subIndex != index) {
                    kLineChartView.hideSelectData()
                    if (subIndex != -1) {
                        subTexts[subIndex].setTextColor(Color.WHITE)
                    }
                    subIndex = index
                    text.setTextColor(Color.parseColor("#eeb350"))
                    kLineChartView.setChildDraw(subIndex)
                }
            }
        }
        subHide.setOnClickListener {
            if (subIndex != -1) {
                kLineChartView.hideSelectData()
                subTexts[subIndex].setTextColor(Color.WHITE)
                subIndex = -1
                kLineChartView.hideChildDraw()
            }
        }
        fenText.setOnClickListener {
            kLineChartView.hideSelectData()
            fenText.setTextColor(Color.parseColor("#eeb350"))
            kText.setTextColor(Color.WHITE)
            kLineChartView.setMainDrawLine(true)
        }
        kText.setOnClickListener {
            kLineChartView.hideSelectData()
            kText.setTextColor(Color.parseColor("#eeb350"))
            fenText.setTextColor(Color.WHITE)
            kLineChartView.setMainDrawLine(false)
        }
    }

    private fun getAllData() {
        val reqInfo = ReqInfo("market.btcusdt.kline.1min", "allData")
        wsHandler.send(GsonUtils.toJson(reqInfo))
    }

    private fun getSubData() {
        val subInfo = SubInfo("market.btcusdt.kline.1min", "subData")
        wsHandler.send(GsonUtils.toJson(subInfo))
    }

    private fun unSubData() {
        val subInfo = UnsubInfo("market.btcusdt.kline.1min", "unsubData")
        wsHandler.send(GsonUtils.toJson(subInfo))
    }

    @Subscribe
    fun onMessageEvent(event: MessageEvent?) {
        event?.let {
            if (JsonUtils.getString(event.msg, "ch") == "market.btcusdt.kline.1min") {
                val subKLineInfo =
                    GsonUtils.fromJson(event.msg, SubKLineInfo::class.java)

                subKLineInfo.tick.let { kLineInfo1 ->
                    val kLineEntity = KLineEntity()
                    kLineEntity.Low = kLineInfo1.low
                    kLineEntity.High = kLineInfo1.high
                    kLineEntity.Open = kLineInfo1.open
                    kLineEntity.Close = kLineInfo1.close
                    kLineEntity.Volume = kLineInfo1.vol
                    kLineEntity.Date = TimeUtils.millis2String(kLineInfo1.id * 1000L, "HH:mm")

                    runOnUiThread {
                        LogUtils.e("==>【${kLineEntity.Date}】【${datas.last().date}】")
                        if (datas.last().date == kLineEntity.Date) {
                            datas[datas.size - 1] = kLineEntity
                            DataHelper.calculate(datas)
                            adapter.changeItem(adapter.count - 1, kLineEntity)
                        } else {
                            datas.add(kLineEntity)
                            DataHelper.calculate(datas)
                            adapter.addHeaderData(datas)
                            adapter.notifyDataSetChanged()
                        }

                    }

                }
            } else if (JsonUtils.getString(event.msg, "id") == "allData") {
                datas.clear()
                val baseInfo = GsonUtils.fromJson(event.msg, BaseSocketResponseList::class.java)
                baseInfo.data.forEach { kLineInfo ->
                    LogUtils.e(kLineInfo.toString())
                    val kLineEntity = KLineEntity()
                    kLineEntity.Low = kLineInfo.low
                    kLineEntity.High = kLineInfo.high
                    kLineEntity.Open = kLineInfo.open
                    kLineEntity.Close = kLineInfo.close
                    kLineEntity.Volume = kLineInfo.vol
                    kLineEntity.Date = TimeUtils.millis2String(kLineInfo.id * 1000L, "HH:mm")

                    datas.add(kLineEntity)
                }
                DataHelper.calculate(datas)
                runOnUiThread {
                    adapter.addFooterData(datas)
                    adapter.notifyDataSetChanged()
                    kLineChartView.refreshComplete()
                }
                unSubData()
                getSubData()
            }

        }
    }


    override fun onDestroy() {
        unSubData()
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}