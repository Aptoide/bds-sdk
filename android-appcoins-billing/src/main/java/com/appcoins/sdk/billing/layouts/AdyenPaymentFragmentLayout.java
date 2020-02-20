package com.appcoins.sdk.billing.layouts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.appcoins.sdk.billing.listeners.CardNumberTextWatcher;
import com.appcoins.sdk.billing.listeners.CvvTextWatcher;
import com.appcoins.sdk.billing.listeners.ExpiryDateTextWatcher;
import com.appcoins.sdk.billing.utils.PaymentErrorViewLayout;
import com.sdk.appcoins_adyen.utils.CardValidationUtils;
import java.io.IOException;
import java.io.InputStream;

import static com.appcoins.sdk.billing.utils.LayoutUtils.IMAGES_RESOURCE_PATH;
import static com.appcoins.sdk.billing.utils.LayoutUtils.dpToPx;
import static com.appcoins.sdk.billing.utils.LayoutUtils.generateRandomId;
import static com.appcoins.sdk.billing.utils.LayoutUtils.mapDisplayMetrics;
import static com.appcoins.sdk.billing.utils.LayoutUtils.setConstraint;
import static com.appcoins.sdk.billing.utils.LayoutUtils.setMargins;
import static com.appcoins.sdk.billing.utils.LayoutUtils.setPadding;

public class AdyenPaymentFragmentLayout {
  private static int CREDIT_CARD_HEADER_ID = 34;
  private static int CREDIT_CARD_VIEW_ID = 33;
  private static int HEADER_ID = 32;
  private static int APPC_PRICE_VIEW_ID = 31;
  private static int FIAT_PRICE_VIEW_ID = 30;
  private static int APP_NAME_ID = 29;
  private static int APP_ICON_ID = 28;
  private static int PAYMENT_METHODS_HEADER_ID = 27;
  private final Activity activity;
  private final int orientation;
  private String densityPath;
  private RelativeLayout errorView;
  private RelativeLayout dialogLayout;
  private ProgressBar progressBar;
  private TextView fiatPriceView;
  private TextView appcPriceView;
  private RelativeLayout creditCardLayout;
  private Button cancelButton;
  private Button positiveButton;
  private LinearLayout buttonsView;

  public AdyenPaymentFragmentLayout(Activity activity, int orientation) {
    this.activity = activity;
    this.orientation = orientation;
  }

  public View build(String fiatPrice, String fiatCurrency, String appcPrice, String sku,
      String packageName) {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    activity.getWindowManager()
        .getDefaultDisplay()
        .getMetrics(displayMetrics);
    densityPath = mapDisplayMetrics(displayMetrics);

    RelativeLayout mainLayout = buildMainLayout();
    PaymentErrorViewLayout paymentErrorViewLayout =
        new PaymentErrorViewLayout(activity, orientation);
    errorView = paymentErrorViewLayout.buildErrorView();
    errorView.setVisibility(View.INVISIBLE);

    dialogLayout = buildDialogLayout(fiatPrice, fiatCurrency, appcPrice, sku, packageName);

    mainLayout.addView(dialogLayout);
    mainLayout.addView(errorView);
    return mainLayout;
  }

  private RelativeLayout buildDialogLayout(String fiatPrice, String fiatCurrency, String appcPrice,
      String sku, String packageName) {
    RelativeLayout dialogLayout = new RelativeLayout(activity);
    dialogLayout.setClipToPadding(false);

    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setColor(Color.WHITE);
    gradientDrawable.setCornerRadius(dpToPx(8));
    dialogLayout.setBackground(gradientDrawable);

    int width;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      width = dpToPx(340);
    } else {
      width = dpToPx(544);
    }
    RelativeLayout.LayoutParams dialogLayoutParams =
        new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    dialogLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
    dialogLayout.setLayoutParams(dialogLayoutParams);

    progressBar = buildProgressBar();
    progressBar.setVisibility(View.INVISIBLE);

    RelativeLayout paymentMethodsHeaderLayout =
        buildPaymentMethodsHeaderLayout(packageName, sku, fiatPrice, fiatCurrency, appcPrice);
    View headerSeparator = buildHeaderSeparatorLayout();
    creditCardLayout = buildCreditCardLayout();
    //creditCardLayout.setVisibility(View.INVISIBLE);
    buttonsView = buildButtonsView();
    buttonsView.setVisibility(View.INVISIBLE);

    dialogLayout.addView(progressBar);
    dialogLayout.addView(paymentMethodsHeaderLayout);
    dialogLayout.addView(headerSeparator);
    dialogLayout.addView(creditCardLayout);
    dialogLayout.addView(buttonsView);
    return dialogLayout;
  }

  private LinearLayout buildButtonsView() {
    LinearLayout linearLayout = new LinearLayout(activity);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.BELOW, CREDIT_CARD_VIEW_ID);

    int end, top, bottom;

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      end = 12;
      top = 24;
      bottom = 24;
    } else {
      top = 24;
      end = 22;
      bottom = 16;
    }

    setMargins(layoutParams, 0, top, end, bottom);
    linearLayout.setGravity(Gravity.END);
    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    linearLayout.setClipChildren(false);
    linearLayout.setClipToPadding(false);
    linearLayout.setLayoutParams(layoutParams);

    cancelButton = buildCancelButtonLayout();
    positiveButton = buildPositiveButtonLayout();
    positiveButton.setEnabled(false);

    linearLayout.addView(cancelButton);
    linearLayout.addView(positiveButton);
    return linearLayout;
  }

  private Button buildPositiveButtonLayout() {
    Button button = new Button(activity);
    LinearLayout.LayoutParams layoutParams =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(36));
    layoutParams.gravity = Gravity.CENTER_VERTICAL;
    int[] gradientColors = { Color.parseColor("#FC9D48"), Color.parseColor("#FF578C") };
    GradientDrawable enableBackground =
        new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
    enableBackground.setShape(GradientDrawable.RECTANGLE);
    enableBackground.setStroke(dpToPx(1), Color.WHITE);
    enableBackground.setCornerRadius(dpToPx(16));
    button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    button.setBackground(enableBackground);

    GradientDrawable disableBackground = new GradientDrawable();
    disableBackground.setShape(GradientDrawable.RECTANGLE);
    disableBackground.setStroke(dpToPx(1), Color.WHITE);
    disableBackground.setCornerRadius(dpToPx(16));
    disableBackground.setColor(Color.parseColor("#c9c9c9"));

    button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

    StateListDrawable stateListDrawable = new StateListDrawable();
    stateListDrawable.addState(new int[] { android.R.attr.state_enabled }, enableBackground);
    stateListDrawable.addState(new int[] { -android.R.attr.state_enabled }, disableBackground);

    button.setBackground(stateListDrawable);

    button.setMaxWidth(dpToPx(142));
    button.setMinWidth(dpToPx(96));

    setPadding(button, 0, 0, 4, 0);
    button.setTextColor(Color.WHITE);
    button.setTextSize(14);
    button.setText("NEXT".toUpperCase());
    button.setLayoutParams(layoutParams);
    return button;
  }

  private Button buildCancelButtonLayout() {
    Button button = new Button(activity);
    LinearLayout.LayoutParams layoutParams =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(36));
    layoutParams.gravity = Gravity.CENTER_VERTICAL;
    GradientDrawable background = new GradientDrawable();
    background.setShape(GradientDrawable.RECTANGLE);
    background.setStroke(dpToPx(1), Color.WHITE);
    background.setCornerRadius(dpToPx(6));
    button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    button.setBackground(background);
    button.setMaxWidth(dpToPx(126));
    button.setMinWidth(dpToPx(80));

    setPadding(button, 0, 0, 4, 0);
    button.setTextColor(Color.parseColor("#8a000000"));
    button.setTextSize(14);
    button.setText("Cancel".toUpperCase());
    button.setLayoutParams(layoutParams);
    return button;
  }

  private RelativeLayout buildCreditCardLayout() {
    RelativeLayout parentLayout = new RelativeLayout(activity);
    CREDIT_CARD_VIEW_ID = generateRandomId(CREDIT_CARD_VIEW_ID);
    parentLayout.setId(CREDIT_CARD_VIEW_ID);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.BELOW, HEADER_ID);
    parentLayout.setLayoutParams(layoutParams);

    RelativeLayout creditCardHeader = buildCreditCardHeader();
    LinearLayout creditCardEditTextLayout = buildCreditCardEditTextLayout();

    parentLayout.addView(creditCardHeader);
    parentLayout.addView(creditCardEditTextLayout);

    return parentLayout;
  }

  @SuppressLint("InlinedApi") private LinearLayout buildCreditCardEditTextLayout() {
    LinearLayout linearLayout = new LinearLayout(activity);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(44));
    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
    layoutParams.addRule(RelativeLayout.BELOW, CREDIT_CARD_HEADER_ID);
    setConstraint(layoutParams, RelativeLayout.ALIGN_START, CREDIT_CARD_HEADER_ID);
    setConstraint(layoutParams, RelativeLayout.ALIGN_END, CREDIT_CARD_HEADER_ID);
    setMargins(layoutParams, 0, 20, 0, 0);

    GradientDrawable background = new GradientDrawable();
    background.setShape(GradientDrawable.RECTANGLE);
    background.setStroke(dpToPx(1), Color.parseColor("#fd7a6a"));
    background.setCornerRadius(dpToPx(6));
    linearLayout.setBackground(background);

    ImageView genericCardView = buildGenericCardView();
    EditText cardNumberEditText = buildCardNumberEditText();
    EditText expiryDateEditText = buildExpiryDateEditText();
    EditText cvvEditText = buildCvvEditText();

    cardNumberEditText.addTextChangedListener(
        new CardNumberTextWatcher(cardNumberEditText, expiryDateEditText));
    expiryDateEditText.addTextChangedListener(
        new ExpiryDateTextWatcher(expiryDateEditText, cvvEditText, cardNumberEditText));
    cvvEditText.addTextChangedListener(new CvvTextWatcher(expiryDateEditText));
    linearLayout.addView(genericCardView);
    linearLayout.addView(cardNumberEditText);
    linearLayout.addView(expiryDateEditText);
    linearLayout.addView(cvvEditText);

    linearLayout.setLayoutParams(layoutParams);
    return linearLayout;
  }

  private ImageView buildGenericCardView() {
    ImageView imageView = new ImageView(activity);
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dpToPx(30), dpToPx(19));
    layoutParams.gravity = Gravity.CENTER_VERTICAL;
    Drawable genericCreditCard = convertAssetDrawable(
        IMAGES_RESOURCE_PATH + "generic_card/" + densityPath + "generic_card.png");
    setMargins(layoutParams, 10, 0, 10, 0);
    imageView.setImageDrawable(genericCreditCard);
    imageView.setLayoutParams(layoutParams);
    return imageView;
  }

  private EditText buildCvvEditText() {
    EditText editText = new EditText(activity);
    LinearLayout.LayoutParams layoutParams =
        new LinearLayout.LayoutParams(dpToPx(40), ViewGroup.LayoutParams.MATCH_PARENT);
    editText.setFilters(new InputFilter[] {
        new InputFilter.LengthFilter(CardValidationUtils.CVV_MAX_LENGTH)
    });
    layoutParams.gravity = Gravity.CENTER_VERTICAL;
    editText.setHint("CVV");
    editText.setHintTextColor(Color.parseColor("#9d9d9d"));
    editText.setLayoutParams(layoutParams);
    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    editText.setBackgroundColor(Color.parseColor("#00000000"));
    editText.setTextSize(14);
    editText.setTextColor(Color.parseColor("#292929"));
    editText.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

    return editText;
  }

  private EditText buildExpiryDateEditText() {
    EditText editText = new EditText(activity);
    LinearLayout.LayoutParams layoutParams =
        new LinearLayout.LayoutParams(dpToPx(60), ViewGroup.LayoutParams.MATCH_PARENT);
    editText.setFilters(new InputFilter[] {
        new InputFilter.LengthFilter(CardValidationUtils.DATE_MAX_LENGTH)
    });
    editText.setHint("MM/YY");
    editText.setHintTextColor(Color.parseColor("#9d9d9d"));
    layoutParams.gravity = Gravity.CENTER_VERTICAL;
    editText.setLayoutParams(layoutParams);
    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    editText.setBackgroundColor(Color.parseColor("#00000000"));
    editText.setTextSize(14);
    editText.setTextColor(Color.parseColor("#292929"));
    editText.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

    return editText;
  }

  private EditText buildCardNumberEditText() {
    EditText editText = new EditText(activity);
    editText.setFilters(new InputFilter[] {
        new InputFilter.LengthFilter(CardValidationUtils.MAXIMUM_CARD_NUMBER_LENGTH
            + CardValidationUtils.MAX_DIGIT_SEPARATOR_COUNT)
    });
    LinearLayout.LayoutParams layoutParams =
        new LinearLayout.LayoutParams(dpToPx(150), ViewGroup.LayoutParams.MATCH_PARENT);

    layoutParams.gravity = Gravity.CENTER_VERTICAL;

    editText.setTextSize(14);
    editText.setTextColor(Color.parseColor("#292929"));
    editText.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
    editText.setHint("Card Number");
    editText.setHintTextColor(Color.parseColor("#9d9d9d"));
    editText.setLayoutParams(layoutParams);
    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
    editText.setBackgroundColor(Color.parseColor("#00000000"));

    return editText;
  }

  private RelativeLayout buildCreditCardHeader() {
    RelativeLayout relativeLayout = new RelativeLayout(activity);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    CREDIT_CARD_HEADER_ID = generateRandomId(CREDIT_CARD_HEADER_ID);
    relativeLayout.setId(CREDIT_CARD_HEADER_ID);
    layoutParams.addRule(RelativeLayout.BELOW, HEADER_ID);

    int top, start, end;

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      top = 8;
      start = 14;
      end = 18;
    } else {
      top = 14;
      start = 20;
      end = 226;
    }
    setMargins(layoutParams, start, top, end, 0);
    relativeLayout.setLayoutParams(layoutParams);

    TextView payAsGuestText = buildPayAsGuestText();
    ImageView creditCardImage = buildCreditCardImage();

    relativeLayout.addView(payAsGuestText);
    relativeLayout.addView(creditCardImage);

    return relativeLayout;
  }

  @SuppressLint("InlinedApi") private ImageView buildCreditCardImage() {
    ImageView imageView = new ImageView(activity);

    int height;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      height = 10;
    } else {
      height = 12;
    }

    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(dpToPx(56), dpToPx(height));
    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
    setConstraint(layoutParams, RelativeLayout.ALIGN_PARENT_END);

    Drawable creditCard = convertAssetDrawable(
        IMAGES_RESOURCE_PATH + "credit_card/landscape/" + densityPath + "ic_credit_card.png");
    imageView.setImageDrawable(creditCard);
    imageView.setLayoutParams(layoutParams);
    return imageView;
  }

  private TextView buildPayAsGuestText() {
    TextView textView = new TextView(activity);

    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

    textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    textView.setTextColor(Color.parseColor("#000000"));
    textView.setTextSize(14);
    textView.setText("Pay as Guest");
    textView.setLayoutParams(layoutParams);
    return textView;
  }

  private View buildHeaderSeparatorLayout() {
    View view = new View(activity);
    HEADER_ID = generateRandomId(HEADER_ID);
    view.setId(HEADER_ID);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1));
    int start, top, end;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      start = 16;
      top = 20;
      end = 16;
    } else {
      start = 20;
      top = 14;
      end = 20;
    }
    setMargins(layoutParams, start, top, end, 0);
    view.setBackgroundColor(Color.parseColor("#eaeaea"));
    layoutParams.addRule(RelativeLayout.BELOW, PAYMENT_METHODS_HEADER_ID);
    view.setLayoutParams(layoutParams);
    return view;
  }

  private RelativeLayout buildPaymentMethodsHeaderLayout(String packageName, String sku,
      String fiatPrice, String fiatCurrency, String appcPrice) {
    RelativeLayout paymentMethodHeaderLayout = new RelativeLayout(activity);
    paymentMethodHeaderLayout.setLayoutParams(
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT));
    PAYMENT_METHODS_HEADER_ID = generateRandomId(PAYMENT_METHODS_HEADER_ID);
    paymentMethodHeaderLayout.setId(PAYMENT_METHODS_HEADER_ID);
    Drawable icon = null;
    String appName = "";
    PackageManager packageManager = activity.getApplicationContext()
        .getPackageManager();
    try {
      icon = packageManager.getApplicationIcon(packageName);
      ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
      appName = packageManager.getApplicationLabel(applicationInfo)
          .toString();
    } catch (Exception e) {
      e.printStackTrace();
    }
    ImageView iconImageView = createAppIconLayout(icon);
    TextView appNameView = createAppNameLayout(appName);
    TextView skuView = createSkuLayout(sku);
    fiatPriceView = createFiatPriceView(fiatPrice, fiatCurrency);
    appcPriceView = createAppcPriceView(appcPrice);

    paymentMethodHeaderLayout.addView(iconImageView);
    paymentMethodHeaderLayout.addView(appNameView);
    paymentMethodHeaderLayout.addView(skuView);
    paymentMethodHeaderLayout.addView(fiatPriceView);
    paymentMethodHeaderLayout.addView(appcPriceView);
    return paymentMethodHeaderLayout;
  }

  @SuppressLint("InlinedApi") private TextView createAppcPriceView(String appcPrice) {
    TextView textView = new TextView(activity);
    APPC_PRICE_VIEW_ID = generateRandomId(APPC_PRICE_VIEW_ID);
    textView.setId(APPC_PRICE_VIEW_ID);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.BELOW, FIAT_PRICE_VIEW_ID);
    setConstraint(layoutParams, RelativeLayout.ALIGN_PARENT_END);
    setMargins(layoutParams, 0, 0, 16, 0);
    textView.setTextColor(Color.parseColor("#828282"));
    textView.setText(String.format("%s APPC", appcPrice));
    textView.setTextSize(12);
    textView.setLayoutParams(layoutParams);
    return textView;
  }

  @SuppressLint("InlinedApi")
  private TextView createFiatPriceView(String fiatPrice, String fiatCurrency) {
    TextView textView = new TextView(activity);
    FIAT_PRICE_VIEW_ID = generateRandomId(FIAT_PRICE_VIEW_ID);
    textView.setId(FIAT_PRICE_VIEW_ID);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    setConstraint(layoutParams, RelativeLayout.ALIGN_PARENT_END);
    setMargins(layoutParams, 0, 17, 16, 0);
    textView.setTextColor(Color.parseColor("#000000"));
    textView.setTextSize(15);
    textView.setText(String.format("%s %s", fiatPrice, fiatCurrency));
    textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    textView.setLayoutParams(layoutParams);
    return textView;
  }

  @SuppressLint("InlinedApi") private TextView createSkuLayout(String sku) {
    TextView textView = new TextView(activity);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.BELOW, APP_NAME_ID);
    setConstraint(layoutParams, RelativeLayout.START_OF, APPC_PRICE_VIEW_ID);
    setConstraint(layoutParams, RelativeLayout.END_OF, APP_ICON_ID);
    setMargins(layoutParams, 10, 0, 12, 0);
    textView.setEllipsize(TextUtils.TruncateAt.END);
    textView.setLines(1);
    textView.setTextColor(Color.parseColor("#8a000000"));
    textView.setTextSize(12);
    textView.setText(sku);
    textView.setLayoutParams(layoutParams);
    return textView;
  }

  @SuppressLint("InlinedApi") private TextView createAppNameLayout(String appName) {
    TextView textView = new TextView(activity);
    APP_NAME_ID = generateRandomId(APP_NAME_ID);
    textView.setId(APP_NAME_ID);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    setConstraint(layoutParams, RelativeLayout.START_OF, FIAT_PRICE_VIEW_ID);
    setConstraint(layoutParams, RelativeLayout.END_OF, APP_ICON_ID);
    setMargins(layoutParams, 10, 15, 12, 0);
    textView.setEllipsize(TextUtils.TruncateAt.END);
    textView.setLines(1);
    textView.setTextColor(Color.parseColor("#de000000"));
    textView.setTextSize(16);
    textView.setText(appName);
    textView.setLayoutParams(layoutParams);
    return textView;
  }

  private ImageView createAppIconLayout(Drawable icon) {
    ImageView imageView = new ImageView(activity);
    APP_ICON_ID = generateRandomId(APP_ICON_ID);
    imageView.setId(APP_ICON_ID);
    if (icon != null) {
      imageView.setImageDrawable(icon);
    }
    RelativeLayout.LayoutParams imageParams =
        new RelativeLayout.LayoutParams(dpToPx(48), dpToPx(48));

    int start;
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
      start = 12;
    } else {
      start = 20;
    }

    setMargins(imageParams, start, 12, 0, 0);
    imageView.setLayoutParams(imageParams);
    return imageView;
  }

  private Drawable convertAssetDrawable(String path) {
    InputStream inputStream = null;
    try {
      inputStream = activity.getResources()
          .getAssets()
          .open(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Drawable.createFromStream(inputStream, null);
  }

  private ProgressBar buildProgressBar() {
    ProgressBar progressBar = new ProgressBar(activity);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
    progressBar.setLayoutParams(layoutParams);
    return progressBar;
  }

  private RelativeLayout buildMainLayout() {
    int backgroundColor = Color.parseColor("#64000000");
    RelativeLayout backgroundLayout = new RelativeLayout(activity);
    backgroundLayout.setBackgroundColor(backgroundColor);
    return backgroundLayout;
  }

  public RelativeLayout getErrorView() {
    return errorView;
  }

  public RelativeLayout getDialogLayout() {
    return dialogLayout;
  }

  public ProgressBar getProgressBar() {
    return progressBar;
  }

  public TextView getFiatPriceView() {
    return fiatPriceView;
  }

  public TextView getAppcPriceView() {
    return appcPriceView;
  }

  public RelativeLayout getCreditCardLayout() {
    return creditCardLayout;
  }

  public Button getCancelButton() {
    return cancelButton;
  }

  public Button getPositiveButton() {
    return positiveButton;
  }

  public LinearLayout getButtonsView() {
    return buttonsView;
  }
}