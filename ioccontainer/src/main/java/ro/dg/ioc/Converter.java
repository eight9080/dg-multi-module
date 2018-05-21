package ro.dg.ioc;

public interface Converter<T> {
    T convert(String value);
}
