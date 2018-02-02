package org.cups4j.server.print;

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
import java.net.URL;

import javax.persistence.Lob;

import org.cups4j.JobStateEnum;
import org.cups4j.server.Entity;
import org.cups4j.server.print.service.PrintStateEnum;

@javax.persistence.Entity
public class PrintJobEt extends Entity {

  private static final long serialVersionUID = -3251183236968486702L;
  @Lob
  private byte[] document;
  private URL cupsPrinterURL;
  private String userName;
  private String password;
  private String jobName;
  private String pages;
  private int copies = 0;
  private boolean duplex = false;
  private PrintStateEnum printState = PrintStateEnum.NEW;
  private JobStateEnum jobStateEnum;
  private String message;
  private int cupsJobId;

  public byte[] getDocument() {
    return document;
  }

  public void setDocument(byte[] document) {
    this.document = document;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public JobStateEnum getJobStateEnum() {
    return jobStateEnum;
  }

  public void setJobStateEnum(JobStateEnum jobStateEnum) {
    this.jobStateEnum = jobStateEnum;
  }

  public String getPages() {
    return pages;
  }

  public void setPages(String pages) {
    this.pages = pages;
  }

  public int getCopies() {
    return copies;
  }

  public void setCopies(int copies) {
    this.copies = copies;
  }

  public boolean isDuplex() {
    return duplex;
  }

  public void setDuplex(boolean duplex) {
    this.duplex = duplex;
  }

  public PrintStateEnum getPrintState() {
    return printState;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setPrintState(PrintStateEnum printState) {
    this.printState = printState;
  }

  public void setPrintStateMessage(String printStateMessage) {
    this.message = printStateMessage;
  }

  public URL getCupsPrinterURL() {
    return cupsPrinterURL;
  }

  public void setCupsPrinterURL(URL cupsPrinterURL) {
    this.cupsPrinterURL = cupsPrinterURL;
  }

  public int getCupsJobId() {
    return cupsJobId;
  }

  public void setCupsJobId(int cupsJobId) {
    this.cupsJobId = cupsJobId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

}
