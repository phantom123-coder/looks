package com.looksmaxxing.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class FloatSlider extends AbstractSliderButton {
    private final String label;
    private final Consumer<Float> onChange;

    public FloatSlider(int x, int y, int w, int h, String label, float initial, Consumer<Float> onChange) {
        super(x, y, w, h, Component.empty(), initial);
        this.label = label;
        this.onChange = onChange;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(label + ": " + (int) Math.round(value * 100)));
    }

    @Override
    protected void applyValue() {
        onChange.accept((float) value);
    }
}
