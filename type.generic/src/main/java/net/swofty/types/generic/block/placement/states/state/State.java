package net.swofty.types.generic.block.placement.states.state;

import org.jetbrains.annotations.NotNull;

public interface State<T extends Comparable<T>> {

    @NotNull
    String getKey();

    @NotNull
    String getValue();

    @NotNull
    T parse(String rawValue);
}
