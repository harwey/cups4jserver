package org.cups4j.server.print.maintenance;

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
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.cups4j.CupsClient;
import org.cups4j.CupsPrinter;
import org.cups4j.JobStateEnum;
import org.cups4j.server.print.PrintJobEt;
import org.cups4j.server.print.PrintJobEt_;
import org.cups4j.server.print.service.IPrintServiceLocal;
import org.cups4j.server.print.service.PrintStateEnum;

@Singleton
@Startup
public class PrintJobMaintenance {
  Logger logger = Logger.getLogger(PrintJobMaintenance.class.getName());
  @Resource
  private TimerService timerService;
  @PersistenceContext
  EntityManager em;
  @EJB
  IPrintServiceLocal printService;

  private static final String TIMER_NAME = "PrintJobMaintenance";
  private static final long TIMEOUT = 30000L;

  @PostConstruct
  private void startup() {
    startMaintenance();
  }

  @PreDestroy
  private void shutdown() {
    stopMaintenance();
  }

  public void startMaintenance() {
    timerService.createTimer(new Date(System.currentTimeMillis() + TIMEOUT), TIMER_NAME);
  }

  public void stopMaintenance() {
    for (Timer timer : timerService.getTimers()) {
      String scheduled = (String) timer.getInfo();
      if (scheduled.equals(TIMER_NAME)) {
        timer.cancel();
      }
    }
  }

  private void recoverJobs(List<PrintJobEt> jobs) {
    for (PrintJobEt job : jobs) {
      logger.info("recover job id: " + job.getId() + " on printer: " + job.getCupsPrinterURL());
      printService.sendPrintJobMessage(job.getId());
    }
  }

  @Timeout
  public void maintenance(javax.ejb.Timer timer) {
    stopMaintenance();
    logger.info("Starting print job maintenance now... ");

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<PrintJobEt> query = cb.createQuery(PrintJobEt.class);
    Root<PrintJobEt> printJob = query.from(PrintJobEt.class);
    query.where(cb.equal(printJob.get(PrintJobEt_.printState), PrintStateEnum.SPOOLED));
    List<PrintJobEt> spooledJobs = em.createQuery(query).getResultList();

    logger.info("found " + spooledJobs.size() + " print jobs");
    maintainJobs(spooledJobs);

    query = cb.createQuery(PrintJobEt.class);
    printJob = query.from(PrintJobEt.class);
    query.where(cb.equal(printJob.get(PrintJobEt_.printState), PrintStateEnum.RECOVERABLE_ERROR));
    List<PrintJobEt> recoverableJobs = em.createQuery(query).getResultList();

    logger.info("found " + recoverableJobs.size() + " recoverable print jobs");
    recoverJobs(recoverableJobs);

    logger.info("Print job maintenance done.");
    startMaintenance();
  }

  private void maintainJobs(List<PrintJobEt> jobs) {
    for (PrintJobEt job : jobs) {
      logger.info("job id: " + job.getId() + " printer: " + job.getCupsPrinterURL());

        try {
          CupsClient cupsClient = new CupsClient();
          CupsPrinter cupsPrinter = cupsClient.getPrinter(job.getCupsPrinterURL());
          JobStateEnum cupsState = cupsPrinter.getJobStatus(job.getCupsJobId());

          logger.info(" current state of print job: " + cupsState);
          if (cupsState == JobStateEnum.COMPLETED) {
            em.remove(job);
          } else if (cupsState != job.getJobStateEnum()) {
            if (cupsState == JobStateEnum.ABORTED || cupsState == JobStateEnum.CANCELED) {
              job.setPrintState(PrintStateEnum.ERROR);
              job.setMessage("CUPS state: " + cupsState);
            }
            job.setJobStateEnum(cupsState);
          }
        } catch (Exception e) {
          logger.severe(e.getMessage());
          e.printStackTrace();
        }

    }
  }
}
