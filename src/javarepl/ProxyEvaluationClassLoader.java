package javarepl;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by pwilkin on 22-Dec-16.
 */
public class ProxyEvaluationClassLoader extends EvaluationClassLoader {

    protected final URLClassLoader delegate;

    public ProxyEvaluationClassLoader(URLClassLoader delegate, EvaluationContext context) {
        super(context);
        this.delegate = delegate;
    }

    @Override
    public void registerURL(URL url) {
        super.registerURL(url);
    }

    @Override
    public Sequence<URL> registeredUrls() {
        List<URL> urls = Arrays.stream(delegate.getURLs()).collect(Collectors.toList());
        urls.addAll(super.registeredUrls());
        return Sequences.init(urls);
    }

    @Override
    public boolean isClassLoaded(String name) {
        try {
            Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            m.setAccessible(true);
            Class<?> cl = (Class<?>) m.invoke(delegate, name);
            boolean loaded = cl != null;
            m.setAccessible(false);
            return loaded || super.isClassLoaded(name);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream res = delegate.getResourceAsStream(name);
        if (res == null) {
            return super.getResourceAsStream(name);
        } else {
            return res;
        }
    }

    @Override
    public void close() throws IOException {
        IOException e = null;
        try {
            delegate.close();
        } catch (IOException ie) {
            e = ie;
        } finally {
            super.close();
            if (e != null) {
                throw e;
            }
        }
    }

    @Override
    public URL[] getURLs() {
        Sequence<URL> reg = registeredUrls();
        return reg.toArray(new URL[reg.size()]);
    }


    @Override
    public URL findResource(String name) {
        URL url = delegate.findResource(name);
        if (url == null) {
            return super.findResource(name);
        } else {
            return url;
        }
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> enr = delegate.findResources(name);
        if (enr == null || !enr.hasMoreElements()) {
            enr = super.findResources(name);
        }
        return enr;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return delegate.loadClass(name);
        } catch (ClassNotFoundException e) {
            return super.loadClass(name);
        }
    }

    @Override
    public URL getResource(String name) {
        URL url = delegate.getResource(name);
        if (url == null) {
            return super.getResource(name);
        } else {
            return url;
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> enr = delegate.getResources(name);
        if (enr == null || !enr.hasMoreElements()) {
            enr = super.getResources(name);
        }
        return enr;
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        super.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        super.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        super.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        super.clearAssertionStatus();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode() * 7 + super.hashCode() * 3;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProxyEvaluationClassLoader && delegate.equals(((ProxyEvaluationClassLoader) obj).delegate) && Arrays.equals(getURLs(), ((ProxyEvaluationClassLoader) obj).getURLs());
    }

    @Override
    public String toString() {
        return "Delegated: " + delegate.toString() + ", extra libs: " + super.registeredUrls();
    }
}
