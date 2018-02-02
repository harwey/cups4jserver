package org.cups4j.ws.client;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.cups4j.CupsClient;
import org.cups4j.server.print.service.CheckPrintJobResponse;
import org.cups4j.server.print.service.IPrintServiceRemote;
import org.cups4j.server.print.service.PrintJob;

public class CupsWSClient {

  public static void main(String[] args) throws FileNotFoundException {
    String jboss = CupsClient.DEFAULT_HOST;

    String jobId = null;
    boolean print = false;
    boolean getStatus = false;

    PrintJob request = new PrintJob();
    request.setHost(CupsClient.DEFAULT_HOST);
    request.setPort(CupsClient.DEFAULT_PORT);

    try {
      if (args.length == 0) {
        usage();
      }
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-w")) {
          jboss = args[++i];
        } else if (args[i].equals("-h")) {
          request.setHost(args[++i]);
        } else if (args[i].equals("-f")) {
          print = true;
          request.setDocument(Files.readAllBytes(Paths.get(args[++i])));
        } else if (args[i].equals("getStatus")) {
          getStatus = true;
        } else if (args[i].equals("-u")) {
          request.setUser(args[++i]);
        } else if (args[i].equals("-j")) {
          request.setJobName(args[++i]);
        } else if (args[i].equals("-c")) {
          request.setCopies(Integer.parseInt(args[++i]));
        } else if (args[i].equals("-p")) {
          request.setPages(args[++i].trim());
        } else if (args[i].equals("-P")) {
          request.setPrinterName(args[++i]);
        } else if (args[i].equals("-duplex")) {
          request.setDuplex(true);
        } else if (args[i].equals("-job-id")) {
          jobId = args[++i].trim();
        } else if (args[i].equals("-help")) {
          usage();
        }
      }

      if (getStatus) {
        getStatus(jboss, jobId);
      } else if (print) {
        print(jboss, request);
      } else {
        usage();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void getStatus(String jboss, String jobId) throws Exception {
    CheckPrintJobResponse response = getProxy(jboss).getJobState(Long.parseLong(jobId));
    printResponse(response);
  }

  private static void print(String jboss, PrintJob printJob) throws Exception {
    if (printJob.getDocument() == null || printJob.getDocument().length == 0) {
      usage();
    }
    printResponse(getProxy(jboss).print(printJob));
  }

  private static IPrintServiceRemote getProxy(String host) throws Exception {
    String endPointAddress = "http://" + host + ":8080/server-ejb/PrintService";

    QName serviceName = new QName("http://service.print.server.cups4j.org/", "PrintServiceService");
    IPrintServiceRemote proxy = null;

    URL wsdlURL = new URL(endPointAddress + "?wsdl");
    Service service = Service.create(wsdlURL, serviceName);
    proxy = service.getPort(IPrintServiceRemote.class);

    return proxy;
  }

  private static void printResponse(Long jobId) {
    System.out.println("job id: " + jobId);
  }

  private static void printResponse(CheckPrintJobResponse r) {
    System.out.println("status: " + r.getJobState());
    System.out.println("message: " + r.getMessage());
  }

  private static void usage() {
    System.out.println(
        "CupsWSClient [-w <wildfly host>] [-h <hostname>] [getStatus [-u <userName>] -P <printer name> -job-id <job ID>][-f <file name> -P <printer name> [-j <job name>] [-c <copies>][-p <pages>][-duplex]] -help ");
    System.out.println("  <wildfly host>  - wildfly host name or ip adress (default: localhost)");
    System.out.println("  <hostname>      - CUPS host name or ip adress (default: localhost)");
    System.out.println("  getStatus       - check status of already printed job.");
    System.out.println(
        "                    defaults are: <hostname>=localhost, printer=default on <hostname>, user=anonymous");
    System.out.println("  printFile       - print the file provided in following parameter");
    System.out.println("  <filename>      - postscript or pdf file to print");
    System.out.println("  <printer name>  - printer name on <hostname>");
    System.out.println("  <job name>      - given name for current print job");
    System.out.println("  <job ID>        - job ID of already sumitted print job");
    System.out
        .println("  <copies>        - number of copies (default: 1 wich means the document will be printed once)");
    System.out.println("  <pages>         - ranges of pages to print in the following syntax: ");
    System.out.println("                    1-2,4,6,10-12 - single ranges need to be in ascending order");
    System.out.println("  -duplex         - turns on double sided printing");
    System.out.println("  <attributes>    - this is a list of additional print-job-attributes separated by '+' like:\n"
        + "                    print-quality:enum:3+job-collation-type:enum:2");

    System.out.println("  -help           - shows this text");
  }

}
