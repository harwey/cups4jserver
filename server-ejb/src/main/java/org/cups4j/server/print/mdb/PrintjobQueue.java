package org.cups4j.server.print.mdb;

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
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.PrintJob;
import org.cups4j.PrintRequestResult;
import org.cups4j.server.print.PrintJobEt;
import org.cups4j.server.print.service.PrintStateEnum;

@MessageDriven(name = "PrintjobQueue", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "queue/PrintJobQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })

public class PrintjobQueue implements MessageListener {
  @PersistenceContext
  EntityManager em;
  private static final Logger LOGGER = Logger.getLogger(PrintjobQueue.class.toString());

  @Override
  public void onMessage(Message message) {
    ObjectMessage msg = null;

    try {
      Long jobId;
      if (message instanceof ObjectMessage) {
        msg = (ObjectMessage) message;
        jobId = msg.getLongProperty("jobID");
        LOGGER.info("New print job received. Job ID: " + jobId);
        PrintJobEt printJob = em.find(PrintJobEt.class, jobId);
        if (printJob != null) {
          print(printJob);
        } else {
          LOGGER.info("**** could not find print job (id=" + jobId + ") in database!! giving up");
        }
      } else {
        LOGGER.warning("Out of band message received! Message type: " + message.getClass().getName());
      }
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
  }

  private void print(PrintJobEt printJob) {
    try {
      LOGGER.info("print job: " + printJob.getId() + " on " + printJob.getCupsPrinterURL());
      CupsClient cupsClient = new CupsClient();
      CupsPrinter cupsPrinter = cupsClient.getPrinter(printJob.getCupsPrinterURL());

      if (cupsPrinter != null) {
        PrintJob job = new PrintJob.Builder(printJob.getDocument()).copies(printJob.getCopies())
            .jobName(printJob.getUserName() + "-" + printJob.getId()).userName(printJob.getUserName()).build();

        LOGGER.info(" printing on " + cupsPrinter.getPrinterURL());
        PrintRequestResult result = cupsPrinter.print(job);
        printJob.setCupsPrinterURL(cupsPrinter.getPrinterURL());

        if (result.isSuccessfulResult()) {
          LOGGER.info(" print job spooled successfully");
          printJob.setPrintState(PrintStateEnum.SPOOLED);
          printJob.setCupsJobId(result.getJobId());
        } else {
          LOGGER.info(" print job could not be spooled - check error message");
          LOGGER.info("          ->" + result.getResultDescription());
          printJob.setPrintStateMessage(result.getResultDescription());
          printJob.setPrintState(PrintStateEnum.ERROR);
        }

      } else {
        LOGGER.info(" print job could not be spooled - check error message");
        printJob.setPrintState(PrintStateEnum.ERROR);
        printJob.setPrintStateMessage(
            " printer " + printJob.getCupsPrinterURL() + " does not exist.");
      }

    } catch (Exception e) {
      LOGGER.info("print job could not be spooled - will retry later");
      LOGGER.info("          ->" + e.getMessage());
      printJob.setPrintState(PrintStateEnum.RECOVERABLE_ERROR);
      printJob.setPrintStateMessage(e.getMessage());
    } finally {
      em.merge(printJob);
      em.flush();
    }
  }

}
