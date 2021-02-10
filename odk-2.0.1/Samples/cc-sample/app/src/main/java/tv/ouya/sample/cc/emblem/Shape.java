package tv.ouya.sample.cc.emblem;

import android.graphics.Path;

public enum Shape {

    SQUARE("Square",
            -1f, -1f,
            new Line(1f, -1f),
            new Line(1f, 1f),
            new Line(-1f, 1f),
            new Line(-1f, -1f)
        ),
    TRIANGLE1("Triangle 1",
            -1f, -1f,
            new Line(1f, 1f),
            new Line(-1f, 1f),
            new Line(-1f, -1f)
        ),
    TRIANGLE2("Triangle 2",
            0f, -1f,
            new Line(1f, 1f),
            new Line(-1f, 1f),
            new Line(0f, -1f)
        ),
    HEXAGON("Hexagon",
            1, 0,
            new Polygon(6)
        ),
    CIRCLE("Circle",
            1, 0,
            new Polygon(64)
            )
    ;

    public String title;
    public Path path;

    private Shape(String title, float startX, float startY, Feature... features) {
        this.title = title;
        path = new Path();
        path.moveTo(startX, startY);
        path.setLastPoint(startX, startY);
        for(Feature f : features) {
            f.addTo(path);
        }
        path.close();
    }

    private abstract static class Feature {
        public abstract void addTo(Path path);
    }

    private static class Line extends Feature {
        private float x, y;
        public Line(float toX, float toY) {
            x = toX;
            y = toY;
        }

        @Override
        public void addTo(Path path) {
            path.lineTo(x, y);
        }
    }

    private static class Polygon extends Feature {
        private int sides;
        public Polygon(int sides) {
            this.sides = sides;
        }

        @Override
        public void addTo(Path path) {
            final float ANGLE_PER = (float) (2*Math.PI/sides);
            // we assume the first point is <1,0>, and that the radius is 1
            for(int i = 1; i < sides; i++) {
                final float angle = i*ANGLE_PER;
                path.lineTo((float)Math.cos(angle), (float)Math.sin(angle));
            }
        }
    }
}
