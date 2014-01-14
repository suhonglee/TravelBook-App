package com.travelbook.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;

import com.facebook.android.R;

public class FacebookLoginButton extends Button{
	public FacebookLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs.getStyleAttribute() == 0) {
                // apparently there's no method of setting a default style in xml,
                // so in case the users do not explicitly specify a style, we need
                // to use sensible defaults.
                this.setTextColor(getResources().getColor(R.color.com_facebook_loginview_text_color));
                this.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.com_facebook_loginview_text_size));
                this.setPadding(getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_left),
                        getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_top),
                        getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_right),
                        getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_padding_bottom));
                this.setWidth(getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_width));
                this.setHeight(getResources().getDimensionPixelSize(R.dimen.com_facebook_loginview_height));
                this.setGravity(Gravity.CENTER);

            if(isInEditMode()) {
                // cannot use a drawable in edit mode, so setting the background color instead
                // of a background resource.
                this.setBackgroundColor(getResources().getColor(R.color.com_facebook_blue));
                // hardcoding in edit mode as getResources().getString() doesn't seem to work in IntelliJ
            } else {
                this.setBackgroundResource(com.travelbook.activities.R.drawable.btn_login_facebook);
            }
        }
    }
}
