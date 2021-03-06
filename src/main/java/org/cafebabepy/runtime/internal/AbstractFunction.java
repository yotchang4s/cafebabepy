package org.cafebabepy.runtime.internal;

import org.cafebabepy.runtime.Frame;
import org.cafebabepy.runtime.PyFunctionObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;
import org.cafebabepy.runtime.object.PyObjectObject;
import org.cafebabepy.runtime.object.proxy.PyLexicalScopeProxyObject;

import java.util.*;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public abstract class AbstractFunction extends AbstractPyObjectObject implements PyFunctionObject {

    protected final PyObject argumentsContext;
    protected final String name;
    protected final PyObject arguments;
    private final PyObject context;
    private List<String> argumentList;
    private List<PyObject> kw_defaults;
    private List<PyObject> defaultArgs;

    protected AbstractFunction(Python runtime, PyObject context, String name, PyObject arguments) {
        super(runtime);

        this.context = context;
        this.argumentsContext = new PyObjectObject(this.runtime); // arguments scope
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize() {
        super.initialize();

        List<PyObject> defaultsList = getattr(this.arguments, "defaults").toJava(List.class);
        List<PyObject> kw_defaultsList = getattr(this.arguments, "kw_defaults").toJava(List.class);
        this.defaultArgs = evalDefaults(defaultsList);
        this.kw_defaults = evalDefaults(kw_defaultsList);

        getFrame().getLocals().put(__call__, this);

        //PyObject globals = this.runtime.dict(getModule().getFrame().getGlobalsPyObjectMap());
        //getFrame().getLocals().put(__globals__, globals);
    }

    private List<PyObject> evalDefaults(List<PyObject> defaultList) {
        List<PyObject> evalDefaultList = new ArrayList<>(defaultList.size());
        for (PyObject defaultValue : defaultList) {
            PyObject evalDefaultValue = evalDefaultValue(this.argumentsContext, defaultValue);
            evalDefaultList.add(evalDefaultValue);
        }

        return evalDefaultList;
    }

    protected abstract PyObject evalDefaultValue(PyObject context, PyObject defaultValue);

    protected PyObject getattr(PyObject object, String key) {
        return this.runtime.getattr(object, key);
    }

    @Override
    public List<String> getArguments() {
        if (this.argumentList == null) {
            synchronized (this) {
                if (this.argumentList == null) {
                    this.argumentList = new ArrayList<>();

                    List<PyObject> args = getattr(this.arguments, "args").toJava(List.class);
                    PyObject vararg = getattr(this.arguments, "vararg");
                    PyObject kwarg = getattr(this.arguments, "kwarg");
                    List<PyObject> kwOnlyArgs = getattr(this.arguments, "kwonlyargs").toJava(List.class);

                    for (int i = 0; i < args.size(); i++) {
                        PyObject arg = getattr(args.get(i), "arg");
                        this.argumentList.add(arg.toJava(String.class));
                    }

                    if (!vararg.isNone()) {
                        PyObject arg = getattr(vararg, "arg");
                        this.argumentList.add(arg.toJava(String.class));
                    }

                    if (!kwarg.isNone()) {
                        PyObject arg = getattr(kwarg, "arg");
                        this.argumentList.add(arg.toJava(String.class));
                    }

                    for (int i = 0; i < kwOnlyArgs.size(); i++) {
                        PyObject arg = getattr(kwOnlyArgs.get(i), "arg");
                        this.argumentList.add(arg.toJava(String.class));
                    }
                }
            }
        }

        return this.argumentList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PyObject call(PyObject[] args, LinkedHashMap<String, PyObject> keywords) {
        PyObject context = new PyLexicalScopeProxyObject(this.context);
        Frame argumentFrame = this.argumentsContext.getFrame();
        Frame frame = context.getFrame();

        frame.getLocals().putAll(argumentFrame.getLocals());

        PyObject vararg = getattr(this.arguments, "vararg");
        PyObject kwarg = getattr(this.arguments, "kwarg");

        List<PyObject> defineArgsList = getattr(this.arguments, "args").toJava(List.class);
        for (int i = 0; i < defineArgsList.size(); i++) {
            PyObject defineArg = getattr(defineArgsList.get(i), "arg");
            defineArgsList.set(i, defineArg);
        }

        List<PyObject> defineKwOnlyArgList = getattr(this.arguments, "kwonlyargs").toJava(List.class);
        for (int i = 0; i < defineKwOnlyArgList.size(); i++) {
            PyObject defineArg = getattr(defineKwOnlyArgList.get(i), "arg");
            defineKwOnlyArgList.set(i, defineArg);
        }

        if (args.length + this.defaultArgs.size() + keywords.size() < defineArgsList.size()) {
            List<PyObject> notEnoughArguments = defineArgsList.subList(args.length, defineArgsList.size());

            StringBuilder error = new StringBuilder(this.name + "() missing " + notEnoughArguments.size() + " required positional argument: ");

            if (notEnoughArguments.size() == 1) {
                error.append(notEnoughArguments.get(0));

            } else {
                for (int i = 0; i < notEnoughArguments.size() - 2; i++) {
                    if (i != 0) {
                        error.append(", ");
                    }
                    error.append(notEnoughArguments.get(i));
                }
                if (notEnoughArguments.size() > 2) {
                    error.append(", ");
                }

                error.append(notEnoughArguments.get(notEnoughArguments.size() - 2))
                        .append(" and ").append(notEnoughArguments.get(notEnoughArguments.size() - 1));
            }

            throw this.runtime.newRaiseTypeError(error.toString());

        } else if (vararg.isNone() && args.length > defineArgsList.size()) {
            throw this.runtime.newRaiseTypeError(
                    this.name + "() takes " + defineArgsList.size() + " positional arguments but " + args.length + " were given"
            );
        }

        int defaultArgumentIndex = args.length + this.defaultArgs.size() - defineArgsList.size() - defineKwOnlyArgList.size();
        int assignIndex = 0;

        // args
        for (int i = 0; i < defineArgsList.size(); i++) {
            PyObject arg = defineArgsList.get(i);

            if (i < args.length) {
                frame.getLocals().put(arg.toJava(String.class), args[i]);
                assignIndex++;

            } else if (i < defineArgsList.size() - keywords.size()) {
                frame.getLocals().put(arg.toJava(String.class), this.defaultArgs.get(defaultArgumentIndex++));
                assignIndex++;
            }
        }

        // variable argument
        if (!vararg.isNone()) {
            PyObject arg = getattr(vararg, "arg");

            PyObject[] varargs = new PyObject[args.length - assignIndex];
            int count = args.length - assignIndex;
            for (int i = 0; i < count; i++) {
                varargs[i] = args[assignIndex++];
            }

            frame.getLocals().put(arg.toJava(String.class), this.runtime.tuple(varargs));
        }

        // keyword arguments
        for (String keywordString : keywords.keySet()) {
            PyObject keyword = this.runtime.str(keywordString);

            if (!defineArgsList.contains(keyword) && !defineKwOnlyArgList.contains(keyword) && kwarg.isNone()) {
                throw this.runtime.newRaiseTypeError(this.name + "() got an unexpected keyword argument " + keyword);
            }

            if (frame.getLocals().containsKey(keywordString)) {
                throw this.runtime.newRaiseTypeError(this.name + "() got multiple values for argument " + keyword);

            } else {
                if (defineArgsList.contains(keyword) && !defineKwOnlyArgList.contains(keyword)) {
                    frame.getLocals().put(keywordString, keywords.get(keywordString));
                }
            }
        }

        int kw_defaultIndex = 0;
        int kwonlyargsCount = defineKwOnlyArgList.size();
        for (PyObject kwonlyarg : defineKwOnlyArgList) {
            String javaKwonlyarg = kwonlyarg.toJava(String.class);
            PyObject kwonlyargValue = keywords.get(javaKwonlyarg);
            if (kwonlyargValue == null) {
                if (kw_defaultIndex < this.kw_defaults.size()) {
                    frame.getLocals().put(javaKwonlyarg, this.kw_defaults.get(kw_defaultIndex++));
                    kwonlyargsCount--;

                } else {
                    throw this.runtime.newRaiseTypeError(this.name + "() missing " + kwonlyargsCount + " required keyword-only argument: " + kwonlyarg);
                }

            } else {
                frame.getLocals().put(javaKwonlyarg, kwonlyargValue);
            }
        }

        // variable keyword
        if (!kwarg.isNone()) {
            LinkedHashMap<PyObject, PyObject> kwargsMap = new LinkedHashMap<>();

            for (String keywordString : keywords.keySet()) {
                PyObject keyword = this.runtime.str(keywordString);
                if (!frame.getLocals().containsKey(keywordString)) {
                    kwargsMap.put(keyword, keywords.get(keywordString));
                }
            }

            PyObject arg = getattr(kwarg, "arg");
            frame.getLocals().put(arg.toJava(String.class), this.runtime.dict(kwargsMap));
        }

        List<PyObject> contexts = this.runtime.getEvaluator().getContexts();
        try {
            contexts.add(this);

            return callImpl(context);

        } finally {
            contexts.remove(contexts.size() - 1);
        }
    }

    protected abstract PyObject callImpl(PyObject context);

    @Override
    public String getName() {
        return this.name;
    }
}
