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
package org.jboss.test.jaxrs.examples.ex12_2.domain;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "customer")
public class Customer
{
   private int id;
   private String firstName;
   private String lastName;
   private String street;
   private String city;
   private String state;
   private String zip;
   private String country;

   @XmlAttribute
   public int getId()
   {
      return id;
   }

   public void setId(int id)
   {
      this.id = id;
   }

   @XmlElement(name = "first-name")
   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   @XmlElement(name = "last-name")
   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   @XmlElement
   public String getStreet()
   {
      return street;
   }

   public void setStreet(String street)
   {
      this.street = street;
   }

   @XmlElement
   public String getCity()
   {
      return city;
   }

   public void setCity(String city)
   {
      this.city = city;
   }

   @XmlElement
   public String getState()
   {
      return state;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   @XmlElement
   public String getZip()
   {
      return zip;
   }

   public void setZip(String zip)
   {
      this.zip = zip;
   }

   @XmlElement
   public String getCountry()
   {
      return country;
   }

   public void setCountry(String country)
   {
      this.country = country;
   }

   @Override
   public String toString()
   {
      return "Customer{" +
              "id=" + id +
              ", firstName='" + firstName + '\'' +
              ", lastName='" + lastName + '\'' +
              ", street='" + street + '\'' +
              ", city='" + city + '\'' +
              ", state='" + state + '\'' +
              ", zip='" + zip + '\'' +
              ", country='" + country + '\'' +
              '}';
   }
}