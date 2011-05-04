/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.gatein.descriptorgenerator;

import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class FQN
{

   /** . */
   private final LinkedList<String> stack = new LinkedList<String>();

   /** . */
   private final String prefix;

   public FQN(String prefix)
   {
      this.prefix = prefix;
   }

   public void push(String suffix)
   {
      if (suffix == null)
      {
         throw new NullPointerException();
      }
      if (suffix.length() == 0)
      {
         throw new IllegalArgumentException();
      }

      //
      stack.addLast(prefix + suffix);
   }

   public void pop()
   {
      stack.removeLast();
   }

   public String getName()
   {
      return stack.getLast();
   }

   public String toId()
   {
      return toString('_');
   }

   public String toPath()
   {
      return toString('/');
   }

   private String toString(char separatorChar)
   {
      StringBuilder sb = new StringBuilder();
      boolean separator = false;
      for (String name : stack)
      {
         if (separator)
         {
            sb.append(separatorChar);
         }
         else
         {
            separator = true;
         }
         sb.append(name);
      }
      return sb.toString();
   }
}
