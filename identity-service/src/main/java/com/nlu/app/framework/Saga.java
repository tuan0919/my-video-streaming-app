package com.nlu.app.framework;

public abstract class Saga {
    /**
     * FIXME: Kiem tra xem event da duoc thuc hien hay chua?
     */
    protected void ensureProcessed(String eventId, Runnable callback) {
        callback.run();
    }

    public enum PayloadType {
        REQUEST, CANCEL
    }

    public abstract void goNext();
    public abstract void goBack();
}
