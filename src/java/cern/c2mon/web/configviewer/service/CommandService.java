package cern.c2mon.web.configviewer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.core.tag.ClientCommandTagImpl;

/**
 * Command service providing the XML representation of a given tag
 */
@Service
public class CommandService {

  /**
   * CommandService logger
   */
  private static Logger logger = LoggerFactory.getLogger(CommandService.class);

  /**
   * Gateway to C2monService
   */
  @Autowired
  private ServiceGateway gateway;

  /**
   * Gets the XML representation of the configuration of a command
   *
   * @param commandId id of the command
   * @return XML representation of command configuration
   * @throws TagIdException if command was not found or a non-numeric id was
   *           requested ({@link TagIdException}), or any other exception thrown
   *           by the underlying service gateway.
   */
  public String getCommandTagXml(final String commandId) throws TagIdException {
    try {
      ClientCommandTagImpl command = (ClientCommandTagImpl) getCommandTag(Long.parseLong(commandId));
      if (command.isExistingCommand())
        return command.getXml();
      else
        throw new TagIdException("No command found");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid command id");
    }
  }

  /**
   * Retrieves a command tag object from the service gateway tagManager
   *
   * @param commandId id of the alarm
   * @return command tag
   */
  public ClientCommandTag<Object> getCommandTag(final long commandId) {
    ClientCommandTag<Object> ct = gateway.getCommandManager().getCommandTag(commandId);
    logger.debug("Command fetch for command " + commandId + ": " + (ct == null ? "NULL" : "SUCCESS"));
    return ct;
  }
}
