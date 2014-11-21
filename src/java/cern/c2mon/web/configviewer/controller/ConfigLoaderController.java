package cern.c2mon.web.configviewer.controller;

import java.io.IOException;

import javax.naming.CannotProceedException;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.shared.util.json.GsonFactory;
import cern.c2mon.web.configviewer.service.ConfigLoaderService;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.util.FormUtility;

import com.google.gson.Gson;

/**
 * A controller for the ConfigLoader
 * */
@Controller
public class ConfigLoaderController {

  /** Used to convert the returned value into JSON format for the AJAX calls */
  private static transient Gson gson = null;

  /**
   * A REST-style URL
   * */
  public static final String CONFIG_LOADER_URL = "/configloader/";

  /**
   * A URL to the config report viewer with input form
   * */
  public static final String CONFIG_LOADER_FORM_URL = CONFIG_LOADER_URL + "form";

  /**
   * URL for ajax progress report requests.
   */
  public static final String CONFIG_LOADER_PROGRESS_REPORT_URL = CONFIG_LOADER_URL + "progress";

  /**
   * URL that retrieves a Stored Configuration Report and displays it.
   */
  public static final String CONFIG_LOADER_PROGRESS_FINAL_REPORT_URL = CONFIG_LOADER_PROGRESS_REPORT_URL + "/finalReport/";

  /**
   * URL that retrieves a Stored Configuration Report and displays it in RAW
   * XML.
   */
  public static final String CONFIG_LOADER_PROGRESS_FINAL_REPORT_XML_URL = CONFIG_LOADER_PROGRESS_REPORT_URL + "/finalReport/" + "xml";

  /**
   * Title for the config form page
   * */
  public static final String CONFIG_LOADER_FORM_TITLE = "Config Loader";

  /**
   * Description for the config form page
   * */
  public static final String CONFIG_LOADER_FORM_INSTR = "Please enter the Configuration ID you want to apply.";

  /**
   * A config loader service
   * */
  @Autowired
  private ConfigLoaderService service;

  /**
   * ConfigLoaderController logger
   * */
  private static Logger logger = Logger.getLogger(ConfigLoaderController.class);

  /**
   * @return Redirects to the form
   */
  @RequestMapping(value = CONFIG_LOADER_URL, method = { RequestMethod.GET })
  public String viewConfig(final Model model) {
    logger.debug(CONFIG_LOADER_URL);
    return ("redirect:" + "/configloader/form");
  }

  /**
   * @return Displays an --ALREADY APPLIED-- configuration report in RAW XML
   *         format.
   *
   * @param id config id
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   * */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_FINAL_REPORT_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewXml(@PathVariable final String id, final Model model) {
    logger.debug(CONFIG_LOADER_PROGRESS_FINAL_REPORT_XML_URL + id);

    try {
      model.addAttribute("xml", service.getStoredConfigurationReport(id).toXML());
    } catch (NotFoundException e) {
      return ("redirect:" + "/configloader/errorform/" + id);
    }
    return "raw_xml_views/rawXml";
  }

  /**
   * @return Applies the configuration for the given Configuration Id and also
   *         displays the generated configuration report.
   *
   * @param id the configuration it to be applied
   * @param response we write the html result to that HttpServletResponse
   *          response
   * @throws IOException
   * */
  @RequestMapping(value = CONFIG_LOADER_URL + "{id}", method = { RequestMethod.GET })
  public String viewConfig(@PathVariable(value = "id") final String id, final HttpServletResponse response) throws IOException {
    logger.debug("/configloader/{id} " + id);

    try {
      response.setContentType("text/html; charset=UTF-8");
      response.getWriter().println(FormUtility.getHeader("../../.."));
      response.getWriter().println(service.generateHtmlResponse(id));
      response.getWriter().println(FormUtility.getFooter());
    } catch (TransformerException e) {
      response.setStatus(400);
      response.getWriter().println(e.getMessage());
      logger.error(e.getMessage());
    } catch (TagIdException e) {
      return ("redirect:" + "/configloader/errorform/" + id);
    } catch (CannotProceedException e) {
      response.setStatus(400);
      response.getWriter().println(e.getMessage());
      logger.error(e.getMessage());
    }
    return null;
  }

  /**
   * @return Retrieves a stored Configuration Report and displays it.
   *
   * @param id the Configuration Report id
   * @param response we write the html result to that HttpServletResponse
   *          response
   * @throws IOException
   * */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_FINAL_REPORT_URL + "{id}", method = { RequestMethod.GET })
  public String viewFinalReport(@PathVariable(value = "id") final String id, final HttpServletResponse response) throws IOException {
    logger.debug(CONFIG_LOADER_PROGRESS_FINAL_REPORT_URL + "/{id} " + id);

    try {
      response.setContentType("text/html; charset=UTF-8");
      response.getWriter().println(FormUtility.getHeader("../../.."));
      response.getWriter().println(service.getStoredConfigurationReportHtml(id));
      response.getWriter().println(FormUtility.getFooter());

    } catch (TagIdException e) {
      return ("redirect:" + "/configloader/errorform/" + id);

    } catch (Exception e) {
      response.setStatus(400);
      response.getWriter().println(e.getMessage());
      logger.error("viewFinalReport() - Error occured whilst trying show final report.", e);
    }
    return null;
  }

  /**
   * @return In case of an error this form is shown. It displays the error and
   *         you can also make a new query.
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   * */
  @RequestMapping(value = "/configloader/errorform/{id}")
  public String viewConfigLoaderErrorForm(@PathVariable(value = "id") final String errorId, @RequestParam(value = "id", required = false) final String id,
      final Model model) {
    logger.debug("/configloader/errorform " + id);

    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, null, null));
    } else {
      return ("redirect:" + CONFIG_LOADER_URL + id);
    }

    model.addAttribute("err", errorId);
    return "notFoundErrorFormWithData";
  }

  /**
   * @return Displays a form where a config id can be entered.
   *
   * @param id config id
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   * */
  @RequestMapping(value = CONFIG_LOADER_FORM_URL + "/{id}", method = { RequestMethod.GET })
  public String viewConfigLoaderWithForm(@PathVariable final String id, final Model model) {
    logger.debug("/configloader/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, id, CONFIG_LOADER_URL + id));

    return "formWithData";
  }

  /**
   * @return Displays an input form for a config id.
   *
   *         If a POST was made with a config id, redirects to CONFIG_LOADER_URL
   *         + id.
   *
   * @param id config id
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   * */
  @RequestMapping(value = CONFIG_LOADER_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
  public String viewConfigLoaderFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
    logger.debug("/configloader/form " + id);
    if (id == null)
      model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, null, null));
    else
      return ("redirect:" + CONFIG_LOADER_URL + id);
    return "formWithData";
  }

  /**
   * @return Displays an input form for a configuration id. If a request is made
   *         from this form, a listener is registered that listens for
   *         ProgressReport updates.
   *
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL, method = RequestMethod.GET)
  public String startConfigurationProcessWithProgressReportForm(final Model model) {
    logger.debug(CONFIG_LOADER_PROGRESS_REPORT_URL);

    model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, "", CONFIG_LOADER_URL));
    model.addAttribute("reports", service.getFinalReports());
    return "configurationReportWithProgressReport";
  }

  /**
   * Starts an applyConfiguration request to the server. Listens for Progress
   * Report updates.
   *
   * @param configurationId the id of the configuration
   * @throws Exception
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL + "/start", method = RequestMethod.POST)
  public void startConfigurationProcess(@RequestParam("configurationId") final String configurationId) throws Exception {
    logger.debug("(AJAX) Starting Configuration Request: " + configurationId);
    service.getConfigurationReportWithReportUpdates(Integer.parseInt(configurationId));
  }

  /**
   * @param configurationId the id of the configuration
   * @return Returns the current progress of the request.
   * @throws InterruptedException in case of error
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL + "/getProgress", method = RequestMethod.POST)
  @ResponseBody
  public Integer getProgressReport(@RequestParam("configurationId") final String configurationId) throws InterruptedException {
    logger.debug("(AJAX) Received Progress Report Request for configurationId:" + configurationId);

    ClientRequestProgressReport report = service.getProgressReportForConfiguration(configurationId);
    if (report == null) {
      return 0;
    }

    int currentProgress = 0;
    if (report.getTotalProgressParts() > 0) {
      currentProgress = ((100 * report.getCurrentProgressPart()) / report.getTotalProgressParts());
      logger.debug("returning:" + report.getCurrentProgressPart() + " out of " + report.getTotalProgressParts());
    } else {
      currentProgress = 100;
    }

    // @ResponseBody will automatically convert the returned value into JSON
    // format
    // (Jackson)
    return currentProgress;
  }

  /**
   * @param configurationId the id of the configuration
   * @return Returns a description of what is happening in the server currently
   * @throws InterruptedException in case of error
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL + "/getProgressDescription", method = RequestMethod.POST)
  @ResponseBody
  public String getProgressDescription(@RequestParam("configurationId") final String configurationId) throws InterruptedException {
    logger.debug("(AJAX) Received Progress Description Request for configurationId:" + configurationId);

    ClientRequestProgressReport report = service.getProgressReportForConfiguration(configurationId);
    String progressDescription = null;

    if (report != null) {
      progressDescription = report.getProgressDescription();
    }
    // @ResponseBody will automatically convert the returned value into JSON
    // format
    // You must have Jackson in your classpath
    // Jackson does not work as expected in this case.. so Gson is used for this
    // case
    return getGson().toJson(progressDescription);
  }

  /**
   * @return The Gson parser singleton instance
   */
  protected static synchronized Gson getGson() {
    if (gson == null) {
      gson = GsonFactory.createGson();
    }
    return gson;
  }
}
