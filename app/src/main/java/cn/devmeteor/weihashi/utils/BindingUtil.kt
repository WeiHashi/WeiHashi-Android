package cn.devmeteor.weihashi.utils

import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter

object BindingUtil {
    @BindingAdapter("goneUnless")
    @JvmStatic
    fun goneUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @BindingAdapter("editable")
    @JvmStatic
    fun editable(editText: EditText, editable: Boolean) {
        editText.isEnabled = editable
    }

    @BindingAdapter("setScore")
    @JvmStatic
    fun setScore(tv: TextView, score: String) {
        val textColor = try {
            val scoreValue = score.toFloat()
            Color.parseColor(
                when {
                    scoreValue == 0F -> "#ababab"
                    scoreValue < 60 -> "#ff0000"
                    scoreValue < 90 -> "#009245"
                    else -> "#fbb03b"
                }
            )
        } catch (e: Exception) {
            Color.parseColor(
                when (score) {
                    "优" -> "#fbb03b"
                    "良" -> "#009245"
                    else -> "#ababab"
                }
            )
        }
        tv.setTextColor(textColor)
        tv.text = score
    }

    @BindingAdapter("setBg")
    @JvmStatic
    fun setBg(tv: TextView, bg: String) {
        if (bg.isBlank()) return
        tv.setBackgroundColor(Color.parseColor(bg))
    }

}