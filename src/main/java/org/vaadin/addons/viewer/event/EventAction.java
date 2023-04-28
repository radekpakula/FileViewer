package org.vaadin.addons.viewer.event;

@FunctionalInterface
public interface EventAction<T> {

    void fireEvent(T fileInfo);
}
