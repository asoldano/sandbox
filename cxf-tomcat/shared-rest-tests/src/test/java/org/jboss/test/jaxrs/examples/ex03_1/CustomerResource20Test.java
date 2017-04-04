/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.jaxrs.examples.ex03_1;

import java.io.File;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.jaxrs.examples.JBossWSTestHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@RunWith(Arquillian.class)
public class CustomerResource20Test
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs20-examples-ex03_1.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.jaxrs.examples.ex03_1.domain.Customer.class)
               .addClass(org.jboss.test.jaxrs.examples.ex03_1.services.CustomerResource.class)
               .addClass(org.jboss.test.jaxrs.examples.ex03_1.services.ShoppingApplication.class)
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.apache.cxf:cxf-rt-frontend-jaxrs:" + System.getProperty("cxf.version")))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.apache.cxf:cxf-core:" + System.getProperty("cxf.version")))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.apache.cxf:cxf-rt-transports-http:" + System.getProperty("cxf.version")))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("javax.annotation:javax.annotation-api:1.2"))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("javax.ws.rs:javax.ws.rs-api:2.0.1"))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.apache.ws.xmlschema:xmlschema-core:2.2.1"))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.codehaus.woodstox:stax2-api:3.1.4"))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.codehaus.woodstox:woodstox-core-asl:4.4.1"))
               
               
//               final String springVersion = System.getProperty("spring.version");
               
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.springframework:spring-core:" + springVersion))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.springframework:spring-web:" + springVersion))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.springframework:spring-webmvc:" + springVersion))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.springframework:spring-context:" + springVersion))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.springframework:spring-expression:" + springVersion))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.springframework:spring-beans:" + springVersion))
//               .addAsLibrary(JBossWSTestHelper.resolveDependency("org.springframework:spring-aop:" + springVersion))
               
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxrs/examples/ex03_1/WEB-INF/web.xml"));
      return archive;
   }
   
   @Test
   @RunAsClient
   public void testCustomerResource() throws Exception
   {
      Client client = ClientBuilder.newClient();
      try {
         String xml = "<customer>"
                 + "<first-name>Bill</first-name>"
                 + "<last-name>Burke</last-name>"
                 + "<street>256 Clarendon Street</street>"
                 + "<city>Boston</city>"
                 + "<state>MA</state>"
                 + "<zip>02115</zip>"
                 + "<country>USA</country>"
                 + "</customer>";

         Response response = client.target(baseURL + "myservices/customers").request().post(Entity.xml(xml));
         if (response.getStatus() != 201)
            throw new RuntimeException("Failed to create");
         String location = response.getLocation().toString();
         Assert.assertTrue(location.contains("jaxrs20-examples-ex03_1/myservices/customers/1"));
         response.close();

         String customer = client.target(location).request().get(String.class);
         Assert.assertTrue(customer.contains("Bill"));

         String updateCustomer = "<customer>"
                 + "<first-name>William</first-name>"
                 + "<last-name>Burke</last-name>"
                 + "<street>256 Clarendon Street</street>"
                 + "<city>Boston</city>"
                 + "<state>MA</state>"
                 + "<zip>02115</zip>"
                 + "<country>USA</country>"
                 + "</customer>";
         response = client.target(location).request().put(Entity.xml(updateCustomer));
         if (response.getStatus() != 204) throw new RuntimeException("Failed to update");
         response.close();
         customer = client.target(location).request().get(String.class);
         Assert.assertTrue(customer.contains("William"));
      } finally {
         client.close();
      }
   }
}
