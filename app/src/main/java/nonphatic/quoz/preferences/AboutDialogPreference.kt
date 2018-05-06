package nonphatic.quoz.preferences

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

import nonphatic.quoz.R

/**
 * Created by Jonathan on 2016-08-07.
 */
class AboutDialogPreference : DialogPreference {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)
        builder.setNegativeButton(null, null)
        builder.setPositiveButton(null, null)
        builder.setTitle(null)
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        val nonphaticLogo = view.findViewById<ImageView>(R.id.nonphatic_logo)
        nonphaticLogo.setOnLongClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/nonphatic")
            context.startActivity(intent)
            true
        }
    }
}
