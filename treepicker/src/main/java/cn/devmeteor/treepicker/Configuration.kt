package cn.devmeteor.treepicker

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat

data class Configuration(
    var tabItemPadding: Int,
    var tabHeight: Int,
    var tabTextSelectedColor: Int,
    var tabTextNormalColor: Int,
    var tabUnderline: Drawable,
    var tabTextSize: Int,
    var levelTextSize: Int,
    var levelPadding: Int,
    var levelCheckDrawable: Drawable,
    var levelTextNormalColor: Int,
    var levelTextSelectedColor: Int
) {
    class Builder(private val context: Context) {
        private var tabItemPadding: Int =
            context.resources.getDimension(R.dimen.padding_tab_text).toInt()
        private var tabHeight: Int = context.resources.getDimension(R.dimen.tab_height).toInt()
        private var tabTextSelectedColor: Int = Color.RED
        private var tabTextNormalColor: Int = Color.BLACK
        private var tabUnderline: Drawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.shape_underline_selected,
            null
        )!!
        private var tabTextSize: Int = context.resources.getDimension(R.dimen.tab_text_size).toInt()
        private var levelTextSize: Int =
            context.resources.getDimension(R.dimen.level_text_size).toInt()
        private var levelPadding: Int =
            context.resources.getDimension(R.dimen.padding_level_text).toInt()
        private var levelCheckDrawable: Drawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.checked,
            null
        )!!
        private var levelTextNormalColor: Int = Color.BLACK
        private var levelTextSelectedColor: Int = Color.RED

        fun tabItemPadding(tabItemPadding: Int): Builder {
            this.tabItemPadding = tabItemPadding
            return this
        }

        fun tabHeight(tabHeight: Int): Builder {
            this.tabHeight = tabHeight
            return this
        }

        fun tabTextSelectedColor(tabTextSelectedColor: Int): Builder {
            this.tabTextSelectedColor = tabTextSelectedColor
            return this
        }

        fun tabTextNormalColor(tabTextNormalColor: Int): Builder {
            this.tabTextNormalColor = tabTextNormalColor
            return this
        }

        fun tabTextSize(tabTextSize: Int): Builder {
            this.tabTextSize = tabTextSize
            return this
        }

        fun tabUnderline(tabUnderline: Drawable): Builder {
            this.tabUnderline = tabUnderline
            return this
        }

        fun tabUnderline(id: Int): Builder {
            tabUnderline(ResourcesCompat.getDrawable(context.resources, id, null)!!)
            return this
        }

        fun levelTextSize(levelTextSize: Int): Builder {
            this.levelTextSize = levelTextSize
            return this
        }

        fun levelPadding(levelPadding: Int): Builder {
            this.levelPadding = levelPadding
            return this
        }

        fun levelCheckDrawable(levelCheckDrawable: Drawable): Builder {
            this.levelCheckDrawable = levelCheckDrawable
            return this
        }

        fun levelCheckDrawable(id: Int): Builder {
            levelCheckDrawable(ResourcesCompat.getDrawable(context.resources, id, null)!!)
            return this
        }

        fun levelTextNormalColor(levelTextNormalColor: Int): Builder {
            this.levelTextNormalColor = levelTextNormalColor
            return this
        }

        fun levelTextSelectedColor(levelTextSelectedColor: Int): Builder {
            this.levelTextSelectedColor = levelTextSelectedColor
            return this
        }

        fun build(): Configuration =
            Configuration(
                tabItemPadding,
                tabHeight,
                tabTextSelectedColor,
                tabTextNormalColor,
                tabUnderline,
                tabTextSize,
                levelTextSize,
                levelPadding,
                levelCheckDrawable,
                levelTextNormalColor,
                levelTextSelectedColor
            )

    }
}