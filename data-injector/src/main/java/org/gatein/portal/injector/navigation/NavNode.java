/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.gatein.portal.injector.navigation;

import org.exoplatform.portal.mop.navigation.NodeContext;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A model simpler than UserNode
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 1/8/13
 */
public class NavNode
{
   /*package*/ NodeContext<NavNode> context;

   private int depth = -1;

   private LinkedList<String> path;

   public NavNode(NodeContext<NavNode> _context)
   {
      context = _context;
   }

   public NavNode getParent()
   {
      NodeContext<NavNode> ctx = context.getParent();
      return ctx == null ? null : ctx.getNode();
   }

   public NavNode getChild(String name)
   {
      NodeContext<NavNode> ctx = context.get(name);
      return ctx == null ? null : ctx.getNode();
   }

   public Collection<NavNode> getChildren()
   {
      return context.getNodes();
   }

   public void addChild(String name)
   {
      context.add(null, name);
   }

   public void addChild(int index, String name)
   {
      context.add(new Integer(index), name);
   }

   public boolean removeChild(String name)
   {
      return context.removeNode(name);
   }

   public void remove()
   {
      context.remove();
   }

   public int getSize()
   {
      return context.getSize();
   }

   public int getDepth()
   {
      if (depth < 0)
      {
         depth = getPath().size();
      }
      return depth;
   }

   public LinkedList<String> getPath()
   {
      if (path == null)
      {
         LinkedList<String> tmp = new LinkedList<String>();
         for(NavNode node = this; node != null; node = node.getParent())
         {
            tmp.addFirst(node.context.getName());
         }
         path = tmp;
      }
      return path;
   }

   /**
    * Search for a descendant node matching given relativePath, the method might return null if:
    *
    * 1. There 's no node in reality that matches path
    *
    * 2. The Scope passed to NavigationService.load method does not allow to load all necessary node
    *
    * @param relativePath
    * @return
    */
   public NavNode getDescendant(String[] relativePath)
   {
      NavNode tmp = this;
      int depth = 0;
      while(tmp != null && depth < relativePath.length)
      {
         tmp = tmp.getChild(relativePath[depth]);
         depth++;
      }
      return tmp;
   }
}
