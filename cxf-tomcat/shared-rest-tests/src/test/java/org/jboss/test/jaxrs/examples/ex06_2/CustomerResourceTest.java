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
package org.jboss.test.jaxrs.examples.ex06_2;

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
import org.jboss.test.jaxrs.examples.ex06_2.domain.Customer;
import org.jboss.test.jaxrs.examples.ex06_2.services.JavaMarshaller;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@RunWith(Arquillian.class)
public class CustomerResourceTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-examples-ex06_2.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.jaxrs.examples.ex06_2.domain.Customer.class)
               .addClass(org.jboss.test.jaxrs.examples.ex06_2.services.CustomerResource.class)
               .addClass(org.jboss.test.jaxrs.examples.ex06_2.services.ShoppingApplication.class)
               .addClass(org.jboss.test.jaxrs.examples.ex06_2.services.JavaMarshaller.class);
         JBossWSTestHelper.setXml(archive, "org.jboss.test.jaxrs.examples.ex06_2.services.ShoppingApplication", "/services/*");
      return archive;
   }
   
   @Test
   @RunAsClient
   public void testCustomerResource() throws Exception
   {
      Client client = ClientBuilder.newClient();
      client.register(JavaMarshaller.class);
      try {
         Customer newCustomer = new Customer();
         newCustomer.setFirstName("Bill");
         newCustomer.setLastName("Burke");
         newCustomer.setStreet("256 Clarendon Street");
         newCustomer.setCity("Boston");
         newCustomer.setState("MA");
         newCustomer.setZip("02115");
         newCustomer.setCountry("USA");

         Response response = client.target(baseURL + "services/customers")
                 .request().post(Entity.entity(newCustomer, "application/example-java"));
         if (response.getStatus() != 201) throw new RuntimeException("Failed to create");
         String location = response.getLocation().toString();
         Assert.assertTrue(location.contains("jaxrs-examples-ex06_2/services/customers"));
         response.close();

         Customer customer = client.target(location).request().get(Customer.class);
         Assert.assertEquals("Bill", customer.getFirstName());

         customer.setFirstName("William");
         response = client.target(location).request().put(Entity.entity(customer, "application/example-java"));
         if (response.getStatus() != 204) throw new RuntimeException("Failed to update");

         // Show the update
         customer = client.target(location).request().get(Customer.class);
         Assert.assertEquals("William", customer.getFirstName());
      } finally {
         client.close();
      }
   }
}
