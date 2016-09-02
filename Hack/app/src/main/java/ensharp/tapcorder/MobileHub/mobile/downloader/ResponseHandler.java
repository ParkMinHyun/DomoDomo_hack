package ensharp.tapcorder.MobileHub.mobile.downloader;

public interface ResponseHandler {
    void onSuccess(long downloadId);
    void onError(String errorMessage);
}
