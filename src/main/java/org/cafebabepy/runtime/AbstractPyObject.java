package org.cafebabepy.runtime;

import org.cafebabepy.runtime.module.builtins.PyObjectType;
import org.cafebabepy.util.LazyHashMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public abstract class AbstractPyObject implements PyObject {

    protected final Python runtime;

    protected final PyObjectScope scope;

    protected final boolean appear;

    private volatile List<PyObject> types;

    private Map<String, Object> javaObjectMap;

    protected AbstractPyObject(Python runtime) {
        this(runtime, true);
    }

    protected AbstractPyObject(Python runtime, PyObjectScope parentScope) {
        this(runtime, true, parentScope);
    }

    protected AbstractPyObject(Python runtime, boolean appear) {
        this.runtime = runtime;
        this.scope = new PyObjectScope();
        this.appear = appear;
    }

    protected AbstractPyObject(Python runtime, boolean appear, PyObjectScope parentScope) {
        this.runtime = runtime;
        this.scope = new PyObjectScope(parentScope);
        this.appear = appear;
    }

    @Override
    public List<PyObject> getTypes() {
        if (this.types == null) {
            synchronized (this) {
                if (this.types == null) {
                    this.types = getC3AlgorithmTypes();
                    this.types = Collections.unmodifiableList(
                            Collections.synchronizedList(this.types));
                }
            }
        }

        return this.types;
    }

    @Override
    public final Python getRuntime() {
        return this.runtime;
    }

    @Override
    public final PyObjectScope getScope() {
        return this.scope;
    }

    @Override
    public final void putJavaObject(String name, Object object) {
        if (this.javaObjectMap == null) {
            synchronized (this) {
                if (this.javaObjectMap == null) {
                    this.javaObjectMap = new ConcurrentHashMap<>();
                }
            }
        }

        this.javaObjectMap.put(name, object);
    }

    @Override
    public final Optional<Object> getJavaObject(String name) {
        if (this.javaObjectMap == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.javaObjectMap.get(name));
    }

    @Override
    public final String getFullName() {
        return getModuleName().map(n -> n + ".").orElse("") + getName();
    }

    @Override
    public void preInitialize() {
    }

    @Override
    public void postInitialize() {
    }

    @Override
    public boolean isAppear() {
        return this.appear;
    }

    @Override
    public final PyObject getStr() {
        return this.runtime.str(asJavaString());
    }

    @Override
    public final boolean isException() {
        // FIXME 親がBaseExceptionかどうかを判定する
        PyObject module = getRuntime().getBuiltinsModule();
        PyObject call = module.getObjectOrThrow("issubclass");

        PyObject baseExceptionType = module.getObjectOrThrow("BaseException");

        return call.call(getType(), baseExceptionType).isTrue();
    }

    @Override
    public final boolean isTrue() {
        return !isFalse();
    }

    @Override
    public boolean isFalse() {
        return getRuntime()
                .getBuiltinsModule()
                .getObjectOrThrow("bool").call(this) == getRuntime().False();
    }

    public final Optional<PyObject> type(String name) {
        return this.runtime.type(name);
    }

    public final Optional<PyObject> type(String name, boolean appear) {
        return this.runtime.type(name, appear);
    }

    public final PyObject typeOrThrow(String name) {
        return getRuntime().typeOrThrow(name);
    }

    public final PyObject typeOrThrow(String name, boolean appear) {
        return getRuntime().typeOrThrow(name, appear);
    }

    @Override
    public final LazyHashMap<String, Supplier<PyObject>> getLazyObjects() {
        return getLazyObjects(true);
    }

    @Override
    public final LazyHashMap<String, Supplier<PyObject>> getLazyObjects(boolean appear) {
        return getScope().getsLazy(appear);
    }


    @Override
    public final Map<String, PyObject> getObjects() {
        return getObjects(true);
    }

    @Override
    public final Map<String, PyObject> getObjects(boolean appear) {
        return getScope().gets(appear);
    }

    @Override
    public final Supplier<Optional<PyObject>> getLazyObject(String name) {
        return getLazyObject(name, true);
    }

    @Override
    public final Supplier<Optional<PyObject>> getLazyObject(String name, boolean appear) {
        return getScope().getLazy(name, appear);
    }

    @Override
    public final Optional<PyObject> getObject(String name) {
        return getObject(name, true);
    }

    @Override
    public final Optional<PyObject> getObject(String name, boolean appear) {
        return getScope().get(name, appear);
    }

    @Override
    public Supplier<PyObject> getLazyObjectOrThrow(String name) {
        return () -> getObjectOrThrow(name);
    }

    @Override
    public Supplier<PyObject> getLazyObjectOrThrow(String name, boolean appear) {
        return () -> getObjectOrThrow(name, appear);
    }

    @Override
    public final PyObject getObjectOrThrow(String name) {
        return getObjectOrThrow(name, true);
    }

    @Override
    public final PyObject getObjectOrThrow(String name, boolean appear) {
        Optional<PyObject> objectOpt = getObject(name, appear);
        if (!objectOpt.isPresent()) {
            if (isModule()) {
                throw getRuntime().newRaiseException("builtins.NameError",
                        "name '"
                                + name
                                + "' is not defined");

            } else if (isType()) {
                throw getRuntime().newRaiseException("builtins.AttributeError",
                        "type object '"
                                + getFullName()
                                + "' has no attribute '"
                                + name
                                + "'");

            } else {
                throw getRuntime().newRaiseException("builtins.AttributeError",
                        "'"
                                + getFullName()
                                + "' object has no attribute '"
                                + name
                                + "'");
            }
        }

        return objectOpt.get();
    }

    @Override
    public final PyObject getCallable() {
        return getObject(__call__).orElseThrow(
                () -> this.runtime.newRaiseException("builtins.TypeError",
                        "'" + getName() + "' object is not callable"));
    }

    @Override
    public PyObject call() {
        PyObject[] objects = new PyObject[0];

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1) {
        PyObject[] objects = new PyObject[1];
        objects[0] = arg1;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2) {
        PyObject[] objects = new PyObject[2];
        objects[0] = arg1;
        objects[1] = arg2;

        return call(objects);
    }

    @Override
    public PyObject call(
            PyObject arg1,
            PyObject arg2,
            PyObject arg3) {
        PyObject[] objects = new PyObject[3];
        objects[0] = arg1;
        objects[1] = arg2;
        objects[2] = arg3;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2,
                         PyObject arg3,
                         PyObject arg4) {
        PyObject[] objects = new PyObject[4];
        objects[0] = arg1;
        objects[1] = arg2;
        objects[2] = arg3;
        objects[3] = arg4;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2,
                         PyObject arg3,
                         PyObject arg4,
                         PyObject arg5) {
        PyObject[] objects = new PyObject[5];
        objects[0] = arg1;
        objects[1] = arg2;
        objects[2] = arg3;
        objects[3] = arg4;
        objects[4] = arg5;

        return call(objects);
    }

    private List<PyObject> getC3AlgorithmTypes() {
        try {
            return getC3AlgorithmTypes(this);

        } catch (CafeBabePyException e) {
            throw this.runtime.newRaiseTypeError(e.getMessage());
        }
    }

    private static List<PyObject> getC3AlgorithmTypes(PyObject object) {
        if (!object.isType() && !object.isModule()) {
            throw new CafeBabePyException("'" + object.getName() + "' is not type");
        }
        List<PyObject> result = new ArrayList<>();
        result.add(object);

        if (object instanceof PyObjectType) {
            return result;
        }

        List<PyObject> bases = new LinkedList<>(object.getBases());

        List<List<PyObject>> listOfLinearization = new ArrayList<>();

        for (PyObject base : bases) {
            listOfLinearization.add(getC3AlgorithmTypes(base));
        }
        listOfLinearization.add(bases);

        try {
            result.addAll(merge(listOfLinearization));

        } catch (CafeBabePyException ignore) {
            String baseNames = bases.stream()
                    .map(PyObject::getName)
                    .collect(Collectors.joining(", "));

            throw new CafeBabePyException("Cannot create a consistent method resolution"
                    + System.lineSeparator() + "order (MRO) for bases " + baseNames);
        }

        return result;
    }

    private static List<PyObject> merge(List<List<PyObject>> listOfLinearization) {
        for (List<PyObject> il : listOfLinearization) {
            PyObject h = il.get(0);

            boolean inNotTail = true;
            for (List<PyObject> jl : listOfLinearization) {
                if (!jl.isEmpty()) {
                    List<PyObject> tail = jl.subList(1, jl.size());
                    inNotTail &= !tail.contains(h);
                }
            }

            if (inNotTail) {
                for (List<PyObject> list : listOfLinearization) {
                    // Remove head
                    if (h == list.get(0)) {
                        list.remove(h);
                    }
                }

                List<List<PyObject>> listOfStripped = new ArrayList<>();

                for (List<PyObject> list : listOfLinearization) {
                    if (!list.isEmpty()) {
                        listOfStripped.add(list);
                    }
                }

                List<PyObject> result = new LinkedList<>();
                result.add(h);

                if (!listOfStripped.isEmpty()) {
                    result.addAll(merge(listOfStripped));
                }

                return result;
            }
        }

        throw new CafeBabePyException();
    }
}
