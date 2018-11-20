package mack.sp.friendlyhand.tasks;

/**
 * Created by Giovana Rodrigues on 16/05/2018.
 */

public abstract class HandlerTaskAdapter implements HandlerTask {
    @Override
    public void onPreHandle() {
    }

    @Override
    public void onSuccess(String valueRead) {
    }

    @Override
    public void onError(Exception erro) {
    }
}
