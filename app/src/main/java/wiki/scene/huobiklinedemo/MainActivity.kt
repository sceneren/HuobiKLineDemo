package wiki.scene.huobiklinedemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zhangke.websocket.SimpleListener
import com.zhangke.websocket.WebSocketHandler
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val wsHandler by lazy {
        WebSocketHandler.getDefault()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wsHandler.addListener(object : SimpleListener() {
            override fun <T : Any?> onMessage(message: String?, data: T) {
                super.onMessage(message, data)
                if (!message.isNullOrEmpty()) {

                }
            }
        })

        btnDemo1.setOnClickListener {
            startActivity(Intent(this@MainActivity, IkvStockChartDemo::class.java))
        }

        btnDemo2.setOnClickListener {
            startActivity(Intent(this@MainActivity, KChartViewDemo::class.java))
        }
    }
}