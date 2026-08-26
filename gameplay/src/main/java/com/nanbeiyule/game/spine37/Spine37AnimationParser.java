package com.nanbeiyule.game.spine37;

import static com.nanbeiyule.game.spine37.Spine37JsonValues.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class Spine37AnimationParser {
    private Spine37AnimationParser() {}

    static Map<String, Spine37Data.Animation> parseAnimations(JSONObject body)
            throws JSONException {
        Map<String, Spine37Data.Animation> result = new LinkedHashMap<>();
        Iterator<String> names = body.keys();
        while (names.hasNext()) {
            String name = names.next();
            JSONObject animation = body.getJSONObject(name);
            Map<String, Spine37Data.BoneTimeline> bones =
                    parseBoneTimelines(animation.optJSONObject("bones"));
            Map<String, Spine37Data.SlotTimeline> slots =
                    parseSlotTimelines(animation.optJSONObject("slots"));
            Map<Spine37Data.DeformKey, List<Spine37Data.DeformFrame>> deform =
                    parseDeform(animation.optJSONObject("deform"));
            List<Spine37Data.EventFrame> events =
                    parseEvents(animation.optJSONArray("events"));
            float duration = duration(bones, slots, deform, events);
            result.put(
                    name,
                    new Spine37Data.Animation(
                            name, duration, bones, slots, deform, events));
        }
        return result;
    }

    private static Map<String, Spine37Data.BoneTimeline> parseBoneTimelines(JSONObject body)
            throws JSONException {
        Map<String, Spine37Data.BoneTimeline> result = new LinkedHashMap<>();
        if (body == null) {
            return result;
        }
        Iterator<String> names = body.keys();
        while (names.hasNext()) {
            String name = names.next();
            JSONObject timelines = body.getJSONObject(name);
            result.put(
                    name,
                    new Spine37Data.BoneTimeline(
                            numericFrames(timelines.optJSONArray("rotate"), "angle", null),
                            numericFrames(timelines.optJSONArray("translate"), "x", "y"),
                            numericFrames(timelines.optJSONArray("scale"), "x", "y"),
                            numericFrames(timelines.optJSONArray("shear"), "x", "y")));
        }
        return result;
    }

    private static List<Spine37Data.EventFrame> parseEvents(JSONArray body)
            throws JSONException {
        List<Spine37Data.EventFrame> result = new ArrayList<>();
        if (body == null) {
            return result;
        }
        for (int index = 0; index < body.length(); index++) {
            JSONObject frame = body.getJSONObject(index);
            result.add(
                    new Spine37Data.EventFrame(
                            number(frame, "time", 0.0f),
                            frame.getString("name")));
        }
        return result;
    }

    private static List<Spine37Data.NumericFrame> numericFrames(
            JSONArray body, String xName, String yName)
            throws JSONException {
        List<Spine37Data.NumericFrame> result = new ArrayList<>();
        if (body == null) {
            return result;
        }
        for (int index = 0; index < body.length(); index++) {
            JSONObject frame = body.getJSONObject(index);
            result.add(
                    new Spine37Data.NumericFrame(
                            number(frame, "time", 0.0f),
                            number(frame, xName, defaultFor(xName)),
                            yName == null
                                    ? 0.0f
                                    : number(frame, yName, defaultFor(yName)),
                            curve(frame)));
        }
        return result;
    }

    private static float defaultFor(String property) {
        return "x".equals(property) || "y".equals(property) ? 0.0f : 0.0f;
    }

    private static Map<String, Spine37Data.SlotTimeline> parseSlotTimelines(JSONObject body)
            throws JSONException {
        Map<String, Spine37Data.SlotTimeline> result = new LinkedHashMap<>();
        if (body == null) {
            return result;
        }
        Iterator<String> names = body.keys();
        while (names.hasNext()) {
            String name = names.next();
            JSONObject timelines = body.getJSONObject(name);
            List<Spine37Data.ColorFrame> colors = new ArrayList<>();
            JSONArray colorBody = timelines.optJSONArray("color");
            if (colorBody != null) {
                for (int index = 0; index < colorBody.length(); index++) {
                    JSONObject frame = colorBody.getJSONObject(index);
                    colors.add(
                            new Spine37Data.ColorFrame(
                                    number(frame, "time", 0.0f),
                                    parseColor(frame.getString("color")),
                                    curve(frame)));
                }
            }
            List<Spine37Data.AttachmentFrame> attachments = new ArrayList<>();
            JSONArray attachmentBody = timelines.optJSONArray("attachment");
            if (attachmentBody != null) {
                for (int index = 0; index < attachmentBody.length(); index++) {
                    JSONObject frame = attachmentBody.getJSONObject(index);
                    attachments.add(
                            new Spine37Data.AttachmentFrame(
                                    number(frame, "time", 0.0f),
                                    frame.has("name") && !frame.isNull("name")
                                            ? frame.getString("name")
                                            : null));
                }
            }
            result.put(name, new Spine37Data.SlotTimeline(colors, attachments));
        }
        return result;
    }

    private static Map<Spine37Data.DeformKey, List<Spine37Data.DeformFrame>> parseDeform(
            JSONObject body)
            throws JSONException {
        Map<Spine37Data.DeformKey, List<Spine37Data.DeformFrame>> result =
                new LinkedHashMap<>();
        if (body == null) {
            return result;
        }
        Iterator<String> skins = body.keys();
        while (skins.hasNext()) {
            JSONObject skin = body.getJSONObject(skins.next());
            Iterator<String> slots = skin.keys();
            while (slots.hasNext()) {
                String slotName = slots.next();
                JSONObject slot = skin.getJSONObject(slotName);
                Iterator<String> attachments = slot.keys();
                while (attachments.hasNext()) {
                    String attachmentName = attachments.next();
                    JSONArray frames = slot.getJSONArray(attachmentName);
                    List<Spine37Data.DeformFrame> parsed = new ArrayList<>();
                    for (int index = 0; index < frames.length(); index++) {
                        JSONObject frame = frames.getJSONObject(index);
                        parsed.add(
                                new Spine37Data.DeformFrame(
                                        number(frame, "time", 0.0f),
                                        frame.optInt("offset", 0),
                                        frame.has("vertices")
                                                ? floats(frame.getJSONArray("vertices"))
                                                : new float[0],
                                        curve(frame)));
                    }
                    result.put(
                            new Spine37Data.DeformKey(slotName, attachmentName),
                            List.copyOf(parsed));
                }
            }
        }
        return result;
    }

    private static float duration(
            Map<String, Spine37Data.BoneTimeline> bones,
            Map<String, Spine37Data.SlotTimeline> slots,
            Map<Spine37Data.DeformKey, List<Spine37Data.DeformFrame>> deform,
            List<Spine37Data.EventFrame> events) {
        float maximum = 0.0f;
        for (Spine37Data.BoneTimeline timeline : bones.values()) {
            maximum = Math.max(maximum, lastNumeric(timeline.rotate()));
            maximum = Math.max(maximum, lastNumeric(timeline.translate()));
            maximum = Math.max(maximum, lastNumeric(timeline.scale()));
            maximum = Math.max(maximum, lastNumeric(timeline.shear()));
        }
        for (Spine37Data.SlotTimeline timeline : slots.values()) {
            if (!timeline.color().isEmpty()) {
                maximum =
                        Math.max(
                                maximum,
                                timeline.color().get(timeline.color().size() - 1).time());
            }
            if (!timeline.attachment().isEmpty()) {
                maximum =
                        Math.max(
                                maximum,
                                timeline.attachment()
                                        .get(timeline.attachment().size() - 1)
                                        .time());
            }
        }
        for (List<Spine37Data.DeformFrame> frames : deform.values()) {
            if (!frames.isEmpty()) {
                maximum = Math.max(maximum, frames.get(frames.size() - 1).time());
            }
        }
        if (!events.isEmpty()) {
            maximum = Math.max(maximum, events.get(events.size() - 1).time());
        }
        return maximum;
    }

    private static float lastNumeric(List<Spine37Data.NumericFrame> frames) {
        return frames.isEmpty() ? 0.0f : frames.get(frames.size() - 1).time();
    }

}
