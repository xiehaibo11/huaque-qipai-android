package com.nanbeiyule.game;

/** Aggregates track-sized timeline data generated from recovered evidence. */
final class LoginRequestLoadingTimelineData {
    private LoginRequestLoadingTimelineData() {}

    static LoginRequestLoadingTimeline.Track[] tracks() {
        return new LoginRequestLoadingTimeline.Track[] {
            LoginRequestLoadingTimelineTrackBei2.track(),
            LoginRequestLoadingTimelineTrackXi2.track(),
            LoginRequestLoadingTimelineTrackNan2.track(),
            LoginRequestLoadingTimelineTrackDong.track(),
            LoginRequestLoadingTimelineTrackNan.track(),
            LoginRequestLoadingTimelineTrackXi.track(),
            LoginRequestLoadingTimelineTrackBei.track()
        };
    }
}
