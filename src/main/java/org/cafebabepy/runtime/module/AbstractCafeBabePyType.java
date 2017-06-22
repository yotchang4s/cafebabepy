package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.util.ModuleOrClassSplitter;

import java.util.Optional;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public abstract class AbstractCafeBabePyType extends AbstractAbstractCafeBabePyAny {

    private String moduleName;

    private String name;

    private boolean appear;

    private String[] baseNames;

    protected AbstractCafeBabePyType(Python runtime) {
        super(runtime);
    }

    @Override
    String[] getBaseNames() {
        return this.baseNames;
    }

    @Override
    public void defineClass() {
        Class<?> clazz = getClass();

        DefineCafeBabePyType defineCafeBabePyType = clazz.getAnnotation(DefineCafeBabePyType.class);
        if (defineCafeBabePyType == null) {
            throw new CafeBabePyException(
                    "DefineCafeBabePyModule annotation is not defined " + clazz.getName());
        }

        this.baseNames = defineCafeBabePyType.parent();

        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(defineCafeBabePyType.name());
        this.moduleName = splitter.getModuleName().orElseThrow(() ->
                new CafeBabePyException("name '"
                        + defineCafeBabePyType.name()
                        + "' is not found module")
        );

        this.name = splitter.getSimpleName();
        this.appear = defineCafeBabePyType.appear();

        PyObject module = this.runtime.module(this.moduleName).orElseThrow(() ->
                new CafeBabePyException(
                        "module '" + this.moduleName + "' is not found " + clazz.getName()));

        module.getScope().put(this.name, this, this.appear);
    }

    @Override
    public String asJavaString() {
        return "<class '" + getFullName() + "'>";
    }

    @Override
    public PyObject getType() {
        return this.runtime.getBuiltinsModule().getObjectOrThrow("type");
    }

    public Optional<String> getModuleName() {
        // moduleName is not null
        return Optional.of(this.moduleName);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isType() {
        return true;
    }

    @Override
    public boolean isModule() {
        return false;
    }

    @Override
    public PyObject call(PyObject self, PyObject... args) {
        return getCallable().call(self, args);
    }

    @DefineCafeBabePyFunction(name = __call__)
    public PyObject __call__(PyObject... args) {
        PyObject object = getObjectOrThrow(__new__).callSelf(this);

        object.getObjectOrThrow(__init__).callSelf(object, args);

        return object;
    }
}
