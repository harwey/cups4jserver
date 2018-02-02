package org.cups4j.server.print.service;

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

public class CheckPrintJobResponse implements Serializable {
  private static final long serialVersionUID = -134625923119019755L;
  private PrintStateEnum jobState;
  private String message;

  public PrintStateEnum getJobState() {
    return jobState;
  }

  public void setJobState(PrintStateEnum jobState) {
    this.jobState = jobState;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
