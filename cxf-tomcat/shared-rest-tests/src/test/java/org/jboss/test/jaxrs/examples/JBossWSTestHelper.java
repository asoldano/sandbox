package org.jboss.test.jaxrs.examples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.WeakHashMap;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class JBossWSTestHelper {

	   private static final String SYSPROP_JBOSS_BIND_ADDRESS = "jboss.bind.address";
	   private static final String SYSPROP_JBOSS_REMOTING_PROTOCOL = "jboss.remoting.protocol";
	   private static final String SYSPROP_INITIAL_CONTEXT_FACTORY = "jboss.initial.context.factory";
	   private static final String SYSPROP_TEST_ARCHIVE_DIRECTORY = "test.archive.directory";
	   private static final String SYSPROP_TEST_RESOURCES_DIRECTORY = "test.resources.directory";
	   private static final String SYSPROP_DEFAULT_CONTAINER_QUALIFIER = "default.container.qualifier";
	   private static final String SYSPROP_DEFAULT_CONTAINER_GROUP_QUALIFIER = "default.container.group.qualifier";
	   private static final String SYSPROP_CONTAINER_PORT_OFFSET_PREFIX = "port-offset.";
	   private static final String TEST_USERNAME = "test.username";
	   private static final String TEST_PASSWORD = "test.password";
	   private static final String testArchiveDir = System.getProperty(SYSPROP_TEST_ARCHIVE_DIRECTORY);
	   private static final String testResourcesDir = System.getProperty(SYSPROP_TEST_RESOURCES_DIRECTORY);
	   private static final String serverHost = System.getProperty(SYSPROP_JBOSS_BIND_ADDRESS, "localhost");
	   private static final String remotingProtocol = System.getProperty(SYSPROP_JBOSS_REMOTING_PROTOCOL);
	   private static final String initialContextFactory = System.getProperty(SYSPROP_INITIAL_CONTEXT_FACTORY);

	   private static WeakHashMap<ClassLoader, Hashtable<String, String>> containerEnvs = new WeakHashMap<ClassLoader, Hashtable<String,String>>();

	   public static String getRemotingProtocol()
	   {
	      return remotingProtocol;
	   }
	   
	   public static String getInitialContextFactory()
	   {
	      return initialContextFactory;
	   }

	   /**
	    * Get the JBoss server host from system property "jboss.bind.address"
	    * This defaults to "localhost"
	    */
	   public static String getServerHost()
	   {
	      return serverHost;
	   }
	   
	   public static int getServerPort()
	   {
	      return getServerPort(null, null);
	   }
	   
	   public static int getServerPort(String groupQualifier, String containerQualifier)
	   {
	      return 8080 + getContainerPortOffset(groupQualifier, containerQualifier);
	   }
	   
	   public static int getSecureServerPort(String groupQualifier, String containerQualifier)
	   {
	      return 8443 + getContainerPortOffset(groupQualifier, containerQualifier);
	   }
	   
	   protected static int getContainerPortOffset(String groupQualifier, String containerQualifier)
	   {
	      Hashtable<String, String> env = getContainerEnvironment();
	      
	      if (groupQualifier == null) {
	         groupQualifier = env.get(SYSPROP_DEFAULT_CONTAINER_GROUP_QUALIFIER);
	      }
	      if (containerQualifier == null) {
	         containerQualifier = env.get(SYSPROP_DEFAULT_CONTAINER_QUALIFIER);
	      }
	      String offset = env.get(SYSPROP_CONTAINER_PORT_OFFSET_PREFIX + groupQualifier + "." + containerQualifier);
	      return offset != null ? Integer.valueOf(offset) : 0;
	   }
	   
	   private static Hashtable<String, String> getContainerEnvironment() {
	      Hashtable<String, String> env;
	      ClassLoader tccl = Thread.currentThread().getContextClassLoader();
	      synchronized (containerEnvs)
	      {
	         env = containerEnvs.get(tccl);
	         if (env == null) {
	            env = new Hashtable<String, String>();
	            final InputStream is = tccl.getResourceAsStream("container.properties");
	            try {
	               if (is != null) {
	                   final Properties props = new Properties();
	                   props.load(is);
	                   Entry<Object, Object> entry;
	                   final Iterator<Entry<Object, Object>> entries = props.entrySet().iterator();
	                   while (entries.hasNext()) {
	                       entry = entries.next();
	                       env.put((String)entry.getKey(), (String)entry.getValue());
	                   }
	               }
	            } catch (IOException e) {
	               throw new RuntimeException(e);
	            }
	            containerEnvs.put(tccl, env);
	         }
	         return env;
	      }
	  }
	   
	   /** Try to discover the URL for the deployment archive */
	   public static URL getArchiveURL(String archive) throws MalformedURLException
	   {
	      return getArchiveFile(archive).toURI().toURL();
	   }

	   /** Try to discover the File for the deployment archive */
	   public static File getArchiveFile(String archive)
	   {
	      File file = new File(archive);
	      if (file.exists())
	         return file;

	      file = new File(getTestArchiveDir() + "/" + archive);
	      if (file.exists())
	         return file;

	      String notSet = (getTestArchiveDir() == null ? " System property '" + SYSPROP_TEST_ARCHIVE_DIRECTORY + "' not set." : "");
	      throw new IllegalArgumentException("Cannot obtain '" + getTestArchiveDir() + "/" + archive + "'." + notSet);
	   }

	   /** Try to discover the URL for the test resource */
	   public static URL getResourceURL(String resource) throws MalformedURLException
	   {
	      return getResourceFile(resource).toURI().toURL();
	   }

	   /** Try to discover the File for the test resource */
	   public static File getResourceFile(String resource)
	   {
	      File file = new File(resource);
	      if (file.exists())
	         return file;

	      file = new File(getTestResourcesDir() + "/" + resource);
	      if (file.exists())
	         return file;

	      String notSet = (getTestResourcesDir() == null ? " System property '" + SYSPROP_TEST_RESOURCES_DIRECTORY + "' not set." : "");
	      throw new IllegalArgumentException("Cannot obtain '" + getTestResourcesDir() + "/" + resource + "'." + notSet);
	   }

	   public static String getTestArchiveDir()
	   {
	      return testArchiveDir;
	   }

	   public static String getTestResourcesDir()
	   {
	      return testResourcesDir;
	   }

	   public static String getTestUsername() {
	      String prop = System.getProperty(TEST_USERNAME);
	      if (prop == null || "".equals(prop) || ("${" + TEST_USERNAME + "}").equals(prop)) {
	         prop = "kermit";
	      }
	      return prop;
	   }

	   public static String getTestPassword() {
	      String prop = System.getProperty(TEST_PASSWORD);
	      if (prop == null || "".equals(prop) || ("${" + TEST_PASSWORD + "}").equals(prop)) {
	         prop = "thefrog";
	      }
	      return prop;
	   }

	   @SuppressWarnings("rawtypes")
	   public static void writeToFile(Archive archive)
	   {
	      File archiveDir = assertArchiveDirExists();
	      File file = new File(archiveDir, archive.getName());
	      archive.as(ZipExporter.class).exportTo(file, true);
	   }
	   
	   public static abstract class BaseDeployment<T extends org.jboss.shrinkwrap.api.Archive<T>>
	   {
	      protected T archive;

	      public BaseDeployment(Class<T> clazz, String name)
	      {
	         archive = ShrinkWrap.create(clazz, name);
	      }

	      public T create()
	      {
	         return archive;
	      }

	      public T writeToFile()
	      {
	         File archiveDir = assertArchiveDirExists();
	         File file = new File(archiveDir, archive.getName());
	         archive.as(ZipExporter.class).exportTo(file, true);
	         return archive;
	      }
	      
	      public String getName()
	      {
	         return archive.getName();
	      }
	   }
	   
	   public static File assertArchiveDirExists()
	   {
	      File archiveDir = new File(testArchiveDir);
	      if (!archiveDir.exists())
	      {
	         if (testArchiveDir == null)
	            throw new IllegalArgumentException("Cannot create archive - system property '"
	                  + JBossWSTestHelper.SYSPROP_TEST_ARCHIVE_DIRECTORY + "' not set.");
	         if (!archiveDir.mkdirs() && !archiveDir.exists())
	            throw new IllegalArgumentException("Cannot create archive - can not create test archive directory '"
	               + archiveDir.getAbsolutePath() + "'");
	      }
	      return archiveDir;
	   }
	   
	   public static String writeToFile(BaseDeployment<?>... deps) {
	      if (deps == null) {
	         return "";
	      }
	      StringBuilder sb = new StringBuilder();
	      for (BaseDeployment<?> dep : deps) {
	         sb.append(dep.writeToFile().getName());
	         sb.append(" ");
	      }
	      return sb.toString().trim();
	   }

	   public static abstract class JarDeployment extends BaseDeployment<JavaArchive>
	   {
	      public JarDeployment(String name)
	      {
	         super(JavaArchive.class, name);
	      }
	   }
	   
	   
	   /**
	     * Get specified single dependency
	     *
	     * @param dependency
	     * @return Dependency gav
	     */
	    public static File resolveDependency(String dependency) {
	        MavenUtil mavenUtil;
	        mavenUtil = MavenUtil.create(true);
	        File mavenGav;

	        try {
	            mavenGav = mavenUtil.createMavenGavFile(dependency);
	        } catch (Exception e) {
	            throw new RuntimeException("Unable to get artifacts from maven via Aether library", e);
	        }

	        return mavenGav;
	    }
	    
	    public static void addSpringDependencies(WebArchive archive) {
	       
           archive.addAsLibrary(JBossWSTestHelper.resolveDependency("org.apache.cxf:cxf-rt-frontend-jaxrs:" + System.getProperty("cxf.version")))
           .addAsLibrary(JBossWSTestHelper.resolveDependency("org.apache.cxf:cxf-core:" + System.getProperty("cxf.version")))
           .addAsLibrary(JBossWSTestHelper.resolveDependency("org.apache.cxf:cxf-rt-transports-http:" + System.getProperty("cxf.version")))
           .addAsLibrary(JBossWSTestHelper.resolveDependency("javax.annotation:javax.annotation-api:1.2"))
           .addAsLibrary(JBossWSTestHelper.resolveDependency("javax.ws.rs:javax.ws.rs-api:2.0.1"))
           .addAsLibrary(JBossWSTestHelper.resolveDependency("org.apache.ws.xmlschema:xmlschema-core:2.2.1"))
           .addAsLibrary(JBossWSTestHelper.resolveDependency("org.codehaus.woodstox:stax2-api:3.1.4"))
           .addAsLibrary(JBossWSTestHelper.resolveDependency("org.codehaus.woodstox:woodstox-core-asl:4.4.1"));
	    }
	    
	    public static void setXml(WebArchive archive, String application, String urlPattern) {
	       archive.setWebXML(new StringAsset("<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                 + "xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\" version=\"3.0\">"
                 + "<servlet><servlet-name>CXFServlet</servlet-name><servlet-class>org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet</servlet-class><load-on-startup>1</load-on-startup>"
                 + "<init-param><param-name>javax.ws.rs.Application</param-name><param-value>" + application + "</param-value></init-param>"
                 + "</servlet><servlet-mapping><servlet-name>CXFServlet</servlet-name><url-pattern>" + urlPattern + "</url-pattern></servlet-mapping></web-app>"));
	    }
}
