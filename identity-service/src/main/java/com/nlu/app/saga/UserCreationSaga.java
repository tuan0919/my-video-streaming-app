package com.nlu.app.saga;

import com.nlu.app.framework.Saga;

public class UserCreationSaga extends Saga {

    public static final String IDENTITY_CREATION = "userCreation";

    public static final String NOTIFICATION_CREATION = "notificationCreation";

    public static final String PROFILE_CREATION = "profileCreation";

    public static final String[] STATE_ORDER = {IDENTITY_CREATION, NOTIFICATION_CREATION, PROFILE_CREATION};

    public void init() {
        goNext();
    }

    @Override
    public void goNext() {}

    @Override
    public void goBack() {}
}
