package org.tasks.sync;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.todoroo.astrid.sync.SyncResultCallback;

import org.tasks.analytics.Tracker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

@Singleton
public class SyncExecutor {

    private final ExecutorService executor = newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("sync-executor-%d").build());

    private final Tracker tracker;

    @Inject
    public SyncExecutor(Tracker tracker) {
        this.tracker = tracker;
    }

    public void execute(SyncResultCallback callback, Runnable command) {
        try {
            executor.execute(wrapWithExceptionHandling(callback, command));
        } catch (RejectedExecutionException e) {
            Timber.e(e, e.getMessage());
            tracker.reportException(e);
            callback.finished();
        }
    }

    private Runnable wrapWithExceptionHandling(final SyncResultCallback callback, final Runnable command) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    command.run();
                } catch (Exception e) {
                    Timber.e(e, e.getMessage());
                    tracker.reportException(e);
                    executor.shutdownNow();
                    callback.finished();
                }
            }
        };
    }
}
