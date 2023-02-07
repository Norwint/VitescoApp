package com.otcengineering.vitesco.view.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.otcengineering.vitesco.R;
import com.otcengineering.vitesco.service.BluetoothService;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by cenci7
 */

public class TitleBar extends FrameLayout {

    public interface TitleBarListener {
        void onLeftClick();

        void onRight1Click();

        void onRight2Click();
    }

    private FrameLayout btnLeft;
    private ImageView imgLeft;
    private TextView txtTitle;
    private FrameLayout btnRight1;
    private ImageView imgRight1;
    private FrameLayout btnRight2;
    private ImageView imgRight2;
    private View viewShadow;
    private FrameLayout notification;
    private TextView notificationCount;

    private Context context;
    private TitleBarListener listener;
    private String title;
    private boolean showLeftButton;
    private boolean showRight1Button;
    private boolean showRight2Button;
    private int leftButtonImage;
    private int right1ButtonImage;
    private int right2ButtonImage;
    private boolean showShadow;

    public TitleBar(@NonNull Context context) {
        super(context);
    }

    public TitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        inflateView(context);
        retrieveAttributes(attrs);
        configureUI();
        setConectionDongle();

//        OtcBle.getInstance().setContext(context);
//        OtcBle.getInstance().createBleLibrary();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void setConectionDongle() {
        Timer mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int color = BluetoothService.Companion.getService(context).isConnected() ? R.color.black : R.color.error;
                imgRight1.setColorFilter(ContextCompat.getColor(context, color));
            }
        }, 0, 200);
    }

    private void inflateView(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_titlebar, this, true);
        retrieveViews(view);
        setEvents();
    }

    private void retrieveViews(View view) {
        btnLeft = view.findViewById(R.id.titlebar_btnLeft);
        imgLeft = view.findViewById(R.id.titlebar_imgLeft);
        txtTitle = view.findViewById(R.id.titlebar_txtTitle);
        btnRight1 = view.findViewById(R.id.titlebar_btnRight1);
        imgRight1 = view.findViewById(R.id.titlebar_imgRight1);
        btnRight2 = view.findViewById(R.id.titlebar_btnRight2);
        imgRight2 = view.findViewById(R.id.titlebar_imgRight2);
        viewShadow = view.findViewById(R.id.titlebar_viewShadow);
        notification = view.findViewById(R.id.titlebar_notifiation);
        notificationCount = view.findViewById(R.id.titlebar_notifiationCount);
    }

    public void setNotification(int count) {
        if (count == 0) {
            notification.setVisibility(GONE);
        } else {
            notification.setVisibility(VISIBLE);
            if (count >= 1000) {
                notificationCount.setText(String.format(Locale.US, "+%d", 999));
            } else {
                notificationCount.setText(String.format(Locale.US, "%d", count));
            }
        }
    }

    private void setEvents() {
        btnLeft.setOnClickListener(view -> {
            if (listener != null) {
                listener.onLeftClick();
            }
        });

        btnRight1.setOnClickListener(view -> {
            if (listener != null) {
                listener.onRight1Click();
            }
        });

        btnRight2.setOnClickListener(view -> {
            if (listener != null) {
                listener.onRight2Click();
            }
        });
    }

    private void retrieveAttributes(AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.TitleBar, 0, 0);
        title = attributes.getString(R.styleable.TitleBar_titlebar_title);
        showLeftButton = attributes.getBoolean(R.styleable.TitleBar_titlebar_show_left_button, true);
        showRight1Button = attributes.getBoolean(R.styleable.TitleBar_titlebar_show_right1_button, true);
        showRight2Button = attributes.getBoolean(R.styleable.TitleBar_titlebar_show_right2_button, false);
        leftButtonImage = attributes.getResourceId(R.styleable.TitleBar_titlebar_left_button, R.drawable.menu_icons_10);
        right1ButtonImage = attributes.getResourceId(R.styleable.TitleBar_titlebar_right1_button, R.drawable.my_drive_icons_16);
        right2ButtonImage = attributes.getResourceId(R.styleable.TitleBar_titlebar_right2_button, R.drawable.my_drive_icons_6);
        showShadow = attributes.getBoolean(R.styleable.TitleBar_titlebar_show_shadow, false);
        attributes.recycle();
    }

    private void configureUI() {
        setTitle();
        manageLeftButton();
        manageRight1Button();
        manageRight2Button();
        manageShadow();
    }

    private void setTitle() {
        if (title == null || title.isEmpty()) {
            txtTitle.setVisibility(View.GONE);
        } else {
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(title);
        }
    }

    private void manageLeftButton() {
        if (showLeftButton) {
            btnLeft.setVisibility(VISIBLE);
            imgLeft.setImageResource(leftButtonImage);
        } else {
            btnLeft.setVisibility(GONE);
        }
    }

    private void manageRight1Button() {
        if (showRight1Button) {
            btnRight1.setVisibility(VISIBLE);
            imgRight1.setImageResource(right1ButtonImage);
        } else {
            btnRight1.setVisibility(GONE);
        }
    }

    private void manageRight2Button() {
        if (showRight2Button) {
            btnRight2.setVisibility(VISIBLE);
            imgRight2.setImageResource(right2ButtonImage);
        } else {
            btnRight2.setVisibility(GONE);
        }
    }

    private void manageShadow() {
        if (showShadow) {
            viewShadow.setVisibility(VISIBLE);
        } else {
            viewShadow.setVisibility(GONE);
        }
    }

    public void setListener(TitleBarListener listener) {
        this.listener = listener;
    }

    public void setTitle(String title) {
        this.title = title;
        txtTitle.setText(title);
    }

    public void setTitle(int titleRes) {
        this.title = context.getString(titleRes);
        txtTitle.setText(title);
    }

    public String getTitle() {
        return title;
    }

    public void hideImgRight2() {
        showRight2Button = false;
        manageRight2Button();
    }

    public void showImgRight2() {
        showRight2Button = true;
        manageRight2Button();
    }

    public void setRight1ButtonImage(int imgRes) {
        showRight1Button = true;
        this.right1ButtonImage = imgRes;
        manageRight1Button();
    }

    public void setLeftButtonImage(int imgRes) {
        showLeftButton = true;
        this.leftButtonImage = imgRes;
        manageLeftButton();
    }
}
