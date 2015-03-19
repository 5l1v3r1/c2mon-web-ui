package cern.c2mon.web.configviewer.statistics.daqlog.exceptions;

/**
 * This exception indicates that the table name has an
 * invalid format (preventing SQL injection from XML config
 * file)
 * 
 * Valid characters are alphanumeric and "_".
 * 
 * @author mbrightw
 *
 */
public class InvalidTableNameException extends Exception {

}
