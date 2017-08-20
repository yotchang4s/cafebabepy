package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyStrObject extends AbstractPyObjectObject {

    private final String value;

    public PyStrObject(Python runtime, String value) {
        super(runtime, runtime.typeOrThrow("builtins.str"));

        this.value = value;
    }

    public PyStrObject add(PyStrObject str) {
        return this.runtime.str(this.value + str.value);
    }

    @Override
    public String asJavaString() {
        return this.value;
    }
}