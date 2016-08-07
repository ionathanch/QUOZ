package nonphatic.quoz.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import nonphatic.quoz.R;

/**
 * Created by Jonathan on 2016-08-07.
 */
public class AboutDialogPreference extends DialogPreference {
    public AboutDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AboutDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AboutDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AboutDialogPreference(Context context) {
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNegativeButton(null, null);
        builder.setPositiveButton(null, null);
        builder.setTitle(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        ImageView nonphaticLogo = (ImageView)view.findViewById(R.id.nonphatic_logo);
        nonphaticLogo.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                //intent.setData(Uri.parse("market://search?q=pub:nonphatic"));
                intent.setData(Uri.parse("https://github.com/nonphatic"));
                getContext().startActivity(intent);
                return true;
            }
        });
    }
}
