package com.sprinter.keychain.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;

public interface OnDialogButtonClickListener {

    void onDialogButtonClick(final int requestCode, final int buttonId, @Nullable Bundle baggage);

}
