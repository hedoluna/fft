package com.fft.factory;

import com.fft.core.FFT;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discovery mechanism for FFT implementations using annotations and classpath scanning.
 * 
 * <p>This class provides automatic discovery of FFT implementations marked with the
 * {@link FFTImplementation} annotation. It uses a combination of classpath scanning
 * and reflection to find and instantiate implementations.</p>
 * 
 * <h3>Discovery Methods:</h3>
 * <ol>
 * <li><strong>Package-based scanning</strong>: Scans specific packages for annotated classes</li>
 * <li><strong>Service loader pattern</strong>: Uses META-INF/services for explicit registration</li>
 * <li><strong>Reflection-based discovery</strong>: Scans the classpath for annotations</li>
 * </ol>
 * 
 * <h3>Performance Considerations:</h3>
 * <p>Discovery is performed once during initialization and results are cached.
 * The discovery process uses lazy initialization to minimize startup overhead.</p>
 * 
 * @author Engine AI Assistant
 * @since 2.0.0
 * @see FFTImplementation
 * @see DefaultFFTFactory
 */
public class FFTImplementationDiscovery {
    
    private static final Logger LOGGER = Logger.getLogger(FFTImplementationDiscovery.class.getName());
    
    /**
     * Package prefixes to scan for FFT implementations.
     */
    private static final String[] SCAN_PACKAGES = {
        "com.fft.optimized",
        "com.fft.experimental",
        "com.fft.custom"
    };
    
    /**
     * Cache of discovered implementations to avoid repeated scanning.
     */
    private static volatile Map<Integer, List<DiscoveredImplementation>> discoveredImplementations;
    
    /**
     * Represents a discovered FFT implementation with its metadata.
     */
    public static class DiscoveredImplementation {
        private final Class<? extends FFT> implementationClass;
        private final FFTImplementation annotation;
        private final Supplier<FFT> supplier;
        
        public DiscoveredImplementation(Class<? extends FFT> implementationClass, 
                                      FFTImplementation annotation) {
            this.implementationClass = implementationClass;
            this.annotation = annotation;
            this.supplier = createSupplier(implementationClass);
        }
        
        public int getSize() {
            return annotation.size();
        }
        
        public int getPriority() {
            return annotation.priority();
        }
        
        public String getDescription() {
            return annotation.description().isEmpty() ? 
                implementationClass.getSimpleName() : annotation.description();
        }
        
        public boolean isAutoRegister() {
            return annotation.autoRegister();
        }
        
        public Supplier<FFT> getSupplier() {
            return supplier;
        }
        
        public Class<? extends FFT> getImplementationClass() {
            return implementationClass;
        }
        
        public String[] getCharacteristics() {
            return annotation.characteristics();
        }
        
        /**
         * Creates a supplier for the implementation class.
         */
        private static Supplier<FFT> createSupplier(Class<? extends FFT> clazz) {
            return () -> {
                try {
                    Constructor<? extends FFT> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    return constructor.newInstance();
                } catch (NoSuchMethodException | InstantiationException | 
                         IllegalAccessException | InvocationTargetException e) {
                    LOGGER.log(Level.WARNING, "Failed to create instance of " + clazz.getName(), e);
                    throw new RuntimeException("Failed to create FFT implementation: " + clazz.getName(), e);
                }
            };
        }
    }
    
    /**
     * Discovers all FFT implementations in the classpath.
     * 
     * @return map of size to list of discovered implementations
     */
    public static Map<Integer, List<DiscoveredImplementation>> discoverImplementations() {
        if (discoveredImplementations == null) {
            synchronized (FFTImplementationDiscovery.class) {
                if (discoveredImplementations == null) {
                    discoveredImplementations = performDiscovery();
                }
            }
        }
        return discoveredImplementations;
    }
    
    /**
     * Performs the actual discovery process.
     */
    private static Map<Integer, List<DiscoveredImplementation>> performDiscovery() {
        Map<Integer, List<DiscoveredImplementation>> implementations = new HashMap<>();
        
        // Discover implementations from known packages
        discoverFromPackages(implementations);
        
        // Discover implementations from service files
        discoverFromServices(implementations);
        
        // Sort implementations by priority (highest first)
        implementations.values().forEach(list -> 
            list.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority())));
        
        LOGGER.info("Discovered " + implementations.values().stream()
                    .mapToInt(List::size).sum() + " FFT implementations");
        
        return implementations;
    }
    
    /**
     * Discovers implementations by scanning known packages.
     */
    private static void discoverFromPackages(Map<Integer, List<DiscoveredImplementation>> implementations) {
        for (String packageName : SCAN_PACKAGES) {
            try {
                scanPackage(packageName, implementations);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to scan package: " + packageName, e);
            }
        }
    }
    
    /**
     * Scans a specific package for FFT implementations.
     */
    private static void scanPackage(String packageName, 
                                  Map<Integer, List<DiscoveredImplementation>> implementations) {
        try {
            String packagePath = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    // Handle file system resources
                    scanFileSystemPackage(resource, packageName, implementations);
                } else if (resource.getProtocol().equals("jar")) {
                    // Handle JAR resources
                    scanJarPackage(resource, packageName, implementations);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to scan package: " + packageName, e);
        }
    }
    
    /**
     * Scans a package in the file system.
     */
    private static void scanFileSystemPackage(URL resource, String packageName,
                                            Map<Integer, List<DiscoveredImplementation>> implementations) {
        try {
            java.io.File directory = new java.io.File(resource.toURI());
            if (directory.exists() && directory.isDirectory()) {
                for (java.io.File file : directory.listFiles()) {
                    if (file.getName().endsWith(".class")) {
                        String className = packageName + "." + 
                            file.getName().substring(0, file.getName().length() - 6);
                        checkClass(className, implementations);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to scan file system package: " + packageName, e);
        }
    }
    
    /**
     * Scans a package in a JAR file.
     */
    private static void scanJarPackage(URL resource, String packageName,
                                     Map<Integer, List<DiscoveredImplementation>> implementations) {
        // For simplicity, we'll use a known class list approach for JAR scanning
        // In a production environment, you might want to use a more sophisticated
        // JAR scanning mechanism or a library like Reflections
        
        // For now, we'll rely on explicit registration through the service loader pattern
        LOGGER.fine("JAR scanning not fully implemented, relying on service loader pattern");
    }
    
    /**
     * Checks if a class is an annotated FFT implementation.
     */
    private static void checkClass(String className, 
                                 Map<Integer, List<DiscoveredImplementation>> implementations) {
        try {
            Class<?> clazz = Class.forName(className);
            
            if (FFT.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(FFTImplementation.class)) {
                @SuppressWarnings("unchecked")
                Class<? extends FFT> fftClass = (Class<? extends FFT>) clazz;
                FFTImplementation annotation = clazz.getAnnotation(FFTImplementation.class);
                
                if (annotation.autoRegister()) {
                    DiscoveredImplementation discovered = new DiscoveredImplementation(fftClass, annotation);
                    implementations.computeIfAbsent(discovered.getSize(), k -> new ArrayList<>())
                                 .add(discovered);
                    
                    LOGGER.fine("Discovered FFT implementation: " + className + 
                              " for size " + discovered.getSize());
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // Expected for some classes, don't log as warning
            LOGGER.finest("Could not load class: " + className);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to check class: " + className, e);
        }
    }
    
    /**
     * Discovers implementations through the service loader pattern.
     */
    private static void discoverFromServices(Map<Integer, List<DiscoveredImplementation>> implementations) {
        try {
            ServiceLoader<FFT> serviceLoader = ServiceLoader.load(FFT.class);
            for (FFT fft : serviceLoader) {
                Class<? extends FFT> clazz = fft.getClass();
                if (clazz.isAnnotationPresent(FFTImplementation.class)) {
                    FFTImplementation annotation = clazz.getAnnotation(FFTImplementation.class);
                    if (annotation.autoRegister()) {
                        DiscoveredImplementation discovered = new DiscoveredImplementation(clazz, annotation);
                        implementations.computeIfAbsent(discovered.getSize(), k -> new ArrayList<>())
                                     .add(discovered);
                        
                        LOGGER.fine("Discovered FFT implementation via service loader: " + 
                                  clazz.getName() + " for size " + discovered.getSize());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to discover implementations via service loader", e);
        }
    }
    
    /**
     * Registers all discovered implementations with the given factory.
     * 
     * @param factory the factory to register implementations with
     */
    public static void registerDiscoveredImplementations(DefaultFFTFactory factory) {
        Map<Integer, List<DiscoveredImplementation>> discovered = discoverImplementations();
        
        for (Map.Entry<Integer, List<DiscoveredImplementation>> entry : discovered.entrySet()) {
            int size = entry.getKey();
            for (DiscoveredImplementation impl : entry.getValue()) {
                if (impl.isAutoRegister()) {
                    factory.registerImplementation(size, impl.getSupplier(), impl.getPriority());
                    LOGGER.fine("Auto-registered " + impl.getImplementationClass().getSimpleName() + 
                              " for size " + size + " with priority " + impl.getPriority());
                }
            }
        }
        
        LOGGER.info("Auto-registration completed for " + 
                   discovered.values().stream().mapToInt(List::size).sum() + " implementations");
    }
    
    /**
     * Gets a detailed report of all discovered implementations.
     * 
     * @return formatted string with discovery details
     */
    public static String getDiscoveryReport() {
        Map<Integer, List<DiscoveredImplementation>> discovered = discoverImplementations();
        StringBuilder report = new StringBuilder();
        report.append("FFT Implementation Discovery Report:\n");
        report.append("===================================\n");
        
        if (discovered.isEmpty()) {
            report.append("No implementations discovered.\n");
        } else {
            for (Map.Entry<Integer, List<DiscoveredImplementation>> entry : discovered.entrySet()) {
                int size = entry.getKey();
                report.append(String.format("\nSize %d:\n", size));
                
                for (DiscoveredImplementation impl : entry.getValue()) {
                    report.append(String.format("  - %s (priority: %d, auto-register: %s)\n",
                                               impl.getDescription(),
                                               impl.getPriority(),
                                               impl.isAutoRegister()));
                    
                    if (impl.getCharacteristics().length > 0) {
                        report.append("    Characteristics: ");
                        report.append(String.join(", ", impl.getCharacteristics()));
                        report.append("\n");
                    }
                }
            }
        }
        
        return report.toString();
    }
}