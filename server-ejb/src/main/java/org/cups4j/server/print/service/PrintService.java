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
import java.util.List;

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
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.server.print.PrintJobEt;

@JMSDestinationDefinitions(value = {
    @JMSDestinationDefinition(name = "java:/queue/PrintJobQueue", interfaceName = "javax.jms.Queue", destinationName = "PrintJobQueue") })

@Stateless
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class PrintService implements IPrintServiceRemote, IPrintServiceLocal {
  @Inject
  private JMSContext context;
  @PersistenceContext
  EntityManager em;
  @Resource(lookup = "java:/queue/PrintJobQueue")
  private Queue queue;

  @Override
  @WebMethod
  public Long print(PrintJob printJob) throws Exception {
    Long id = persistPrintJob(printJob);
    sendPrintJobMessage(id);
    return id;

  }

  private Long persistPrintJob(PrintJob printJob) throws Exception {
    CupsClient client = new CupsClient(printJob.getHost(), printJob.getPort());
    CupsPrinter printer = getPrinter(client, printJob.getPrinterName());

    PrintJobEt entity = new PrintJobEt();
    entity.setCopies(printJob.getCopies());
    entity.setDocument(printJob.getDocument());
    entity.setDuplex(printJob.isDuplex());
    entity.setPages(printJob.getPages());
    entity.setCupsPrinterURL(printer.getPrinterURL());
    entity.setUserName(printJob.getUser());
    entity.setJobName(printJob.getJobName());

    em.persist(entity);

    return entity.getId();
  }

  private CupsPrinter getPrinter(CupsClient client, String printerName) throws Exception {
    List<CupsPrinter> printerList = client.getPrinters();
    for (CupsPrinter printer : printerList) {
      if (printerName.equals(printer.getName())) {
        return printer;
      }
    }
    return null;
  }

  @Override
  @WebMethod
  public CheckPrintJobResponse getJobState(Long jobId) {
    PrintJobEt printJob = em.find(PrintJobEt.class, jobId);

    CheckPrintJobResponse response = new CheckPrintJobResponse();
    if (printJob != null) {
      PrintStateEnum jobState = em.find(PrintJobEt.class, jobId).getPrintState();
      response.setJobState(jobState);
    } else {
      response.setMessage("print job with id " + jobId + " not found.");
    }

    return response;
  }

  @Override
  public void sendPrintJobMessage(Long jobId) {
    ObjectMessage message = context.createObjectMessage();
    try {
      message.setLongProperty("jobID", jobId);
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
    context.createProducer().send(queue, message);
  }

}
