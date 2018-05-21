package ro.dg.ioc;


import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Container {

    private Map<Class, Registration> registrations;
    private Map<Class, Converter> converters = new HashMap<>();

    public Container(String configurationPath) throws IoCException {

        final ClassLoader classLoader = getClass().getClassLoader();
        final File configFile = new File(classLoader.getResource(configurationPath).getFile());
        if (!configFile.exists()) {
            throw new IoCException(new FileNotFoundException());
        }

        final Loader loader = new Loader();
        registrations = loader.loadConfiguration(configurationPath);

        registerConverters();

    }

    private void registerConverters() {
        converters.put(int.class, Integer::parseInt);
        converters.put(float.class, Float::parseFloat);
        converters.put(double.class, Double::parseDouble);
        converters.put(byte.class, Byte::parseByte);
        converters.put(long.class, Long::parseLong);
        converters.put(short.class, Short::parseShort);
        converters.put(boolean.class, Boolean::parseBoolean);
        converters.put(String.class, s -> s);
        converters.put(Character.class, c -> c);
    }

    public <T> T resolve(Class<T> type) {
        T instance = null;
        try {
            //find type in registrations map
            final Registration registration = registrations.get(type);
            final List<Constructor> constructorParams = registration.getConstructorParams();

            final Class<?> aClass = Class.forName(registration.getMapTo());

            //find biggest constructor
            final java.lang.reflect.Constructor longestConstructor = getLongestConstructor(aClass);

            //resolve all constructor params
            final Parameter[] parameters = longestConstructor.getParameters();

            //apply constructor arms from map by name
            List<Object> parameterInstances = new ArrayList<>();
            for (Parameter parameter : parameters) {
                final Class<?> parameterType = parameter.getType();
                if (parameterType.isPrimitive() || parameterType.isAssignableFrom(String.class)) {
                    getNonReferenceParameters(constructorParams, parameterInstances,
                            parameter, parameterType);
                } else {
                    getConfiguredParameters(parameterInstances, parameterType);
                }
            }

            //create type
            instance = createInstance(longestConstructor, parameterInstances);

        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return instance;
    }

    private <T> T createInstance(java.lang.reflect.Constructor longestConstructor, List<Object> parameterInstances) throws IllegalAccessException, InvocationTargetException, InstantiationException {

        final Parameter[] parameters = longestConstructor.getParameters();
        Object[] parametersValues = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Class<?> argumentClass = parameterInstances.get(i).getClass();
            final Class<?> parameterClass = parameters[i].getType();
            if (parameterClass.isPrimitive() || argumentClass.isPrimitive()) {
                if (primitivesMatch(argumentClass, parameterClass)) {
                    parametersValues[i] = parameterInstances.get(i);
                }
            }
            if (parameterClass.isAssignableFrom(argumentClass)) {
                parametersValues[i] = parameterInstances.get(i);
            }

        }

        return (T) longestConstructor.newInstance(parametersValues);
    }

    private boolean primitivesMatch(Class argumentClass, Class parameterClass) {
        if ((argumentClass == int.class || argumentClass == Integer.class) && (parameterClass == int.class || parameterClass == Integer.class)) {
            return true;
        }
        if ((argumentClass == byte.class || argumentClass == Byte.class) && (parameterClass == byte.class || parameterClass == Byte.class)) {
            return true;
        }
        if ((argumentClass == short.class || argumentClass == Short.class) && (parameterClass == short.class || parameterClass == Short.class)) {
            return true;
        }
        if ((argumentClass == long.class || argumentClass == Long.class) && (parameterClass == long.class || parameterClass == Long.class)) {
            return true;
        }
        if ((argumentClass == char.class || argumentClass == Character.class) && (parameterClass == char.class || parameterClass == Character.class)) {
            return true;
        }
        if ((argumentClass == double.class || argumentClass == Double.class) && (parameterClass == double.class || parameterClass == Double.class)) {
            return true;
        }
        if ((argumentClass == float.class || argumentClass == Float.class) && (parameterClass == float.class || parameterClass == Float.class)) {
            return true;
        }
        if ((argumentClass == boolean.class || argumentClass == Boolean.class) && (parameterClass == boolean.class || parameterClass == Boolean.class)) {
            return true;
        }
        if ((argumentClass == int.class || argumentClass == Integer.class) && (parameterClass == int.class || parameterClass == Integer.class)) {
            return true;
        }
        return false;
    }

    private void getNonReferenceParameters(List<Constructor> constructorParams, List<Object> parameterInstances,
                                           Parameter parameter, Class<?> parameterType) {
        Object value = null;
        for (Constructor constructor : constructorParams) {
            if (parameter.getName().equals(constructor.getName())) {
                value = constructor.getValue();
                break;
            }
        }
        final Converter converter = converters.get(parameterType);
        final Object convert = converter.convert(value.toString());
        parameterInstances.add(convert);

    }

    private void getConfiguredParameters(List<Object> parameterInstances, Class<?> parameterType) {
        final Object instance = resolve(parameterType);
        parameterInstances.add(instance);
    }

    private java.lang.reflect.Constructor getLongestConstructor(Class<?> aClass) {
        final java.lang.reflect.Constructor<?>[] constructors = aClass.getConstructors();
        java.lang.reflect.Constructor longestConstructor = constructors[0];
        for (java.lang.reflect.Constructor constructor : constructors) {
            if (constructor.getParameterCount() > longestConstructor.getParameterCount()) {
                longestConstructor = constructor;
            }
        }
        return longestConstructor;
    }
}
