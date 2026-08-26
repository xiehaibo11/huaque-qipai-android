package com.nanbeiyule.game.spine37;

import java.util.List;

final class Spine37TimelineSampler {
    private Spine37TimelineSampler() {}

    static String attachmentAt(
            String setupAttachment,
            Spine37Data.SlotTimeline timeline,
            float time) {
        if (timeline == null || timeline.attachment().isEmpty()) {
            return setupAttachment;
        }
        String result = setupAttachment;
        for (Spine37Data.AttachmentFrame frame : timeline.attachment()) {
            if (frame.time() > time) {
                break;
            }
            result = frame.name();
        }
        return result;
    }

    static Spine37Data.ColorValue colorAt(
            Spine37Data.SlotTimeline timeline,
            float time) {
        if (timeline == null
                || timeline.color().isEmpty()
                || time < timeline.color().get(0).time()) {
            return Spine37Data.ColorValue.WHITE;
        }
        List<Spine37Data.ColorFrame> frames = timeline.color();
        int previousIndex = previousColorFrame(frames, time);
        Spine37Data.ColorFrame previous = frames.get(previousIndex);
        if (previousIndex == frames.size() - 1 || previous.stepped()) {
            return previous.color();
        }
        Spine37Data.ColorFrame next = frames.get(previousIndex + 1);
        float alpha =
                previous.curve()
                        .apply(
                                (time - previous.time())
                                        / (next.time() - previous.time()));
        return lerp(previous.color(), next.color(), alpha);
    }

    static float[] sampleDeform(
            List<Spine37Data.DeformFrame> frames,
            Spine37Data.Attachment attachment,
            float time) {
        if (frames == null
                || frames.isEmpty()
                || time < frames.get(0).time()
                || attachment.weighted()) {
            return new float[0];
        }
        int length = attachment.vertices().length;
        int previousIndex = previousDeformFrame(frames, time);
        Spine37Data.DeformFrame previous = frames.get(previousIndex);
        float[] previousValues = expanded(previous, length);
        if (previousIndex == frames.size() - 1 || previous.stepped()) {
            return previousValues;
        }
        Spine37Data.DeformFrame next = frames.get(previousIndex + 1);
        float[] nextValues = expanded(next, length);
        float alpha =
                previous.curve()
                        .apply(
                                (time - previous.time())
                                        / (next.time() - previous.time()));
        float[] result = new float[length];
        for (int index = 0; index < length; index++) {
            result[index] =
                    previousValues[index]
                            + (nextValues[index] - previousValues[index]) * alpha;
        }
        return result;
    }

    static float[] sampleDeform(
            List<Spine37Data.DeformFrame> frames,
            Spine37Data.ClippingAttachment attachment,
            float time) {
        if (frames == null
                || frames.isEmpty()
                || time < frames.get(0).time()
                || attachment.weighted()) {
            return new float[0];
        }
        int length = attachment.vertexCount() * 2;
        int previousIndex = previousDeformFrame(frames, time);
        Spine37Data.DeformFrame previous = frames.get(previousIndex);
        float[] previousValues = expanded(previous, length);
        if (previousIndex == frames.size() - 1 || previous.stepped()) {
            return previousValues;
        }
        Spine37Data.DeformFrame next = frames.get(previousIndex + 1);
        float[] nextValues = expanded(next, length);
        float alpha =
                previous.curve()
                        .apply(
                                (time - previous.time())
                                        / (next.time() - previous.time()));
        float[] result = new float[length];
        for (int index = 0; index < length; index++) {
            result[index] =
                    previousValues[index]
                            + (nextValues[index] - previousValues[index]) * alpha;
        }
        return result;
    }

    static float[] expanded(Spine37Data.DeformFrame frame, int length) {
        float[] result = new float[length];
        float[] values = frame.vertices();
        int copyLength = Math.min(values.length, Math.max(0, length - frame.offset()));
        if (copyLength > 0) {
            System.arraycopy(values, 0, result, frame.offset(), copyLength);
        }
        return result;
    }

    static int previousColorFrame(
            List<Spine37Data.ColorFrame> frames, float time) {
        int index = 0;
        while (index + 1 < frames.size() && frames.get(index + 1).time() <= time) {
            index++;
        }
        return index;
    }

    static int previousDeformFrame(
            List<Spine37Data.DeformFrame> frames, float time) {
        int index = 0;
        while (index + 1 < frames.size() && frames.get(index + 1).time() <= time) {
            index++;
        }
        return index;
    }

    static Spine37Data.ColorValue lerp(
            Spine37Data.ColorValue start,
            Spine37Data.ColorValue end,
            float alpha) {
        return new Spine37Data.ColorValue(
                start.red() + (end.red() - start.red()) * alpha,
                start.green() + (end.green() - start.green()) * alpha,
                start.blue() + (end.blue() - start.blue()) * alpha,
                start.alpha() + (end.alpha() - start.alpha()) * alpha);
    }

    static Spine37Data.ColorValue multiply(
            Spine37Data.ColorValue left,
            Spine37Data.ColorValue right) {
        return new Spine37Data.ColorValue(
                left.red() * right.red(),
                left.green() * right.green(),
                left.blue() * right.blue(),
                left.alpha() * right.alpha());
    }}
