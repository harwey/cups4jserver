package org.cups4j.server;
/**
 * Copyright (C) 2018 Harald Weyhing
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
*/
import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Entity implements Serializable {
  private static final long serialVersionUID = 369578504689015483L;

  @Id
  @GeneratedValue(strategy = GenerationType.TABLE)
  protected Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object obj) {
    boolean result;
    if (id != null && obj != null
        && (obj.getClass().isAssignableFrom(this.getClass()) || this.getClass().isAssignableFrom(obj.getClass()))) {
      result = id.equals(((Entity) obj).getId());
    } else {
      result = super.equals(obj);
    }
    return result;
  }

  @Override
  public int hashCode() {
    int result;
    if (id != null) {
      result = id.intValue();
    } else {
      result = super.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return "Entity [id=" + id + "]";
  }
}
