package net.swofty.anticheat.math;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleUnaryOperator;

@AllArgsConstructor
public final class Pos implements Point {
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public Pos() {
        yaw = fixYaw(yaw);
    }

    public Pos(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    /**
     * Changes the 3 coordinates of this position.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return a new position
     */
    @Contract(pure = true)
    public @NotNull Pos withCoord(double x, double y, double z) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public @NotNull Pos withView(float yaw, float pitch) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public @NotNull Pos withView(@NotNull Pos pos) {
        return withView(pos.yaw(), pos.pitch());
    }

    public static @NotNull Pos fromPoint(@NotNull Point point) {
        if (point instanceof Pos pos) return pos;
        return new Pos(point.x(), point.y(), point.z());
    }

    /**
     * Sets the yaw and pitch to point
     * in the direction of the point.
     */
    @Contract(pure = true)
    public @NotNull Pos withDirection(@NotNull Pos point) {
        /*
         * Sin = Opp / Hyp
         * Cos = Adj / Hyp
         * Tan = Opp / Adj
         *
         * x = -Opp
         * z = Adj
         */
        final double x = point.x();
        final double z = point.z();
        if (x == 0 && z == 0) {
            return withPitch(point.y() > 0 ? -90f : 90f);
        }
        final double theta = Math.atan2(-x, z);
        final double xz = Math.sqrt(MathUtils.square(x) + MathUtils.square(z));
        final double _2PI = 2 * Math.PI;
        return withView((float) Math.toDegrees((theta + _2PI) % _2PI),
                (float) Math.toDegrees(Math.atan(-point.y() / xz)));
    }

    @Contract(pure = true)
    public @NotNull Pos withYaw(float yaw) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public @NotNull Pos withYaw(@NotNull DoubleUnaryOperator operator) {
        return withYaw((float) operator.applyAsDouble(yaw));
    }

    @Contract(pure = true)
    public @NotNull Pos withPitch(float pitch) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Contract(pure = true)
    public @NotNull Pos withLookAt(@NotNull Pos point) {
        final Vel delta = Vel.fromPoint(point.sub(this)).normalize();
        return withView(PositionUtils.getLookYaw(delta.x(), delta.z()),
                PositionUtils.getLookPitch(delta.x(), delta.y(), delta.z()));
    }

    @Contract(pure = true)
    public @NotNull Pos withPitch(@NotNull DoubleUnaryOperator operator) {
        return withPitch((float) operator.applyAsDouble(pitch));
    }

    /**
     * Checks if two positions have a similar view (yaw/pitch).
     *
     * @param position the position to compare
     * @return true if the two positions have the same view
     */
    public boolean sameView(@NotNull Pos position) {
        return sameView(position.yaw(), position.pitch());
    }

    public boolean sameView(float yaw, float pitch) {
        return Float.compare(this.yaw, yaw) == 0 &&
                Float.compare(this.pitch, pitch) == 0;
    }

    /**
     * Gets a unit-Vel()tor pointing in the direction that this Location is
     * facing.
     *
     * @return a Vel()tor pointing the direction of this location's {@link
     * #pitch() pitch} and {@link #yaw() yaw}
     */
    public @NotNull Vel direction() {
        final float rotX = yaw;
        final float rotY = pitch;
        final double xz = Math.cos(Math.toRadians(rotY));
        return new Vel(-xz * Math.sin(Math.toRadians(rotX)),
                -Math.sin(Math.toRadians(rotY)),
                xz * Math.cos(Math.toRadians(rotX)));
    }

    /**
     * Returns a new position based on this position fields.
     *
     * @param operator the operator deconstructing this object and providing a new position
     * @return the new position
     */
    @Contract(pure = true)
    public @NotNull Pos apply(@NotNull Operator operator) {
        return operator.apply(x, y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withX(@NotNull DoubleUnaryOperator operator) {
        return new Pos(operator.applyAsDouble(x), y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withX(double x) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withY(@NotNull DoubleUnaryOperator operator) {
        return new Pos(x, operator.applyAsDouble(y), z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withY(double y) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withZ(@NotNull DoubleUnaryOperator operator) {
        return new Pos(x, y, operator.applyAsDouble(z), yaw, pitch);
    }

    @Override
    @Contract(pure = true)
    public @NotNull Pos withZ(double z) {
        return new Pos(x, y, z, yaw, pitch);
    }

    @Override
    public @NotNull Pos add(double x, double y, double z) {
        return new Pos(this.x + x, this.y + y, this.z + z, yaw, pitch);
    }

    @Override
    public @NotNull Pos add(@NotNull Point point) {
        return add(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Pos add(double value) {
        return add(value, value, value);
    }

    @Override
    public @NotNull Pos sub(double x, double y, double z) {
        return new Pos(this.x - x, this.y - y, this.z - z, yaw, pitch);
    }

    @Override
    public @NotNull Pos sub(@NotNull Point point) {
        return sub(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Pos sub(double value) {
        return sub(value, value, value);
    }

    @Override
    public @NotNull Pos mul(double x, double y, double z) {
        return new Pos(this.x * x, this.y * y, this.z * z, yaw, pitch);
    }

    @Override
    public @NotNull Pos mul(@NotNull Point point) {
        return mul(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Pos mul(double value) {
        return mul(value, value, value);
    }

    @Override
    public @NotNull Pos div(double x, double y, double z) {
        return new Pos(this.x / x, this.y / y, this.z / z, yaw, pitch);
    }

    @Override
    public @NotNull Pos div(@NotNull Point point) {
        return div(point.x(), point.y(), point.z());
    }

    @Override
    public @NotNull Pos div(double value) {
        return div(value, value, value);
    }

    @Contract(pure = true)
    public @NotNull Vel asVel() {
        return new Vel(x, y, z);
    }

    @FunctionalInterface
    public interface Operator {
        @NotNull Pos apply(double x, double y, double z, float yaw, float pitch);
    }

    @Override
    public String toString() {
        return "Pos{x=" + x + ", y=" + y + ", z=" + z + ", yaw=" + yaw + ", pitch=" + pitch + '}';
    }

    /**
     * Fixes a yaw value that is not between -180.0F and 180.0F
     * So for example -1355.0F becomes 85.0F and 225.0F becomes -135.0F
     *
     * @param yaw The possible "wrong" yaw
     * @return a fixed yaw
     */
    private static float fixYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw < -180.0F) {
            yaw += 360.0F;
        } else if (yaw > 180.0F) {
            yaw -= 360.0F;
        }
        return yaw;
    }
}
