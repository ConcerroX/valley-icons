package concerrox.valley.data.source

import android.annotation.SuppressLint
import android.content.Context

class AndroidIconDrawableProvider(private val context: Context) : IconDrawableProvider {

    @SuppressLint("DiscouragedApi")
    override fun getDrawable(name: String): Any {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

}