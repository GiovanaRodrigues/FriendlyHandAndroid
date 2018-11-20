package mack.sp.friendlyhand.tasks;

/**
 * Created by Giovana Rodrigues on 16/05/2018.
 */

public interface HandlerTask {
    void onPreHandle();

    void onSuccess(String valueRead);

    void onError(Exception erro);
}
