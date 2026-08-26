package com.nanbeiyule.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class GameRecordApiCompatibilityTest {
    @Test
    public void requestEncodingReachesCallbackOnAndroid32() throws Exception {
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>("");
        GameRecordApiClient client = new GameRecordApiClient("http://127.0.0.1:1");
        try {
            client.load(
                    "test-token",
                    "2026-08-24",
                    0L,
                    false,
                    new GameRecordApiClient.Callback() {
                        @Override
                        public void onSuccess(GameRecordPage page) {
                            result.set("success");
                            finished.countDown();
                        }

                        @Override
                        public void onUnauthorized() {
                            result.set("unauthorized");
                            finished.countDown();
                        }

                        @Override
                        public void onError(String message) {
                            result.set("error");
                            finished.countDown();
                        }
                    });

            assertTrue("战绩请求必须回调且不得让进程崩溃", finished.await(5, TimeUnit.SECONDS));
            assertEquals("error", result.get());
        } finally {
            client.shutdown();
        }
    }
}
