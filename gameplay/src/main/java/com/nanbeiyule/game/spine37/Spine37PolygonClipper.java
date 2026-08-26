package com.nanbeiyule.game.spine37;

import java.util.ArrayList;
import java.util.List;

/** Clips textured Spine triangles against a recovered simple clipping attachment. */
final class Spine37PolygonClipper {
    record Result(float[] vertices, float[] uvs, short[] triangles) {}

    private static final float EPSILON = 0.0001f;

    private Spine37PolygonClipper() {}

    static Result clip(
            float[] vertices,
            float[] uvs,
            short[] triangles,
            float[] clipPolygon) {
        if (vertices.length != uvs.length || vertices.length % 2 != 0) {
            throw new IllegalArgumentException("Clipped vertices and UVs must contain pairs");
        }
        if (clipPolygon.length < 6 || clipPolygon.length % 2 != 0) {
            throw new IllegalArgumentException("Clip polygon must contain at least three points");
        }
        List<float[]> clippingPieces = convexPieces(clipPolygon);
        List<Float> clippedVertices = new ArrayList<>();
        List<Float> clippedUvs = new ArrayList<>();
        List<Short> clippedTriangles = new ArrayList<>();
        for (int triangle = 0; triangle + 2 < triangles.length; triangle += 3) {
            List<Vertex> sourceTriangle = new ArrayList<>(3);
            for (int corner = 0; corner < 3; corner++) {
                int vertexIndex = Short.toUnsignedInt(triangles[triangle + corner]);
                int offset = vertexIndex * 2;
                sourceTriangle.add(
                        new Vertex(
                                vertices[offset],
                                vertices[offset + 1],
                                uvs[offset],
                                uvs[offset + 1]));
            }
            for (float[] clippingPiece : clippingPieces) {
                List<Vertex> polygon = new ArrayList<>(sourceTriangle);
                float orientation = signedArea(clippingPiece) >= 0.0f ? 1.0f : -1.0f;
                for (int edge = 0; edge < clippingPiece.length; edge += 2) {
                    int next = (edge + 2) % clippingPiece.length;
                    polygon =
                            clipAgainstEdge(
                                    polygon,
                                    clippingPiece[edge],
                                    clippingPiece[edge + 1],
                                    clippingPiece[next],
                                    clippingPiece[next + 1],
                                    orientation);
                    if (polygon.isEmpty()) {
                        break;
                    }
                }
                for (int corner = 1; corner + 1 < polygon.size(); corner++) {
                    appendVertex(
                            polygon.get(0),
                            clippedVertices,
                            clippedUvs,
                            clippedTriangles);
                    appendVertex(
                            polygon.get(corner),
                            clippedVertices,
                            clippedUvs,
                            clippedTriangles);
                    appendVertex(
                            polygon.get(corner + 1),
                            clippedVertices,
                            clippedUvs,
                            clippedTriangles);
                }
            }
        }
        return new Result(
                floats(clippedVertices),
                floats(clippedUvs),
                shorts(clippedTriangles));
    }

    private static List<float[]> convexPieces(float[] polygon) {
        float orientation = signedArea(polygon) >= 0.0f ? 1.0f : -1.0f;
        if (isConvex(polygon, orientation)) {
            return List.of(polygon.clone());
        }
        int vertexCount = polygon.length / 2;
        List<Integer> remaining = new ArrayList<>(vertexCount);
        for (int index = 0; index < vertexCount; index++) {
            remaining.add(index);
        }
        List<float[]> triangles = new ArrayList<>(vertexCount - 2);
        while (remaining.size() > 3) {
            boolean earFound = false;
            for (int cursor = 0; cursor < remaining.size(); cursor++) {
                int previous = remaining.get((cursor + remaining.size() - 1) % remaining.size());
                int current = remaining.get(cursor);
                int next = remaining.get((cursor + 1) % remaining.size());
                if (cross(polygon, previous, current, next) * orientation <= EPSILON) {
                    continue;
                }
                boolean containsVertex = false;
                for (int candidate : remaining) {
                    if (candidate != previous && candidate != current && candidate != next
                            && insideTriangle(
                                    polygon,
                                    candidate,
                                    previous,
                                    current,
                                    next,
                                    orientation)) {
                        containsVertex = true;
                        break;
                    }
                }
                if (containsVertex) {
                    continue;
                }
                triangles.add(triangle(polygon, previous, current, next));
                remaining.remove(cursor);
                earFound = true;
                break;
            }
            if (!earFound) {
                throw new IllegalArgumentException(
                        "Clip polygon must be simple and contain no duplicate vertices");
            }
        }
        triangles.add(triangle(polygon, remaining.get(0), remaining.get(1), remaining.get(2)));
        return triangles;
    }

    private static boolean isConvex(float[] polygon, float orientation) {
        int vertexCount = polygon.length / 2;
        for (int index = 0; index < vertexCount; index++) {
            int previous = (index + vertexCount - 1) % vertexCount;
            int next = (index + 1) % vertexCount;
            if (cross(polygon, previous, index, next) * orientation < -EPSILON) {
                return false;
            }
        }
        return true;
    }

    private static boolean insideTriangle(
            float[] polygon,
            int point,
            int a,
            int b,
            int c,
            float orientation) {
        return edgeCross(polygon, a, b, point) * orientation >= -EPSILON
                && edgeCross(polygon, b, c, point) * orientation >= -EPSILON
                && edgeCross(polygon, c, a, point) * orientation >= -EPSILON;
    }

    private static float cross(float[] polygon, int a, int b, int c) {
        return edgeCross(polygon, a, b, c);
    }

    private static float edgeCross(float[] polygon, int a, int b, int point) {
        float ax = polygon[a * 2];
        float ay = polygon[a * 2 + 1];
        return (polygon[b * 2] - ax) * (polygon[point * 2 + 1] - ay)
                - (polygon[b * 2 + 1] - ay) * (polygon[point * 2] - ax);
    }

    private static float[] triangle(float[] polygon, int a, int b, int c) {
        return new float[] {
            polygon[a * 2], polygon[a * 2 + 1],
            polygon[b * 2], polygon[b * 2 + 1],
            polygon[c * 2], polygon[c * 2 + 1]
        };
    }

    private static List<Vertex> clipAgainstEdge(
            List<Vertex> input,
            float edgeStartX,
            float edgeStartY,
            float edgeEndX,
            float edgeEndY,
            float orientation) {
        if (input.isEmpty()) {
            return List.of();
        }
        List<Vertex> output = new ArrayList<>();
        Vertex previous = input.get(input.size() - 1);
        boolean previousInside =
                inside(
                        previous,
                        edgeStartX,
                        edgeStartY,
                        edgeEndX,
                        edgeEndY,
                        orientation);
        for (Vertex current : input) {
            boolean currentInside =
                    inside(
                            current,
                            edgeStartX,
                            edgeStartY,
                            edgeEndX,
                            edgeEndY,
                            orientation);
            if (currentInside != previousInside) {
                output.add(
                        intersection(
                                previous,
                                current,
                                edgeStartX,
                                edgeStartY,
                                edgeEndX,
                                edgeEndY));
            }
            if (currentInside) {
                output.add(current);
            }
            previous = current;
            previousInside = currentInside;
        }
        return output;
    }

    private static boolean inside(
            Vertex point,
            float edgeStartX,
            float edgeStartY,
            float edgeEndX,
            float edgeEndY,
            float orientation) {
        float cross =
                (edgeEndX - edgeStartX) * (point.y() - edgeStartY)
                        - (edgeEndY - edgeStartY) * (point.x() - edgeStartX);
        return cross * orientation >= -EPSILON;
    }

    private static Vertex intersection(
            Vertex start,
            Vertex end,
            float edgeStartX,
            float edgeStartY,
            float edgeEndX,
            float edgeEndY) {
        float segmentX = end.x() - start.x();
        float segmentY = end.y() - start.y();
        float edgeX = edgeEndX - edgeStartX;
        float edgeY = edgeEndY - edgeStartY;
        float denominator = edgeX * segmentY - edgeY * segmentX;
        if (Math.abs(denominator) <= EPSILON) {
            return start;
        }
        float numerator =
                -((edgeX * (start.y() - edgeStartY))
                        - (edgeY * (start.x() - edgeStartX)));
        float amount = Math.max(0.0f, Math.min(1.0f, numerator / denominator));
        return new Vertex(
                start.x() + segmentX * amount,
                start.y() + segmentY * amount,
                start.u() + (end.u() - start.u()) * amount,
                start.v() + (end.v() - start.v()) * amount);
    }

    private static void appendVertex(
            Vertex vertex,
            List<Float> vertices,
            List<Float> uvs,
            List<Short> triangles) {
        int index = vertices.size() / 2;
        if (index > 0xffff) {
            throw new IllegalArgumentException("Clipped mesh exceeds unsigned-short indices");
        }
        vertices.add(vertex.x());
        vertices.add(vertex.y());
        uvs.add(vertex.u());
        uvs.add(vertex.v());
        triangles.add((short) index);
    }

    private static float signedArea(float[] polygon) {
        float area = 0.0f;
        for (int index = 0; index < polygon.length; index += 2) {
            int next = (index + 2) % polygon.length;
            area +=
                    polygon[index] * polygon[next + 1]
                            - polygon[next] * polygon[index + 1];
        }
        return area * 0.5f;
    }

    private static float[] floats(List<Float> values) {
        float[] result = new float[values.size()];
        for (int index = 0; index < values.size(); index++) {
            result[index] = values.get(index);
        }
        return result;
    }

    private static short[] shorts(List<Short> values) {
        short[] result = new short[values.size()];
        for (int index = 0; index < values.size(); index++) {
            result[index] = values.get(index);
        }
        return result;
    }

    private record Vertex(float x, float y, float u, float v) {}
}
